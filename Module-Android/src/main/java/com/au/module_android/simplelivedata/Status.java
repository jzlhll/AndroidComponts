package com.au.module_android.simplelivedata;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author au
 * Date: 2023/2/15
 * Description 设计运行的模式
 */
@IntDef({Status.RUNNING, Status.OVER_SUCCESS, Status.OVER_ERROR, Status.NONE, Status.PAGE_INIT_ERROR})
@Retention(RetentionPolicy.CLASS)
public @interface Status {
    int NONE = 0; //没有运行，空数据。
    int RUNNING = 10; //开始了。
    int OVER_SUCCESS = 1; //结束：成功。
    int OVER_ERROR = -1; //结束：失败。
    int PAGE_INIT_ERROR = -2; //初始化error
}
