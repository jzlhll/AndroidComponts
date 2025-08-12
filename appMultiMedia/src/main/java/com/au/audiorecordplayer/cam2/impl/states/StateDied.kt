package com.au.audiorecordplayer.cam2.impl.states

import com.au.audiorecordplayer.cam2.impl.AbstractStateBase
import com.au.audiorecordplayer.cam2.impl.MyCamManager

/**
 * 一个特例，我只想用这个类来描述camera 没有open或者died的状态
 */
class StateDied(mgr: MyCamManager) : AbstractStateBase(mgr) {
    override fun step0_createSurfaces() {
    }
}
