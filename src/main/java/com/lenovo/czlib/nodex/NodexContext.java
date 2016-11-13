package com.lenovo.czlib.nodex;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.lenovo.czlib.nodex.conf.ZKProperties;

public class NodexContext {
    private static final Logger logger = Logger.getLogger(NodexContext.class);

    public static final String PN_NODEX_ZOOKEEPER_CONNSTR = "nodex.zookeeper.connstr";
    public static final String PN_NODEX_APPID = "nodex.app.id";
    public static final String PN_NODEX_GROUPID = "nodex.group.id";

    public static final String PN_NODEX_FC_GROUPID = "nodex.fc.groupid";
    public static final String PN_NODEX_FC_CACHE_HOST = "nodex.fc.cache.host";
    public static final String PN_NODEX_FC_CACHE_PORT = "nodex.fc.cache.port";

    private static Properties properties;

    private static ZKHelper zkh;

    private NodexContext() {
    }

    static {
        InputStream is = null;
        try {
            String nodexConfPath = System.getProperty("nodex.conf");
            if (nodexConfPath != null) {
                is = new FileInputStream(nodexConfPath);
            } else {
                String envType = System.getProperty("envType", System.getenv("envType"));
                if (envType != null && envType.trim().length() > 0) {
                    is = NodexContext.class.getClassLoader().getResourceAsStream(envType + "/nodex.conf");
                } else {
                    is = NodexContext.class.getClassLoader().getResourceAsStream("nodex.conf");
                }

                if (is == null) {
                    throw new NodexException("Load nodex config error , can not open classpath file : nodex.conf");
                }
            }
            properties = new Properties();
            properties.load(is);
            String zkConnStr = properties.getProperty(PN_NODEX_ZOOKEEPER_CONNSTR);
            zkh = new ZKHelper(zkConnStr, 60000);
            ZKProperties zkProperties = new ZKProperties(new String[] { "/nodex" });
            zkProperties.putAll(properties);
            properties = zkProperties;
        } catch (Exception e) {
            throw new NodexException("Load nodex config error", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.warn(e, e);
                }
            }
        }
    }

    public static Properties getNodexConf() {
        return properties;
    }

    public static ZKHelper getZKHelper() {
        return zkh;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getZKConnStr() {
        return properties.getProperty(PN_NODEX_ZOOKEEPER_CONNSTR);
    }

}
