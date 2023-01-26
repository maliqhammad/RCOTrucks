package com.rco.rcotrucks.businesslogic.rms.recordcommon;

import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.utils.DatabaseHelper;

public class RecordRulesHelper extends BusinessRules {
    public static DatabaseHelper getDb() {
        return db;
    }
//    public static User getUser() {
//        return BusinessRules.instance().getAuthenticatedUser();
//    }
}
