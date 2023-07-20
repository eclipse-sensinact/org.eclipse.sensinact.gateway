/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.launcher;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.osgi.service.feature.FeatureExtension.Kind.MANDATORY;
import static org.osgi.service.feature.FeatureExtension.Type.ARTIFACTS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.feature.Feature;
import org.osgi.service.feature.FeatureArtifact;
import org.osgi.service.feature.FeatureBundle;
import org.osgi.service.feature.FeatureConfiguration;
import org.osgi.service.feature.FeatureExtension;
import org.osgi.service.feature.FeatureService;
import org.osgi.service.feature.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPid = "sensinact.launcher")
public class FeatureLauncher {

    @interface Config {
        String[] features() default {};

        String[] repository() default { "repository" };

        String[] featureDir() default { "features" };
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureLauncher.class);

    private static final String SENSINACT_FEATURE_DEPENDENCY = "sensinact.feature.depends";

    private final Pattern variablePattern = Pattern.compile("\\$\\{([^\\{\\}\\$]+)\\}");

    @Reference
    FeatureService featureService;

    @Reference
    ConfigurationManager configManager;

    private BundleContext context;

    private FeatureExtension noDependencies;

    /**
     * The bundles installed by this launcher
     */
    private final Map<String, Bundle> bundlesByIdentifier = new HashMap<>();

    /**
     * The lists of artifacts for each feature installed
     */
    private final Map<String, List<String>> featuresToBundles = new HashMap<>();

    /**
     * The list of configurations for each feature installed
     */
    private final Map<String, Collection<String>> featuresConfigurations = new HashMap<>();

    /**
     * The list of installed features in installation order
     */
    private final List<String> features = new ArrayList<>();

    private List<Path> repositories;
    private List<Path> featureDirs;

    @Activate
    void start(BundleContext context, Config config) throws ConfigurationException {

        this.context = context;

        this.noDependencies = featureService.getBuilderFactory()
                .newExtensionBuilder(SENSINACT_FEATURE_DEPENDENCY, ARTIFACTS, MANDATORY).build();

        update(config);
    }

    Path getPath(String initialPath) {
        String newPath = Paths.get(initialPath).normalize().toString();

        // Inject variables
        Pattern envVarsPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");
        Matcher matcher = envVarsPattern.matcher(newPath);
        while (matcher.find()) {
            final String innerVar = matcher.group(1);
            final String resolvedEnv = System.getenv(innerVar);
            if (resolvedEnv != null) {
                newPath = newPath.replace(matcher.group(), resolvedEnv);
            }
        }

        // Replace user home
        newPath = Paths
                .get(newPath.replaceFirst("^~(" + Pattern.quote(File.separator) + "|/)",
                        Matcher.quoteReplacement(System.getProperty("user.home") + File.separator)))
                .normalize().toString();

        return Paths.get(newPath).normalize();
    }

    List<Path> getPaths(String[] paths) {
        if (paths == null || paths.length == 0) {
            return List.of();
        }

        if (paths.length == 1) {
            if (paths[0].contains(";")) {
                // Windows-like ';' as path separator, use \; to escape
                paths = paths[0].split("(?<!\\\\);");
            } else if (paths[0].contains(":")) {
                // Unix-like ':' as path separator, use \: to escape
                paths = paths[0].split("(?<!\\\\):");
            }
        }

        return Arrays.stream(paths).map(p -> getPath(p)).collect(toList());
    }

    @Modified
    void update(Config config) throws ConfigurationException {
        repositories = getPaths(config.repository());
        featureDirs = getPaths(config.featureDir());

        List<String> newFeatures = stream(config.features()).collect(toList());

        LOGGER.info("Feature installation for features {} requested using repositories {} and feature directories {}",
                newFeatures, repositories, featureDirs);

        /**
         * Removed, in installation order
         */
        List<String> removed = features.stream().filter(s -> !newFeatures.contains(s)).collect(toList());

        if (!removed.isEmpty()) {
            LOGGER.info("The following features {} are no longer required and will be removed.", removed);
        }

        // First remove all removes
        removeFeatures(removed);
        try {
            removeFeaturesConfigurations(removed);
        } catch (IOException e) {
            LOGGER.error("Error removing configurations of features: {}", removed, e);
        }

        // Finally do all add/updates
        List<String> installProgress = new ArrayList<>(newFeatures.size());
        for (String newFeature : newFeatures) {
            addOrUpdate(newFeature, installProgress);
            installProgress.add(newFeature);
        }

        features.clear();
        features.addAll(newFeatures);

        LOGGER.info("Feature update complete.");
    }

    @Deactivate
    void stop() {
        // Remove all the installed features
        removeFeatures(features);
        features.clear();
    }

    /**
     * Remove the named features
     *
     * @param removed - the features to remove in the order they were installed
     */
    private void removeFeatures(List<String> removed) {

        // Get all the bundles to remove in "install order", clearing the features map
        // Also get all associated configurations
        Set<String> bundles = new LinkedHashSet<>();
        for (String feature : removed) {
            bundles.addAll(featuresToBundles.remove(feature));
        }

        // Create a deque of bundles to remove, in the order they should be removed
        Deque<String> orderedBundlesForRemoval = new LinkedList<>();
        for (String bundle : bundles) {
            // Only remove the bundle if no remaining features reference it
            if (featuresToBundles.values().stream().noneMatch(c -> c.contains(bundle))) {
                // Add to the start of the deque, so that we reverse the install order
                orderedBundlesForRemoval.addFirst(bundle);
            }
        }

        LOGGER.debug("The following bundles {} are no longer required and will be removed.", orderedBundlesForRemoval);

        // Normal iteration order is now reverse install order
        // First we stop then we uninstall
        for (String s : orderedBundlesForRemoval) {
            Bundle b = bundlesByIdentifier.get(s);
            if (b != null) {
                try {
                    b.stop();
                } catch (BundleException e) {
                    LOGGER.warn("An error occurred stopping bundle {}.", s, e);
                }
            }
        }

        for (String s : orderedBundlesForRemoval) {
            Bundle b = bundlesByIdentifier.remove(s);
            if (b != null) {
                try {
                    b.uninstall();
                } catch (BundleException e) {
                    LOGGER.warn("An error occurred uninstalling bundle {}.", s, e);
                }
            }
        }
    }

    private void addOrUpdate(String feature, List<String> installProgress) throws ConfigurationException {

        Feature featureModel = loadFeature(feature);

        validateFeatureModel(feature, featureModel, installProgress);

        // Update configuration
        final Map<String, Hashtable<String, Object>> featureConfs = loadFeatureConfigurations(featureModel);
        Collection<String> removedConfs = featuresConfigurations.remove(feature);
        if(removedConfs != null) {
            removedConfs = removedConfs.stream().filter(pid -> !featureConfs.containsKey(pid))
                    .collect(Collectors.toList());
        }
        featuresConfigurations.put(feature, featureConfs.keySet());

        if (!featureConfs.isEmpty() || (removedConfs != null && !removedConfs.isEmpty())) {
            try {
                configManager.updateConfigurations(featureConfs, removedConfs);
            } catch (IOException e) {
                LOGGER.error("Error updating configuration for feature {}", feature, e);
            }
        }

        List<String> bundles = featureModel.getBundles().stream().map(fb -> fb.getID().toString()).collect(toList());
        if (featuresToBundles.containsKey(feature)) {
            LOGGER.debug("Updating feature {}", feature);
            if (featuresToBundles.get(feature).equals(bundles)) {
                // No work to do, already installed
                LOGGER.debug("The feature {} is already up to date", feature);
                return;
            } else {
                LOGGER.debug("The feature {} is out of date and will be removed and re-installed", feature);
                removeFeatures(List.of(feature));
            }
        }

        featuresToBundles.put(feature, bundles);

        // Install
        List<Bundle> installed = new ArrayList<>();
        for (FeatureBundle fb : featureModel.getBundles()) {
            ID bundleIdentifier = fb.getID();
            String bid = bundleIdentifier.toString();
            if (!bundlesByIdentifier.containsKey(bid)) {
                Bundle bundle = installBundle(bundleIdentifier);
                bundlesByIdentifier.put(bid, bundle);
                installed.add(bundle);
            }
        }

        // Start
        for (Bundle b : installed) {
            try {
                BundleRevision rev = b.adapt(BundleRevision.class);
                if (rev != null && (rev.getTypes() & BundleRevision.TYPE_FRAGMENT) == 0) {
                    // Start all but fragment bundles
                    b.start();
                } else {
                    LOGGER.debug("Not starting bundle {} as it is a fragment", b.getSymbolicName());
                }
            } catch (Exception e) {
                LOGGER.warn("An error occurred starting a bundle in feature {}", feature, e);
            }
        }
    }

    private Feature loadFeature(String feature) {

        LOGGER.debug("Loading feature model {}", feature);

        Path featureFile = null;
        if (feature.indexOf(':') >= 0) {
            try {
                featureFile = getFileFromRepository(featureService.getIDfromMavenCoordinates(feature), "json");
            } catch (FileNotFoundException e) {
                LOGGER.warn("Can't find feature file: {}", feature, e);
            }
        } else {
            String simpleFileName = feature;
            if (!simpleFileName.endsWith(".json")) {
                simpleFileName += ".json";
            }

            for (Path featureDir : featureDirs) {
                Path testedFile = featureDir.resolve(simpleFileName);
                if (Files.isRegularFile(testedFile)) {
                    featureFile = testedFile;
                    break;
                }
            }
        }

        if (featureFile != null && Files.isRegularFile(featureFile)) {
            try (Reader fr = Files.newBufferedReader(featureFile, UTF_8)) {
                return featureService.readFeature(fr);
            } catch (IOException ioe) {
                LOGGER.error("Failed to parse feature {} from file {}", feature, featureFile, ioe);
            }
        } else {
            LOGGER.error("No feature file for feature {}", feature);
        }

        return null;
    }

    private void validateFeatureModel(String feature, Feature featureModel, List<String> installProgress)
            throws ConfigurationException {
        if (featureModel == null) {
            LOGGER.error("Unable to locate a valid feature " + feature);
            throw new ConfigurationException("features",
                    "The feature " + feature + " cannot be deployed as it cannot be found.");
        } else {
            // Check feature dependencies
            FeatureExtension dependencies = featureModel.getExtensions().getOrDefault(SENSINACT_FEATURE_DEPENDENCY,
                    noDependencies);
            if (dependencies.getType() != ARTIFACTS) {
                LOGGER.error("The feature {} includes a sensinact.feature.depends extension of the wrong type {}",
                        feature, dependencies.getType());
                throw new ConfigurationException("features",
                        "The feature " + feature + "contains a sensinact.feature.depends extension of the wrong type.");
            }

            List<ID> unsatisfied = new ArrayList<>();
            for (FeatureArtifact featureArtifact : dependencies.getArtifacts()) {
                ID id = featureArtifact.getID();

                if (installProgress.contains(id.getArtifactId())) {
                    LOGGER.debug("The feature {} depends on the feature {}, which is installed", feature,
                            id.getArtifactId());
                } else if (installProgress.contains(id.toString())) {
                    LOGGER.debug("The feature {} depends on the feature {}, which is installed", feature,
                            id.toString());
                } else {
                    LOGGER.error("The feature {} depends on the feature {} which is not installed before it", feature,
                            id.toString());
                    unsatisfied.add(id);
                }
            }
            if (!unsatisfied.isEmpty()) {
                throw new ConfigurationException("features", "The feature " + feature
                        + "contains a sensinact.feature.depends extension which is not satisfied. The unsatisfied dependencies are"
                        + unsatisfied.toString());
            }

            List<String> unknownMandatory = featureModel.getExtensions().entrySet().stream()
                    .filter(e -> e.getValue().getKind() == MANDATORY).map(Entry::getKey)
                    .filter(s -> !SENSINACT_FEATURE_DEPENDENCY.equals(s)).collect(toList());
            if (!unknownMandatory.isEmpty()) {
                LOGGER.error("The feature {} has mandatory extensions {} which are not understood by sensiNact");
                throw new ConfigurationException("features", "The feature " + feature
                        + "contains mandatory extension(s) " + unsatisfied.toString() + " which are not understood.");
            }
        }
    }

    private Bundle installBundle(ID bundleIdentifier) {
        LOGGER.debug("Installing bundle {}", bundleIdentifier.toString());
        try (InputStream is = newInputStream(getFileFromRepository(bundleIdentifier, "jar"))) {
            return context.installBundle(bundleIdentifier.toString(), is);
        } catch (Exception e) {
            LOGGER.warn("An error occurred installing bundle {}", bundleIdentifier.toString(), e);
        }
        return null;
    }

    private Path getFileFromRepository(ID id, String defaultType) throws FileNotFoundException {
        LOGGER.debug("Searching for feature {} in repositories {}", id, repositories);

        Path file;
        String groupPath = id.getGroupId().replace('.', File.separatorChar);

        Path path = null;
        for (Path repository : repositories) {
            Path testedPath = repository.resolve(groupPath).resolve(id.getArtifactId()).resolve(id.getVersion());
            if (Files.isDirectory(testedPath)) {
                path = testedPath;
                break;
            }
        }

        if (path == null) {
            throw new FileNotFoundException("Can't find feature " + id);
        }

        final Path repoPath = path;

        String fileName;
        if (id.getClassifier().isEmpty()) {
            fileName = String.format("%s-%s", id.getArtifactId(), id.getVersion());
        } else {
            fileName = String.format("%s-%s-%s", id.getArtifactId(), id.getVersion(), id.getClassifier().get());
        }

        fileName = fileName.concat(String.format(".%s", id.getType().orElse(defaultType)));

        file = repoPath.resolve(fileName);

        LOGGER.debug("Expected file path for feature {} is {}", id, file);

        if (!Files.isRegularFile(file) && id.getVersion().endsWith("-SNAPSHOT")) {
            LOGGER.debug("File not found, looking for the latest SNAPSHOT");
            String regex;
            if (id.getClassifier().isEmpty()) {
                regex = String.format("%s-%s-\\d{8}\\.\\d{6}-\\d+\\.%s", id.getArtifactId(),
                        id.getVersion().substring(0, id.getVersion().length() - "-SNAPSHOT".length()),
                        id.getType().orElse(defaultType));
            } else {
                regex = String.format("%s-%s-\\d{8}\\.\\d{6}-\\d+-%s\\.%s", id.getArtifactId(),
                        id.getVersion().substring(0, id.getVersion().length() - "-SNAPSHOT".length()),
                        id.getClassifier().get(), id.getType().orElse(defaultType));
            }

            Pattern pattern = Pattern.compile(regex);

            LOGGER.debug("File not found, looking for the latest SNAPSHOT");
            try {
                Optional<Path> found = Files.list(repoPath).map(p -> p.getFileName().toString())
                        .filter(pattern.asMatchPredicate()).sorted(Comparator.reverseOrder()).findFirst()
                        .map(s -> repoPath.resolve(s));

                if (found.isPresent()) {
                    file = found.get();
                    LOGGER.debug("A SNAPSHOT file was found for feature {} at {}", id, file);
                }
            } catch (IOException e) {
                LOGGER.warn("An error occurred while searching for snapshot files", e);
            }
        }

        return file;
    }

    private void removeFeaturesConfigurations(final Collection<String> removedFeatures) throws IOException {
        final Set<String> removedPids = removedFeatures.stream().map(featuresConfigurations::remove)
                .filter(Objects::nonNull).flatMap(pids -> pids.stream()).collect(Collectors.toSet());
        if (!removedPids.isEmpty()) {
            configManager.updateConfigurations(null, removedPids);
        }
    }

    private Map<String, Hashtable<String, Object>> loadFeatureConfigurations(final Feature feature) {
        final Map<String, Object> variables = feature.getVariables();

        final Map<String, Hashtable<String, Object>> result = new HashMap<>();
        for (final Entry<String, FeatureConfiguration> entry : feature.getConfigurations().entrySet()) {
            final Hashtable<String, Object> values = new Hashtable<>(entry.getValue().getValues());
            fillInVariables(values, variables);
            result.put(entry.getKey(), values);
        }
        return result;
    }

    void fillInVariables(final Map<String, Object> config, final Map<String, Object> vars) {
        for (Entry<String, Object> entry : config.entrySet()) {
            final Object rawValue = entry.getValue();
            if (rawValue instanceof String) {
                entry.setValue(fillInVariables((String) rawValue, vars));
            }
        }
    }

    private Object fillInVariables(final String value, final Map<String, Object> vars) {
        final Matcher matcher = variablePattern.matcher(value);

        String newValue = value;
        while (matcher.find()) {
            if(matcher.start() == 0 && matcher.end() == value.length()) {
                // Replace the whole entry
                return vars.getOrDefault(matcher.group(1), matcher.group());
            } else {
                newValue = newValue.replace(matcher.group(),
                        String.valueOf(vars.getOrDefault(matcher.group(1), matcher.group())));
            }
        }

        return newValue;
    }
}
