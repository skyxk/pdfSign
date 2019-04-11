package com.clt.ess.utils;

import org.springframework.stereotype.Component;

@Component
/**
 * pdf文字位置信息的对象
 */
public class Location {
    private String text; //内容
    private float x;      //x坐标
    private float y;       //y坐标
    private double width; //宽度
    private double height;//高度
    private int pageNum;//所在页码

    public Location() {
    }

    public Location(String text, float x, float y, double width, double height, int pageNum) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pageNum = pageNum;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public String toString() {
        return "Location{" +
                "text='" + text + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", pageNum=" + pageNum +
                '}';
    }
}
