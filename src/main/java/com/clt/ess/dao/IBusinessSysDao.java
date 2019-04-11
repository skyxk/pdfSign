package com.clt.ess.dao;

import com.clt.ess.entity.BusinessSys;

public interface IBusinessSysDao {

    BusinessSys findBusinessSysById(String businessSysId);
}
