package sp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *     0 仅支持静态 final 常量（推荐）：
 *         - Java：static final String；
 *         - Kotlin：const val。
 *     1 支持模式0，并支持任意全局变量（todo）：
 *         - Java：String a = "str"; final String a = "str";
 *         - Kotlin：val 或 var。
 *     2 支持模式0、1，且可修改函数内部的字符串（todo）。
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface EncryptString {
    int mode() default 0;
}
