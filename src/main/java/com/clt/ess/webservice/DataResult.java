package com.clt.ess.webservice;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="DataResult")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"resultType","resultMessage","dataHandler"})
public class DataResult  {

    public Boolean resultType;
    public String resultMessage;
    public DataHandler dataHandler;

    public Boolean getResultType() {
        return resultType;
    }

    public void setResultType(Boolean resultType) {
        this.resultType = resultType;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }


}
