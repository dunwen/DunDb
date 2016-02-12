package com.dunwen.Units;

import android.text.TextUtils;

import com.dunwen.annotation.Column;
import com.dunwen.annotation.ID;
import com.dunwen.annotation.Transient;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by dun on 2016/1/15.
 */
public class FieldUnit {
    public static boolean isBaseDateType(Field field) {
        Class<?> clazz = field.getType();
        return clazz.equals(String.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Date.class) ||
                clazz.equals(java.util.Date.class) ||
                clazz.equals(java.sql.Date.class) ||
                clazz.isPrimitive();
    }

    /**
     * 判断某个字段是否被数据库过滤
     */
    public static boolean isTransient(Field field) {
        Transient ann = field.getAnnotation(Transient.class);
        return ann != null;
    }

    public static Field getIdField(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Field result = null;
        for (int i = 0; i < fields.length; i++) {
            Field currentField = fields[i];
            ID idann = currentField.getAnnotation(ID.class);
            String name = currentField.getName();
            if (name.equals("_id") || name.equals("id") || idann != null) {
                return currentField;
            }
        }
        return result;
    }


    public static String getColumnName(Field field) {
        Column col = field.getAnnotation(Column.class);
        if (col != null && !TextUtils.isEmpty(col.column())) {
            return col.column();
        } else {
            return field.getName();
        }
    }

    public static String getColumnTypeString(Class<?> dataType) {
        if (dataType == int.class || dataType == Integer.class
                || dataType == long.class || dataType == Long.class) {
            return "INTEGER";
        } else if (dataType == float.class || dataType == Float.class
                || dataType == double.class || dataType == Double.class) {
            return "REAL";
        } else if (dataType == boolean.class || dataType == Boolean.class) {
            return "NUMERIC";
        }
        return "TEXT";
    }

    public static Class getFieldTypeClass(Field field) {
        return field.getType();
    }

    public static Method getSetterMethod(Class clazz, Field field) {
        String fieldName = field.getName();
        String methodName = "set" + fieldName;

        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            String currentMethodName = method.getName().toLowerCase();
            if (currentMethodName.equals(methodName)) {
                return method;
            }
        }
        throw new RuntimeException("Can not find setter Method for " + fieldName + " make sure you hava set it");
    }

    public static Method getGetterMethod(Class clazz, Field field) {
        String fieldName = field.getName();
        Class type = field.getType();
        String methodName;
        if (type == boolean.class || type == Boolean.class) {
            methodName = "is" + fieldName;
        } else {
            methodName = "get" + fieldName;
        }
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            String currentMethodName = method.getName().toLowerCase();
            if (currentMethodName.equals(methodName)) {
                return method;
            }
        }
        throw new RuntimeException("Can not find setter Method for " + fieldName + " make sure you hava set it");
    }

    /**
     * 执行指定方法
     *
     * @param method 指定方法；
     * @param o      执行对象
     */
    public static Object invokeMethod(Method method, Object o,Object... args) {

        try {
            return method.invoke(o,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("invoke method + " + method.getName() + " failure");
    }

    public static String getDefaultValue(Field field){
        Column column = field.getAnnotation(Column.class);
        if(column == null){
            return null;
        }
        return column.defaultValue();

    }

}