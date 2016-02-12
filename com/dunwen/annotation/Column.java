package com.dunwen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by dun on 2016/1/15.
 *
 * 属性，
 * column 数据库中的列名称
 * defaultValue 改列的默认值
 *
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    public String column() default "";
    public String defaultValue() default "";
}
