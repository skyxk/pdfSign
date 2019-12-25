package com.clt.ess.utils;

import com.itextpdf.text.DocumentException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.clt.ess.utils.FileUtil.File2byte;
import static com.clt.ess.utils.Sign.addSeal;

public class test {


    public static void main(String[] args) throws IOException, GeneralSecurityException, DocumentException {
//        PDDocument document = PDDocument.load(new File("D:\\temp\\demo.pdf"));
//        int pageNum = document.getNumberOfPages();
//        PDPageTree pages = document.getPages();
//
//        PDPage p = pages.get(0);
//        int pWidth = (int) p.getMediaBox().getWidth();
//        int pHeight = (int) p.getMediaBox().getHeight();
//        System.out.println(pWidth + ""+pHeight);
//

        long l1 =System.currentTimeMillis();
        byte[] imgFile = File2byte("D:\\pdf\\0b809b58-405e-4bec-b297-c4622afd2cc7\\seal.gif");
        addSeal("D:\\temp\\111.pdf",imgFile,20,
                10,1,100,100,"D:\\temp\\rootjs.pfx","111111","11111");
        long l2 =System.currentTimeMillis();
        System.out.println(l2-l1);

    }
}
