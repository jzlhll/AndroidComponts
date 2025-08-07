package com.au.audiorecordplayer.recorder

class RecordWrap(private val record: ISimpleRecord) : IRecord {
    override fun start() {
        record.start()
        mOb?.onRecordStart()
    }

    override fun stop() {
        record.stop()
        mOb?.onRecordStop(currentFilePath)
    }

    override fun isRecording(): Boolean {
        val r = record.isRecording
        return r
    }

    override fun resume() {
        if (record is IRecord) {
            record.resume()
            mOb?.onRecordResume()
        } else {
            throw UnsupportedOperationException("resume not support");
        }
    }

    override fun pause() {
        if (record is IRecord) {
            record.pause()
            mOb?.onRecordPause()
        } else {
            throw UnsupportedOperationException("pause not support");
        }
    }

    override fun getCurrentFilePath(): String? {
        return record.getCurrentFilePath()
    }

    private var mOb : IRecordObserver? = null

    fun setRecordOb(ob: IRecordObserver) {
        mOb = ob
    }
}