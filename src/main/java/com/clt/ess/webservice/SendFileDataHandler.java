package com.clt.ess.webservice;

import com.clt.ess.HTTP.HttpClient;
import com.clt.ess.base.Constant;
import com.clt.ess.dao.*;
import com.clt.ess.entity.*;
import com.clt.ess.utils.Location;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.security.cert.CertificateException;
import javax.xml.bind.annotation.XmlMimeType;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.clt.ess.utils.Base64Utils.*;
import static com.clt.ess.utils.CutImageUtil.markImageBySingleText;
import static com.clt.ess.utils.GetLocation.getLastKeyWord;
import static com.clt.ess.utils.GetLocation.locationByBookMark;
import static com.clt.ess.utils.Sign.*;
import static com.clt.ess.utils.SocketUtils.wordToPdfClient;
import static com.clt.ess.utils.uuidUtil.getUUID;

@WebService(name="pdfSignWS",serviceName="pdfSignService",targetNamespace="http://ESSPDFSIGN/client")
public class SendFileDataHandler {

    @Autowired
    private ISealDao sealDao;
    @Autowired
    private IBusinessSysDao businessSysDao;
    @Autowired
    private IUnitDao unitDao;
    @Autowired
    private IPersonDao personDao;
    @Autowired
    private IUserDao userDao;

    /**
     * 网页签章接口（暂时只支持手写签名签章）
     * @param sSysID 业务系统id
     * @param sSysPersonID 手签人标识，可以是江苏省统一人员id，也可以是系统内人员的身份证号。
     * @param sPlain 需要签名的明文信息
     * @param sAuthInfo 未使用参数 （待定义）
     * @param sPlainEncodeType 签名编码格式
     * @return JSON字符串
     */
    @WebMethod
    public String WebServerHandWritingSign(@WebParam(name = "sSysID") String sSysID,
                                       @WebParam(name = "sSysPersonID") String sSysPersonID,
                                       @WebParam(name = "sPlain") String sPlain,
                                       @WebParam(name = "sAuthInfo") String sAuthInfo,
                                       @WebParam(name = "sPlainEncodeType") String sPlainEncodeType){

        //新建json对象，作为返回信息
        JSONObject jsonObject = new JSONObject();
        //签章序列号 每次签章的序号，取uuid;保证唯一性
        String signSerialNum = getUUID();
        // 初始化身份证号，查询印章需要。
        String idNum =null;
        //查找对应的签章
        if(sSysPersonID.length()==18){
            //如果传入的参数为身份证号，则直接根据传入数据查找印章
            idNum = sSysPersonID;
        }else{
            //如果传入的参数为统一人员ID，默认只传入两种数据，身份证号和统一人员id,
            //查找person对象
            Person person = personDao.findPersonByProvincialUserId(sSysPersonID);
            //如果person对象为空，返回错误信息
            if (person ==null){
                //返回结果类型
                jsonObject.put("resultType","false");
                //错误信息类型
                jsonObject.put("errorCode","0003");
                return jsonObject.toString();
            }
            idNum = person.getIdNum();
        }
        //根据身份证号查询印章数据
        Seal seal = sealDao.findSealByIdNum(idNum);
        if (seal==null){
            //如果印章数据为空
            jsonObject.put("resultType","false");
            jsonObject.put("errorCode","0001");
            return jsonObject.toString();
        }
        //获取图像字符节
        byte[] bGif = ESSGetBase64Decode(seal.getSealImg().getSealImgGifBase64());
        //获取证书字符节
        byte[] bPfx = ESSGetBase64Decode(seal.getCertificate().getPfxBase64());
        //获取证书密码
        String pfxPwd = seal.getCertificate().getCerPsw();
        //签名方法
        WebSign ws = new WebSign();
        try {
            if(ws.SignData(sPlain, sPlainEncodeType,bPfx, pfxPwd,bGif)) {
                jsonObject.put("resultType","true");
                jsonObject.put("EncodeData",ws.EncodedData);
                jsonObject.put("ImageData",ws.ImageData);
                jsonObject.put("ImageID",ws.ImageID);
                jsonObject.put("SignSerialNum",signSerialNum);
            }else{
                jsonObject.put("resultType","false");
                jsonObject.put("errorCode",ws.ErrorCode);
            }
        } catch (IOException | GeneralSecurityException | CertificateException e) {
            jsonObject.put("resultType","false");
            jsonObject.put("errorCode","0002");
            return jsonObject.toString();
        }

        if (!addSignLog(sSysID,sSysID,"","","","demo",signSerialNum,
                seal.getSealId(),seal.getUnitId())){
            jsonObject.put("resultType","false");
            jsonObject.put("errorCode","0004");
            return jsonObject.toString();
        }
        return jsonObject.toString();
    }

    @WebMethod
    public String WebServerHandWritingVerify(@WebParam(name = "encodeData") String encodeData,
                                               @WebParam(name = "imageData") String imageData,
                                               @WebParam(name = "plainText") String plainText,
                                               @WebParam(name = "encodeType") String encodeType,
                                               @WebParam(name = "imgType") int imgType,
                                               @WebParam(name = "SignSerialNum") String SignSerialNum,
                                             @WebParam(name = "sAuthInfo") String sAuthInfo,
                                             @WebParam(name = "wantEncodeType") String wantEncodeType){
        JSONObject result = new JSONObject();
        String sealName ="";
        String gifBase64 ="";
        String sealTime ="";
        String bsUnitName ="";
        String url ="";

        WebSign ws = new WebSign();
        if (!"".equals(encodeData)&&!"".equals(imageData)&&!"".equals(plainText)&&"".equals(SignSerialNum)){
            if(ws.VerifyData(plainText, encodeType,encodeData, imageData)) {
                result.put("resultType","true");
                result.put("gifBase64",ws.PictureData);
                result.put("sealTime",ws.SealTime);
            }else{
                try {
                    result.put("resultType","false");
                    result.put("errorCode",ws.ErrorCode);
                    if("".equals(ws.PictureData)){
                        result.put("gifBase64",Constant.errImg);
                    }else{
                        result.put("gifBase64",addMarkErrorText(ws.PictureData));
                    }
                } catch (IOException e) {
                    result.put("resultType","false");
                    result.put("errorCode","10004");
                    result.put("gifBase64",Constant.errImg);
                }
            }
        }
        if ("".equals(encodeData)&&"".equals(imageData)&&"".equals(plainText)&&!"".equals(SignSerialNum)){
            url =Constant.essClientQuerySignLogBySerialNum+"?serialNum="+SignSerialNum;
            //根据签章序列号获取签章日志
            String HttpResult = HttpClient.doGet(url);
            JSONObject jsonObject_1 = new JSONObject(HttpResult);
            sealTime = jsonObject_1.getString("signTime");
            String sealId = jsonObject_1.getString("sealId");
            String businessSysId = jsonObject_1.getString("businessSysId");
            Seal seal = sealDao.findSealById(sealId);

            String idNum = seal.getSealHwIdNum();
            Person person = personDao.findPersonByIdNum(idNum);

            String signUserName = person.getPersonName();
            sealName = seal.getSealName();
            gifBase64 = seal.getSealImg().getSealImgGifBase64();

            bsUnitName = unitDao.findBSUnitNameById(businessSysId);
            result.put("resultType","true");
            result.put("sealName",sealName);
            result.put("sealTime",sealTime);
            result.put("signUserName",signUserName);
            result.put("bsSystemName",bsUnitName);
        }
        if(!"".equals(encodeData)&&!"".equals(imageData)&&!"".equals(plainText)&&!"".equals(SignSerialNum)){
            url =Constant.essClientQuerySignLogBySerialNum+"?serialNum="+SignSerialNum;
            //根据签章序列号获取签章日志
            String HttpResult = HttpClient.doGet(url);
            JSONObject jsonObject_1 = new JSONObject(HttpResult);
            sealTime = jsonObject_1.getString("signTime");
            String sealId = jsonObject_1.getString("sealId");
            String businessSysId = jsonObject_1.getString("businessSysId");
            Seal seal = sealDao.findSealById(sealId);

            String idNum = seal.getSealHwIdNum();
            Person person = personDao.findPersonByIdNum(idNum);

            String signUserName = person.getPersonName();
            sealName = seal.getSealName();
            gifBase64 = seal.getSealImg().getSealImgGifBase64();

            bsUnitName = unitDao.findBSUnitNameById(businessSysId);
            result.put("resultType","true");
            result.put("sealName",sealName);
            result.put("sealTime",sealTime);
            result.put("signUserName",signUserName);
            result.put("bsSystemName",bsUnitName);
            if(ws.VerifyData(plainText, encodeType,encodeData, imageData)) {
                result.put("gifBase64",gifBase64);
            }else{
                try {
                    result.put("resultType","false");
                    result.put("errorCode",ws.ErrorCode);
                    result.put("gifBase64",addMarkErrorText(gifBase64));
                } catch (IOException e) {
                    result.put("resultType","false");
                    result.put("errorCode","10004");
                    result.put("gifBase64",Constant.errImg);
                }
            }
        }
        if (imgType ==0){
            return result.toString();
        }else if (imgType == 1){
            return ws.GetImgHtml(SignSerialNum,result.getString("gifBase64"),sealName,sealTime,bsUnitName);
        }else{
            return result.toString();
        }
    }
    public String addMarkErrorText(String data) throws IOException {
        byte[] img = markImageBySingleText(ESSGetBase64Decode(data), Color.red,"——————————————",null);
        return ESSGetBase64Encode(img);
    }

    @WebMethod
    public String WebServerHandWritingVerifys(@WebParam(name = "data") String data){

        //获取数据数组
        JSONArray jsonArray = new JSONArray("data");
        //创建返回json数组
        JSONArray resultArray = new JSONArray();
        //遍历数组，执行验证
        for (int i= 0;i<jsonArray.length();i++){
            //单个签章验证
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String encodeData = jsonObject.getString("encodeData");
            String imageData = jsonObject.getString("imageData");
            String plainText = jsonObject.getString("plainText");
            String encodeType = jsonObject.getString("encodeType");
            int imgType = jsonObject.getInt("imgType");
            String SignSerialNum = jsonObject.getString("SignSerialNum");
            String sAuthInfo = jsonObject.getString("sAuthInfo");
            String wantEncodeType = jsonObject.getString("wantEncodeType");
            //验证代码
            JSONObject result = new JSONObject();
            String sealName ="";
            String gifBase64 ="";
            String sealTime ="";
            String bsUnitName ="";
            String url ="";

            WebSign ws = new WebSign();
            if (!"".equals(encodeData)&&!"".equals(imageData)&&!"".equals(plainText)&&"".equals(SignSerialNum)){
                if(ws.VerifyData(plainText, encodeType,encodeData, imageData)) {
                    result.put("resultType","true");
                    result.put("gifBase64",ws.PictureData);
                    result.put("sealTime",ws.SealTime);
                }else{
                    try {
                        result.put("resultType","false");
                        result.put("errorCode",ws.ErrorCode);
                        if("".equals(ws.PictureData)){
                            result.put("gifBase64",Constant.errImg);
                        }else{
                            result.put("gifBase64",addMarkErrorText(ws.PictureData));
                        }
                    } catch (IOException e) {
                        result.put("resultType","false");
                        result.put("errorCode","10004");
                        result.put("gifBase64",Constant.errImg);
                    }
                }
            }
            if ("".equals(encodeData)&&"".equals(imageData)&&"".equals(plainText)&&!"".equals(SignSerialNum)){
                url =Constant.essClientQuerySignLogBySerialNum+"?serialNum="+SignSerialNum;
                //根据签章序列号获取签章日志
                String HttpResult = HttpClient.doGet(url);
                JSONObject jsonObject_1 = new JSONObject(HttpResult);
                sealTime = jsonObject_1.getString("signTime");
                String sealId = jsonObject_1.getString("sealId");
                String businessSysId = jsonObject_1.getString("businessSysId");
                Seal seal = sealDao.findSealById(sealId);

                String idNum = seal.getSealHwIdNum();
                Person person = personDao.findPersonByIdNum(idNum);

                String signUserName = person.getPersonName();
                sealName = seal.getSealName();
                gifBase64 = seal.getSealImg().getSealImgGifBase64();

                bsUnitName = unitDao.findBSUnitNameById(businessSysId);
                result.put("resultType","true");
                result.put("sealName",sealName);
                result.put("sealTime",sealTime);
                result.put("signUserName",signUserName);
                result.put("bsSystemName",bsUnitName);
            }
            if(!"".equals(encodeData)&&!"".equals(imageData)&&!"".equals(plainText)&&!"".equals(SignSerialNum)){
                url =Constant.essClientQuerySignLogBySerialNum+"?serialNum="+SignSerialNum;
                //根据签章序列号获取签章日志
                String HttpResult = HttpClient.doGet(url);
                JSONObject jsonObject_1 = new JSONObject(HttpResult);
                sealTime = jsonObject_1.getString("signTime");
                String sealId = jsonObject_1.getString("sealId");
                String businessSysId = jsonObject_1.getString("businessSysId");
                Seal seal = sealDao.findSealById(sealId);

                String idNum = seal.getSealHwIdNum();
                Person person = personDao.findPersonByIdNum(idNum);

                String signUserName = person.getPersonName();
                sealName = seal.getSealName();
                gifBase64 = seal.getSealImg().getSealImgGifBase64();

                bsUnitName = unitDao.findBSUnitNameById(businessSysId);
                result.put("resultType","true");
                result.put("sealName",sealName);
                result.put("sealTime",sealTime);
                result.put("signUserName",signUserName);
                result.put("bsSystemName",bsUnitName);
                if(ws.VerifyData(plainText, encodeType,encodeData, imageData)) {
                    result.put("gifBase64",gifBase64);
                }else{
                    try {
                        result.put("resultType","false");
                        result.put("errorCode",ws.ErrorCode);
                        result.put("gifBase64",addMarkErrorText(gifBase64));
                    } catch (IOException e) {
                        result.put("resultType","false");
                        result.put("errorCode","10004");
                        result.put("gifBase64",Constant.errImg);
                    }
                }
            }
            //验证结果添加到数组
            resultArray.put(result);
        }
        return resultArray.toString();
    }

    @WebMethod
    public DataResult PDFSignature(@WebParam(name = "dataHandler") @XmlMimeType(value = "application/octet-stream")
                                                  DataHandler dataHandler, @WebParam(name = "jsonString") String jsonString) {
        DataResult dataResult = new DataResult();
        dataResult.setResultType(true);
        File source = null;
        String uuid = getUUID();
        String fileName = uuid+".pdf";
        JSONObject jsonSealInfo = new JSONObject(jsonString);

        if(jsonSealInfo.getString("businessSysId") !=null &&jsonSealInfo.getString("docType") !=null
                &&jsonSealInfo.getString("fileType") !=null){
            String businessSysId = jsonSealInfo.getString("businessSysId");
            String docType = jsonSealInfo.getString("docType");
            String fileType = jsonSealInfo.getString("fileType");
            BusinessSys businessSys = businessSysDao.findBusinessSysById(businessSysId);
            if (businessSys!=null){
                username = businessSys.getFtpAccount();
                password = businessSys.getFtpPsw();
                File saveDir = new File(businessSys.getFtpPath());
                if(!saveDir.exists()){
                    saveDir.mkdir();
                }
                if("doc".equals(fileType)){
                    fileName = uuid+".doc";
                    try {
                        saveFile(dataHandler,businessSys.getFtpPath()+fileName);
                    } catch (IOException e) {
                        return null;
                    }
                    //文件路径
                    boolean wordToPdfResult = wordToPdfClient(businessSys.getFtpPath()+fileName);
                    if (wordToPdfResult){
                        fileName = uuid+".pdf";
                    }else{
                        dataResult.setResultType(false);
                        dataResult.setResultMessage("文档转换异常");
                        return dataResult;
                    }
                }else{
                    try {
                        saveFile(dataHandler,businessSys.getFtpPath()+fileName);
                    } catch (IOException e) {
                        dataResult.setResultType(false);
                        dataResult.setResultMessage("保存文件失败");
                        return dataResult;
                    }
                }
                try {
                    JSONArray s = jsonSealInfo.getJSONArray("sealInfos");
                    List<SealInfo> sealInfoList = jsonToSealInfoList(s);
                    for(SealInfo sealInfo : sealInfoList){
                        //执行签章步骤
                        int result1 = doSealInfo(sealInfo,businessSysId,docType,businessSys.getFtpPath()+fileName);
                        switch (result1){
                            case 1:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("查找印章错误");
                                break;
                            case 2:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("保存图片错误");
                                break;
                            case 3:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("书签定位错误");
                                break;
                            case 4:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("关键字定位错误");
                                break;
                            case 5:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("未知错误");
                                break;
                        }
                        if(result1!=0){
                            break;
                        }
                    }
                    source = new File(businessSys.getFtpPath()+fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    dataResult.setResultType(false);
                    dataResult.setResultMessage("签章过程异常");
                }
            }else{
                dataResult.setResultType(false);
                dataResult.setResultMessage("业务系统查询失败");
            }

        }else {
            dataResult.setResultType(false);
            dataResult.setResultMessage("签章信息确实");
        }

        if(dataResult.getResultType()){
            dataResult.setResultMessage("签章成功");
            DataSource dataSource = new FileDataSource(source);
            dataResult.setDataHandler(new DataHandler(dataSource));
        }
        return dataResult;
    }


    @WebMethod
    public DataResult PDFSignatureOne(@WebParam(name = "dataHandler") @XmlMimeType(value = "application/octet-stream")
                                           DataHandler dataHandler, @WebParam(name = "jsonString") String jsonString) {
        DataResult dataResult = new DataResult();
        dataResult.setResultType(true);
        File source = null;
        String uuid = getUUID();
        String fileName = uuid+".pdf";
        JSONObject jsonSealInfo = new JSONObject(jsonString);

        if(jsonSealInfo.getString("businessSysId") !=null &&jsonSealInfo.getString("docType") !=null
                &&jsonSealInfo.getString("fileType") !=null){
            String businessSysId = jsonSealInfo.getString("businessSysId");
            String docType = jsonSealInfo.getString("docType");
            String fileType = jsonSealInfo.getString("fileType");
            BusinessSys businessSys = businessSysDao.findBusinessSysById(businessSysId);
            if (businessSys!=null){
                username = businessSys.getFtpAccount();
                password = businessSys.getFtpPsw();
                File saveDir = new File(businessSys.getFtpPath());
                if(!saveDir.exists()){
                    saveDir.mkdir();
                }
                if("doc".equals(fileType)){
                    fileName = uuid+".doc";
                    try {
                        saveFile(dataHandler,businessSys.getFtpPath()+fileName);
                    } catch (IOException e) {
                        return null;
                    }
                    //文件路径
                    boolean wordToPdfResult = wordToPdfClient(businessSys.getFtpPath()+fileName);
                    if (wordToPdfResult){
                        fileName = uuid+".pdf";
                    }else{
                        dataResult.setResultType(false);
                        dataResult.setResultMessage("文档转换异常");
                        return dataResult;
                    }
                }else{
                    try {
                        saveFile(dataHandler,businessSys.getFtpPath()+fileName);
                    } catch (IOException e) {
                        dataResult.setResultType(false);
                        dataResult.setResultMessage("保存文件失败");
                        return dataResult;
                    }
                }
                try {
                    JSONArray s = jsonSealInfo.getJSONArray("sealInfos");
                    List<SealInfo> sealInfoList = jsonToSealInfoList(s);
                    for(SealInfo sealInfo : sealInfoList){
                        //执行签章步骤
                        int result1 = doSealInfo_1(sealInfo,businessSysId,docType,businessSys.getFtpPath()+fileName);
                        switch (result1){
                            case 1:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("查找印章错误");
                                break;
                            case 2:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("保存图片错误");
                                break;
                            case 3:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("书签定位错误");
                                break;
                            case 4:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("关键字定位错误");
                                break;
                            case 5:
                                dataResult.setResultType(false);
                                dataResult.setResultMessage("未知错误");
                                break;
                        }
                        if(result1!=0){
                            break;
                        }
                    }
                    source = new File(businessSys.getFtpPath()+fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    dataResult.setResultType(false);
                    dataResult.setResultMessage("签章过程异常");
                }
            }else{
                dataResult.setResultType(false);
                dataResult.setResultMessage("业务系统查询失败");
            }

        }else {
            dataResult.setResultType(false);
            dataResult.setResultMessage("签章信息确实");
        }

        if(dataResult.getResultType()){
            dataResult.setResultMessage("签章成功");
            DataSource dataSource = new FileDataSource(source);
            dataResult.setDataHandler(new DataHandler(dataSource));
        }
        return dataResult;
    }


    private String notifyConvert(String fileName, String businessSysId) {
        Map<String,Object> paras = new HashMap<String, Object>();
        paras.put("fileName",fileName);
        paras.put("systemId", businessSysId);
        String sRet = HttpClient.doPost(Constant.createConvertLog_1,paras);
        String[] stmp = sRet.split("@");
        if(stmp.length != 3)
        {
            return null;
        }
        if(!stmp[0].equals("ESSB") || !stmp[2].equals("ESSE")){
            return null;
        }
        if(stmp[1].equals("ERROR")){
            return null;
        }

        String serialNo = stmp[1];
        int iTry = 0;
        String sPdfFile = "";
        long t1=System.currentTimeMillis();
        do{
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sRet = HttpClient.doGet(Constant.queryConvertState+"?fid=" + serialNo);
            if(sRet.substring(0, 6).equals("ESSB@3")){
                String[] sTmp2 = sRet.split("@");
                sPdfFile = sTmp2[2];
                break;
            }
            else{
                iTry++;
            }
            if(iTry > 20)
                break;
        }while(true);
        if(sPdfFile.equals("")){
            return null;
        }
        return sPdfFile;

    }

    /**
     * 保存文件
     * @param dataHandler 文件数据
     * @param filePath 文件地址
     * @return 结果
     * @throws IOException io异常
     */
    private boolean saveFile(DataHandler dataHandler,String filePath) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            dataHandler.writeTo(outputStream);
        }finally {
            IOUtils.closeQuietly(outputStream);
        }

        return true;
    }


    /**
     * 根据签章信息处理，并进行签章动作
     * @param sealInfo 签章信息
     * @return  错误类型
     * @throws Exception 异常
     */
    private int doSealInfo(SealInfo sealInfo,String businessSysId,String docType,String fileName ) throws
            Exception {
        String signSerialNum = getUUID();
        int result = 0;
        //授权id
        String sjId ="";
        //转授id
        String tjId = "";
        //全省统一人员id
        String provincialUserId  ="";

        if(sealInfo.getsStaffID()==null){
            provincialUserId = businessSysId;
        }else{
            provincialUserId = sealInfo.getsStaffID();
        }
        //初始化印章
        Seal seal =new Seal();
        //判断是否手签
        if(sealInfo.getsSealType().contains("st7")){
            //如果是手签，根据全省统一人员id查找印章
            Person person = personDao.findPersonByProvincialUserId(sealInfo.getsStaffID());
            seal = sealDao.findSealByIdNum(person.getIdNum());
        }else{
            if(sealInfo.getsSealID()==null||"".equals(sealInfo.getsSealID())){
                //公章，并且没有印章id,根据单位id和印章类型查找
                List<Unit> unitList = unitDao.findUnitByOrgId(sealInfo.getsOrgID(),businessSysId);
                for (Unit unit :unitList){
                    seal = sealDao.findSealByUnitAndType(sealInfo.getsSealType(),unit.getUnitId());
                    if(seal.getSealId() == null||"".equals(seal.getSealId())){
                        break;
                    }
                }
            }else{
                seal = sealDao.findSealById(sealInfo.getsSealID());
            }
        }
        //判断印章是否为空
        if(seal.getSealId() == null||"".equals(seal.getSealId())){
            //查找印章错误
            return 1;
        }
        //保存图片
        byte[] sealImg;
        if (seal.getSealImg() != null) {
            sealImg = ESSGetBase64Decode(seal.getSealImg().getSealImgGifBase64());
            decoderBase64File(seal.getSealImg().getSealImgGifBase64(), Constant.imgPath);
        }else{
            //保存图片错误
            return 2;
        }
        //保存证书
        decoderBase64File(seal.getCertificate().getPfxBase64(), Constant.pfxPath);
        //获取证书密码
        String pwd = seal.getCertificate().getCerPsw();

        //确认定位方式
        if(sealInfo.getLocationMode()==-1){
            //坐标定位
        }else if(sealInfo.getLocationMode()==0){
            //书签定位
            Location location = locationByBookMark(fileName,sealInfo.getsKeyWords());
            if(location==null){
                //书签定位错误
               return 3;
            }
//            int x = sealInfo.getiOffsetX();
//            int y = sealInfo.getiOffsetY();
            sealInfo.setiOffsetX((int) location.getX());
            sealInfo.setiOffsetY((int) location.getY());
            sealInfo.setPageNum(location.getPageNum());
        }else{
            //关键字定位
            int x = sealInfo.getiOffsetX();
            int y = sealInfo.getiOffsetY();
            //
            List<Location> list = getLastKeyWord(fileName,sealInfo.getsKeyWords().toLowerCase());
            Location location = null;
            if (list != null && list.size() >= sealInfo.getLocationMode() ) {
                //确定
                location =list.get(sealInfo.getLocationMode() - 1);
            }
            if(location!=null){
                //根据偏移量修正坐标
                sealInfo.setiOffsetX((int) location.getX()+x);
                sealInfo.setiOffsetY((int) location.getY()+y);
                sealInfo.setPageNum(location.getPageNum());
            }else{
                //关键字定位错误
                return 4;
            }
        }
        //判断是否骑缝章
        if(sealInfo.isBlPagingSeal()){
            //骑缝章
            addOverSeal(fileName,Constant.imgPath,Constant.pfxPath,pwd,signSerialNum,sealInfo.getiSealImgW(),sealInfo.getiSealImgH());
        }else{
            //单个签章
            addSeal(fileName,sealImg,sealInfo.getiSealImgW(),sealInfo.getiSealImgH(),sealInfo.getPageNum(),
                    sealInfo.getiOffsetX(), sealInfo.getiOffsetY(),Constant.pfxPath,pwd,signSerialNum);
        }

//        addSignLog(provincialUserId,businessSysId,tjId,sjId,fileName,docType,signSerialNum,
//                seal.getSealId(),seal.getUnitId());
        return result;
    }


    /**
     * 根据签章信息处理，并进行签章动作
     * @param sealInfo 签章信息
     * @return  错误类型
     * @throws Exception 异常
     */
    private int doSealInfo_1(SealInfo sealInfo,String businessSysId,String docType,String fileName ) throws
            Exception {
        String signSerialNum = getUUID();
        int result = 0;
        //授权id
        String sjId ="";
        //转授id
        String tjId = "";
        //全省统一人员id
        String provincialUserId  ="";

        if(sealInfo.getsStaffID()==null){
            provincialUserId = businessSysId;
        }else{
            provincialUserId = sealInfo.getsStaffID();
        }
        //初始化印章
        Seal seal =new Seal();
        //判断是否手签
        if(sealInfo.getsSealType().contains("st7")){
            //如果是手签，根据全省统一人员id查找印章
            Person person = personDao.findPersonByProvincialUserId(sealInfo.getsStaffID());
            seal = sealDao.findSealByIdNum(person.getIdNum());
        }else{
            if(sealInfo.getsSealID()==null||"".equals(sealInfo.getsSealID())){
                //公章，并且没有印章id,根据单位id和印章类型查找
                List<Unit> unitList = unitDao.findUnitByOrgId(sealInfo.getsOrgID(),businessSysId);
                for (Unit unit :unitList){
                    seal = sealDao.findSealByUnitAndType(sealInfo.getsSealType(),unit.getUnitId());
                    if(seal.getSealId() == null||"".equals(seal.getSealId())){
                        break;
                    }
                }
            }else{
                seal = sealDao.findSealById(sealInfo.getsSealID());
            }
        }
        //判断印章是否为空
        if(seal.getSealId() == null||"".equals(seal.getSealId())){
            //查找印章错误
            return 1;
        }
        //保存图片
        byte[] sealImg;
        if (seal.getSealImg() != null) {
            sealImg = ESSGetBase64Decode(seal.getSealImg().getSealImgGifBase64());
            decoderBase64File(seal.getSealImg().getSealImgGifBase64(), Constant.imgPath);
        }else{
            //保存图片错误
            return 2;
        }
        //保存证书
        decoderBase64File(seal.getCertificate().getPfxBase64(), Constant.pfxPath);
        //获取证书密码
        String pwd = seal.getCertificate().getCerPsw();

        //确认定位方式
        if(sealInfo.getLocationMode()==-1){
            //坐标定位
        }else if(sealInfo.getLocationMode()==0){
            //书签定位
            Location location = locationByBookMark(fileName,sealInfo.getsKeyWords());
            if(location==null){
                //书签定位错误
                return 3;
            }
//            int x = sealInfo.getiOffsetX();
//            int y = sealInfo.getiOffsetY();
            sealInfo.setiOffsetX((int) location.getX());
            sealInfo.setiOffsetY((int) location.getY());
            sealInfo.setPageNum(location.getPageNum());
        }else{
            //关键字定位
            int x = sealInfo.getiOffsetX();
            int y = sealInfo.getiOffsetY();
            //
            List<Location> list = getLastKeyWord(fileName,sealInfo.getsKeyWords().toLowerCase());
            Location location = null;
            if (list != null && list.size() >= sealInfo.getLocationMode() ) {
                //确定
                location =list.get(sealInfo.getLocationMode() - 1);
            }
            if(location!=null){
                //根据偏移量修正坐标
                sealInfo.setiOffsetX((int) location.getX()+x);
                sealInfo.setiOffsetY((int) location.getY()+y);
                sealInfo.setPageNum(location.getPageNum());
            }else{
                //关键字定位错误
                return 4;
            }
        }
        //判断是否骑缝章
        if(sealInfo.isBlPagingSeal()){
            //单个签章
            addOverSealPage(fileName,sealImg,sealInfo.getiSealImgW(),sealInfo.getiSealImgH(),
                    sealInfo.getiOffsetX(), sealInfo.getiOffsetY(),Constant.pfxPath,pwd,signSerialNum);
        }else{
            //单个签章
            addSeal(fileName,sealImg,sealInfo.getiSealImgW(),sealInfo.getiSealImgH(),sealInfo.getPageNum(),
                    sealInfo.getiOffsetX(), sealInfo.getiOffsetY(),Constant.pfxPath,pwd,signSerialNum);
        }

        return result;
    }


    private boolean addSignLog(String provincialUserId,String businessSysId,String tjId,String sjId,String docId,
                          String docType,String signSerialNum,String sealId,String unitId){
        //写日志的链接
        String url = Constant.addSignLogInServerUrl ;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("provincialUserId",provincialUserId);
        paramMap.put("businessSysId",businessSysId);
        paramMap.put("tjId",tjId);
        paramMap.put("sjId",sjId);
        paramMap.put("terminalType",Constant.terminalType);
        paramMap.put("docId","demo");
        paramMap.put("productType",Constant.productType);
        paramMap.put("docType",docType);
        paramMap.put("docContent","");
        paramMap.put("fileTypeNum","127");
        paramMap.put("mac","");
        paramMap.put("deviceId","");
        paramMap.put("signSerialNum",signSerialNum);
        paramMap.put("unitId",unitId);
        paramMap.put("sealId",sealId);
        String HttpResult = HttpClient.doPost(url,paramMap);
        //返回数据格式{"EssResultPre":"ESSRESULT","verson":"1.0.0",'msg':'1'}
        JSONObject jsonObject = new JSONObject(HttpResult);
        if(jsonObject.getInt("msg")!=1){
            //写入日志失败
            return false;
        }
        return true;
    }

    /**
     * 将JSON数据转换sealInfoList
     * @param jsonArray json数组
     * @return  签章信息list
     */
    private List<SealInfo> jsonToSealInfoList(JSONArray jsonArray){

        List<SealInfo> sealInfos = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            SealInfo sealInfo = new SealInfo();

            sealInfo.setsKeyWords(jsonObject1.getString("sKeyWords"));
            sealInfo.setLocationMode(jsonObject1.getInt("locationMode"));
            sealInfo.setiOffsetX(jsonObject1.getInt("iOffsetX"));
            sealInfo.setiOffsetY(jsonObject1.getInt("iOffsetY"));
            sealInfo.setPageNum(jsonObject1.getInt("pageNum"));
            sealInfo.setiSealImgW(jsonObject1.getInt("iSealImgW"));
            sealInfo.setiSealImgH(jsonObject1.getInt("iSealImgH"));
            sealInfo.setsOrgID(jsonObject1.getString("sOrgID"));
            sealInfo.setsStaffID(jsonObject1.getString("sStaffID"));
            sealInfo.setsSealType(jsonObject1.getString("sSealType"));
            sealInfo.setsSealID(jsonObject1.getString("sSealID"));
            sealInfo.setBlPagingSeal(jsonObject1.getBoolean("blPagingSeal"));
            sealInfos.add(sealInfo);
        }
        return  sealInfos;
    }


    /**
     * 以下为ftp处理上传下载的代码，放在类外会出现异常，暂时放在此处，待解决问题
     * 参数除了地址接口不可变外，用户名密码会根据上方方法参数改变
     */
    //ftp地址
    public static  String hostname = Constant.ftpIp;
    //ftp端口
    public static Integer port = Constant.ftpPort;
    //ftp账号
    public static String username = Constant.ftpUser;
    //ftp密码
    public static String password = Constant.ftpPwd;

    public static FTPClient ftpClient = null;
    private static void initFtpClient() {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding("utf-8");
        try {
            ftpClient.connect(hostname, port);
            ftpClient.login(username, password);
            int replyCode = ftpClient.getReplyCode();
            if(!FTPReply.isPositiveCompletion(replyCode)){
            }
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static boolean uploadFile( String pathname, String fileName,String originfilename){
        boolean flag = false;
        InputStream inputStream = null;
        try{
            inputStream = new FileInputStream(new File(originfilename));
            initFtpClient();
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            CreateDirecroty(pathname);
            ftpClient.makeDirectory(pathname);
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.logout();
            flag = true;
        }catch (Exception e) {
            e.printStackTrace();
            return flag;
        }finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private static boolean uploadFile( String pathname, String fileName,InputStream inputStream){
        boolean flag = false;
        try{
            initFtpClient();
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            CreateDirecroty(pathname);
            ftpClient.makeDirectory(pathname);
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.logout();
            flag = true;
        }catch (Exception e) {
            e.printStackTrace();
            return flag;
        }finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
    //?????��??
    private static boolean changeWorkingDirectory(String directory) {
        boolean flag = true;
        try {
            flag = ftpClient.changeWorkingDirectory(directory);
            if (flag) {
            } else {
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return flag;
    }
    private static boolean CreateDirecroty(String remote) throws IOException {
        boolean success = true;
        String directory = remote + "/";
        if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(new String(directory))) {
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            String path = "";
            String paths = "";
            while (true) {
                String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
                path = path + "/" + subDirectory;
                if (!existFile(path)) {
                    if (makeDirectory(subDirectory)) {
                        changeWorkingDirectory(subDirectory);
                    } else {
                        changeWorkingDirectory(subDirectory);
                    }
                } else {
                    changeWorkingDirectory(subDirectory);
                }

                paths = paths + "/" + subDirectory;
                start = end + 1;
                end = directory.indexOf("/", start);
                if (end <= start) {
                    break;
                }
            }
        }
        return success;
    }
    private static boolean existFile(String path) throws IOException {
        boolean flag = false;
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }
    private static boolean makeDirectory(String dir) {
        boolean flag = true;
        try {
            flag = ftpClient.makeDirectory(dir);
            if (flag) {

            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


    private static boolean downloadFile(String pathname, String filename, String localpath){
        boolean flag = false;
        OutputStream os=null;
        try {
            initFtpClient();
            ftpClient.changeWorkingDirectory(pathname);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for(FTPFile file : ftpFiles){
                if(filename.equalsIgnoreCase(file.getName())){
                    File localFile = new File(localpath);
                    os = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(file.getName(), os);
                    os.close();
                }
            }
            ftpClient.logout();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(null != os){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    private static boolean deleteFile(String pathname, String filename){
        boolean flag = false;
        try {
            initFtpClient();
            //?��?FTP??
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.dele(filename);
            ftpClient.logout();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }



}