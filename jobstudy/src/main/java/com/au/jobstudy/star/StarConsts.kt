package com.au.jobstudy.star

import com.au.jobstudy.check.AppDatabase
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.check.NameList
import com.au.jobstudy.check.bean.StarEntity
import com.au.module_cached.AppDataStore
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import kotlinx.coroutines.delay

/**
 * @author allan
 * @date :2024/7/15 11:07
 * @description:
 */
object StarConsts {
    val allStarsLiveData = NoStickLiveData<List<StarItemBean>>()

    val mineStarData = NoStickLiveData<StarEntity>()

    private val bestStudents = mapOf(
        NameList.NAMES_JIANG_TJ to true,
        NameList.BEST_STUDENT_1 to true,
        NameList.BEST_STUDENT_2 to true,
        NameList.BEST_STUDENT_3 to true)
    private val goodStudents = mapOf(
        NameList.GOOD_STUDENT_1 to true,
        NameList.GOOD_STUDENT_2 to true,
        NameList.GOOD_STUDENT_3 to true,
        NameList.GOOD_STUDENT_4 to true,
        NameList.GOOD_STUDENT_5 to true,
        NameList.GOOD_STUDENT_6 to true,
        NameList.GOOD_STUDENT_7 to true,
        NameList.GOOD_STUDENT_8 to true
    )
    private val normalStudents = mapOf(
        NameList.NORMAL_STUDENT_1 to true,
        NameList.NORMAL_STUDENT_2 to true,
        NameList.NORMAL_STUDENT_3 to true,
        NameList.NORMAL_STUDENT_4 to true,
        NameList.NORMAL_STUDENT_5 to true
    )

    private const val INIT_STAR_LIST_VERSION = 1

    var myRank = -1

    suspend fun onlyInitOnce() {
        delay(0)
        //如果version不同，名单变化，则会进行更新。
        if (AppDataStore.read("initStarList", -1) != INIT_STAR_LIST_VERSION) {
            AppDatabase.db.runInTransaction {
                val dao = AppDatabase.db.getStarDao()
                val starList = dao.queryAll()
                val savedStarNames = starList.map { it.name }
                bestStudents.forEach { (k, v) ->
                    if (!savedStarNames.contains(k)) {
                        dao.insert(StarEntity(k, 0, 0, 0))
                    }
                }
                goodStudents.forEach { (k, v) ->
                    if (!savedStarNames.contains(k)) {
                        dao.insert(StarEntity(k, 0, 0, 0))
                    }
                }
                normalStudents.forEach { (k, v) ->
                    if (!savedStarNames.contains(k)) {
                        dao.insert(StarEntity(k, 0, 0, 0))
                    }
                }
            }

            AppDataStore.save("initStarList", INIT_STAR_LIST_VERSION)
        }
    }

    fun dbStarEntity(name:String) : StarEntity {
        val starEntities = AppDatabase.db.getStarDao().queryDingByName(name)
        val starEntity = if (starEntities.isEmpty()) {
            StarEntity(NameList.NAMES_JIANG_TJ, 0, 0, 0)
        } else {
            starEntities[0]
        }
        return starEntity
    }

    /**
     * 更新某人的某天是否已经顶过了。
     */
    fun updateNameDing(name:String, day:Int) {
        Globals.mainScope.launchOnThread {
            val entity = dbStarEntity(name)
            entity.dingDay = day
            entity.dingCount += 1
            AppDatabase.db.getStarDao().insert(entity)
            checkIfMineEntityNotify(entity)
        }
    }

    private fun checkIfMineEntityNotify(entity: StarEntity) {
        logd { "checkIfMine ${entity.name}" }
        if (entity.name == NameList.NAMES_JIANG_TJ) {
            mineStarData.setValueSafe(entity)
        }
    }

    fun updateNameStar(name:String) {
        Globals.mainScope.launchOnThread {
            val entity = dbStarEntity(name)
            entity.starCount += 1
            AppDatabase.db.getStarDao().insert(entity)
            checkIfMineEntityNotify(entity)
        }
    }

    suspend fun fakeUpdateStudentsDingCount() {
        delay(0)
        AppDatabase.db.runInTransaction{
            val dao = AppDatabase.db.getStarDao()
            val allExistStarEntities = dao.queryAll()
            val mineStar = dbStarEntity(NameList.NAMES_JIANG_TJ)
            val myStarCt = mineStar.starCount

            val eachDayUpdateDing = (Math.random() * 5 + 6).toInt()

            mineStar.dingCount += eachDayUpdateDing

            dao.insert(mineStar)

            //给出随即的名次
            var bestStarCount = myStarCt
            if (Math.random() > 0.6) { //第一名
                bestStarCount -= 1
            } else {
                bestStarCount += 1
            }

            val otherStarCount = myStarCt - 2
            val eachDayUpdateDingBest = eachDayUpdateDing
            val eachDayUpdateDingOther = eachDayUpdateDing - 2

            allExistStarEntities.forEach {
                if (bestStudents.containsKey(it.name)) {
                    val delta = (Math.random() * 2).toInt()
                    val delta2 = (Math.random() * 2).toInt()
                    it.starCount = if(bestStarCount - delta > 0) bestStarCount - delta else 0
                    it.dingCount += if(eachDayUpdateDingBest - delta2 > 0) eachDayUpdateDingBest - delta2 else 0
                    dao.insert(it)
                } else if (goodStudents.containsKey(it.name)) {
                    val delta = (Math.random() * 3 + 0.5).toInt() + 1
                    val delta2 = (Math.random() * 3 + 0.5).toInt() + 1
                    it.starCount = if(otherStarCount - delta > 0) otherStarCount - delta else 0
                    it.dingCount += if(eachDayUpdateDingOther - delta2 > 0) eachDayUpdateDingOther - delta2 else 0
                    dao.insert(it)
                } else if (normalStudents.containsKey(it.name)) {
                    val delta = (Math.random() * 3 + 0.5).toInt() + 4
                    val delta2 = (Math.random() * 3 + 0.5).toInt() + 6
                    it.starCount = if(otherStarCount - delta > 0) otherStarCount - delta else 0
                    it.dingCount += if(eachDayUpdateDingOther - delta2 > 0) eachDayUpdateDingOther - delta2 else 0
                    dao.insert(it)
                }
            }
        }
    }

    /**
     * 加载星星榜。现在是假数据。根据自己来生成。
     */
    suspend fun initData() {
        delay(0)

        val curDay = CheckConsts.currentDay()
        val allStars = AppDatabase.db.getStarDao().queryAll()

        allStars.find { it.name == NameList.NAMES_JIANG_TJ }?.let {
            checkIfMineEntityNotify(it)
        }

        val starList = allStars
            .map { StarItemBean(it.name, it.starCount, it.dingCount, it.dingDay == curDay) }.toMutableList()

        starList.sortWith { o1, o2 ->
            if (o1.starNum > o2.starNum) {
                -1
            } else if (o1.starNum < o2.starNum) {
                1
            } else {
                if (o1.dingNum > o2.dingNum) {
                    -1
                } else if (o1.dingNum < o2.dingNum) {
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

        allStarsLiveData.setValueSafe(starList)
    }
}