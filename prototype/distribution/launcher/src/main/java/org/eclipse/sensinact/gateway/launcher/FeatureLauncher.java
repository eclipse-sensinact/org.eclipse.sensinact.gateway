/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.feature.Feature;
import org.osgi.service.feature.FeatureBundle;
import org.osgi.service.feature.FeatureService;
import org.osgi.service.feature.ID;

@Component
public class FeatureLauncher {

    @interface Config {
        String[] features() default {};

        String repository() default "repository";

        String featureDir() default "features";
    }

    @Reference
    FeatureService featureService;

    private BundleContext context;

    /**
     * The bundles installed by this launcher
     */
    private final Map<String, Bundle> bundlesByIdentifier = new HashMap<>();

    /**
     * The lists of artifacts for each feature installed
     */
    private final Map<String, List<String>> featuresToBundles = new HashMap<>();

    /**
     * The list of installed features in installation order
     */
    private final List<String> features = new ArrayList<>();

    private File repository;
    private File featureDir;

    @Activate
    void start(BundleContext context, Config config) {

        this.context = context;

        update(config);
    }

    @Modified
    void update(Config config) {
        repository = new File(config.repository());
        featureDir = new File(config.featureDir());

        List<String> newFeatures = stream(config.features()).collect(toList());

        /**
         * Removed, in installation order
         */
        List<String> removed = features.stream().filter(s -> !newFeatures.contains(s)).collect(toList());

        // First remove all removes
        removeFeatures(removed);

        // Second do all add/updates
        newFeatures.forEach(this::addOrUpdate);

        features.clear();
        features.addAll(newFeatures);

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

        // Normal iteration order is now reverse install order
        // First we stop then we uninstall
        for (String s : orderedBundlesForRemoval) {
            Bundle b = bundlesByIdentifier.get(s);
            if (b != null) {
                try {
                    b.stop();
                } catch (BundleException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        for (String s : orderedBundlesForRemoval) {
            Bundle b = bundlesByIdentifier.remove(s);
            if (b != null) {
                try {
                    b.uninstall();
                } catch (BundleException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void addOrUpdate(String feature) {

        Feature featureModel = loadFeature(feature);

        List<String> bundles = featureModel.getBundles().stream().map(fb -> fb.getID().toString()).collect(toList());
        if (featuresToBundles.containsKey(feature)) {
            if (featuresToBundles.get(feature).equals(bundles)) {
                // No work to do, already installed
                return;
            } else {
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
                b.start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private Feature loadFeature(String feature) {

        File featureFile;
        if (feature.indexOf(':') >= 0) {
            ID id = featureService.getIDfromMavenCoordinates(feature);
            featureFile = getFileFromRepository(id, "json");
        } else {
            String simpleFileName = feature;
            if (!simpleFileName.endsWith(".json")) {
                simpleFileName += ".json";
            }

            featureFile = new File(featureDir, simpleFileName);
        }

        if (featureFile.exists()) {
            try (FileReader fr = new FileReader(featureFile, UTF_8)) {
                return featureService.readFeature(fr);
            } catch (IOException ioe) {
                // TODO Auto-generated catch block
                ioe.printStackTrace();
            }
        }

        return null;
    }

    private Bundle installBundle(ID bundleIdentifier) {
        try (InputStream is = getBundle(bundleIdentifier)) {
            return context.installBundle(bundleIdentifier.toString(), is);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private File getFileFromRepository(ID id, String defaultType) {
        File file;
        String groupPath = id.getGroupId().replace('.', File.separatorChar);

        Path path = repository.toPath().resolve(groupPath).resolve(id.getArtifactId()).resolve(id.getVersion());

        String fileName;
        if (id.getClassifier().isEmpty()) {
            fileName = String.format("%s-%s", id.getArtifactId(), id.getVersion());
        } else {
            fileName = String.format("%s-%s-%s", id.getArtifactId(), id.getVersion(), id.getClassifier().get());
        }

        fileName = fileName.concat(String.format(".%s", id.getType().orElse(defaultType)));

        file = path.resolve(fileName).toFile();

        if (!file.exists() && id.getVersion().endsWith("-SNAPSHOT")) {
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

            try {
                file = Files.list(path).map(p -> p.getFileName().toString()).filter(pattern.asMatchPredicate())
                        .sorted(Comparator.reverseOrder()).findFirst().map(s -> path.resolve(s).toFile()).orElse(file);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return file;
    }

    private InputStream getBundle(ID id) throws IOException {
        File bundleFile = getFileFromRepository(id, "jar");
        return new FileInputStream(bundleFile);
    }

}
