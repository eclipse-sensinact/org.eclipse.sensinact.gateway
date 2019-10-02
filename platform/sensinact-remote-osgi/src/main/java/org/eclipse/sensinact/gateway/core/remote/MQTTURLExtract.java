package org.eclipse.sensinact.gateway.core.remote;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MQTTURLExtract {

    final String brokerString;
    private Pattern p;
    private Matcher m;

    public MQTTURLExtract(String brokerString){
        this.brokerString=brokerString;
        this.p=Pattern.compile("(?<protocol>.*)(\\:\\/\\/{1})(?<host>.*)((:)(?<port>\\d+))");
        this.m=p.matcher(this.brokerString);
        this.m.matches();
    }

    public String getProtocol(){
        return m.group("protocol");
    }

    public String getHost(){
        return m.group("host");
    }

    public Integer getPort(){
        return Integer.parseInt(m.group("port"));
    }

    public static void main(String[] args) {
        MQTTURLExtract q=new MQTTURLExtract("tcp://sensinact-cea.ddns.net:5269");
        System.out.println(q.getHost());
        System.out.println(q.getPort());
        System.out.println(q.getProtocol());
    }

}
