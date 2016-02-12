package com.dunwen;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dunwen.Beans.TableInfo;
import com.dunwen.Bulider.SQLBulider;
import com.dunwen.Units.CursorUnit;
import com.dunwen.Units.FieldUnit;
import com.dunwen.Units.TableUnits;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by dun on 2016/1/15.
 * <p/>
 * compile 'io.reactivex:rxjava:1.1.0'
 */
public class DunDb {
    private Bulider mBulider = null;
    private static HashMap<String, TableInfo> tableInfoHashMap = new HashMap<>();
    private boolean isDebug = false;

    private final String TAG = "DunDb";
    private SQLiteDatabase db;

    public DunDb(Bulider mBulider) {
        if (mBulider.getmContext() == null) {
            throw new RuntimeException("Context is null");
        }
        if (mBulider == null) {
            throw new RuntimeException("Bulider is null");
        }

        db = new DbHelper(
                mBulider.getmContext(),
                mBulider.getDbName(),
                mBulider.getVersion(),
                mBulider.getmUpDateListener()
        ).getWritableDatabase();
        this.mBulider = mBulider;
        this.isDebug = mBulider.isDebug;
    }

    public Bulider getBulider() {
        return mBulider;
    }

    public static DunDb create(Context mContext, String dbName) {
        Bulider b = new Bulider(mContext);
        b.setDbName(dbName);
        return new DunDb(b);
    }

    public static DunDb create(Context mContext) {
        Bulider b = new Bulider(mContext);
        return new DunDb(b);
    }

    public static DunDb create(Context mContext, String dbName, int version, boolean isDebug, UpDateListener mUpDateListener) {
        Bulider b = new Bulider(mContext)
                .setDbName(dbName)
                .setDebug(isDebug)
                .setmUpDateListener(mUpDateListener)
                .setVersion(version);
        return new DunDb(b);
    }


    public void save(Object object){
        save(object, false);
    }

    public void save(Object object,boolean isUpdateId){
        if(object == null){
            return;
        }
        Class clazz = object.getClass();
        TableInfo tableInfo = getTableInfo(clazz, true);

        if(!tableInfo.isTableExist()){
            saveTable(tableInfo);
        }

        String sql = SQLBulider.instertObject(object, tableInfo);
        if(isDebug){
            Log.i(TAG, "save: " + sql);
        }
        executeSQL(sql);

        if(isUpdateId&&(tableInfo.getIdProperty().getFieldValueType() == int.class ||
                tableInfo.getIdProperty().getFieldValueType() == Integer.class)){
            Cursor c = executeSQLWithReturnCursor(SQLBulider.getLastId(),null);
            c.moveToNext();
            int index = c.getColumnIndex("last_insert_rowid()");
            int id = (int)(c.getLong(index));
            Method method = tableInfo.getIdProperty().getSetterMethod();
            FieldUnit.invokeMethod(method, object, id);
            c.close();
        }
    }


    public void saveTable(TableInfo tableInfo) {
        Class clazz = tableInfo.getClazz();
        String sqlString = SQLBulider.creatTable(clazz);

        if(isDebug){
            Log.i(TAG, "saveTable: " + sqlString);
        }
        executeSQL(sqlString);
        tableInfo.setTableExist(true);
    }

    public void dropTable(Class clazz){
        TableInfo tableInfo = tableInfoHashMap.get(clazz.getName());
        if(tableInfo!=null&&tableInfo.isTableExist()){
            deleteTable(clazz);
            return;
        }
        if(CheckTableIsExist(clazz)){
            deleteTable(clazz);
            return;
        }
    }


    /**
     * @param isCheckTableExist
     *              是否检查表是否存在，并保存到缓存
     * */
    private TableInfo getTableInfo(Class clazz,boolean isCheckTableExist){
        TableInfo tableInfo = tableInfoHashMap.get(clazz.getName());
        if(tableInfo == null){
            tableInfo = new TableInfo(clazz);
            if(isCheckTableExist){
                CheckTableIsExist(tableInfo);
                tableInfoHashMap.put(clazz.getName(),tableInfo);
            }

            return tableInfo;
        }else {
            return tableInfo;
        }
    }

    /**
     * 使用此方法删除表中数据时，Object的id不能为空
     * */
    public boolean delete(Object object){
        Class clazz = object.getClass();
        TableInfo tableInfo = getTableInfo(clazz, true);

        if(CheckIdIsNull(tableInfo, object)){
            Log.i(TAG, "update: id of object" + object.toString() + " is null");
            return false;
        }

        return delete(tableInfo,SQLBulider.deleteConditionForObject(tableInfo, object));

    }



    public boolean delete(Class clazz,Object id){
        if(clazz==null||id==null){
            return false;
        }

        TableInfo tableInfo =  getTableInfo(clazz, true);


        return delete(tableInfo,SQLBulider.deleteConditionForId(tableInfo, id));
    }

    public boolean delete(Class clazz,String condition){
        TableInfo tableInfo =  getTableInfo(clazz, true);
        return delete(tableInfo,condition);
    }

    private boolean delete(TableInfo tableInfo,String condition){
        if(!tableInfo.isTableExist()){
            Log.i(TAG, "delete: table for " + tableInfo.getClazz().getName() +" is not exist");
            return false;
        }

        try{
            String sqlString = SQLBulider.delete(tableInfo, condition);
            if(isDebug){
                Log.i(TAG, "delete: "+sqlString);
            }
            executeSQL(sqlString);
            return true;
        }catch (Exception e){
            return false;
        }
    }


    /**
     * 更新的object id 必须不为空
     * */
    public boolean update(Object object){
        TableInfo tableInfo = getTableInfo(object.getClass(), true);
        if(CheckIdIsNull(tableInfo,object)){
            Log.i(TAG, "update: id of object" + object.toString() +" is null" );
            return false;
        }

        String sqlString = SQLBulider.updateForObject(tableInfo,object);

        if(isDebug){
            Log.i(TAG, "update: "+ sqlString);
        }

        try{
            executeSQL(sqlString);
        }catch (Exception e){
            Log.i(TAG, "update: undate object>>"+object.toString()+" failure");
            return false;
        }
        return true;
    }


    private boolean CheckIdIsNull(TableInfo tableInfo, Object object) {
        Method getMethod = tableInfo.getIdProperty().getGetterMethod();
        Object o = FieldUnit.invokeMethod(getMethod, object);
        return o == null;
    }

    public <T> List<T> findAll(Class<T> clazz){
        List<T> list = new ArrayList<>();

        TableInfo tableInfo = getTableInfo(clazz,true);

        if(!tableInfo.isTableExist()){
            Log.i(TAG, "findAll: table for" + clazz.getName()+" is not exist");
            return list;
        }

        String sqlString = SQLBulider.select(clazz,null);

        if(isDebug){
            Log.i(TAG, "findAll: "+sqlString);
        }

        Cursor c = executeSQLWithReturnCursor(sqlString,null);
        while(c.moveToNext()){
            T t = CursorUnit.getInstanceFromCursor(clazz,tableInfo,c);
            list.add(t);
        }
        return list;
    }

    private void deleteTable(Class clazz) {
        String sqlString = SQLBulider.dropTable(clazz);
        if (isDebug){
            Log.i(TAG, "deleteTable: "+sqlString);
        }
        executeSQL(sqlString);
    }

    private boolean CheckTableIsExist(TableInfo tableInfo) {
        Class clazz = tableInfo.getClazz();
        boolean isExist= CheckTableIsExist(clazz);
        tableInfo.setTableExist(isExist);
        return isExist;
    }

    private boolean CheckTableIsExist(Class clazz) {

        String sqlString = SQLBulider.queryTableIsExist(TableUnits.getTableName(clazz));

        if (isDebug) {
            Log.i(TAG, "saveTable :" + sqlString);
        }

        Cursor cursor = executeSQLWithReturnCursor(sqlString,new String[]{});
        cursor.moveToNext();
        int isExist = cursor.getInt(cursor.getColumnIndex("count(*)"));
        cursor.close();
        return isExist != 0;
    }


    private Cursor executeSQLWithReturnCursor(String sqlString, String[] args) {
        return db.rawQuery(sqlString, args);
    }

    private void executeSQL(String sqlString) {
        db.execSQL(sqlString);
    }


    public static class Bulider {
        private String dbName = "DunDb.db";
        private int version = 1;
        private boolean isDebug = true;
        private Context mContext = null;
        private UpDateListener mUpDateListener = null;

        public Bulider(Context mContext) {
            this.mContext = mContext.getApplicationContext();
        }

        public String getDbName() {
            return dbName;
        }

        public Bulider setDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public int getVersion() {
            return version;
        }

        public Bulider setVersion(int version) {
            this.version = version;
            return this;
        }

        public boolean isDebug() {
            return isDebug;
        }

        public Bulider setDebug(boolean debug) {
            isDebug = debug;
            return this;
        }

        public Context getmContext() {
            return mContext;
        }


        public UpDateListener getmUpDateListener() {
            return mUpDateListener;
        }

        public Bulider setmUpDateListener(UpDateListener mUpDateListener) {
            this.mUpDateListener = mUpDateListener;
            return this;
        }

        public DunDb create() {
            return new DunDb(this);
        }

    }

    public interface UpDateListener {
        void onUpdate(SQLiteDatabase db);
    }

    private class DbHelper extends SQLiteOpenHelper {
        UpDateListener mUpDateListener = null;

        public DbHelper(Context context, String name, int version, UpDateListener mUpDateListener) {
            super(context, name, null, version);
            this.mUpDateListener = mUpDateListener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(mUpDateListener!=null){
                mUpDateListener.onUpdate(db);
            }
        }
    }


}
