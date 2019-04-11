package com.clt.ess.HTTP;

import com.clt.ess.base.Constant;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class httpTest {
    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("fileName","demo.doc");
        paramMap.put("systemId", "025bs1");
        String sRet = HttpClient.doPost(Constant.createConvertLog_1,paramMap);
//        String HttpResult = HttpClient.doPost(url,paramMap);

        System.out.println("HttpResult"+sRet);
    }
}
