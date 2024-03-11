package com.au.aulitesql.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author au
 * @date :2023/11/10 14:51
 * @description: 或者使用trasient标记
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AuIgnore {
}
