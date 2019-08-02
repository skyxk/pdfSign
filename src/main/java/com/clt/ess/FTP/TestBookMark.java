package com.clt.ess.FTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.clt.ess.utils.Location;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import static com.clt.ess.utils.GetLocation.getLastKeyWord;
import static com.clt.ess.utils.GetLocation.locationByBookMark;
import static com.clt.ess.utils.Sign.addSeal;

public class TestBookMark {

    public static void main(String[] args) throws IOException, DocumentException, GeneralSecurityException {
        // TODO Auto-generated method stub
        PdfReader reader = new PdfReader("D:\\temp\\demo.pdf");

        Document document = new Document();

        PdfCopy copy = new PdfCopy(document, new FileOutputStream("E:\\temp\\demo1.pdf"));
        document.open();

        PdfOutline root = copy.getRootOutline();

        copy.addDocument(reader);
        PdfDestination destination = new PdfDestination(
                PdfDestination.XYZ, 0, 0, 0);

        PdfAction action;
        copy.freeReader(reader);

        action = PdfAction.gotoLocalPage(2, destination, copy);
        new PdfOutline(root, action, "abc", false);

        copy.flush();
        copy.close();
        document.close();



        PdfReader reader1 = new PdfReader("D:\\temp\\test1.pdf");
//        Document document = new Document();
        //List<HashMap<>>
        List<HashMap<String,Object>> bmList;

        bmList = SimpleBookmark.getBookmark(reader1);
        for(int i=0;i<bmList.size();i++){
            HashMap<String,Object> hm= bmList.get(i);
            for(Entry<String, Object> entry: hm.entrySet())
            {
                System.out.println("Key: "+ entry.getKey()+ " Value: "+entry.getValue());
            }
        }
        reader1.close();
        reader.close();


//        书签定位
//        Location location = locationByBookMark("D:\\temp\\demo10.pdf","SIGNATRUE1");
////        List<Location> list = getLastKeyWord("D:\\temp\\demo4.pdf","SIGNATRUE1");
//        if(location==null){
//            //书签定位错误
//            System.out.println(1);
//        }else if(location.getPageNum()==0){
//            System.out.println(2);
//        }
//        System.out.println(location.toString());


//        File file = new File("C:\\Users\\陈晓坤\\Desktop\\国徽章\\透明度25.png");
//        byte[] imgByte = Files.readAllBytes(file.toPath());
//
////        addSeal("D:\\temp\\demo11.pdf",imgByte,141,141,1,350,700,
////                "D:\\temp\\rootjs.pfx",
////                "111111","123456");
//        file = new File("C:\\Users\\陈晓坤\\Desktop\\国徽章\\seal.png");
//        imgByte = Files.readAllBytes(file.toPath());
//        addSeal("D:\\temp\\demo.pdf",imgByte,141,141,1,290,150,
//                "D:\\temp\\rootjs.pfx",
//                "111111","123456");
    }

}