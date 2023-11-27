package com.au.jobstudy.bean

data class CheckupMode(val type:Int, val min:Int, val max:Int)
class CheckupDescMode(val desc:String, val modes:Array<CheckupMode>)