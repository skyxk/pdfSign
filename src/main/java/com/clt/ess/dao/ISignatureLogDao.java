package com.clt.ess.dao;

import com.clt.ess.entity.SignatureLog;

public interface ISignatureLogDao {
    SignatureLog findSignatureLogBySerNum(String serNum);
}
