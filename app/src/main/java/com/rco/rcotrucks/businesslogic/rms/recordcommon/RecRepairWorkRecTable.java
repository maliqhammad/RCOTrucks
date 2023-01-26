package com.rco.rcotrucks.businesslogic.rms.recordcommon;

import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.rco.rcotrucks.utils.StringUtils;

public class RecRepairWorkRecTable extends RecRepairWork {
    public static final String TAG = RecRepairWorkRecTable.class.getSimpleName();

    public RecRepairWorkRecTable(String tablename) {
        super(tablename);
    }

    public static class RecWorkCombo {
        private RmsRecTableRec recWork;
//        private RecRepairWork recRepairWork;
        private RecordCommonHelperTableRec recordHelper;


        public RecordCommonHelperTableRec getRecordHelper() {
            return recordHelper;
        }

        public RmsRecTableRec getRecWork() {
            return recWork;
        }

//        public RecRepairWork getRecRepairWork() {
//            return recRepairWork;
//        }

        public RecWorkCombo(RmsRecTableRec recWork,
//                                  RecRepairWork recRepairWork,
                            RecordCommonHelperTableRec recordHelper) {
            this.recWork = recWork;
//            this.recRepairWork = recRepairWork;
            this.recordHelper = recordHelper;
        }

        public String toString() {
            return "recWork=" + recWork; //  + ", recRepairWork=" + recRepairWork;
        }
    }

}
