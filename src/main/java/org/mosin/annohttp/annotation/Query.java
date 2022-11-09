package org.mosin.annohttp.annotation;

import java.lang.annotation.*;

/**
 * 声明一个方法参数作为请求的查询参数（单个）。
 * <p>只能接受String。
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Query {
    String value();
}
