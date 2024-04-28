package com.allan.androidlearning.alg;

public class AdcFilter {
    public static float clipping_filter_run(ClippingFilter object) {
        if(Math.abs(object.in_data - object.old_data) < ClippingFilter.max_deifference_value) {
            object.out_data = object.old_data;//让旧值生效
        } else {
            object.old_data = object.in_data;//新值变旧值
            object.out_data = object.in_data;//让新值生效
        }
        return(object.out_data);
    }
}

