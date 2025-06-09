package com.au.module_androidui.toast;

import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@StringDef({"success", "fail", "error", "warn", "info", "none"})
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.PARAMETER})
public @interface IconType {
}
