package com.au.module_android.init

import android.os.Build
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue

fun optimizeSpTask() {
    if (Build.VERSION.SDK_INT < 26) {
        reflectSPendingWorkFinishers()
    } else {
        reflectSFinishers()
    }
}

/**
 * 8.0以上 Reflect finishers
 *
 */
private fun reflectSFinishers() {
    try {
        val clz = Class.forName("android.app.QueuedWork")
        val field = clz.getDeclaredField("sFinishers")
        field.isAccessible = true
        val queue = field.get(clz) as? LinkedList<Runnable>
        if (queue != null) {
            val linkedListProxy = LinkedListProxy(queue)
            field.set(queue, linkedListProxy)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

/**
 * 8.0以下 Reflect pending work finishers
 */
private fun reflectSPendingWorkFinishers() {
    try {
        val clz = Class.forName("android.app.QueuedWork")
        val field = clz.getDeclaredField("sPendingWorkFinishers")
        field.isAccessible = true
        val queue = field.get(clz) as? ConcurrentLinkedQueue<Runnable>
        if (queue != null) {
            val proxy = ConcurrentLinkedQueueProxy(queue)
            field.set(queue, proxy)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

/**
 * 在8.0以上apply()中QueuedWork.addFinisher(awaitCommit), 需要代理的是LinkedList，如下：
 * # private static final LinkedList<Runnable> sFinishers = new LinkedList<>()
 */
private class LinkedListProxy(private val sFinishers: LinkedList<Runnable>) :
    LinkedList<Runnable>() {

    override fun add(element: Runnable): Boolean {
        return sFinishers.add(element)
    }

    override fun remove(element: Runnable): Boolean {
        return sFinishers.remove(element)
    }

    override fun isEmpty(): Boolean = true

    /**
     * 代理的poll()方法，永远返回空，这样UI线程就可以避免被阻塞，继续执行了
     */
    override fun poll(): Runnable? {
        return null
    }
}

/**
 * 在8.0以下代理
 * // The set of Runnables that will finish or wait on any async activities started by the application.
 * private static final ConcurrentLinkedQueue<Runnable> sPendingWorkFinishers = new ConcurrentLinkedQueue<Runnable>();
 */

private class ConcurrentLinkedQueueProxy(private val sPendingWorkFinishers: ConcurrentLinkedQueue<Runnable>) :
    ConcurrentLinkedQueue<Runnable>() {

    override fun add(element: Runnable?): Boolean {
        return sPendingWorkFinishers.add(element)
    }

    override fun remove(element: Runnable?): Boolean {
        return sPendingWorkFinishers.remove(element)
    }

    override fun isEmpty(): Boolean = true

    /**
     * 代理的poll()方法，永远返回空，这样UI线程就可以避免被阻塞，继续执行了
     */
    override fun poll(): Runnable? {
        return null
    }
}