package com.dunwen.Units;

import android.database.Cursor;

import com.dunwen.Beans.Property;
import com.dunwen.Beans.TableInfo;

import net.tsz.afinal.utils.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by dun on 2016/1/15.
 */
public class CursorUnit {

    public static <T> T getInstanceFromCursor(Class<T> clazz,TableInfo tableInfo, Cursor c){
        Collection<Property> collection = tableInfo.getPropertyMap().values();
        T t = null;
        try {
            t = clazz.newInstance();

            for (Property property : collection) {

                Method setterMethod = property.getSetterMethod();
                Field mField = property.getmField();
                String columnName = FieldUnit.getColumnName(mField);
                String value = c.getString(c.getColumnIndex(columnName));
                Class ValueType = property.getFieldValueType();
                setValue(t,setterMethod,ValueType,value);

            }

        } catch (InstantiationException e) {
         } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }

    private static void setValue(Object receiver,Method set,Class dataType,String value){
        if(receiver!=null&&set!=null&&value!=null){
            try {
                if (dataType == String.class) {
                    set.invoke(receiver, value.toString());
                } else if (dataType == int.class || dataType == Integer.class) {
                    set.invoke(receiver, value == null ? (Integer) null : Integer.parseInt(value.toString()));
                } else if (dataType == float.class || dataType == Float.class) {
                    set.invoke(receiver, value == null ? (Float) null: Float.parseFloat(value.toString()));
                } else if (dataType == double.class || dataType == Double.class) {
                    set.invoke(receiver, value == null ? (Double) null: Double.parseDouble(value.toString()));
                } else if (dataType == long.class || dataType == Long.class) {
                    set.invoke(receiver, value == null ? (Long) null: Long.parseLong(value.toString()));
                } else if (dataType == java.util.Date.class || dataType == java.sql.Date.class) {
                    set.invoke(receiver, value == null ? (Date) null: FieldUtils.stringToDateTime(value.toString()));
                } else if (dataType == boolean.class || dataType == Boolean.class) {
                    set.invoke(receiver, value == null ? (Boolean) null: "1".equals(value.toString()));
                } else {
                    set.invoke(receiver, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
