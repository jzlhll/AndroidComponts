package com.au.jobstudy.star

/**
 * @author allan
 * @date :2024/7/15 10:59
 * @description:
 */

interface IStarBean
data class StarItemBean(val name:String, var starNum:Int, var dingNum:Int, var isDing:Boolean? = null) : IStarBean
data class StarHeadBean(val html:String) : IStarBean
class StarMarkupBean():IStarBean