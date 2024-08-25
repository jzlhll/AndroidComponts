package com.allan.nested.recyclerview.page;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author au
 * Date: 2023/8/24
 * Description
 * todo 每个公司的api，分页加载的框架。需要自行修改。或者进行转换。
 */
public class ApiPageBean<E> {
    /**
     * 总共的pageSize
     */
    public int pages;
    /**
     * 当前的size
     */
    @Nullable
    public List<E> records;
    /**
     * 当前的size
     */
    public int current;
}
