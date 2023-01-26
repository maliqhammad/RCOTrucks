package com.rco.rcotrucks.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String TAG = "com.rco.rcotrucks.database";

    private ArrayList<String> tablenames;
    private String[] creationscript;
    private String name;

    private SQLiteDatabase cnn;
    private Context ctx;

    private boolean isNewVersion = false;
    //private Lock lock = new ReentrantLock();

    // Database life-cycle

    public DatabaseHelper(Context context, String name, int dbversion, ArrayList<String> tablenames, String[] creationscript) {
        super(context, name, null, dbversion);

        this.ctx = context;
        this.name = name;
        this.creationscript = creationscript;
        this.tablenames = tablenames;

        this.cnn = this.getWritableDatabase();
    }

    public void createTables() {
        //Log.d(TAG, "Creating tables...");

        if (creationscript != null)
            for (int i=0; i<creationscript.length; i++) {
                //Log.d(TAG, "Executing query " + creationscript[i]);
                cnn.execSQL(creationscript[i]);
            }

        //Log.d(TAG, "Done");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.d(TAG, "onCreate() Creating database " + name + ", creationscript != null: " + (creationscript != null) + "...");

        if (creationscript != null)
            for (int i=0; i<creationscript.length; i++) {
                //Log.d(TAG, "onCreate() creationscript[" + i + "]=" + creationscript[i]);
                db.execSQL(creationscript[i]);
            }
        isNewVersion = true;

        //Log.d(TAG, "Done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.d(TAG, "Upgrading database " + name + " (" + oldVersion + " -> " + newVersion + ")...");

        dropAllTables(db);
        onCreate(db);

        //Log.d(TAG, "Done with upgrade");
    }

    // State management

    public boolean isNewVersion() {
        return isNewVersion;
    }

    public boolean exists() {
        //Log.d(TAG, "Checking if database " + name + " exists...");
        //lock.lock();

        SQLiteDatabase cnn2 = null;
        boolean result = false;

        try {
            String path = ctx.getDatabasePath(name).getAbsolutePath();
            Log.d(TAG, path);

            cnn2 = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            result = true;
        } catch (SQLiteException ex) {

        }

        if (cnn2 != null)
            cnn2.close();

        //Log.d(TAG, "Exists:" + (result ? "T" : "F"));
        return result;
    }

    public void open() {
        //Log.d(TAG, "Opening database " + name + "...");
        //lock.lock();

        if (cnn != null)
            return;

        String path = ctx.getDatabasePath(name).getAbsolutePath();
        cnn = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);

        //Log.d(TAG, "Done");
    }

    public void close() {
        //lock.lock();

        if (cnn != null) {
            cnn.close();
            cnn = null;
        }
    }

    public String getName() {
        return name;
    }

    public SQLiteDatabase getConnection() {
        return cnn;
    }

    public Context getContext() {
        return ctx;
    }

    // DML

    public boolean exists(String sqlString) {
        //lock.lock();
        Cursor c = null;
        int icount = 0;

        try {
            c = getQuery(sqlString);
            if (c != null) icount = c.getCount();
//            if (c == null || c.getCount() == 0)
//                return false;
//
//            return c.getCount() > 0;
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }

        return icount > 0;
    }

    public int count(String sqlString) {
        //Log.d(TAG, "Count: " + sqlString);
        //lock.lock();

        Cursor c = getQuery(sqlString);

        if (c == null || c.getCount() == 0)
            return 0;

        //Log.d(TAG, "Total: " + c.getCount());
        return c.getCount();
    }

    public Cursor getQuery(String sqlString) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Query: " + sqlString);
        return cnn.rawQuery(sqlString, null);
    }

    public Cursor getQuery(String sqlString, String[] selectionArgs) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Query: " + sqlString);
        return cnn.rawQuery(sqlString, selectionArgs);
    }

    public Cursor getRow(String sqlString) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Query: " + sqlString);
        Cursor c = cnn.rawQuery(sqlString, null);
        c.moveToFirst();

        return c;
    }

    public void insert(String tablename, ContentValues values) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Insert: " + tablename + " " + print(values))
        cnn.insert(tablename, null, values);
    }

    public int update(String tablename, ContentValues values, String whereClause) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Update: " + tablename + " " + print(values) + " WHERE " + whereClause);
        return cnn.update(tablename, values, whereClause, null);
    }

    public int update(String tablename, ContentValues values, String whereClause, String[] whereArgs) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Update: " + tablename + " " + print(values) + " WHERE " + whereClause);
        return cnn.update(tablename, values, whereClause, whereArgs);
    }
    public int delete(String tablename) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Delete: " + tablename);
        return cnn.delete(tablename, null, null);
    }

    public int delete(String tablename, String whereClause) {
        //lock.lock();

        if (cnn == null)
            open();

        //Log.d(TAG, "Delete: " + tablename + " WHERE " + whereClause);
        return cnn.delete(tablename, whereClause, null);
    }

    public SQLiteStatement compileStatement(String sql) {
        if (cnn == null)
            open();

        return cnn.compileStatement(sql);
    }

    public void beginTransaction() {
        cnn.beginTransaction();
    }

    public void setTransactionSuccessful() {
        cnn.setTransactionSuccessful();
    }

    public void endTransaction() {
        cnn.endTransaction();
    }

    // DDL

    public void dropAllTables(SQLiteDatabase db) {
        //Log.d(TAG, "Dropping all tables...");

        for (int i=0; i<tablenames.size(); i++) {
            //Log.d(TAG, "Dropping " + tablenames.get(i) + "...");
            db.execSQL("DROP TABLE IF EXISTS " + tablenames.get(i));
        }

        //Log.d(TAG, "Done");
    }

    public void dropAllTables() {
        //Log.d(TAG, "Dropping all tables...");

        for (int i=0; i<tablenames.size(); i++) {
            //Log.d(TAG, "Dropping " + tablenames.get(i) + "...");
            cnn.execSQL("DROP TABLE IF EXISTS " + tablenames.get(i));
        }

        //Log.d(TAG, "Done");
    }

    // Metadata

/*
0|cid||0||0
1|name||0||0
2|type||0||0
3|notnull||0||0
4|dflt_value||0||0
5|pk||0||0
 */

    public static class DbTableColumnInfo {
        public int columnIndex;
        public String name;
        public String type;
        public boolean isNotNull;
        public String defaultValue;
        public boolean isPrimaryKey;
    }

    public List<DbTableColumnInfo> getDbTableColumnInfo(String table) {
        String sql = "select * from pragma_table_info('" + table + "');";
        ArrayList<DbTableColumnInfo> result = new ArrayList();

        Cursor c = getQuery(sql);

        try {
            c.moveToFirst();

            while (!c.isAfterLast()) {
                DbTableColumnInfo info = new DbTableColumnInfo();

                int ix = 0;
                info.columnIndex = c.getInt(ix++);
                info.name = c.getString(ix++);
                info.type = c.getString(ix++);
                info.isNotNull = c.getInt(ix++) == 1;
                info.defaultValue = c.getString(ix++);
                info.isPrimaryKey = c.getInt(ix++) == 1;

                result.add(info);

                c.moveToNext();
            }
        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        } finally {
            try {if (c != null) c.close();}catch(Throwable t2){t2.printStackTrace();}
        }

        return result;
    }

    // Helpers

    private String print(ContentValues contentValues) {
        try {
            if (contentValues == null)
                return null;

            StringBuilder result = new StringBuilder();

            for (String key : contentValues.keySet())
                result.append(result.length() > 0 ? ", " : "").append(key).append(": ").append(contentValues.get(key));

            return result.toString();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return null;
    }

    public static void bindString(SQLiteStatement stmt, int index, String value)
    {
        if (value == null) stmt.bindNull(index);
        else stmt.bindString(index, value);
    }

    // ------------------------------ Nested classes, interfaces ----------------------------------------
    public static class TxControl {
        private boolean isAlreadyInTransaction = false;
        private boolean isDisableTransactions = false;

        public TxControl(boolean isAlreadyInTransaction, boolean isDisableTransactions) {
            this.isAlreadyInTransaction = isAlreadyInTransaction;
            this.isDisableTransactions = isDisableTransactions;
        }

        public boolean isAlreadyInTransaction() {
            return isAlreadyInTransaction;
        }

        public void setAlreadyInTransaction(boolean alreadyInTransaction) {
            isAlreadyInTransaction = alreadyInTransaction;
        }

        public boolean isDisableTransactions() {
            return isDisableTransactions;
        }

        public void setDisableTransactions(boolean disableTransactions) {
            isDisableTransactions = disableTransactions;
        }

        public boolean isUseTransaction() {
            return (!isAlreadyInTransaction && !isDisableTransactions);
        }

        public String toString() {
            return "isUseTransaction()=" + isUseTransaction() + ", isAlreadyInTransaction=" + isAlreadyInTransaction
                    + ", isDisableTransactions=" + isDisableTransactions;
        }
    }
}
