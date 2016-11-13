package com.lenovo.czlib.nodex.demo;

import com.lenovo.czlib.nodex.NodexContext;
import com.lenovo.czlib.nodex.conf.ConfigChangeListener;
import com.lenovo.czlib.nodex.conf.NodexConfigurer;

/**
 * Created by chenzhao on 2015/4/28.
 */
public class SubscribeConfigDemo implements ConfigChangeListener {
    private String testStr;
    private boolean testBool;
    private long testNum;

    @Override
    public void configChanged(String key, Object targetObj, String targetFieldName) {
        System.out.println("config changed : " + targetFieldName);
        if("testStr".equals(targetFieldName)){
            System.out.println("new value is : " + testStr);
        }else if("testBool".equals(targetFieldName)){
            System.out.println("new value is : " + testBool);
        }else if("testNum".equals(targetFieldName)){
            System.out.println("new value is : " + testNum);
        }
    }


    private NodexConfigurer configurer = new NodexConfigurer();

    public SubscribeConfigDemo(){
        configurer.subscibe("/test/01/test.str",this,"testStr",this);
        configurer.subscibe("/test/01/test.bool",this,"testBool",this);
        configurer.subscibe("/test/01/test.number",this,"testNum",this);
    }

    public static void main(String[] args) throws InterruptedException {
        SubscribeConfigDemo demo = new SubscribeConfigDemo();
        Thread.sleep(99999L);
    }
}
