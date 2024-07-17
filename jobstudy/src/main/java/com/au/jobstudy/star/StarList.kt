package com.au.jobstudy.star

import com.au.jobstudy.check.NameList
import com.au.module_android.simplelivedata.SafeLiveData
import com.au.module_android.utils.logd
import kotlinx.coroutines.delay

/**
 * @author allan
 * @date :2024/7/15 11:07
 * @description:
 */
object StarList {
    val allStarsLiveData = SafeLiveData<List<StarItemBean>>()

    private val bestStudents = listOf(NameList.BEST_STUDENT_1, NameList.BEST_STUDENT_2, NameList.BEST_STUDENT_3, NameList.BEST_STUDENT_4)
    private val goodStudents = listOf(
        NameList.GOOD_STUDENT_1,
        NameList.GOOD_STUDENT_2,
        NameList.GOOD_STUDENT_3,
        NameList.GOOD_STUDENT_4,
        NameList.GOOD_STUDENT_5,
        NameList.GOOD_STUDENT_6,
        NameList.GOOD_STUDENT_7,
        NameList.GOOD_STUDENT_8
    )
    private val normalStudents = listOf(
        NameList.NORMAL_STUDENT_1,
        NameList.NORMAL_STUDENT_2,
        NameList.NORMAL_STUDENT_3,
        NameList.NORMAL_STUDENT_4,
        NameList.NORMAL_STUDENT_5
    )

    private var isInited = false

    var myRank:Int = -1

    /**
     * 加载星星榜。现在是假数据。根据自己来生成。
     */
    suspend fun initData(myStarCt:Int, myDingCt:Int) {
        if (isInited) {
            return
        }

        isInited = true
        delay(0)

        val starList = mutableListOf<StarItemBean>()
        starList.add(StarItemBean(NameList.NAMES_JIANG_TJ, myStarCt, myDingCt))

        //给出随即的名次
        var bestStarCount = myStarCt
        var bestDingCount = myDingCt

        val otherStarCount = myStarCt - 2
        val otherDingCount = myDingCt - 2
        if (Math.random() > 0.6) { //第一名
            bestStarCount -= 1
            bestDingCount -= 1
        } else {
            bestStarCount += 1
            bestDingCount += 1
        }

        bestStudents.forEach {
            val delta = (Math.random() * 2).toInt()
            val delta2 = (Math.random() * 2).toInt()
            val starCount = if(bestStarCount - delta > 0) bestStarCount - delta else 0
            val favouriteCount = if(bestDingCount - delta2 > 0) bestDingCount - delta2 else 0
            starList.add(StarItemBean(it, starCount, favouriteCount))
        }

        goodStudents.forEach {
            val delta = (Math.random() * 3 + 0.5).toInt() + 1
            val delta2 = (Math.random() * 3 + 0.5).toInt() + 1
            val starCount = if(otherStarCount - delta > 0) otherStarCount - delta else 0
            val favouriteCount = if(otherDingCount - delta2 > 0) otherDingCount - delta2 else 0
            starList.add(StarItemBean(it, starCount, favouriteCount))
        }

        normalStudents.forEach {
            val delta = (Math.random() * 3 + 0.5).toInt() + 4
            val delta2 = (Math.random() * 3 + 0.5).toInt() + 6
            val starCount = if(otherStarCount - delta > 0) otherStarCount - delta else 0
            val favouriteCount = if(otherDingCount - delta2 > 0) otherDingCount - delta2 else 0
            starList.add(StarItemBean(it, starCount, favouriteCount))
        }

        starList.sortWith { o1, o2 ->
            if (o1.starCount > o2.starCount) {
                -1
            } else if (o1.starCount < o2.starCount) {
                1
            } else {
                if (o1.dingCount > o2.dingCount) {
                    -1
                } else if (o1.dingCount < o2.dingCount) {
                    1
                } else {
                    0
                }
            }
        }

        var myRankIndex = 0
        for (star in starList) {
            if (star.name == NameList.NAMES_JIANG_TJ) {
                break
            }
            myRankIndex++
        }
        myRank = myRankIndex + 1
        logd { "myRank $myRank" }

        allStarsLiveData.setValueSafe(starList)
    }
}