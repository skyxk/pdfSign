package com.clt.ess.utils;

import com.clt.ess.base.Constant;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSigLockDictionary;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.UUID;

public class Sign {

    public static void addOverSeal(String pdfFile,byte[] sealImg,String pfxPath,String pwd,String signSerialNum,
                                   float width, float heigth) throws IOException,
            DocumentException, GeneralSecurityException {
        //PDF骑缝章签署过程
        //首先处理签章图片，
        //获取签章的页数


        PDDocument document = PDDocument.load(new File(pdfFile));
        int pageNum = document.getNumberOfPages();
        PDPageTree pages = document.getPages();
        document.close();
        if (pageNum>1){
            //处理图片，将图片按照pdf页数等分，并获取图片宽度和长度。
            List<String> ImageBase64List = CutImageUtil.cutImageToBase64(sealImg,pageNum);
            //每页的签章位置 需要知道 每页的签章图片和签章起始坐标
            for(int num = 0;num<pageNum;num++){
                PDPage p = pages.get(num);
                int pWidth = (int) p.getMediaBox().getWidth();
                int pHeight = (int) p.getMediaBox().getHeight();

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64Utils.ESSGetBase64Decode(
                        ImageBase64List.get(num)));
                BufferedImage source = ImageIO.read(byteArrayInputStream);
                int cWidth = source.getWidth();
                int cHeigth = source.getHeight();
                cWidth = (int) width/pageNum;
                cHeigth = (int) heigth;
                //计算签章其实坐标
                int x = pWidth-cWidth/2;
                int y = pHeight/2;

                addSeal(pdfFile,Base64Utils.ESSGetBase64Decode(ImageBase64List.get(num)),cWidth,cHeigth,
                        num+1,x,y,pfxPath,pwd,signSerialNum);
                //骑缝章有一个签署不成功则离开循环，签章错误
            }
        }

    }

    public static void addOverSealPage(String pdfPath, byte[] picPath, float width, float heigth,float x, float y,
                                       String pdxPath, String pwd,String signSerialNum) throws IOException,
            DocumentException, GeneralSecurityException {

        PDDocument document = PDDocument.load(new File(pdfPath));
        int pageNum = document.getNumberOfPages();
        PDPageTree pages = document.getPages();
        document.close();

        //每页的签章位置 需要知道 每页的签章图片和签章起始坐标
        for(int num = 0;num<pageNum;num++){
            addSeal(pdfPath,picPath,width,heigth,
                    num+1,x,y,pdxPath,pwd,signSerialNum);
            //骑缝章有一个签署不成功则离开循环，签章错误
        }
    }
    /**
     *签章并生成新的文件
     */
    public static void addSeal(String pdfPath, byte[] picPath, float width, float heigth, int pageNum, float x, float y,
                               String pdxPath, String pwd,String signSerialNum) throws IOException, DocumentException, GeneralSecurityException {
        String uuid = UUID.randomUUID().toString();
        FileOutputStream os =null;
        PdfReader reader =null;
        PdfStamper stamper =null;
        FileInputStream in =null;
        File file = new File(pdfPath);
        File file2 = new File(pdfPath+"_");
        try{
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            InputStream inp = null;
            //初始化盖章的私钥和证书 和签名的图片信息

            Image pic = Image.getInstance(picPath);
            KeyStore ks = KeyStore.getInstance("PKCS12");
            in = new FileInputStream(pdxPath);
            ks.load(in, pwd.toCharArray());
            String alias = (String) ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, pwd.toCharArray());
            Certificate[] chain = ks.getCertificateChain(alias);
            //相当于temp文件
            os = new FileOutputStream(file2);
            //读取源文件
            reader = new PdfReader(pdfPath);
            stamper = PdfStamper
                    .createSignature(reader, os, '\u0000', null, true);//注意此处的true 允许多次盖章，false则只能盖一个章。
            //设置签名的外观显示
            PdfSignatureAppearance appearance = stamper
                    .getSignatureAppearance();
            //规定签章的权限，包括三种，详见itext 5 api，这里是不允许任何形式的修改
            PdfSigLockDictionary dictionary = new PdfSigLockDictionary(PdfSigLockDictionary.LockPermissions.FORM_FILLING_AND_ANNOTATION);
            appearance.setFieldLockDict(dictionary);
            appearance.setReason(signSerialNum);
            appearance.setImage(pic);
            //此处的fieldName 每个文档只能有一个，不能重名
            appearance.setVisibleSignature(
                    new Rectangle(x-width/2, y-heigth/2, x + width/2,
                            y + heigth/2), pageNum,
                    "ESSPDFSign" + uuid);//fileName 一个文档中不能有重名的filedname
            appearance.setLayer2Text("");//设置文字为空否则签章上将会有文字 影响外观
            ExternalSignature es = new PrivateKeySignature(pk,
                    "SHA-256", "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, es,
                    chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (os != null) {
                os.close();
            }
            if (stamper != null) {
                stamper.close();
            }
            if (reader != null) {
                reader.close();
            }
            file.delete();
            file2.renameTo(file);
            if (in != null) {
                in.close();
            }
        }



//      file.getAbsolutePath();
    }



    public static void main(String[] args) throws IOException {

        File source = new File("E:\\temp\\demo.pdf");
        File dest = new File("E:\\temp\\demo1.pdf");
        Files.copy(source.toPath(), dest.toPath());
//        addOverSeal(new File(Constant.pdfPath),Constant.imgPath,Constant.pfxPath,"111111");
        //不是骑缝章
//        File imgFile = new File(Constant.imgPath);
//        InputStream input = new FileInputStream(imgFile);
//        byte[] byt = new byte[input.available()];
//        input.read(byt);
//        addSeal(Constant.pdfPath,byt,159,159,1,200,
//                200,Constant.pfxPath,"111111");
    }
}
