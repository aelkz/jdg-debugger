package com.redhat.jdg.debugger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonTester {
    public static void main(String[] args) {

        String uglyJson = "{\"counter\":\"1\",\"creationTime\":\"Tue Aug 18 11:47:37 EDT 2020\",\"lastAccessedTime\":\"Tue Aug 18 11:47:37 EDT 2020\",\"serverInfo\":\"JBoss EAP 7.3.1.GA (WildFly Core 10.1.7.Final-redhat-00001) - 2.0.30.SP3-redhat-00001\",\"virtualServerName\":\"default-host\",\"remoteAddr\":\"172.16.201.1\",\"remoteHost\":\"172.16.201.1\",\"remotePort\":\"59008\",\"JSESSIONID\":\"GDKOnccPI2nNhCBg09rBM3_3UvEg7YttulVymn6p\",\"attributes\": [\"obj\":\"org.jboss.example.counter.Counter@6c760dd3\"]}";

        System.out.println(uglyJson);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(uglyJson);

        System.out.println(gson.toJson(je));

    }
}
