package com.dunwen.Bulider;

import android.text.TextUtils;

import com.dunwen.Beans.Id;
import com.dunwen.Beans.Property;
import com.dunwen.Beans.TableInfo;
import com.dunwen.Units.FieldUnit;
import com.dunwen.Units.TableUnits;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dun on 2016/1/15.
 * 拼凑SQL语句类
 */
public class SQLBulider {

    public static String queryTableIsExist(String tableName) {


        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(*) FROM sqlite_master WHERE type='table' AND name = ");
        sb.append("'" + tableName + "'");
        return sb.toString();
    }

    public static String creatTable(Class clazz) {
        Field idField = FieldUnit.getIdField(clazz);
        if (idField == null) {
            throw new RuntimeException("no id column");
        }


        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " +
                TableUnits.getTableName(clazz) + " (");

        String idColumnName = idField.getName();
        Class idtype = idField.getType();

        if (idtype == int.class || idtype == Integer.class
                || idtype == long.class || idtype == Long.class) {
            sb.append(idColumnName + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        } else {
            sb.append(idColumnName + " TEXT PRIMARY KEY,");
        }


        Field[] fields = clazz.getDeclaredFields();

        for (int i = 0,len = fields.length; i < len; i++) {
            Field currentField = fields[i];

            if (!FieldUnit.isBaseDateType(currentField)) {
                continue;
            }
            if (FieldUnit.isTransient(currentField)) {
                continue;
            }

            String columnName = FieldUnit.getColumnName(currentField);;
            Class type = currentField.getType();
            String typeColumnString = FieldUnit.getColumnTypeString(type);

            if (columnName.equals(idColumnName) || columnName.equals(idColumnName)) {
                continue;
            }

            sb.append(columnName+" ");
            sb.append(typeColumnString+" ");



            sb.append(",");

        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" )");

        return sb.toString();
    }

    public static String getLastId(){
        return "SELECT last_insert_rowid()";
    }

    public static String instertObject(Object object, TableInfo tableInfo){
        StringBuilder result = new StringBuilder();
        StringBuilder colSB = new StringBuilder();
        StringBuilder valSB = new StringBuilder();

        Collection<Property> collection = tableInfo.getPropertyMap().values();

        colSB.append("(");
        valSB.append("(");
        for (Property property : collection) {
            if(property instanceof Id){
                Class type = property.getFieldValueType();
                if(type == int.class || type == Integer.class){
//                    colSB.append(FieldUnit.getColumnName(property.getmField())+",");
//                    valSB.append("null"+",");
                }else{
                    String keyString = (String) FieldUnit.invokeMethod(property.getGetterMethod(),object);
                    if(TextUtils.isEmpty(keyString)){
                        throw new RuntimeException("hava you set your String key?");
                    }
                    valSB.append(keyString+",");
                }
                continue;
            }

            colSB.append("'"+FieldUnit.getColumnName(property.getmField())+"',");

            Method method = property.getGetterMethod();
            Object val = FieldUnit.invokeMethod(method,object);
            if(val == null){
                val = FieldUnit.getDefaultValue(property.getmField());
            }

            valSB.append("'"+(val==null?"":val)+"',");

        }
        valSB.deleteCharAt(valSB.length()-1);
        colSB.deleteCharAt(colSB.length()-1);
        colSB.append(")");
        valSB.append(")");


        result.append("INSERT INTO ");
        result.append(tableInfo.getTableName());
        result.append(" ");
        result.append(colSB.toString());
        result.append(" VALUES ");
        result.append(valSB.toString());


        return result.toString();
    }


    public static String dropTable(Class clazz){
        StringBuilder sb = new StringBuilder();

        String tableName = TableUnits.getTableName(clazz);
        sb.append("DROP TABLE ");
        sb.append(tableName);

        return sb.toString();
    }

    /**
     * @param clazz
     * @param condition 条件，比如 WHERE _ID = 1;ORDER BY 。。。
     * */
    public static String select(Class clazz,String condition){
        if(condition == null){
            condition = "";
        }
        String tableName = TableUnits.getTableName(clazz);
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(tableName);
        sb.append(" "+condition);
        return sb.toString();
    }

    public static String delete(TableInfo tableInfo,String condition){
        StringBuilder sb = new StringBuilder();
        if(condition == null){
            condition = "";
        }

        String tableName = tableInfo.getTableName();
        sb.append("DELETE FROM ");
        sb.append(tableName);
        sb.append(" ");
        sb.append(condition);

        return sb.toString();


    }


    /**
     * @param idValue id的值
     * */
    public static String deleteConditionForId(TableInfo tableInfo,Object idValue){
        if(idValue == null || tableInfo == null) return "";
        StringBuilder sb = new StringBuilder();

        Property id = tableInfo.getIdProperty();
        String idName = FieldUnit.getColumnName(id.getmField());


        sb.append(" WHERE ");
        sb.append(idName);
        sb.append(" = ");
        sb.append(" '"+idValue+"' ");
        return sb.toString();
    }

    /**
     * @param tableInfo
     * @param object 实体对象
     * */
    public static String deleteConditionForObject(TableInfo tableInfo,Object object){
        if(object == null || tableInfo == null) return "";
        StringBuilder sb = new StringBuilder();

        Property id = tableInfo.getIdProperty();
        String idName = FieldUnit.getColumnName(id.getmField());
        Method method = id.getGetterMethod();

        Object value = FieldUnit.invokeMethod(method, object);

        sb.append(" WHERE ");
        sb.append(idName);
        sb.append(" = ");
        sb.append(" '"+value+"' ");
        return sb.toString();
    }


    public static String updateForObject(TableInfo tableInfo,Object object){

        String tableName = tableInfo.getTableName();
        Field idFile = tableInfo.getIdProperty().getmField();
        String idColumnName = FieldUnit.getColumnName(idFile);
        Method idSetterMethod = tableInfo.getIdProperty().getGetterMethod();
        Object idValue = FieldUnit.invokeMethod(idSetterMethod, object);

        Collection<Property> propertyList = tableInfo.getPropertyMap().values();

        String idName = idFile.getName();
        StringBuilder VALsb = new StringBuilder();
        for (Property property : propertyList) {
            Field mField = property.getmField();

            if(mField.getName().equals(idName)){
                continue;
            }

            String columnName = FieldUnit.getColumnName(mField);
            Method getterMethod = property.getGetterMethod();
            Object value = FieldUnit.invokeMethod(getterMethod,object);

            VALsb.append(" ");
            VALsb.append(columnName);
            VALsb.append(" = '");
            VALsb.append(value);
            VALsb.append("',");
        }

        VALsb.deleteCharAt(VALsb.length()-1);

        String sqlString = "UPDATE " + tableName + " SET "+ VALsb.toString() + " WHERE "+idColumnName + " = "+" '"+idValue+"'";

        return sqlString;
    }



}
