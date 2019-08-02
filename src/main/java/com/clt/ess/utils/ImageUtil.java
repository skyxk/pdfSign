package com.clt.ess.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.clt.ess.utils.FileUtil.File2byte;

/**
 * @date 2018年01月13日 上午12:24:26
 */
public class ImageUtil {

    private static String DEFAULT_THUMB_PREVFIX = "thumb_";
    private static String DEFAULT_CUT_PREVFIX = "cut_";
    private static Boolean DEFAULT_FORCE = false;

    /**
     * <p>Title: cutImage</p>
     * <p>Description:  根据原图与裁切size截取局部图片</p>
     * @param srcImg    源图片
     * @param output    图片输出流
     * @param rect        需要截取部分的坐标和大小
     */
    public void cutImage(File srcImg, OutputStream output, Rectangle rect){
        if(srcImg.exists()){
            FileInputStream fis = null;
            ImageInputStream iis = null;
            try {
                fis = new FileInputStream(srcImg);
                // ImageIO 支持的图片类型 : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP, GIF, gif]
                String types = Arrays.toString(ImageIO.getReaderFormatNames()).replace("]", ",");
                String suffix = null;
                // 获取图片后缀
                if(srcImg.getName().indexOf(".") > -1) {
                    suffix = srcImg.getName().substring(srcImg.getName().lastIndexOf(".") + 1);
                }// 类型和图片后缀全部小写，然后判断后缀是否合法
                if(suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()+",") < 0){
//                    log.error("Sorry, the image suffix is illegal. the standard image suffix is {}." + types);
                    return ;
                }
                // 将FileInputStream 转换为ImageInputStream
                iis = ImageIO.createImageInputStream(fis);
                // 根据图片类型获取该种类型的ImageReader
                ImageReader reader = ImageIO.getImageReadersBySuffix(suffix).next();
                reader.setInput(iis,true);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceRegion(rect);
                BufferedImage bi = reader.read(0, param);
                ImageIO.write(bi, suffix, output);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(fis != null) fis.close();
                    if(iis != null) iis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
//            log.warn("the src image is not exist.");
        }
    }

    public void cutImage(File srcImg, OutputStream output, int x, int y, int width, int height){
        cutImage(srcImg, output, new Rectangle(x, y, width, height));
    }

    public void cutImage(File srcImg, String destImgPath, Rectangle rect){
        File destImg = new File(destImgPath);
        if(destImg.exists()){
            String p = destImg.getPath();
            try {
                if(!destImg.isDirectory()) p = destImg.getParent();
                if(!p.endsWith(File.separator)) p = p + File.separator;
                cutImage(srcImg, new FileOutputStream(p + DEFAULT_CUT_PREVFIX + "_" + new java.util.Date().getTime() + "_" + srcImg.getName()), rect);
            } catch (FileNotFoundException e) {
//                log.warn("the dest image is not exist.");
            }
        }
//        else log.warn("the dest image folder is not exist.");
    }

    public void cutImage(File srcImg, String destImg, int x, int y, int width, int height){
        cutImage(srcImg, destImg, new Rectangle(x, y, width, height));
    }

    public void cutImage(String srcImg, String destImg, int x, int y, int width, int height){
        cutImage(new File(srcImg), destImg, new Rectangle(x, y, width, height));
    }
    /**
     * <p>Title: thumbnailImage</p>
     * <p>Description: 根据图片路径生成缩略图 </p>
//     * @param imagePath    原图片路径
     * @param w            缩略图宽
     * @param h            缩略图高
     * @param prevfix    生成缩略图的前缀
     * @param force        是否强制按照宽高生成缩略图(如果为false，则生成最佳比例缩略图)
     */
    public void thumbnailImage(File srcImg, OutputStream output, int w, int h, String prevfix, boolean force){
        if(srcImg.exists()){
            try {
                // ImageIO 支持的图片类型 : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP, GIF, gif]
                String types = Arrays.toString(ImageIO.getReaderFormatNames()).replace("]", ",");
                String suffix = null;
                // 获取图片后缀
                if(srcImg.getName().indexOf(".") > -1) {
                    suffix = srcImg.getName().substring(srcImg.getName().lastIndexOf(".") + 1);
                }// 类型和图片后缀全部小写，然后判断后缀是否合法
                if(suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()+",") < 0){
//                    log.error("Sorry, the image suffix is illegal. the standard image suffix is {}." + types);
                    return ;
                }
//                log.debug("target image's size, width:{}, height:{}.",w,h);
                Image img = ImageIO.read(srcImg);
                // 根据原图与要求的缩略图比例，找到最合适的缩略图比例
                if(!force){
                    int width = img.getWidth(null);
                    int height = img.getHeight(null);
                    if((width*1.0)/w < (height*1.0)/h){
                        if(width > w){
                            h = Integer.parseInt(new java.text.DecimalFormat("0").format(height * w/(width*1.0)));
//                            log.debug("change image's height, width:{}, height:{}.",w,h);
                        }
                    } else {
                        if(height > h){
                            w = Integer.parseInt(new java.text.DecimalFormat("0").format(width * h/(height*1.0)));
//                            log.debug("change image's width, width:{}, height:{}.",w,h);
                        }
                    }
                }
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics g = bi.getGraphics();
                g.drawImage(img, 0, 0, w, h, Color.LIGHT_GRAY, null);
                g.dispose();
                // 将图片保存在原目录并加上前缀
                ImageIO.write(bi, suffix, output);
                output.close();
            } catch (IOException e) {
//                log.error("generate thumbnail image failed.",e);
            }
        }else{
//            log.warn("the src image is not exist.");
        }
    }

    public void thumbnailImage(File srcImg, int w, int h, String prevfix, boolean force){
        String p = srcImg.getAbsolutePath();
        try {
            if(!srcImg.isDirectory()) p = srcImg.getParent();
            if(!p.endsWith(File.separator)) p = p + File.separator;
            thumbnailImage(srcImg, new FileOutputStream(p + prevfix +srcImg.getName()), w, h, prevfix, force);
        } catch (FileNotFoundException e) {
//            log.error("the dest image is not exist.",e);
        }
    }

    public void thumbnailImage(String imagePath, int w, int h, String prevfix, boolean force){
        File srcImg = new File(imagePath);
        thumbnailImage(srcImg, w, h, prevfix, force);
    }

    public void thumbnailImage(String imagePath, int w, int h, boolean force){
        thumbnailImage(imagePath, w, h, DEFAULT_THUMB_PREVFIX, DEFAULT_FORCE);
    }

    public void thumbnailImage(String imagePath, int w, int h){
        thumbnailImage(imagePath, w, h, DEFAULT_FORCE);
    }



    /**
     * 给图片添加水印文字、可设置水印文字的旋转角度
     * @param logoText 要写入的文字
     * @param srcImgPath 源图片路径
     * @param newImagePath 新图片路径
     * @param degree 旋转角度
     * @param color  字体颜色
     * @param formaName 图片后缀
     */
    public static void markImageByText(String logoText, String srcImgPath, String newImagePath, Integer degree, Color color, String formaName) {
        InputStream is = null;
        OutputStream os = null;
        try {
            // 1、源图片
            java.awt.Image srcImg = ImageIO.read(new File(srcImgPath));
            BufferedImage buffImg = new BufferedImage(srcImg.getWidth(null),srcImg.getHeight(null)+20, BufferedImage.TYPE_INT_ARGB);
            // 2、得到画笔对象
            Graphics2D g = buffImg.createGraphics();
            // 3、设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), java.awt.Image.SCALE_SMOOTH),
                    0, 0, Color.white,null);
            // 4、设置水印旋转
            if (null != degree) {
                g.rotate(Math.toRadians(degree),  buffImg.getWidth()/2,buffImg.getHeight() /2);
            }
            // 5、设置水印文字颜色
            g.setColor(color);
            // 6、设置水印文字Font
            g.setFont(new java.awt.Font("宋体", java.awt.Font.BOLD, 12));
            // 7、设置水印文字透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
            // 8、第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y)
            g.drawString(logoText,  buffImg.getWidth()/9 , buffImg.getHeight()*4/5+30);
            // 9、释放资源
            g.dispose();
            // 10、生成图片
            os = new FileOutputStream(newImagePath);
            ImageIO.write(buffImg, formaName, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != is)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (null != os)
                    os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[]  markImageByText(byte[] imgByte,String text,Color color) throws IOException {
        OutputStream os = null;
        ByteArrayInputStream inpic = new ByteArrayInputStream(imgByte);
        BufferedImage bufferedImage = ImageIO.read(inpic);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        //2.创建一个空白大小相同的RGB背景
        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                bufferedImage.getHeight(), 1);
        // 2、得到画笔对象
        Graphics2D g = newBufferedImage.createGraphics();
        // ----------  增加下面的代码使得背景透明  -----------------
        newBufferedImage = g.getDeviceConfiguration()
                .createCompatibleImage(bufferedImage.getWidth(), bufferedImage.getHeight()*10/9, Transparency.TRANSLUCENT);
        g.dispose();
        g = newBufferedImage.createGraphics();
        // ----------  背景透明代码结束  -----------------
        // 3、设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(bufferedImage.getScaledInstance(bufferedImage.getWidth(null),
                bufferedImage.getHeight(null), Image.SCALE_SMOOTH), 0, 0,null);
        // 5、设置水印文字颜色
        g.setColor(color);
        // 6、设置水印文字Font
        g.setFont(new java.awt.Font("宋体", java.awt.Font.BOLD, 45));
        // 7、设置水印文字透明度 AlphaComposite.SRC_ATOP
        g.setComposite(AlphaComposite.getInstance(2, 1f));
        // 8、第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y)
        g.drawString(text,  newBufferedImage.getWidth()/6 ,newBufferedImage.getHeight()-4);
        // 9、释放资源
        g.dispose();
//
//        os = new FileOutputStream("C:\\Users\\mrche\\Desktop\\2.gif");
//        ImageIO.write(newBufferedImage, "gif", os);
//        output.close();
        ImageIO.write(newBufferedImage,"png",output);
        return output.toByteArray();
    }
    public static String GetSerialNumber()
    {
        Date nowDate = new Date();
        long lNow = nowDate.getTime();
        lNow = lNow - 1564222021076L;
        String sNowTime = Long.toString(lNow,32);
        SimpleDateFormat ft = new SimpleDateFormat("MMddHHmmss");
        String sTmp = ft.format(nowDate);
        long lTmp = Long.parseLong(sTmp);
        sTmp = Long.toString(lTmp, 32);
        sTmp = "0000000" + sTmp;
        sTmp = sTmp.substring(sTmp.length() - 7);
        sTmp = sNowTime + "" + sTmp;
        return sTmp.toUpperCase();
    }

    public static boolean VerifySerialNumber(String sSerialNum)
    {
        try
        {
            String s = sSerialNum.toLowerCase();
            String s1 = s.substring(s.length() - 7);
            long l1 = Long.parseLong(s1, 32);
            s1 = Long.toString(l1);
            if(s1.length() == 9)
                s1 = "0" + s1;
            int iM = Integer.parseInt(s1.substring(0,2));
            int iD = Integer.parseInt(s1.substring(2, 4));
            int iH = Integer.parseInt(s1.substring(4, 6));
            int im = Integer.parseInt(s1.substring(6, 8));
            int is = Integer.parseInt(s1.substring(8, 10));
            String sDate = sSerialNum.substring(0,sSerialNum.length() - 7);
            l1 = Long.parseLong(sDate, 32);
            l1 = l1 + 1564222021076L;
            Date dTime = new Date();
            dTime.setTime(l1);
            Calendar d = Calendar.getInstance();
            d.setTime(dTime);
            if(iD == d.get(Calendar.DAY_OF_MONTH) && iM == (d.get(Calendar.MONTH)+1) && iH == d.get(Calendar.HOUR_OF_DAY) && im == d.get(Calendar.MINUTE) && is == d.get(Calendar.SECOND))
                return true;
            else
                return false;
        }catch(Exception e){
            return false;
        }
    }
}