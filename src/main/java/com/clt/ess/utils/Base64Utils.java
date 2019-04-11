package com.clt.ess.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.clt.ess.utils.FileUtil.byte2File;
import static com.clt.ess.utils.StringUtils.getDecryptPwd;
import static com.multica.crypt.MuticaCrypt.ESSGetBase64Decode;

/**
 * Created by clt_abc on 2017/5/31.
 */

public class Base64Utils {
    /**
     * 将传入数据BASE64编码
     * @param bMsg  ?  编码的数 ?
     * @return String
     */
    public static String ESSGetBase64Encode(byte[] bMsg) {
        BASE64Encoder ben = new BASE64Encoder();
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        String sBase64File = ben.encode(bMsg);
        Matcher m = p.matcher(sBase64File);
        sBase64File = m.replaceAll("");
        return sBase64File;

    }

    /**
     * 将传入数据BASE64解码
     * @param sEncMsg  ?  解码码的数据
     * @return byte[]
     */
    public static byte[] ESSGetBase64Decode(String sEncMsg) {
        byte[] date= null;
        try
        {
            BASE64Decoder bdr = new BASE64Decoder();
            date = bdr.decodeBuffer(sEncMsg);
            return date;
        }catch(IOException e)
        {
//            throw(new MuticaCryptException(e.getMessage()));
        }
        return date;
    }
    /**
     * <p>将base64字符解码保存文件</p>
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void decoderBase64File(String base64Code,String targetPath) throws Exception {
        byte[] buffer = new BASE64Decoder().decodeBuffer(base64Code);
//        FileOutputStream out = new FileOutputStream(targetPath);
//        out.write(buffer);
//        out.close();
        byte2File(buffer,targetPath);



    }

}
