package com.clt.ess.utils;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  获得pdf关键字的位置的类
 */
public class GetLocation {

     public static List<Location> locations ;
    /**
     *  根据获得的 pdfreader 和页码数字解析文本获得Location 对象即位置信息
     * @param pdfReader
     * @param pageNum
     * @return
     * @throws IOException
     */
    //获得某一页的关键字信息的集合
    public static List<Location> getKeyWords(PdfReader pdfReader, final int pageNum) throws IOException {
        locations = new ArrayList<Location>();
        try {
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(
                    pdfReader);
            pdfReaderContentParser.processContent(pageNum, new RenderListener() {
                @Override
                public void beginTextBlock() {

                }
                @Override
                public void renderText(TextRenderInfo textRenderInfo) {
                    //去除pdf中文本的空格
                    String strings=textRenderInfo.getText().replace(" ", "").toLowerCase();
                    //当文本长度大于0的时候获得文字矩形范围
                    if (strings.length() > 0) {
                        Rectangle2D.Float boundingRectange = textRenderInfo
                                .getBaseline().getBoundingRectange();
                        float x = boundingRectange.x;
                        float y = boundingRectange.y;
                        double width = boundingRectange.getWidth();
                        double height = boundingRectange.getHeight();
                        //把文字的相关信息封装成location对象加入集合，此处为某个页面的所有文字信息
                        locations.add(new Location(strings, x, y, width, height, pageNum));
                    }
                }
                @Override
                public void endTextBlock() {

                }

                @Override
                public void renderImage(ImageRenderInfo imageRenderInfo) {

                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return locations;
    }

    /**
     *  传入的pdf 文件和关键字 返回一个全文中包含所有关键字的信息集合
     * @param pdfPath
     * @param keyWord
     * @return
     */
    public static List<Location> getLastKeyWord(String pdfPath, String keyWord) {
        PdfReader reader = null;
        List<Location> fiList = new ArrayList<Location>();

        char[] a = keyWord.replace(" ","").toLowerCase().toCharArray();
        try {
            reader = new PdfReader(pdfPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获得pdf的页码
        int num = reader.getNumberOfPages();
        List<Location> locations = null;
        //循环每一页 获得所有的关键字
        for (int i = 0; i< num; i++) {
            try {
                locations = getKeyWords(reader, i+1);
            } catch (IOException e) {
                continue;
            }
            if (locations != null && locations.size() > 0) {
                //根据解析函数筛选包含的关键字（因为pdf中的文本是不规则长度和断句的需要进行筛选）
                List<Location> list2 = getLocationFromList(locations, a);
                if (list2 != null) {
                    //把筛选到的内容放入集合中
                    fiList.addAll(list2);
                }

            } else if (i > 1) {
                //不是第一页的时候继续循环
                continue;
            } else {
                //到达第1页结束后关闭reader
                reader.close();
                return null;
            }
        }
        reader.close();
        return fiList;
    }


    public static Location locationByBookMark(String filePath, String title) throws IOException {
        Location location = new Location();
//        PDDocument document = PDDocument.load(new File(filePath));
        PdfReader reader1 = new PdfReader(filePath);
        try {
            List<HashMap<String,Object>> bmList = SimpleBookmark.getBookmark(reader1);
            for(HashMap<String,Object> hm :bmList){
                if(title.equals(hm.get("Title"))) {
                    Object value = hm.get("Page");
                    String[] a = value.toString().split(" ");
                    location.setPageNum(Integer.parseInt(a[0]));
                    location.setX(Integer.parseInt(a[2]));
//                PDPage page  = document.getPage(Integer.parseInt(a[0])-1);
//                int height = (int) page.getMediaBox().getHeight();
                    location.setY(Integer.parseInt(a[3]));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            reader1.close();
        }
        return location;
    }

    /**
     *  解析关键字的方法 根据输入的页面所有的内容的集合和关键字的char 数组进行匹配
     * @param list
     * @param chars
     * @return
     */
    static List<Location> getLocationFromList(List<Location> list, char[] chars) {
        //定义一个容器集合 收集位置信息对象
        List<Location> list2 = new ArrayList<Location>();
        //获得关键字的长度
        int length = chars.length;
        //循环某个页面中的所有内容
        for (int i = 0; i < list.size(); i++) {
            //关键字出现在某个词组的位置
            int lo = -1;
            //如果大于-1 则代表出现了关键字的第一个字
            if ((lo = list.get(i).getText().indexOf(chars[0])) > -1) {
                //进一步判断，获得该对象的所有文字内容
                String s = list.get(i).getText();
                //计数变量
                int count=0;
                //把该词条后的n个词条加起来 并且n要小于关键字的长度且n加上当前的次数要小于总集合的长度
                for (int j = 1; j < length && (j + i) < list.size(); j++) {
                    s += list.get(i + j).getText();
                    //更新计数次数，以便后面循环跳过计数次数个对象
                     count=j;
                }
              //如果组合的文字包含关键字内容，并且是从当前第一个文字对象起始的
                if (s.contains(new String(chars)) && s.indexOf(new String(chars))<list.get(i).getText().length()) {
                    //更新位置信息，放入集合中
                    Location location1 = list.get(i);
                    Location location = new Location();
                    int textlength = location1.getText().length();
                    location.setText(new String(chars));
                    location.setX(location1.getX() +(float) location1.getWidth() * ((float) lo / textlength));
                    location.setY(location1.getY());
                    location.setPageNum(location1.getPageNum());
                    list2.add(location);
                    //向后推移count个对象
                    i=i+count;
                } else {
                    //循环继续
                    continue;
                }

            } else {
                //循环继续
                continue;
            }
        }
        //返回筛选过的集合
        return list2;
    }

    public static void main(String[] args) throws IOException {
//        locationByBookMark("E:\\temp\\demo1.pdf","abc");
        List<Location> locations = getLastKeyWord("E:\\temp\\demo2.pdf","印章");

        System.out.println(locations.get(0).toString());

//        System.out.println(locations.get(1).toString());
    }

}
