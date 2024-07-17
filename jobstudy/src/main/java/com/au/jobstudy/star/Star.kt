package com.au.jobstudy.star

/**
 * @author allan
 * @date :2024/7/15 10:59
 * @description:
 */

interface IStarBean
data class StarItemBean(val name:String, val starCount:Int, val dingCount:Int, var isDing:Boolean? = null) : IStarBean
data class StarHeadBean(val html:String) : IStarBean
class StarMarkupBean():IStarBean