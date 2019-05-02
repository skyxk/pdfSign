package com.clt.ess.dao;

import com.clt.ess.entity.UnitRelation;

public interface IUnitRelationDao {
    UnitRelation findUnitRelation(String childunitcode);
}
