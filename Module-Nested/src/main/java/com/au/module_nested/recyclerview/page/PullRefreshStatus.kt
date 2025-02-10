package com.au.module_nested.recyclerview.page

/**
 * author: allan
 * Time: 2022/11/30
 * Desc:
 */
enum class PullRefreshStatus {
    /**
     * 不在下拉状态或者底部加载中
     */
    Normal,

    /**
     * 下拉刷新中
     */
    Refreshing,

    /**
     * 正在底部加载中
     */
    LoadingMore,
}