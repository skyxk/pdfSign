package com.clt.ess.base;



public class Constant {


    //写日志用到的产品类型，本产品为ESSPDFSIGN V1.0
    public static final String productType = "ESSPDFSIGN V1.0";
    //签章时用到的图片临时路径（可以通过datahandler加以改造，取消在磁盘上写临时文件，下面证书路径同理）
//    public static final String imgPath = "/usr/esstempfile/demo.gif";
//    public static final String imgPath = "D:\\temp\\demo.gif";

    //证书的临时路径
//    public static final String pfxPath = "/usr/esstempfile/demo.pfx";
//    public static final String pfxPath = "D:\\temp\\demo.pfx";  192.144.176.134:8081  10.40.4.7:8080


    //检查授权url
    public static final String checkJurUrl = "http://10.40.4.7:8080/SealCenter/seal/pdfSignAbtCheckJur";
//    //写签章日志接口
//    public static final String addSignLogInServerUrl = "http://10.40.4.7:8080/SealCenter/seal/pdfSignAbtAddSignLogInServer";
//    //查询日志的接口
//    public static final String essClientQuerySignLogBySerialNum="http://10.40.4.7:8080/SealCenter/seal/essClientQuerySignLogBySerialNum";
    //写签章日志接口
    public static final String addSignLogInServerUrl = "http://192.144.176.134:8081/SealCenter/seal/pdfSignAbtAddSignLogInServer";
    //查询日志的接口
    public static final String essClientQuerySignLogBySerialNum="http://192.144.176.134:8081/SealCenter/seal/essClientQuerySignLogBySerialNum";

    //校验是否符合授权条件url
    public static final String pdfSignAbtCheckJurLimitMsg = "http://10.40.4.7:8080/SealCenter/seal/pdfSignAbtCheckJurLimitMsg";
    //文件类型编码
    public static final String fileTypeNum = "32";
    //终端类型
    public static final String terminalType  = "服务器端";
    //创建文档转换
    public static final String createConvertLog_1 = "http://10.41.0.66:8081/wordToPdf/createConvertLog_1.html";
    //查询转换状态
    public static final String queryConvertState = "http://10.41.0.66:8081/wordToPdf/queryConvertState.html";

    public static final String errImg ="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";

}
