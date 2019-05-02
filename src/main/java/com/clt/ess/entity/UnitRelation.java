package com.clt.ess.entity;

public class UnitRelation {

    private int id;
    private String childunitcode;
    private String parentunitcode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChildunitcode() {
        return childunitcode;
    }

    public void setChildunitcode(String childunitcode) {
        this.childunitcode = childunitcode;
    }

    public String getParentunitcode() {
        return parentunitcode;
    }

    public void setParentunitcode(String parentunitcode) {
        this.parentunitcode = parentunitcode;
    }
}
