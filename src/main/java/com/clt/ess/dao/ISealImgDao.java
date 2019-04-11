package com.clt.ess.dao;


import com.clt.ess.entity.SealImg;

public interface ISealImgDao {

    int addSealImg(SealImg sealImg);
    int updateSealImg(SealImg sealImg);
    SealImg findSealImgById(String imgId);

    boolean deleteSealImgById(String seaImgId);
}
