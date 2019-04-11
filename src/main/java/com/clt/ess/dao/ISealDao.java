package com.clt.ess.dao;

import com.clt.ess.entity.Seal;
import com.clt.ess.entity.SealType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ISealDao {

    Seal findDemo(String id);

    int addSeal(Seal seal);
    int updateSeal(Seal seal);
    int delSeal(Seal seal);
    List<Seal> findSealList(Seal seal);
    List<SealType> findSealType(SealType sealType);

    List<Seal> findSealListByIdNums(List<String> personIdNums);

    Seal findSealById(String sealId);

    int findSealCountByUnitId(String unitId);

    Seal findSealByUnitAndType(@Param("sealType") String sealType, @Param("sOrgID") String sOrgID);

    Seal findSealByIdNum(String idNum);
}
