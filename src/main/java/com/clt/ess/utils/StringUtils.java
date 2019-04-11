package com.clt.ess.utils;

import com.multica.crypt.MuticaCryptException;

import java.util.regex.Pattern;

import static com.clt.ess.utils.uuidUtil.getUUID;
import static com.multica.crypt.MuticaCrypt.*;

public class StringUtils {

    /**推荐，速度最快
     * 判断是否为整数
     * @param str 传入的字符串
     * @return 是整数返回true,否则返回false
     */

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 获取一个对称加密的随机密码
     * @return  随机密码
     */
    public static String getEncryptPwd() {
        //得到一个uuid
        String uuid = getUUID();
        //取中间的8位
        String pwd = uuid.substring(8,16);
        byte[] b = new byte[0];
        try {
            //对密码进行对称加密
            b = ESSEncryptData(pwd.getBytes(),null,"esspwd".getBytes());
        } catch (MuticaCryptException e) {
            e.printStackTrace();
        }
        //将加密后的密码base64编码
        return ESSGetBase64Encode(b);
    }
    /**
     * 解密本系统生成的密码
     * @param pwd  密码
     * @return 密码明文
     */
    public static String getDecryptPwd(String pwd) throws MuticaCryptException {
        byte[]  pwdByte = ESSDecryptData(ESSGetBase64Decode(pwd),"esspwd".getBytes());
        return new String(pwdByte);
    }

    /**
     * 判断字符是否为空
     * @param s 待判断字符
     * @return 判断结果
     */
    public static boolean isNull(String s) {
        if(s == null){
            return false;
        }
        s = s.replace(" ","");
        if("".equals(s) ){
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws MuticaCryptException {


        int a  = GetAuthNumberFromProductInfo("ESSPdfSign");
        System.out.println(a);
    }

    /**
     * 根据授权内容转换层授权代码
     * @param sProducts 授权内容
     * @return 授权内容
     */
    public static int GetAuthNumberFromProductInfo(String sProducts){
        int iAuth = 0;
        String s[] = sProducts.split("@");
        for(int i=0;i<s.length;i++){
            if(s[i].indexOf("office") != -1 && s[i].indexOf("annotation") != -1 )
            {
                iAuth = iAuth | 1;
            }else if(s[i].indexOf("web") != -1 && s[i].indexOf("annotation") != -1 ){
                iAuth = iAuth | 2;
            }else if(s[i].indexOf("ESSWebSign") != -1){
                iAuth = iAuth | 4;
            }else if(s[i].indexOf("ESSWordSign") != -1){
                iAuth = iAuth | 8;
            }else if(s[i].indexOf("ESSExcelSign") != -1){
                iAuth = iAuth | 16;
            }else if(s[i].indexOf("ESSPdfSign") != -1){
                iAuth = iAuth | 32;
            }else if(s[i].indexOf("ESSMidWare") != -1){
                iAuth = iAuth | 64;
            }else{
            }
        }
        return iAuth;
    }

}
