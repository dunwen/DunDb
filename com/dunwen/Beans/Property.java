package com.dunwen.Beans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by dun on 2016/1/15.
 */
public class Property {
    private Class fieldValueType;
    private Method setterMethod;
    private Method getterMethod;
    private Field mField;

    public Property() {
    }

    public Field getmField() {
        return mField;
    }

    public void setmField(Field mField) {
        this.mField = mField;
    }

    public Class getFieldValueType() {
        return fieldValueType;
    }

    public void setFieldValueType(Class fieldValueType) {
        this.fieldValueType = fieldValueType;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public void setSetterMethod(Method setterMethod) {
        this.setterMethod = setterMethod;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public void setGetterMethod(Method getterMethod) {
        this.getterMethod = getterMethod;
    }

}
