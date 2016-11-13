package com.lenovo.czlib.nodex;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by chenzhao on 2015/3/30.
 */
public class NodexUtils {
    private static final Logger logger = Logger.getLogger(NodexUtils.class);

    public static String getMyIp(String zkConnStr) {
        if (zkConnStr != null) {
            String[] address = zkConnStr.split(",");
            for (String addr : address) {
                logger.debug("Try get my ip by connect to :" + addr.toString());
                Socket s = new Socket();
                try {
                    String[] ipAndPort = addr.split(":");
                    s.connect(new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1])), 1000);
                    return s.getLocalAddress().getHostAddress();
                } catch (IOException e) {
                    logger.debug("Faild get my ip by connect to :" + addr.toString(), e);
                } finally {
                    try {
                        s.close();
                    } catch (IOException e) {
                        logger.warn(e, e);
                    }
                }
            }
        }
        try {
            logger.info("Try get my ip by InetAddress.getLocalHost().getHostAddress()");
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
