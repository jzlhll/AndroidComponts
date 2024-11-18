package com.allan.androidlearning.utils

/**
 * @author allan
 * @date :2024/11/14 10:10
 * @description:
 */
class HardWorkTest {
    var count = 4
    private val cycle = 999999

    fun run() : String{
        var time = System.currentTimeMillis()
        var s = "ss"
        var index = 0

        while (count-- >= 0) {
            while(index++ < cycle) {
                s += index
                s = s.substring(0, 2)
            }
            index = 0
        }

        time = System.currentTimeMillis() - time
        return "Time: $time ms"
    }
}