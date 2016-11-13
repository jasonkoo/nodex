package com.lenovo.czlib.nodex.demo;

import com.lenovo.czlib.nodex.conf.ZKProperties;

/**
 * Created by chenzhao on 2015/4/28.
 * 这个Demo演示如何使用ZKProperties。ZKProperties从一组指定的Nodex配置路径中加载配置，
 * 并封装成java.util.Properties的子类。
 */
public class ZKPropertiesDemo {
    public static void main(String[] args){
        ZKProperties properties
                = new ZKProperties(new String[]{"/msg/broker","/msg/eventbus"},false);

        //启动配置懒加载:
        //ZKProperties properties = new ZKProperties(new String[]{"/msg/broker","/msg/eventbus"});

        for(String key:properties.stringPropertyNames()){
            System.out.println(key + "=" + properties.getProperty(key));
        }

        System.out.println("[LAZY] metadata.broker.list=" + properties.getProperty("metadata.broker.list"));
    }
}
