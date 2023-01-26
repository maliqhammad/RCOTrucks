package com.rco.rcotrucks.businesslogic.rms.recordcommon;

import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;

public abstract class RmsRecTableRec extends RmsRecCommon implements IRmsRecordCommon.IRmsRecTableRec {
    public RmsRecTableRec(String tablename, String recordType) {
        setTablename(tablename);
        setRecordType(recordType);
        BusHelperRmsCoding.RmsRecordType rtype = BusinessRules.getMapRecordTypeInfoFromRecordTypeName().get(recordType);
        setObjectType(rtype.objectType);
        setIdRecordType(rtype.id);
    }
}
