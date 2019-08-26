package com.clt.ess.HTTP;

import com.clt.ess.base.Constant;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.clt.ess.utils.Base64Utils.ESSGetBase64Decode;
import static com.clt.ess.utils.CutImageUtil.markImageBySingleText;
import static com.clt.ess.utils.FileUtil.byte2File;
import static com.clt.ess.utils.FileUtil.getfileBytes;

public class httpTest {
    public static void main(String[] args) throws IOException {


//        PDDocument document = PDDocument.load(new File("D:\\temp\\demo.pdf"));
//        int pageNum = document.getNumberOfPages();
//
//        System.out.println(pageNum);
//        Map<String, Object> paramMap = new HashMap<>();
//
//        paramMap.put("fileName","demo.doc");
//        paramMap.put("systemId", "025bs1");
//        String sRet = HttpClient.doPost(Constant.createConvertLog_1,paramMap);
////        String HttpResult = HttpClient.doPost(url,paramMap);
//
//        System.out.println("HttpResult"+sRet);ESSGetBase64Decode(data)

        String data = "";
        byte[] imga = markImageBySingleText(getfileBytes("E:\\1.gif"), new Color(13,255,255),"——————",null);

        byte2File(imga,"E:\\2.gif");


    }
}
