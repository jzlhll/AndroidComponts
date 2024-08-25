package com.allan.nested.mgr

/**
 * @param pullDownTriggerValue 触发下拉刷新偏移量。默认传入 80.dp
 * @param realMoveRatio 真实的移动比例偏差。一般不用设置。
 * @param endOffsetY 设定rcv最终停止的高度偏移， 如果是Smooth模式或者Fake则没用。
 */
open class Params(pullDownTriggerValue:Int,
                  realMoveRatio:Float,
                  val endOffsetY:Float,
) : SmoothParams(pullDownTriggerValue, realMoveRatio)

open class SmoothParams(val pullDownTriggerValue:Int,
                        val realMoveRatio:Float)