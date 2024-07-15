package com.au.jobstudy.check

import com.au.jobstudy.check.star.Star
import com.au.module_android.simplelivedata.SafeLiveData
import kotlinx.coroutines.delay

/**
 * @author allan
 * @date :2024/7/15 11:07
 * @description:
 */
object StarList {
    val allStarsLiveData = SafeLiveData<List<Star>>()

    private val bestStudents = listOf("111", "222", "333", "444")
    private val goodStudents = listOf("aaa", "ccc", "e", "dfadf")
    private val normalStudents = listOf("zdf", "zxcz", "zcxczxc", "czcz")

    //给出随即的名次
    private val selfRank = if(Math.random() > 0.5) 1 else 0

    private var isInited = false

    /**
     * 加载星星榜。现在是假数据。根据自己来生成。
     */
    suspend fun initData(myStarCt:Int, myDingCt:Int) {
        if (isInited) {
            return
        }

        isInited = true
        delay(0)

        val my = Star(NameList.NAMES_JIANG_TJ, myStarCt, myDingCt)
        var myStarCount = myStarCt
        var myDingCount = myDingCt
        if (selfRank == 0) {
            myStarCount -= 1
            myDingCount -= 2
        }

        val starList = mutableListOf<Star>()
        starList.add(my)

        bestStudents.forEach {
            val delta = if(Math.random() > 0.5) 1 else 0
            val delta2 = if(Math.random() > 0.5) 1 else 0
            val starCount = if(myStarCount - delta > 0) myStarCount - delta else 0
            val favouriteCount = if(myDingCount - delta2 > 0) myDingCount - delta2 else 0
            starList.add(Star(it, starCount, favouriteCount))
        }
        goodStudents.forEach {
            val delta = (Math.random() * 3 + 0.5).toInt() + 1
            val delta2 = (Math.random() * 3 + 0.5).toInt() + 1
            val starCount = if(myStarCount - delta > 0) myStarCount - delta else 0
            val favouriteCount = if(myDingCount - delta2 > 0) myDingCount - delta2 else 0
            starList.add(Star(it, starCount, favouriteCount))
        }

        normalStudents.forEach {
            val delta = (Math.random() * 3 + 0.5).toInt() + 4
            val delta2 = (Math.random() * 3 + 0.5).toInt() + 4
            val starCount = if(myStarCount - delta > 0) myStarCount - delta else 0
            val favouriteCount = if(myDingCount - delta2 > 0) myDingCount - delta2 else 0
            starList.add(Star(it, starCount, favouriteCount))
        }

        starList.sortWith { o1, o2 ->
            if (o1.starCount > o2.starCount) {
                1
            } else if (o1.starCount < o2.starCount) {
                -1
            } else {
                if (o1.dingCount > o2.dingCount) {
                    1
                } else if (o1.dingCount < o2.dingCount) {
                    -1
                } else {
                    0
                }
            }
        }

        allStarsLiveData.setValueSafe(starList)
    }
}