package com.lenovo.czlib.nodex.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Created by chenzhao on 2015/4/28.
 */
@Service
public class SpringPlaceholderDemoBean {
    @Value("${test.str}")
    private String field1;
    @Value("${test.bool}")
    private boolean field2;
    @Value("${test.number}")
    private long field3;
    @Value("${mongodb.conn.str}")
    private String field4;
    @Value("${local.test.str}")
    private String field5;

    @Override
    public String toString() {
        return "SpringPlaceholderDemoBean{" +
                "field1='" + field1 + '\'' +
                ", field2=" + field2 +
                ", field3=" + field3 +
                ", field4='" + field4 + '\'' +
                ", field5='" + field5 + '\'' +
                '}';
    }

    public static void main(String[] args){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("placeholder.spring.test.xml");
        System.out.println(ctx.getBean(SpringPlaceholderDemoBean.class).toString());
    }
}
