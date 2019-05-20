package com.clt.ess.utils;

import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CutImageUtil {

    /**
     * 切割图片
     * @param sourceFilePath 源文件路径
     * @param index 水平方向等分切割数
     * @return List<String>   base64编码数组
     */
//    public static List<String> cutImageToBase64(String sourceFilePath, int index){
//        File file = new File(sourceFilePath);
//        if (file.exists()) {
//            return cutImageToBase64(file,index);
//        }else{
//            return null;
//        }
//    }

    /**
     * 切割图片
     * @param sourceFilePath 源文件路径
     * @param sourceFilePath 保存文件路径
     * @param index 水平方向等分切割数
     * @return List<File>   File数组
     */
    public static List<File> cutImageToFile(String sourceFilePath, String targetDir, int index){
        File file = new File(sourceFilePath);
        if (file.exists()) {
            return cutImageToFile(file, targetDir, index);
        }else{
            return null;
        }
    }
    /**
     * 切割图片
     *
     * @param sourceFile
     *            源文件
     * @param index
     *            水平方向等分切割数
     * @return List<String> base64编码数组
     */
    public static List<String> cutImageToBase64(byte[] sourceFile, int index) {
        List<String> list = new ArrayList<String>();
//        int suffixIndex = sourceFile.getName().lastIndexOf(".");
//        String suffix = sourceFile.getName().substring(suffixIndex+1);
        try {
            ByteArrayInputStream inpic = new ByteArrayInputStream(sourceFile);
            BufferedImage source = ImageIO.read(inpic);
            int width = source.getWidth(); // 图片宽度
            int height = source.getHeight(); // 图片高度
            if (index>1) {
                int cWidth = width/index; // 切片宽度
                BufferedImage image = null;
                for (int i = 0; i < index; i++) {
                    // x坐标,y坐标,宽度,高度
                    BASE64Encoder encoder = new BASE64Encoder();
                    int cw = i*cWidth;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image = source.getSubimage(cw,0,cWidth,height);
                    ImageIO.write(image, "PNG", baos);
                    byte[] bytes = baos.toByteArray();
                    list.add(encoder.encodeBuffer(bytes));
                    baos.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 给图片添加单个文字水印、可设置水印文字旋转角度
     * @param source 需要添加水印的图片路径（如：F:/images/6.jpg）
     * @param color 水印文字的颜色
     * @param word 水印文字
     * @param degree 水印文字旋转角度，为null表示不旋转
     */
    public static byte[] markImageBySingleText(byte[] source, Color color, String word, Integer degree) throws IOException {



        //读取原图片信息
        InputStream imgInput = new ByteArrayInputStream(source);
        Image img = ImageIO.read(imgInput);
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        //加水印
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(img, 0, 0, width, height, null);
        //设置水印字体样式
        Font font = new Font("宋体", Font.PLAIN, 100);
        //根据图片的背景设置水印颜色
        g.setColor(color);
        if (null != degree) {
            //设置水印旋转
            g.rotate(Math.toRadians(degree),(double) bi.getWidth() / 2, (double) bi.getHeight() / 2);
        }
        g.setFont(font);
        int x = 0;
        int y = height-height/9;
        //水印位置
        g.drawString(word, x, y);
        g.dispose();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi,"gif",bos);
        return bos.toByteArray();
    }


    /**
     * 切割图片
     *
     * @param sourceFile
     *            源文件
     * @param index
     *            水平方向等分切割数
     * @return List<String> base64编码数组
     */
    public static List<File> cutImageToFile(File sourceFile, String targetDir, int index) {
        List<File> list = new ArrayList<File>();
        int suffixIndex = sourceFile.getName().lastIndexOf(".");
        String suffix = sourceFile.getName().substring(suffixIndex+1);
        String name =  sourceFile.getName().substring(0,suffixIndex);
        try {
            BufferedImage source = ImageIO.read(sourceFile);
            int width = source.getWidth(); // 图片宽度
            int height = source.getHeight(); // 图片高度
            if (index>1) {
                int cWidth = width/index; // 切片宽度
                BufferedImage image = null;
                File file = new File(targetDir);
                if (!file.exists()) { // 存储目录不存在，则创建目录
                    file.mkdirs();
                }
                int num =(int)(Math.random()*100000000);
                for (int i = 0; i < index; i++) {
                    // x坐标,y坐标,宽度,高度
                    int cw = i*cWidth;
                    image = source.getSubimage(cw,0,cWidth,height);
                    String fileName = targetDir + "/"+name+"_"+num+"_"+i+ "."+suffix;
                    file = new File(fileName);
                    ImageIO.write(image,"PNG", file);
                    list.add(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


}
