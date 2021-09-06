package org.huihui.supercamera.library.camera.record

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/29 10:05
 */
interface IRecorder {

    fun startRecord(path: String, videoParam: VideoRecorder.VideoParam)

    fun stopRecord()

    fun videoFrameAvailable(texureId: Int)

    interface RecorderListener {

        fun onRecordStart()

        fun onRecordDuration(duration: Long)

        fun onRecordEnd()
    }
}