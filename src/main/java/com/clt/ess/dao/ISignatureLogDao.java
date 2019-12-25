package com.clt.ess.dao;

import com.clt.ess.entity.SignatureLog;

public interface ISignatureLogDao {
    SignatureLog findSignatureLogBySerNumForOA(String serNum);
    SignatureLog findSignatureLogBySerNumZM(String serNum);

    int addSignatureLogForOA(SignatureLog signatureLog);
}
