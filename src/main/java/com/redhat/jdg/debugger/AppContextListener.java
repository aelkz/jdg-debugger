package com.redhat.jdg.debugger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppContextListener implements ServletContextListener {

    private static Pattern envPattern = Pattern.compile("(?<=\\$\\{)(.*?)(?=\\})");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String jdg_serverList = sce.getServletContext().getInitParameter("jdg_serverList");
        String jdg_cache = sce.getServletContext().getInitParameter("jdg_cache");

        if(jdg_serverList == null || jdg_cache == null){
            throw new IllegalStateException("jdg_serverList and jdg_cache context parameters must be specified!");
        }

        String jdg_userName = sce.getServletContext().getInitParameter("jdg_userName");
        String jdg_password = sce.getServletContext().getInitParameter("jdg_password");

        String jdg_serverList_replaced = jdg_serverList;
        Matcher matcher = envPattern.matcher(jdg_serverList);

        while(matcher.find()){
            String group = matcher.group();
            String env = System.getenv(group);
            jdg_serverList_replaced = jdg_serverList_replaced.replace("${"+group+"}", env);
        }

        System.setProperty("infinispan.client.hotrod.server_list", jdg_serverList_replaced);
        System.setProperty("infinispan.client.hotrod.cache", jdg_cache);

        if(jdg_userName != null && jdg_password != null){
            System.setProperty("infinispan.client.hotrod.username", jdg_userName);
            System.setProperty("infinispan.client.hotrod.password", jdg_password);
        }
   }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JdgResourcesConfig.closeRemoteCacheManager();
    }

}
