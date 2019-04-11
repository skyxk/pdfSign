package com.clt.ess.webservice;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.swing.ImageIcon;

import sun.misc.BASE64Encoder;

public class WebServerSign {

    public String EncodedData = null;
    public String ImageData = null;
    public int ErrorCode = 0;
    public String ImageID = null;
    public String PictureData = "";

    public void writeLog(String sFile,byte[] b) throws IOException{
        File f = new File(sFile);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(b);
        fos.close();
    }


    private Calendar toGMT(Calendar cal) {
        Calendar cal1 = (Calendar) cal.clone();
        TimeZone tzSave = cal.getTimeZone();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal1.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal
                .get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        cal1.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));
        cal.setTimeZone(tzSave);
        cal.getTime();
        return cal1;
    }

    private String ReplaceReturn(String str) {
        String s;
        s = str.replaceAll("\r", "");
        s = s.replaceAll("\n", "");
        return s;
    }

    public X509Certificate GetPublicKeyFromPfx(byte[] bPfx, String sPwd) throws IOException, GeneralSecurityException,
            javax.security.cert.CertificateException {
        InputStream fis = new ByteArrayInputStream(bPfx);
        char[] nPassword = null;
        nPassword = sPwd.toCharArray();
        KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");
        inputKeyStore.load(fis, nPassword);
        Enumeration enuma = inputKeyStore.aliases();
        String keyAlias = null;
        keyAlias = (String) enuma.nextElement();
        X509Certificate xc = null;
        if (inputKeyStore.isKeyEntry(keyAlias)) {
            Certificate[] certChain = inputKeyStore
                    .getCertificateChain(keyAlias);
            xc = X509Certificate.getInstance(certChain[0].getEncoded());
        }
        fis.close();
        return xc;

    }


    public X509Certificate GetPublicKeyFromPfx(String sFile, String sPwd) throws IOException, GeneralSecurityException,
            javax.security.cert.CertificateException {
        FileInputStream fis;
        fis = new FileInputStream(sFile);
        char[] nPassword = null;
        nPassword = sPwd.toCharArray();
        KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");
        inputKeyStore.load(fis, nPassword);
        Enumeration enuma = inputKeyStore.aliases();
        String keyAlias = null;
        keyAlias = (String) enuma.nextElement();
        X509Certificate xc = null;

        if (inputKeyStore.isKeyEntry(keyAlias)) {
            Certificate[] certChain = inputKeyStore
                    .getCertificateChain(keyAlias);
            xc = X509Certificate.getInstance(certChain[0].getEncoded());
            // xc = (X509Certificate) certChain[0];
        }
        fis.close();
        return xc;

    }

    public PrivateKey GetPrivateKeyFromPfx(String sFile, String sPwd) throws IOException, GeneralSecurityException {
        FileInputStream fis;
        fis = new FileInputStream(sFile);
        char[] nPassword = null;
        nPassword = sPwd.toCharArray();
        KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");
        inputKeyStore.load(fis, nPassword);
        Enumeration enuma = inputKeyStore.aliases();
        String keyAlias = null;
        keyAlias = (String) enuma.nextElement();
        Key key = null;
        if (inputKeyStore.isKeyEntry(keyAlias))
            key = inputKeyStore.getKey(keyAlias, nPassword);
        fis.close();
        PrivateKey pk = (PrivateKey) key;
        return pk;

    }

    public PrivateKey GetPrivateKeyFromPfx(byte[] bPfx, String sPwd) throws IOException, GeneralSecurityException {
        InputStream fis = new ByteArrayInputStream(bPfx);
        char[] nPassword = null;
        nPassword = sPwd.toCharArray();
        KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");
        inputKeyStore.load(fis, nPassword);
        Enumeration enuma = inputKeyStore.aliases();
        String keyAlias = null;
        keyAlias = (String) enuma.nextElement();
        Key key = null;
        if (inputKeyStore.isKeyEntry(keyAlias))
            key = inputKeyStore.getKey(keyAlias, nPassword);
        fis.close();
        PrivateKey pk = (PrivateKey) key;
        return pk;

    }


    /**
     * @param plain 	要签名的明文数据
     * @param sPlainEncodeType   明文plain的编码方式
     * @param bPfx		用来签名的PFX证书
     * @param image		签章图像(GIF)
     *
     * */

    public boolean SignData(String plain,String sPlainEncodeType,byte[] bPfx,String sPwd, byte[] image) throws IOException,
            GeneralSecurityException, CertificateException {

        X509Certificate m_x509Cert = null;
        PrivateKey m_privateKey = null;
        m_x509Cert = GetPublicKeyFromPfx(bPfx, sPwd);
        m_privateKey = GetPrivateKeyFromPfx(bPfx, sPwd);
        int xPos = 0;
        int yPos = 0;

        String algorithm = "MD5withRSA";
        String provider = "SunRsaSign";
        String algorithmDigest = "MD5";
        String providerDigest = "SUN";
        String version = " 1.0";
        String m_strEncodedData = null;
        String m_strImageData = null;

        PublicKey pub = m_x509Cert.getPublicKey();
        if (pub == null || m_privateKey == null || m_x509Cert == null) {
            ErrorCode = 9005;
            return false;
        }
        if (plain == null) {
            ErrorCode = 9006;
            return false;
        }

        // encode left top
        String str4 = "" + xPos;
        while (str4.length() < 4) {
            str4 = " " + str4;
        }
        m_strEncodedData = str4;
        str4 = "" + yPos;
        while (str4.length() < 4) {
            str4 = " " + str4;
        }
        m_strEncodedData = m_strEncodedData + str4;
        m_strEncodedData = m_strEncodedData + version;

        BASE64Encoder encoder = new BASE64Encoder();
        String dataBase64;

        try {
            dataBase64 = encoder.encode(m_x509Cert.getEncoded());
            dataBase64 = this.ReplaceReturn(dataBase64);
            String strLen = "" + dataBase64.length();
            while (strLen.length() < 4) {
                strLen = " " + strLen;
            }
            m_strEncodedData = m_strEncodedData + strLen;
            m_strEncodedData = m_strEncodedData + dataBase64;
        } catch (javax.security.cert.CertificateEncodingException e) {
            ErrorCode = 9007;
            return false;
        }

        // encode signature
        try {
            Signature signer = Signature.getInstance(algorithm, provider);
            signer.initSign(m_privateKey);
            try {
                signer.update(plain.getBytes(sPlainEncodeType));
            } catch (UnsupportedEncodingException e) {
                ErrorCode = 9008;
                return false;
            }
            byte[] signature = signer.sign();

            byte[] sig = new byte[signature.length];

            for (int j = 0; j < sig.length; j++) {
                sig[j] = signature[signature.length - 1 - j];
            }
            dataBase64 = encoder.encode(sig);
            dataBase64 = this.ReplaceReturn(dataBase64);
            String strLen = "" + dataBase64.length();
            while (strLen.length() < 4) {
                strLen = " " + strLen;
            }
            m_strEncodedData = m_strEncodedData + strLen;
            m_strEncodedData = m_strEncodedData + dataBase64;
        } catch (NoSuchAlgorithmException nsae) {
            ErrorCode = 9009;
            return false;
        } catch (NoSuchProviderException nspe) {
            ErrorCode = 9010;
            return false;
        } catch (InvalidKeyException ike) {
            ErrorCode = 9011;
            return false;
        } catch (SignatureException sge) {
            ErrorCode = 9012;
            return false;
        }

        // encode sign time
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal = toGMT(cal);
        date = cal.getTime();
        long ltime = 10000 * date.getTime() + 116445024000000000L;
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        DataOutputStream doStream = new DataOutputStream(baoStream);
        try {
            doStream.writeLong(ltime);
        } catch (IOException ioe) {
            ErrorCode = 9013;
            return false;
        }
        byte[] timeBytes = baoStream.toByteArray();
        byte[] timeBytes1 = new byte[timeBytes.length];
        for (int j = 0; j < timeBytes.length; j++) {
            timeBytes1[j] = timeBytes[timeBytes.length - 1 - j];
        }
        dataBase64 = encoder.encode(timeBytes1);
        String strLen = "" + dataBase64.length();
        while (strLen.length() < 4) {
            strLen = " " + strLen;
        }
        m_strEncodedData = m_strEncodedData + strLen;
        m_strEncodedData = m_strEncodedData + dataBase64;
        // encode sign or not
        m_strEncodedData = m_strEncodedData + "   1";
        // encode image hash
        BASE64Encoder be = new BASE64Encoder();
        String imgbe = be.encode(image);
        imgbe = ReplaceReturn(imgbe);
        try {
            MessageDigest md = MessageDigest.getInstance(algorithmDigest,
                    providerDigest);
            md.update(imgbe.getBytes());
            byte[] digest = md.digest();
            dataBase64 = encoder.encode(digest);
            strLen = "" + dataBase64.length();
            while (strLen.length() < 4) {
                strLen = " " + strLen;
            }
            m_strEncodedData = m_strEncodedData + strLen;
            m_strEncodedData = m_strEncodedData + dataBase64;
            ImageID = dataBase64;

        } catch (NoSuchAlgorithmException nsae) {
            ErrorCode = 9014;
            return false;
        } catch (NoSuchProviderException nspe) {
            ErrorCode = 9015;
            return false;
        }
        // encode reserved
        m_strEncodedData = m_strEncodedData + "   0";
        EncodedData = m_strEncodedData;
        String stemp = "    ";
        ImageIcon icon = new ImageIcon(image);
        int iW = icon.getIconWidth();
        stemp = stemp + Integer.toString(iW);
        stemp = stemp.substring(stemp.length() - 4);
        m_strImageData = stemp;
        stemp = "    ";
        // /////////////////////////////////////////
        int iH = icon.getIconHeight();
        stemp = stemp + Integer.toString(iH);
        stemp = stemp.substring(stemp.length() - 4);
        m_strImageData = m_strImageData + stemp;
        stemp = "    ";
        m_strImageData = m_strImageData + "   7";
        int itemp = imgbe.length();
        stemp = "        " + Integer.toString(itemp);
        stemp = stemp.substring(stemp.length() - 8);
        m_strImageData = m_strImageData + stemp;
        m_strImageData = m_strImageData + imgbe;
        ImageData = m_strImageData;
        return true;
    }


    public static void main(String[] args) throws IOException, GeneralSecurityException, CertificateException{

    }
}

/**
 * WebServerHandWriting(
 * 		String 	sSysID,			//业务系统ID
 * 		String	sSysPersonID,	//业务系统传入的人员ID
 * 		String	sPlain,			//被签署的明文信息
 * 		String 	sPlainEncodeType,		//sPlain的编码方式 'utf8' 'gbk' 等等
 * 		String	sAuthInfo		//认证信息（可以为空)
 * }
 *
 * 返回值：
 * 成功时
 * {"STATUS":"1","ENCODEDDATA":"XXXXX","IMAGEDATA":"XXXXXXXXXXX","IMAGEID":"XXXXXXX","PICTUREDATA":"XXXXXXXXXXXX"}
 * 失败时：
 *  {"STATUS":"0","ERRORINFO":"XXXXX"}
 * */
