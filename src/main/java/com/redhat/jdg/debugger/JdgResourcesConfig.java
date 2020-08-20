package com.redhat.jdg.debugger;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.logging.Logger;
import javax.security.auth.callback.*;
import javax.security.sasl.RealmCallback;
import java.io.IOException;

public class JdgResourcesConfig {

    private static final org.jboss.logging.Logger LOGGER = Logger.getLogger(JdgResourcesConfig.class);

    private static RemoteCacheManager cacheManager = null;
    private static final String SERVER_NAME = "jdgserver";

    static RemoteCache<Object, Object> remoteCache(String query) {
        String cacheName = null;

        if (query != null && query.contains("cache=")) {
            cacheName = query.split("=")[1];
        }else {
            cacheName = System.getProperty("infinispan.client.hotrod.cache");
        }

        LOGGER.infov("acquiring remote cache connection: {0}", cacheName);
        return remoteCacheManager().getCache(cacheName);
    }

    private static RemoteCacheManager remoteCacheManager() {
        if (cacheManager == null) {
            String serverList = System.getProperty("infinispan.client.hotrod.server_list");
            final String userName = System.getProperty("infinispan.client.hotrod.username");
            final String password = System.getProperty("infinispan.client.hotrod.password");
            LOGGER.infov("REMOTE-CacheManager builder initialization with servers: {0}", serverList);

            ConfigurationBuilder confBuilder = new ConfigurationBuilder()
                .tcpNoDelay(true).connectionPool().numTestsPerEvictionRun(3).testOnBorrow(true).testOnReturn(true)
                .testWhileIdle(true).addServers(serverList);

            org.infinispan.client.hotrod.configuration.Configuration remoteConf = null;

            if (userName != null && password != null) {
                final String REALM = "ApplicationRealm";

                remoteConf = confBuilder
                        // add configuration for authentication
                        // define server name, should be specified in XML configuration
                        .security().authentication().serverName(SERVER_NAME)
                        .saslMechanism("DIGEST-MD5") // define SASL mechanism,
                        // in this example we use DIGEST with MD5 hash
                        .callbackHandler(new CallbackHandler() {
                            @Override
                            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                                for (Callback callback : callbacks) {
                                    if (callback instanceof NameCallback) {
                                        ((NameCallback) callback).setName(userName);
                                    } else if (callback instanceof PasswordCallback) {
                                        ((PasswordCallback) callback).setPassword(password.toCharArray());
                                    } else if (callback instanceof RealmCallback) {
                                        ((RealmCallback) callback).setText(REALM);
                                    } else {
                                        throw new UnsupportedCallbackException(callback);
                                    }
                                }
                            }
                        }) // define login handler, implementation defined
                        .enable().build();
            } else {
                remoteConf = confBuilder.build();
            }
            cacheManager = new RemoteCacheManager(remoteConf);
        }
        return cacheManager;
    }

    static void closeRemoteCacheManager() {
        LOGGER.info("Stop REMOTE-cache's RemoteCacheManager \n");
        remoteCacheManager().stop();
        cacheManager = null;
    }

}
