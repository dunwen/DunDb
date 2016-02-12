package com.dunwen.Units;

import android.text.TextUtils;

import com.dunwen.annotation.Table;

/**
 * Created by dun on 2016/1/15.
 */
public class TableUnits {
    public static String getTableName(Class clazz){
        Table t = (Table) clazz.getAnnotation(Table.class);
        if(t!=null){
            String name = t.name();
            if(!TextUtils.isEmpty(name)){
                return name;
            }
        }
       return clazz.getName().replace('.','_');
    }
}
