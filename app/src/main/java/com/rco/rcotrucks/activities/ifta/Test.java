package com.rco.rcotrucks.activities.ifta;

import com.rco.rcotrucks.businesslogic.rms.recordcommon.IRmsRecordCommon;

public class Test {

    public static interface IRecordHelper <T extends DatabaseRecordCommon> {
        public void init(T databaserecord);
    }

    public static class TestHelper implements IRecordHelper {
        public void init(DatabaseRecordCar car) {
            car.make = "mymake";
            car.model = "mymodel";
        }

        @Override
        public void init(DatabaseRecordCommon databaserecord) {
            DatabaseRecordCar car = (DatabaseRecordCar) databaserecord;
            car.make = "mymake";
            car.model = "mymodel";
        }

        /**
         * Artificial processing example.
         * @param arHelpers
         * @param arRecords
         */
        public static void process(IRecordHelper[] arHelpers, DatabaseRecordCommon[] arRecords) {
            for (int i = 0; i < arHelpers.length; i++)
                arHelpers[i].init(arRecords[i]);
        }
    }

    public static class DatabaseRecordCommon {
        public int id;
    }

    public static class DatabaseRecordCar extends DatabaseRecordCommon {
        public String make;
        public String model;
    }
}
