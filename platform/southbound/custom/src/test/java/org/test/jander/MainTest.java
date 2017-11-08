package org.test.jander;

import io.moquette.BrokerConstants;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;
import junit.framework.Assert;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTConnection;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Service;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt.MQTTBroker;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime.MQTTManagerRuntime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class MainTest {

    @Inject
    BundleContext bc;

    private static final String SENSINACT_HTTP_PORT="8097";
    private static final String SENSINACT_MQTT_PORT="1885";
    private static final String SENSINACT_VERSION="1.5-SNAPSHOT";
    private static Server server;
    @Before
    public void beforeddd() throws IOException {
        System.out.println("ok");
        MemoryConfig mc=new MemoryConfig(new Properties()){
            void assignDefaults() {
                setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(BrokerConstants.PORT));
                setProperty(BrokerConstants.HOST_PROPERTY_NAME, BrokerConstants.HOST);
                //setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(BrokerConstants.WEBSOCKET_PORT));
                setProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, "");
                //setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, BrokerConstants.DEFAULT_PERSISTENT_PATH);
                setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
                setProperty(BrokerConstants.AUTHENTICATOR_CLASS_NAME, "");
                setProperty(BrokerConstants.AUTHORIZATOR_CLASS_NAME, "");
            };
        };
        mc.setProperty(BrokerConstants.PORT_PROPERTY_NAME, SENSINACT_MQTT_PORT);
        mc.setProperty(BrokerConstants.HOST_PROPERTY_NAME, "localhost");
        mc.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME,"");
        //mc.setProperty("storage_class","io.moquette.persistence.h2.H2PersistentStore");
        server=new Server();
        //server.
        server.startServer(mc);
    }
/*
    public static void main(String[] args) throws Exception {
        java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] { String.class });
        m.setAccessible(true);
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Object test1 = m.invoke(cl, "TestLoaded$ClassToTest");
        System.out.println(test1 != null);
        ClassToTest.reportLoaded();
        Object test2 = m.invoke(cl, "TestLoaded$ClassToTest");
        System.out.println(test2 != null);
    }
    static class ClassToTest {
        static {
            System.out.println("********************** Loading " + AbstractMessage.class.getName());
            AbstractMessage.QOSType m=AbstractMessage.QOSType.FAILURE;
            System.out.println(m);
        }
        static void reportLoaded() {
            System.out.println("Loaded");
        }
    }
*/
    @After
    public void afterbb(){
        server.stopServer();
    }

    @Test
    public void te() throws IOException {

    }

    private Option[] getProperties(){

        String path=null;
/*
        try {
            File temps=File.createTempFile("pax",".policy",new File("."));
            path=temps.getAbsolutePath();
            System.out.println("PATH="+path);
            FileOutputStream fos=new FileOutputStream(temps);
            BufferedOutputStream bos=new BufferedOutputStream(fos);
            StringBuffer sb=new StringBuffer();
            sb.append("grant codeBase \"file:bin/felix.jar\" { permission java.security.AllPermission;};\n");
            sb.append("grant codeBase \"http://felix.extensions:9/\" { permission java.security.AllPermission; };\n");
            sb.append("grant codeBase \"file:bundle/\" { permission java.security.AllPermission; };\n");
            sb.append("grant codeBase \"file:/tmp/\" { permission java.security.AllPermission; };\n");
            bos.write(sb.toString().getBytes());
            bos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        String prop=null;
/*
        try {
            File temps=File.createTempFile("pax",".properties",new File("."));
            prop=temps.getAbsolutePath();
            System.out.println("PATH="+prop);
            FileOutputStream fos=new FileOutputStream(temps);
            BufferedOutputStream bos=new BufferedOutputStream(fos);
            StringBuffer sb=new StringBuffer();
            sb.append("a=b");
            bos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

*/
        return options(
                new FrameworkPropertyOption("org.osgi.framework.system.packages.extra").value("com.google.common.base,javax.net.ssl,javax.smartcardio,sun.security.action,com.sun.net.httpserver,javax.mail,javax.mail.internet")
                //,new FrameworkPropertyOption("org.osgi.framework.security").value("osgi")
                ,new FrameworkPropertyOption("org.eclipse.sensinact.gateway.security.jks.filename").value("/home/nj246216/projects/sensinact-eclipse/distribution/sensinact-distribution-template/datastore/keystore/keystore.jks")
                ,new FrameworkPropertyOption("org.eclipse.sensinact.gateway.security.jks.password").value("sensiNact_team")
                //,new FrameworkPropertyOption("java.security.policy").value(path)
                ,new FrameworkPropertyOption("felix.shutdown.hook").value("false")
                ,new FrameworkPropertyOption("org.osgi.service.http.port").value(SENSINACT_HTTP_PORT)


        );
    }

    private static Option[] combine(Option[]...options){

        List<Option> optionsnew=new ArrayList<Option>();

        for(Option[] optionsit:options){
            optionsnew.addAll(Arrays.asList(OptionUtils.combine(optionsit)));
        }

        Option optionResult[]=new Option[optionsnew.size()];

        return optionsnew.toArray(optionResult);

    }

    private Option[] depProfile1(){
        return options(
                mavenBundle("org.slf4j", "osgi-over-slf4j", "1.7.25"),
                mavenBundle("org.apache.felix", "org.apache.felix.framework.security", "2.4.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime", "0.12.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell", "0.12.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.command", "0.12.0"),
                mavenBundle("org.slf4j", "slf4j-api", "1.7.25"),
                mavenBundle("ch.qos.logback", "logback-classic", "1.1.7"),
                mavenBundle("ch.qos.logback", "logback-core", "1.1.7")
        );
    }

    private Option[] depProfile2(){
        return options(
                mavenBundle("org.apache.felix", "org.apache.felix.fileinstall", "3.5.0"),
                mavenBundle("org.eclipse.platform", "org.eclipse.equinox.cm", "1.1.200"),
                mavenBundle("org.osgi", "org.osgi.compendium", "5.0.0"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-framework-extension", SENSINACT_VERSION)
        );
    }

    private Option[] depProfile3(){
        return options(
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-utils", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-common", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-datastore-api", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-signature-validator", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-security-none", "1.5-SNAPSHOT").noStart(),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-core", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-generic", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-shell", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway", "sensinact-system", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway.nthbnd", "sensinact-northbound-access", "1.5-SNAPSHOT")
        );
    }

    private Option[] depProfile4(){
        return options(
                mavenBundle("org.apache.felix", "org.apache.felix.ipojo", "1.12.0")
        );
    }

    private Option[] depProfileMosquitto(){
        return options(
                mavenBundle("org.eclipse.paho", "org.eclipse.paho.client.mqttv3", "1.2.0"),
                mavenBundle("org.eclipse.sensinact.gateway.sthbnd", "sensinact-mosquitto", "1.5-SNAPSHOT")
        );
    }

    private Option[] depProfileHttp(){
        return options(
                mavenBundle("org.eclipse.sensinact.gateway.protocol", "http", "1.5-SNAPSHOT"),
                mavenBundle("org.eclipse.sensinact.gateway.sthbnd.http", "http-device", "1.5-SNAPSHOT"),
                mavenBundle("org.apache.felix", "org.apache.felix.http.api", "2.3.2"),
                mavenBundle("org.apache.felix", "org.apache.felix.http.jetty", "3.0.0"),
                mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0")
        );
    }

    private Option[] depProfileREST(){
        return options(
                mavenBundle("org.eclipse.sensinact.gateway.nthbnd", "rest-access", "1.5-SNAPSHOT")
        );
    }

    /*

<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-codec-mqtt</artifactId>
    <version>4.1.16.Final</version>
</dependency>

     */

    private Option[] depMoquette9(){
        final String moquetteversion="0.9";
        return options(
                mavenBundle("io.moquette", "moquette-broker", moquetteversion),
                mavenBundle("org.mapdb", "mapdb", "1.0.8"),
                mavenBundle("com.hazelcast", "hazelcast", "3.5.4"),
                mavenBundle("io.moquette", "moquette-netty-parser", moquetteversion),
                //mavenJar("io.moquette", "moquette-parser-commons", "0.8.1"),
                mavenJar("io.netty", "netty-all", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-buffer", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-common", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-transport", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-resolver", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-codec-http", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-codec", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-handler", "4.1.6.Final"),
                mavenJar("io.netty", "netty-codec-mqtt", "4.1.16.Final"),
                /*
                mavenJar("io.netty", "netty-codec-mqtt", "4.1.16.Final"),
                */
                mavenBundle("commons-codec", "commons-codec", "1.10")
        );
    }

/*
           <dependency>
              <groupId>io.netty</groupId>
              <artifactId></artifactId>
              <version>${project.version}</version>
            </dependency>
 */

    private Option[] depMoquette10(){
        final String moquetteversion="0.9";
        final String netty="4.1.12.Final";
        return options(
                frameworkProperty("org.osgi.framework.system.capabilities").value("osgi.ee;osgi.ee=\"JavaSE\";version:List=\"1.6,1.7,1.8\""),
                wrappedBundle(mavenJar("io.netty", "netty-transport-native-epoll", netty).classifier("linux-x86_64")).exports("io.netty.channel.unix"),
                wrappedBundle(mavenJar("io.netty", "netty-transport-native-unix-common", netty)).exports("io.netty.channel.unix"),
                wrappedBundle(mavenJar("io.netty", "netty-codec-mqtt", netty)),
                mavenBundle("io.moquette", "moquette-broker", moquetteversion),
                wrappedBundle(mavenJar("com.bugsnag", "bugsnag", "3.1.1").getURL()).exports("com.bugsnag"),
                mavenBundle("org.mapdb", "mapdb", "1.0.8"),
                mavenBundle("io.dropwizard.metrics", "metrics-core", "3.2.2"),
                wrappedBundle(mavenBundle("com.librato.metrics", "metrics-librato", "5.1.0")).bundleSymbolicName("librato"),
                mavenBundle("com.hazelcast", "hazelcast", "3.5.4"),
                mavenBundle("io.moquette", "moquette-netty-parser", "0.9"),
                //mavenJar("io.moquette", "moquette-parser-commons", "0.8.1"),
                mavenJar("io.netty", "netty-all", "4.1.6.Final"),
                mavenBundle("io.netty", "netty-buffer", netty),
                mavenBundle("io.netty", "netty-common", netty),
                mavenBundle("io.netty", "netty-transport", netty),
                mavenBundle("io.netty", "netty-resolver", netty),
                mavenBundle("io.netty", "netty-codec-http", netty),
                mavenBundle("io.netty", "netty-codec", netty),
                mavenBundle("io.netty", "netty-handler", netty),
                mavenJar("io.netty", "netty-codec-mqtt", netty),
                mavenBundle("commons-codec", "commons-codec", "1.10")
        );
    }

    @Configuration
    public Option[] config(){
        return combine(
                OptionUtils.expand(getProperties()),
                OptionUtils.expand(junitBundles()),
                depMoquette10(),
                depProfile1(),
                depProfile2(),
                depProfile3(),
                depProfile4(),
                depProfileHttp(),
                depProfileREST(),
                depProfileMosquitto()
                );
    }

    public MqttClient getMqttConnection(String host, String port) throws MqttException {
        final String brokerURLConnection=String.format("%s://%s:%s","tcp",host,port);
        final MemoryPersistence persistence = new MemoryPersistence();
        MQTTConnection connection = new MQTTConnection(new MqttClient(brokerURLConnection, "clientid", persistence));
        connection.getConnection().connect();
        return connection.getConnection();
    }

    private Provider createDevicePojo(String providerString,String serviceString,String resourceString,String topic){
        MQTTBroker mb=new MQTTBroker();
        mb.setHost("localhost");
        mb.setPort(Long.parseLong(SENSINACT_MQTT_PORT));

        Provider provider=new Provider();
        provider.setName(providerString);
        provider.setBroker(mb);

        Service service=new Service(provider);
        service.setName(serviceString);

        provider.getServices().add(service);

        Resource resource=new Resource(service);
        resource.setName(resourceString);
        resource.setTopic(topic);
        service.getResources().add(resource);

        return provider;
    }

    public JSONObject invokeRestAPI(String URL) throws Exception {
        HTTP http=new HTTP();

        String result=http.submit(String.format("http://localhost:%s/%s",SENSINACT_HTTP_PORT,URL));
        JSONObject jo=new JSONObject(result);

        System.out.println("result:" + result);

        return jo;
    }

    private Set<String> parseJSONArrayIntoSet(JSONArray joArray){

        final Set<String> resultSet=new HashSet<String>();

        for(Iterator it=joArray.iterator();it.hasNext();){
            resultSet.add((String)it.next());
        }

        return resultSet;
    }

    @Test
    public void testProviderCreation() throws Exception {
        Provider provider=createDevicePojo("myprovider","myservice","myresource","/myresource");
        bc.registerService(Provider.class.getName(),provider,new Hashtable<String, Object>());
        final Set<String> providersSet=parseJSONArrayIntoSet(invokeRestAPI("sensinact/providers").getJSONArray("providers"));
        Assert.assertTrue("Provider was not created, or at least is not shown via REST api",providersSet.contains("myprovider"));
    }

    @Test
    public void testProviderRemoval() throws Exception {
        Provider provider=createDevicePojo("myprovider","myservice","myresource","/myresource");
        bc.registerService(Provider.class.getName(),provider,new Hashtable<String, Object>());
        final Set<String> providersSet=parseJSONArrayIntoSet(invokeRestAPI("sensinact/providers").getJSONArray("providers"));
        Assert.assertTrue("Provider was not created, or at least is not shown via REST api",providersSet.contains("myprovider"));
        //Make provider disappear
        MQTTManagerRuntime.getInstance().processRemoval("myprovider");
        final Set<String> providersSetNo=parseJSONArrayIntoSet(invokeRestAPI("sensinact/providers").getJSONArray("providers"));
        Assert.assertTrue("Provider was not created, or at least is not shown via REST api",!providersSetNo.contains("myprovider"));
    }



    @Test
    public void testServiceCreation() throws Exception {
        testProviderCreation();
        final Set<String> servicesSet=parseJSONArrayIntoSet(invokeRestAPI("sensinact/providers/myprovider").getJSONObject("response").getJSONArray("services"));
        Assert.assertTrue("Service was not created, or at least is not shown via REST api",servicesSet.contains("myservice"));
    }

    @Test
    public void testResourceCreation() throws Exception {
        testServiceCreation();
        final JSONArray servicesArray=invokeRestAPI("sensinact/providers/myprovider/services/myservice").getJSONObject("response").getJSONArray("resources");

        final Set<String> resourcesSet=new HashSet<String>();

        for(Iterator it=servicesArray.iterator();it.hasNext();) {
            resourcesSet.add(((JSONObject) it.next()).getString("name"));
        }

        Assert.assertTrue("Resource was not created, or at least is not shown via REST api", resourcesSet.contains("myresource"));
    }

    @Test
    public void testResourceValueEmpty() throws Exception {
        testServiceCreation();

        //Checks first value empty
        JSONObject responseJSON=invokeRestAPI("sensinact/providers/myprovider/services/myservice/resources/myresource/GET");
        String value=responseJSON.getJSONObject("response").getString("value");
        Assert.assertTrue("Initial Resource value should be empty ", value.equals(""));
    }

    @Test
    public void testResourceValueNonEmpty() throws Exception {
        te();

        testResourceValueEmpty();

        MqttClient mqttClient=getMqttConnection("localhost",SENSINACT_MQTT_PORT);

        final String firstValueTest="firstValue";
        final String secondValueTest="13";
        //(byte) 0x80

        MqttMessage mes=new MqttMessage(firstValueTest.getBytes());
        mes.setQos(0);
        mqttClient.publish("/myresource",mes );

        //Checks first value empty
        JSONObject responseJSON=invokeRestAPI("sensinact/providers/myprovider/services/myservice/resources/myresource/GET");

        Assert.assertTrue("Value should be updated on new message arrival, and was not the case", responseJSON.getJSONObject("response").getString("value").equals(firstValueTest));

        MqttMessage mes2=new MqttMessage(secondValueTest.getBytes());
        mes2.setQos(0);
        mqttClient.publish("/myresource",mes2 );

        String value=invokeRestAPI("sensinact/providers/myprovider/services/myservice/resources/myresource/GET").getJSONObject("response").getString("value");

        Assert.assertTrue("Value should be updated on new message arrival, and was not the case", value.equals(secondValueTest));

    }


}