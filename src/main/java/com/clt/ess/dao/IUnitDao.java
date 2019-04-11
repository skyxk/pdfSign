package com.clt.ess.dao;


import com.clt.ess.entity.Unit;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IUnitDao {

    Unit findUnitByUnitId(String unitId);

    /**
     * 递归查询所有的子单位
     * @param parentUnitId
     * @return
     */
    List<Unit> findUnitByParentUnitId(String parentUnitId);

    List<Unit> findUnitByOrgId(@Param("OrgID")String OrgID, @Param("businessSysId")String businessSysId);

    String findBSUnitNameById(String businessSysId);
}
