package org.eclipse.sensinact.gateway.device.mosquitto.lite.it;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.it.util.MosquittoTestITAbstract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class DownloadFileTest extends MosquittoTestITAbstract {

    @Test
    public void downloadFile(){
        try {
            String urlString=mavenBundle("org.slf4j", "slf4j-simple", "1.7.25").type("jar").getURL();
            //wrappedBundle(mavenBundle("org.eclipse.sensinact.thirdparty", "mqtt-server-moquette", "0.10-SNAPSHOT")).overwriteManifest(WrappedUrlProvisionOption.OverwriteMode.FULL).instructions("Import-Package=&Export-Package=io.moquette.*").getURL();//wrappedBundle(mavenBundle("org.eclipse.sensinact.thirdparty","mqtt-server-moquette","0.10-SNAPSHOT")).overwriteManifest(WrappedUrlProvisionOption.OverwriteMode.FULL).instructions("Import-Package=&Export-Package=io.moquette.*").getURL();//mavenBundle("org.slf4j", "slf4j-api", "1.7.25").getURL();//mavenBundle("io.netty", "netty-codec-http", "4.1.6.Final").getURL();
            URL url=new URL(urlString);

            InputStreamReader fis=new InputStreamReader(url.openStream());//new FileInputStream(new File(wrappedBundle(mavenBundle("org.mapdb", "mapdb", "1.0.8")).instructions("Embed-Dependency=").getURL().toString()));

            File temps=new File("/home/nj246216/mybundle2.jar");
            FileOutputStream fos=new FileOutputStream(temps,false);

            if(true){
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }else {

                /***
                 *
                 *             Connection conn=(Connection)url.openConnection();

                 URL resourceUrl, base, next;
                 String location;

                 switch (conn.getResponseCode())
                 {
                 case HttpURLConnection.HTTP_MOVED_PERM:
                 case HttpURLConnection.HTTP_MOVED_TEMP:
                 location = conn.getHeaderField("Location");
                 location = URLDecoder.decode(location, "UTF-8");
                 base     = new URL(url.getPath());
                 next     = new URL(base, location);  // Deal with relative URLs
                 url      = new URL(next.toExternalForm());
                 }
                 */
                int val=-1;
                //ByteArrayOutputStream sb=new ByteArrayOutputStream();

                while((val=fis.read())!=-1){
                    //sb.write((byte)val);

                    fos.write((byte)val);
                }
                //fos.write(HTTP.decompress(sb.toByteArray()));
                //fis.close();
                //sb.writeTo(fos);
                //fos.write(sb.toByteArray());
                fos.flush();
                fos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Configuration
    public Option[] config(){
        return combine(
                OptionUtils.expand(when(false).useOptions(//Boolean.getBoolean( "isDebugEnabled" )
                        vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                        systemTimeout(10000))),
                OptionUtils.expand(getProperties()),
                OptionUtils.expand(junitBundles()),
                depProfile1(),
                depProfile2(),
                depProfile3(),
                depProfile4(),
                depProfileHttp(),
                depProfileREST(),
                depProfileMosquitto(),
                OptionUtils.expand(
                        //systemProperty("org.ops4j.pax.url.mvn.defaultRepositories").value(""),
                        //systemProperty("org.ops4j.pax.url.mvn.repositories").value("http://repo1.maven.org/maven2@id=main1@snapshots,http://central.maven.org/maven2/@id=mainrepository@snapshots"),
                        mavenBundle("org.ops4j.pax.url", "pax-url-mvn", "1.3.7"),
                        mavenBundle("org.ops4j.pax.url", "pax-url-wrap", "2.5.3"),
                        mavenBundle("org.ops4j.pax.swissbox", "pax-swissbox-bnd", "1.8.2"),
                        mavenBundle("biz.aQute.bnd", "bndlib", "2.4.0"),
                        mavenBundle("org.ops4j.pax.url", "pax-url-commons", "2.5.3"),
                        mavenBundle("org.ops4j.pax.swissbox", "pax-swissbox-property","1.8.2")
                        //mavenBundle("org.slf4j", "slf4j-simple", "1.7.25").noStart()
                ));
    }

}
