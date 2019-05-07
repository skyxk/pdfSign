package com.clt.ess.FTP;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.clt.ess.utils.Location;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;

import static com.clt.ess.utils.GetLocation.locationByBookMark;

public class TestBookMark {

    public static void main(String[] args) throws IOException, DocumentException {
        // TODO Auto-generated method stub
//        PdfReader reader = new PdfReader("E:\\temp\\demo.pdf");
//
//        Document document = new Document();
//
//        PdfCopy copy = new PdfCopy(document, new FileOutputStream("E:\\temp\\demo1.pdf"));
//        document.open();
//
//        PdfOutline root = copy.getRootOutline();
//
//        copy.addDocument(reader);
//        PdfDestination destination = new PdfDestination(
//                PdfDestination.XYZ, 200, 100, 0);
//
//        PdfAction action;
//        copy.freeReader(reader);
//
//        action = PdfAction.gotoLocalPage(2, destination, copy);
//        new PdfOutline(root, action, "abc", false);
//
//        copy.flush();
//        copy.close();
//        document.close();



//        PdfReader reader1 = new PdfReader("E:\\temp\\test1.pdf");
////        Document document = new Document();
//        //List<HashMap<>>
//        List<HashMap<String,Object>> bmList;
//
//        bmList = SimpleBookmark.getBookmark(reader1);
//        for(int i=0;i<bmList.size();i++){
//            HashMap<String,Object> hm= bmList.get(i);
//            for(Entry<String, Object> entry: hm.entrySet())
//            {
//                System.out.println("Key: "+ entry.getKey()+ " Value: "+entry.getValue());
//            }
//        }
//        reader1.close();
//        reader.close();

        //书签定位
//        Location location = locationByBookMark("D:\\temp\\demo.pdf","essword");
//
//        System.out.println(location.toString());
        String value = "1 2 1";
        String[] a = value.split(" ");
        System.out.println(Integer.parseInt(a[0]));
    }

}