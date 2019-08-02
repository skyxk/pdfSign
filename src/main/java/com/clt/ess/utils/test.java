package com.clt.ess.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import java.io.File;
import java.io.IOException;

public class test {


    public static void main(String[] args) throws IOException {
        PDDocument document = PDDocument.load(new File("D:\\temp\\demo.pdf"));
        int pageNum = document.getNumberOfPages();
        PDPageTree pages = document.getPages();

        PDPage p = pages.get(0);
        int pWidth = (int) p.getMediaBox().getWidth();
        int pHeight = (int) p.getMediaBox().getHeight();
        System.out.println(pWidth + ""+pHeight);
    }
}
