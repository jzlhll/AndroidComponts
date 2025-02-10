package com.au.module_nested.mgr

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import com.au.module_android.utils.dp

/**
 * 用于将recyclerView的item如果某个item卡在一半，则还原它。
 *  GallerySnapHelper().attachToRecyclerView(rcv) 即可。
 */
class GallerySnapHelper : SnapHelper() {
    private var mRecyclerView: RecyclerView? = null
    private var mHorizontalHelper: OrientationHelper? = null
    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        mRecyclerView = recyclerView
        super.attachToRecyclerView(recyclerView)
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }
        return out
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding - 10.dp
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return findStartView(layoutManager, getHorizontalHelper(layoutManager))
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        if (layoutManager !is ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val currentPosition = layoutManager.getPosition(currentView)
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        val vectorProvider = layoutManager as ScrollVectorProvider
        // deltaJumps sign comes from the velocity which may not match the order of children in
        // the LayoutManager. To overcome this, we ask for a vector from the LayoutManager to
        // get the direction.
        val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            ?: // cannot get a vector for the given position.
            return RecyclerView.NO_POSITION

        //在松手之后,列表最多只能滚多一屏的item数
//        int deltaThreshold = layoutManager.getWidth() / getHorizontalHelper(layoutManager).getDecoratedMeasurement(currentView);
        val deltaThreshold = 2
        var hDeltaJump: Int
        if (layoutManager.canScrollHorizontally()) {
            hDeltaJump = estimateNextPositionDiffForFling(
                layoutManager,
                getHorizontalHelper(layoutManager), velocityX, 0
            )
            if (hDeltaJump > deltaThreshold) {
                hDeltaJump = deltaThreshold
            }
            if (hDeltaJump < -deltaThreshold) {
                hDeltaJump = -deltaThreshold
            }
            if (vectorForEnd.x < 0) {
                hDeltaJump = -hDeltaJump
            }
        } else {
            hDeltaJump = 0
        }
        if (hDeltaJump == 0) {
            return RecyclerView.NO_POSITION
        }
        var targetPos = currentPosition + hDeltaJump
        if (targetPos < 0) {
            targetPos = 0
        }
        if (targetPos >= itemCount) {
            targetPos = itemCount - 1
        }
        return targetPos
    }

    private fun estimateNextPositionDiffForFling(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper, velocityX: Int, velocityY: Int
    ): Int {
        val distances = calculateScrollDistance(velocityX, velocityY)
        val distancePerChild = computeDistancePerChild(layoutManager, helper)
        if (distancePerChild <= 0) {
            return 0
        }
        val distance = distances[0]
        return if (distance > 0) {
            Math.floor((distance / distancePerChild).toDouble()).toInt()
        } else {
            Math.ceil((distance / distancePerChild).toDouble()).toInt()
        }
    }

    private fun computeDistancePerChild(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): Float {
        var minPosView: View? = null
        var maxPosView: View? = null
        var minPos = Int.MAX_VALUE
        var maxPos = Int.MIN_VALUE
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return INVALID_DISTANCE
        }
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val pos = layoutManager.getPosition(child!!)
            if (pos == RecyclerView.NO_POSITION) {
                continue
            }
            if (pos < minPos) {
                minPos = pos
                minPosView = child
            }
            if (pos > maxPos) {
                maxPos = pos
                maxPosView = child
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE
        }
        val start = Math.min(
            helper.getDecoratedStart(minPosView),
            helper.getDecoratedStart(maxPosView)
        )
        val end = Math.max(
            helper.getDecoratedEnd(minPosView),
            helper.getDecoratedEnd(maxPosView)
        )
        val distance = end - start
        return if (distance == 0) {
            INVALID_DISTANCE
        } else 1f * distance / (maxPos - minPos + 1)
    }

    @Deprecated("Deprecated in Java")
    override fun createSnapScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller? {
        return if (layoutManager !is ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView!!.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                val snapDistances =
                    calculateDistanceToFinalSnap(mRecyclerView!!.layoutManager!!, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }
    }

    private fun getHorizontalHelper(
        layoutManager: RecyclerView.LayoutManager
    ): OrientationHelper {
        if (mHorizontalHelper == null || mHorizontalHelper!!.layoutManager !== layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }

    private fun findStartView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        return if (layoutManager is LinearLayoutManager) {
            val firstChildPosition =
                layoutManager.findFirstVisibleItemPosition()
            if (firstChildPosition == RecyclerView.NO_POSITION) {
                return null
            }
            if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                return null
            }
            val firstChildView = layoutManager.findViewByPosition(firstChildPosition)
            if (helper.getDecoratedEnd(firstChildView) >= helper.getDecoratedMeasurement(
                    firstChildView
                ) / 2 && helper.getDecoratedEnd(firstChildView) > 0
            ) {
                firstChildView
            } else {
                layoutManager.findViewByPosition(firstChildPosition + 1)
            }
        } else {
            null
        }
    }

    companion object {
        private const val INVALID_DISTANCE = 1f
        private const val MILLISECONDS_PER_INCH = 30f
    }
}