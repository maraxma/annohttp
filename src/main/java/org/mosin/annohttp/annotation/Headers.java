package org.mosin.annohttp.annotation;

import java.lang.annotation.*;

/**
 * 声明一个方法参数作为请求的请求头。
 * <p>只能接受如下的类型：</p>
 * <ul>
 *     <li>Map&lt;String, String&gt;：每个 Entry 指定请求头名称和其值。</li>
 *     <li>String[]：每个元素指定 XXX: XXX。</li>
 * </ul>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Headers {

}
