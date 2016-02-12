package com.dunwen.Beans;

import android.text.TextUtils;

import com.dunwen.Units.FieldUnit;
import com.dunwen.Units.TableUnits;
import com.dunwen.annotation.Column;
import com.dunwen.annotation.Table;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by dun on 2016/1/15.
 */
public class TableInfo {
    private String tableName;
    private Class clazz;
    private boolean isTableExist = false;

    private HashMap<String,Property> PropertyMap = new HashMap<>();

    public TableInfo(Object object) {
       this(object.getClass());
    }

    public TableInfo(Class clazz){
        this.clazz = clazz;
        this.tableName = TableUnits.getTableName(clazz);
        getAllProperty();
    }


    /**
     * setter getter 命名必须规范。推荐采用系统生成的set get
     * */
    private void getAllProperty() {
        Field[] fields = this.clazz.getDeclaredFields();
        Id id = getIdProperty();
        PropertyMap.put(id.getmField().getName(),id);
        Field idField = id.getmField();


        for(int i = 0,length = fields.length;i<length;i++){
            Field currentField = fields[i];

            if(!FieldUnit.isBaseDateType(currentField)){
                continue;
            }

            if(idField.getName().equals(currentField.getName())){
                continue;
            }

            if(FieldUnit.isTransient(currentField)){
                continue;
            }


            Property currentProperty = new Property();
            currentProperty.setmField(currentField);
            currentProperty.setFieldValueType(FieldUnit.getFieldTypeClass(currentField));
            currentProperty.setSetterMethod(FieldUnit.getSetterMethod(clazz,currentField));
            currentProperty.setGetterMethod(FieldUnit.getGetterMethod(clazz,currentField));

            PropertyMap.put(currentField.getName(),currentProperty);

        }

    }


    public HashMap<String, Property> getPropertyMap() {
        return PropertyMap;
    }


    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isTableExist() {
        return isTableExist;
    }

    public void setTableExist(boolean tableExist) {
        isTableExist = tableExist;
    }

    public Id getIdProperty() {
        Id id = new Id();
        Field f = FieldUnit.getIdField(clazz);
        id.setmField(f);
        id.setFieldValueType(FieldUnit.getFieldTypeClass(f));
        id.setSetterMethod(FieldUnit.getSetterMethod(clazz,f));
        id.setGetterMethod(FieldUnit.getGetterMethod(clazz,f));
        return id;
    }
}
