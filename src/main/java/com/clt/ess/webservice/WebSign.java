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
import java.nio.ByteBuffer;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.swing.ImageIcon;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class WebSign {
	
	
	public String EncodedData = null;
	public String ImageData = null;
	public int ErrorCode = 0;
	public String ImageID = null;
	public String PictureData = "";
	public String SealTime = "";
	
	public void writeLog(String sFile,byte[] b) throws IOException{
		File f = new File(sFile);
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(b);
		fos.close();
	}
	
	

	private String formatStrUTCToDateStr(String utcTime) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone utcZone = TimeZone.getTimeZone("UTC");
		sf.setTimeZone(utcZone);
		Date date = null;
		String dateTime = "";
		try {
			date = sf.parse(utcTime);
			dateTime = sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateTime;
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
	
    private long longFrom8Bytes(byte[] input){
    	ByteBuffer buffer = ByteBuffer.allocate(8); 
    	buffer.put(input, 0, input.length);
    	buffer.flip();
    	return buffer.getLong();
    	
    }

    
	private String ReplaceReturn(String str) {
		String s;
		s = str.replaceAll("\r", "");
		s = s.replaceAll("\n", "");
		return s;
	}
	
	private PublicKey GetPublicKeyFromCert(byte[] bCert) throws CertificateException{
		X509Certificate xc = null;
		xc = X509Certificate.getInstance(bCert);
		return xc.getPublicKey();
	}
	
	
	public X509Certificate GetPublicKeyFromPfx(byte[] bPfx, String sPwd) throws IOException, GeneralSecurityException,
			CertificateException {
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
			CertificateException {
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
	
	public byte[] GetPictureFromImageData(String imgData){
		String img = imgData.substring(20);
		BASE64Decoder d64 = new BASE64Decoder();
		byte[] bRet = null;
		try {
			bRet = d64.decodeBuffer(img);
			return bRet;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * 获得一个<img>xxxxx</img》
	 * @param sSerialNum 	签章序号
	 * @param sGifBase64	签章图像的BASE64
	 * @param sSealName		印章名称
	 * @param sSealTime		签名时间
	 * @param sSysName		业务系统名称
	 * */
	
	public String GetImgHtml(String sSerialNum,String sGifBase64,String sSealName,String sSealTime,String sSysName){
		String imgID = sSerialNum;
		if(imgID == null)
			imgID = "";
		if(imgID.length() == 0)
			imgID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		
		//<img id= "img1"  src="pic/hw.gif" onmouseover="ljtips(this).show({content:'盖章人：李津<br>盖章时间：2018-2-12 3：23：40<br>业务系统：办公自动化<br>链接：<a href=\'http://www.sina.com.cn\' target=\'_blank\'>新浪</a>',p:'bottom',time:4000,closeBtn:true,p:'right'})" />
		String s = "<img id=\"" + imgID + "\"  class=\"sealImg\"  src=\"data:image/gif;base64," + sGifBase64 + "\" onmouseover=\"ljtips(this).show({content:'盖章人:" + sSealName;
		s = s + "<br>盖章时间:" + sSealTime + "<br>业务系统:" + sSysName + "<br>详情:<a href=\\'#\\' target=\\'_blank\\'>查看</a>',p:'bottom',time:4000,closeBtn:true,p:'right'})\" />";
		return s;
	}
	
	
	/**
	 * @param plain 被签名数据
	 * @param sPlainEncodeType  被签名数据的编码方式
	 * @param sEncodedData		签名值
	 * @param sImgData			图像值
	 * */
	public boolean VerifyData(String plain,String sPlainEncodeType,String sEncodedData,String sImgData){
		String algorithm = "MD5withRSA";
		String provider = "SunRsaSign";
		String algorithmDigest = "MD5";
		String providerDigest = "SUN";
		String sImgID = "";

		//从签名值中解析出证书
		String sLen = sEncodedData.substring(12, 16);
		sLen = sLen.trim();
		//证书长度
		int iCertLen = Integer.parseInt(sLen);
		String sTmp = sEncodedData.substring(16, 16 + iCertLen);
		BASE64Decoder d64 = new BASE64Decoder();
		byte[] bCert = null;
		try {
			bCert = d64.decodeBuffer(sTmp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		int iOffset = 16 + iCertLen;
		//从签名数据中解析出签名值
		sLen = sEncodedData.substring(iOffset,iOffset + 4);
		sLen = sLen.trim();
		iOffset = iOffset + 4;
		int iSignValLen = Integer.parseInt(sLen);
		sTmp = sEncodedData.substring(iOffset,iOffset + iSignValLen);
		byte[] bSignVal = null;
		try {
			bSignVal = d64.decodeBuffer(sTmp);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		iOffset = iOffset + iSignValLen;
		try {
			//验证签名
			Signature signer = Signature.getInstance(algorithm, provider);
			PublicKey pk = GetPublicKeyFromCert(bCert);
			signer.initVerify(pk);
			signer.update(plain.getBytes(sPlainEncodeType));
			
			byte[] sig = new byte[bSignVal.length];
			for (int j = 0; j < sig.length; j++) {
				sig[j] = bSignVal[bSignVal.length - 1 - j];
			}
			
			if(signer.verify(sig) == false)
				return false;
			//签名时间
			sLen = sEncodedData.substring(iOffset,iOffset + 4);
			sLen = sLen.trim();
			int iTmp = Integer.parseInt(sLen);
			iOffset = iOffset + 4;
			sTmp = sEncodedData.substring(iOffset, iOffset + iTmp);
			byte[] bTime1 = d64.decodeBuffer(sTmp);
			
			byte[] bTime = new byte[bTime1.length];
			
			for (int j = 0; j < bTime1.length; j++) {
				bTime[j] = bTime1[bTime1.length - 1 - j];
			}
			
			long lTime = longFrom8Bytes(bTime);
			lTime = lTime - 116445024000000000L;
			lTime = lTime / 10000;
			
			//签名时间
			Date date = new Date();
			date.setTime(lTime);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    String ss = format.format(date);
		    ss = formatStrUTCToDateStr(ss);
		    SealTime = ss;
		    iOffset = iOffset + iTmp;
		    //从签名值中解析出图像ID
		    iOffset = iOffset + 4;
			sLen = sEncodedData.substring(iOffset,iOffset + 4);
			sLen = sLen.trim();
			iOffset = iOffset + 4;
			iTmp = Integer.parseInt(sLen);
			sImgID = sEncodedData.substring(iOffset,iOffset + iTmp);
			
			//根据图像值，计算图像ID
			iOffset = 12;
			sLen = sImgData.substring(iOffset, 20);
			sLen = sLen.trim();
			iTmp = Integer.parseInt(sLen);
			iOffset = iOffset + 8;
			sTmp = sImgData.substring(iOffset,iOffset + iTmp);
			
			MessageDigest md = MessageDigest.getInstance(algorithmDigest,
					providerDigest);
			md.update(sTmp.getBytes());
			PictureData = sTmp;
			byte[] digest = md.digest();
			BASE64Encoder b64 = new BASE64Encoder();
			String sImgID2 = b64.encode(digest);
			if(sImgID2.compareTo(sImgID) == 0){
				return true;
			}else
				return false;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
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
			ImageID = dataBase64;		//印章ID是印章图像BASE64编码后，再计算HASH得到

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
		int iHeight = icon.getIconHeight();
		int iWidth = icon.getIconWidth();
		
		
//		double fR = 0.0;
//		if(iHeight > 76){
//			fR = iHeight / 76;
//			iHeight = 76;
//		}
//		int iW = icon.getIconWidth();
//		if(fR > 0)
//		{
//			iW = (int)(iW / fR);
//		}
		
		if(true){
			int iW = iWidth;
			int iH = iHeight;
			
			//先把高度控制在76以内
			float fR = 0;
			if(iH > 76)
			{
				fR = iH / 76;
				iH = 76;
			}
			else
				fR = 1;
			//根据高度缩小到比例，缩小宽度
			float fW = (iW / fR);
			float fH = iH;
			fR = 0;
			if(fW > 126)
			{
				fR = fW / 126;
				fW = 126;
			}
			else
				fR = 1;
			//根据宽度缩减到比例再次缩小高度
			fH = iH / fR;
			iW = (int)fW;
			iH = (int)fH;
			
			iHeight = iH;
			iWidth = iW;
		}
		
		stemp = stemp + Integer.toString(iWidth);
		stemp = stemp.substring(stemp.length() - 4);
		m_strImageData = stemp;
		stemp = "    ";
		// /////////////////////////////////////////
		int iH = iHeight;
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
		
		int iW = 398;
		int iH = 136;
		double iWR = 0.0;
		double iHR = 0.0;
		if(iH > 76)
			iHR = iH / 76;
		if(iW > 126)
			iWR = iW / 126;
		
		double iR = 0x0;
		if(iWR > iHR)
			iR = iWR;
		else
			iR = iHR;
		
		iW = (int)(iW / iR);
		iH = (int)(iH / iR);
		
		System.out.println(iW + "&" + iH);
		
//		//盖章用的图像
//		File f = new File("d:\\temp\\3.gif");
//		FileInputStream fis = new FileInputStream(f);
//		int iLen = (int) f.length();
//		byte[] bGif = new byte[iLen];
//		fis.read(bGif);
//		fis.close();
//		
//		//盖章用的PFX证书
//		f = new File("d:\\temp\\test.pfx");
//		fis = new FileInputStream(f);
//		iLen = (int)f.length();
//		byte[] bPfx = new byte[iLen];
//		fis.read(bPfx);
//		fis.close();
//		
//		WebSign ws = new WebSign();
//		if(ws.SignData("123456", "utf8",bPfx, "111111",bGif))
//		{
//			System.out.println("yes");
//			ws.VerifyData("123456", "utf8", ws.EncodedData, ws.ImageData);
//			
//			
////			byte[] b = ws.GetPictureFromImageData(ws.ImageData);
////			f = new File("d:\\temp\\22.gif");
////			FileOutputStream fos = new FileOutputStream(f);
////			fos.write(b);
////			fos.close();
//			
//			//签章成功
//			//// {"ENCODEDDATA":"XXXXX","IMAGEDATA":"XXXXXXXXXXX","IMAGEID":"XXXXXXX","PICTUREDATA":"XXXXXXXXXXXX"}
//		//	ENCODEDDATA = ws.EncodedData
//		//	IMAGEDATA = ws.ImageData
//		//	IMAGEID = 	ws.ImageID
//		// PICTUREDATA= ws.PictureData
//		}else{
//			System.out.println("no");
//		}	
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
 *  
 *  
 *  
 *  
 * */
