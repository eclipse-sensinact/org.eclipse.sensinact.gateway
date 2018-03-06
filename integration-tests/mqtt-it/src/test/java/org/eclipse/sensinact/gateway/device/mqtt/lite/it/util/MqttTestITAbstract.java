/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.device.mqtt.lite.it.util;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.slf4j.Slf4jLoggerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Provider;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Resource;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ops4j.pax.exam.*;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;

import java.util.*;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Abstract class used to configure the base for the integration tests
 */
public abstract class MqttTestITAbstract {

    protected static final String SENSINACT_HTTP_PORT="8097";
    protected static final String SENSINACT_VERSION="1.5-SNAPSHOT";
    protected static final String MQTT_HOST ="127.0.0.1";
    protected static final Integer MQTT_PORT =1883;

    protected static Option[] combine(Option[]...options){

        List<Option> optionsnew=new ArrayList<Option>();

        for(Option[] optionsit:options){
            optionsnew.addAll(Arrays.asList(OptionUtils.combine(optionsit)));
        }

        Option optionResult[]=new Option[optionsnew.size()];

        return optionsnew.toArray(optionResult);

    }

    protected Option[] depProfile1(){
        return options(
                systemProperty("org.ops4j.pax.url.mvn.repositories").value("http://central.maven.org/maven2/@snapshots@id=ops4j-snapshotkok"),
                mavenBundle("org.apache.felix", "org.apache.felix.framework.security", "2.4.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime", "0.12.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell", "0.12.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.command", "0.12.0"),
                mavenBundle("ch.qos.logback", "logback-core", "1.1.7"),
                mavenBundle("org.slf4j", "slf4j-api", "1.7.25"),
                mavenBundle("ch.qos.logback", "logback-classic", "1.1.7")
        );
    }

    protected Option[] depProfile2(){
        return options(
                mavenBundle("org.eclipse.platform", "org.eclipse.equinox.cm", "1.1.200"),
                mavenBundle("org.osgi", "org.osgi.compendium", "5.0.0"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-framework-extension", SENSINACT_VERSION)
        );
    }

    protected Option[] depProfile3(){
        return options(
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-utils", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-common", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-datastore-api", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-signature-validator", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-security-none", SENSINACT_VERSION).noStart(),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-core", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-generic", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-shell", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-system", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway.nthbnd", "sensinact-northbound-access", SENSINACT_VERSION)
        );
    }

    protected Option[] depProfile4(){
        return options(
                mavenBundle("org.apache.felix", "org.apache.felix.ipojo", "1.12.0"),
                wrappedBundle(mavenJar("org.apache.maven", "maven-aether-provider", "3.3.9")),
                wrappedBundle(mavenJar("org.eclipse.aether", "aether-connector-basic", "1.1.0")),
                wrappedBundle(mavenJar("org.eclipse.aether", "aether-spi", "1.1.0")),
                wrappedBundle(mavenJar("org.eclipse.aether", "aether-transport-file", "1.1.0")),
                //wrappedBundle(mavenJar("org.eclipse.aether", "aether-transport-http", "1.1.0")),
                wrappedBundle(mavenJar("org.eclipse.aether", "aether-util", "1.1.0")),
                wrappedBundle(mavenJar("org.eclipse.aether", "aether-impl", "1.1.0")),
                wrappedBundle(mavenJar("org.eclipse.aether", "aether-api", "1.1.0"))
        );
    }

    /**
     * This method bypass the need of having the dependency declared in the pom.xml, allowing the test to declare dependencies not associated
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     */
    private MavenArtifactProvisionOption fetch(String groupId, String artifactId, String version){
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.setService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.setService(TransporterFactory.class, FileTransporterFactory.class);
        locator.setService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.setService(org.eclipse.aether.spi.log.LoggerFactory.class, Slf4jLoggerFactory.class);
        RepositorySystem system = locator.getService(RepositorySystem.class);
        RepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        ((DefaultRepositorySystemSession)session).setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(System.getProperty("user.home")+"/.m2/repository/")));

        ArtifactRequest req = new ArtifactRequest();
        req.addRepository(new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2").build());
        req.setArtifact(new DefaultArtifact(groupId, artifactId, "jar", version));
        try {
            ArtifactResult res = system.resolveArtifact(session, req);
        } catch (ArtifactResolutionException e) {
            e.printStackTrace();
        }

        return mavenBundle(groupId, artifactId, version);
    }

    protected Option[] depProfileMqtt(){

        return options(
                mavenBundle("org.eclipse.paho", "org.eclipse.paho.client.mqttv3", "1.2.0"),
                //mavenBundle("org.eclipse.paho", "org.eclipse.paho.api.mqttv3", "1.2.0"),
                mavenBundle("org.eclipse.sensinact.gateway.sthbnd.mqtt", "mqtt-device", SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway.sthbnd.mqtt", "smart-topic-device", SENSINACT_VERSION)
        );
    }

    protected Option[] depProfileHttp(){
        return options(
                mavenBundle("org.eclipse.sensinact.gateway.protocol", "http",SENSINACT_VERSION),
                mavenBundle("org.eclipse.sensinact.gateway.sthbnd.http", "http-device", SENSINACT_VERSION),
                mavenBundle("org.apache.felix", "org.apache.felix.http.api", "2.3.2"),
                mavenBundle("org.apache.felix", "org.apache.felix.http.jetty", "3.0.0"),
                mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0")
        );
    }

    protected Option[] depProfileREST(){
        return options(
                mavenBundle("org.eclipse.sensinact.gateway.nthbnd", "rest-access", SENSINACT_VERSION)
        );
    }

    protected Option[] getBundleRequiredByURLResolvers(){
        return options(OptionUtils.expand(
                mavenBundle("org.ops4j.pax.url", "pax-url-mvn", "1.3.7"),
                mavenBundle("org.ops4j.pax.url", "pax-url-wrap", "2.5.3"),
                mavenBundle("org.ops4j.pax.url", "pax-url-commons", "2.5.3"),
                mavenBundle("org.ops4j.pax.swissbox", "pax-swissbox-bnd", "1.8.2"),
                mavenBundle("org.ops4j.pax.swissbox", "pax-swissbox-property", "1.8.2"),
                mavenBundle("org.ops4j.pax.swissbox", "pax-swissbox-extender", "1.8.2"),
                mavenBundle("biz.aQute.bnd", "bndlib", "2.4.0")
        ));
    }

    protected Option[] getProperties(){
        return options(
                new FrameworkPropertyOption("org.osgi.framework.system.packages.extra").value("com.google.common.base,javax.net.ssl,javax.smartcardio,sun.security.action,com.sun.net.httpserver,javax.mail,javax.mail.internet,javax.cache.spi,javax.cache,javax.cache.integration,javax.cache.empiry,javax.cache.expiry,javax.cache.configuration,javax.cache.processor,javax.cache.management,javax.cache.event,sun.misc")
                ,new FrameworkPropertyOption("felix.shutdown.hook").value("false")
                ,new FrameworkPropertyOption("org.osgi.service.http.port").value(SENSINACT_HTTP_PORT)
                //must import certificate keytool -import -alias mosquitto.org -file mosquitto.pem -keystore /opt/jre/lib/security/cacerts
                ,new SystemPropertyOption("javax.net.ssl.trustStore").value("/opt/jre/lib/security/cacerts")///etc/ssl/certs/java/cacerts
                //,new SystemPropertyOption("javax.net.ssl.keyStore").value("/home/nj246216/mosquitto.jks")//mosquitto.jks
                ,new SystemPropertyOption("javax.net.ssl.keyStorePassword").value("ceacea")
        );
    }

    @Configuration
    public Option[] config(){
        return combine(
                OptionUtils.expand(when(Boolean.getBoolean( "isDebug" )).useOptions(
                        vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" ),
                        systemTimeout( 10000 ))),
                OptionUtils.expand(getProperties()),
                OptionUtils.expand(junitBundles()),
                depProfile1(),
                depProfile2(),
                depProfile3(),
                depProfile4(),
                depProfileHttp(),
                depProfileREST(),
                depProfileMqtt(),
                getBundleRequiredByURLResolvers()
        );

    }

    protected Provider createDevicePojo(String providerString, String serviceString, String resourceString, String topic) throws MqttException {
        MqttBroker broker = new MqttBroker.Builder()
                .host(MQTT_HOST)
                .port(MQTT_PORT)
                .build();

        broker.connect();

        while(broker.getClient()==null||!broker.getClient().isConnected()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Provider provider = new Provider();
        provider.setName(providerString);
        provider.setBroker(broker);

        Service service=new Service(provider);
        service.setName(serviceString);

        provider.getServices().add(service);

        Resource resource = new Resource(service);
        resource.setName(resourceString);
        resource.setTopic(topic);
        service.getResources().add(resource);

        return provider;
    }

    public MqttClient getMqttConnection(String host, int port) throws MqttException {
        MqttBroker broker = new MqttBroker.Builder()
                .host(host)
                .port(port)
                .protocol(MqttBroker.Protocol.TCP)
                .build();

        broker.connect();

        return broker.getClient();
    }

    protected JSONObject invokeRestAPI(String URL) throws Exception {
        HTTP http = new HTTP();
        String result = http.submit(String.format("http://localhost:%s/%s", SENSINACT_HTTP_PORT, URL));
        JSONObject jo = new JSONObject(result);
        System.out.println(String.format("HTTPResponse body: %s",result));

        return jo;
    }

    protected Set<String> parseJSONArrayIntoSet(JSONArray joArray){

        final Set<String> resultSet = new HashSet<String>();

        for(Iterator it=joArray.iterator();it.hasNext();) {
            resultSet.add((String)it.next());
        }

        return resultSet;
    }

}
