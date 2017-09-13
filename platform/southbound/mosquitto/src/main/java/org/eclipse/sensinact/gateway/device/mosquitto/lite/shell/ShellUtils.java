/*
 * #%L
 * sensiNact IoT Gateway - EchoNet Lite Protocol Device
 * %%
 * Copyright (C) 2015 CEA
 * %%
 * sensiNact - 2015
 * 
 * CEA - Commissariat a l'energie atomique et aux energies alternatives
 * 17 rue des Martyrs
 * 38054 Grenoble
 * France
 * 
 * Copyright(c) CEA
 * All Rights Reserved
 * #L%
 */
package org.eclipse.sensinact.gateway.device.mosquitto.lite.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility Shell class used to create ASCII boxes
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Nascimento</a>
 */
public class ShellUtils {

    public static String reproduceChar(String ch, Integer amount){

        StringBuffer sb=new StringBuffer();

        for(int x=0;x<amount;x++){
            sb.append(ch);
        }

        return sb.toString();
    }

    public static StringBuilder createASCIIBox(String prolog, StringBuilder sb) throws IOException {

        StringBuilder result=new StringBuilder();

        StringReader sr=new StringReader(sb.toString());

        List<Integer> sizeColums=new ArrayList<Integer>();

        String line;

        BufferedReader br=new BufferedReader(sr);
        while((line=br.readLine())!=null){
            sizeColums.add(Integer.valueOf(line.length()));
        }

        Collections.sort(sizeColums);
        Collections.reverse(sizeColums);

        Integer maxColumn=sizeColums.isEmpty()?0:sizeColums.get(0);
        if(maxColumn>45) maxColumn=45;
        Integer prologSize=prolog.length();

        result.append(reproduceChar(" ",prologSize)+"."+reproduceChar("_",maxColumn)+"\n");

        sr=new StringReader(sb.toString());
        br=new BufferedReader(sr);
        int lineIndex=0;
        while((line=br.readLine())!=null){

            if(lineIndex==((Integer)(sizeColums.size()/2))){
                result.append(prolog);
            }else {
                result.append(reproduceChar(" ",prologSize));
            }

            result.append("|" + line + "\n");
            lineIndex++;
        }

        result.append(reproduceChar(" ",prologSize)+"|"+reproduceChar("_",maxColumn)+"\n");



        return result;
    }

    public static String getArgumentValue(String option, String... params) {
        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {

            /**
             * In case of a Null option, returns the last parameter.
             */
            if (option == null) {
                return params[params.length - 1];
            }

            if (i <= (params.length - 1) && params[i].equals(option)) {
                found = true;
                try {
                    value = params[i + 1];
                }catch (ArrayIndexOutOfBoundsException e){
                    value = "";
                }
                break;
            }
        }

        if (found) {
            return value;
        }
        return null;
    }

}
