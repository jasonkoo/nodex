package com.lenovo.czlib.nodex;

import com.lenovo.czlib.nodex.fc.FCItem;
import com.lenovo.czlib.nodex.fc.FlowController;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenzhao on 2015/5/20.
 */
public class FCTest {
    private FlowController fc = FlowController.getDefault();

    private String key = "test.key.1";

    long cnt = 0;

    @Test
    public void test() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
        fc.save(new FCItem(key ,10000,1200000,500));


        boolean print = false;
        while(true){
            if(fc.isAllow(key,1)){
                print= true;
                cnt++;
                Thread.sleep(5L);
            }else{
                if(print){
                    System.out.println(cnt);
                    cnt = 0;
                    print= false;
                }
            }
        }
    }
}
