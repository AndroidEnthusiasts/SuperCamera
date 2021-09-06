package org.huihui.supercamera.library.camera.record

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import org.huihui.supercamera.library.camera.record.CameraRecoder.RecordType
import org.huihui.supercamera.library.util.lock
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/29 10:51
 */
class CameraRecoder : IRecorder, AVRecorderListener {
    companion object {
        const val TAG = "CameraRecoder"
    }

    private var audioRecorder = AudioRecorder()
    private var audioTrackId = -1
    private var videoRecorder = VideoRecorder()
    private var videoTrackId = -1

    private var muxer: MediaMuxer? = null

    private var lock = ReentrantLock()
    private var condition = lock.newCondition()

    private var recording = false
    private var muxerStart = false
    private var stoping = false

    var savePath = ""

    init {
        audioRecorder.recorderListener = this
        videoRecorder.recorderListener = this
    }

    override fun startRecord(path: String, videoParam: VideoRecorder.VideoParam) {
        savePath = path
        if (savePath.isEmpty()) {
            return
        }
        lock(lock) {
            if (!recording) {
                recording = true
                audioRecorder.start()
                videoRecorder.start(videoParam)
                muxer?.apply {
                    stop()
                    release()
                    muxer = null
                }
                muxer = MediaMuxer(
                    path,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )
            }
        }

    }

    private fun waitForStop() {
        lock(lock) {
            while (stoping) {
                condition.await(100, TimeUnit.MILLISECONDS)
            }
        }
    }

    override fun stopRecord() {
        Log.e(TAG, "stopRecord start")
        lock(lock) {
            if (recording) {
                audioRecorder.stop(false)
                videoRecorder.stop(false)
                audioTrackId = -1
                videoTrackId = -1
                stoping = true
                recording = false
            }
        }
        waitForStop()
        Log.e(TAG, "stopRecord finish")
    }

    fun release() {
        muxer?.apply {
            Log.e(TAG, "release stop")
            stop()
            Log.e(TAG, "release release")
            release()
            muxer = null
        }
    }

    override fun videoFrameAvailable(texureId: Int) {
        videoRecorder.frameAvaliable(texureId)
    }

    override fun onRecordStart(type: RecordType, format: MediaFormat) {
        lock(lock) {
            if (recording) {
                muxer?.apply {
                    if (type == RecordType.AUDIO) {
                        audioTrackId = addTrack(format)
                    } else if (type == RecordType.VIDEO) {
                        videoTrackId = addTrack(format)
                    }
                    if (videoTrackId >= 0 && audioTrackId >= 0) {
                        start()
                        muxerStart = true
                    }
                }
            }
        }


    }

    override fun onRecordData(type: RecordType, data: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        lock(lock) {
            muxer?.apply {
                if (muxerStart) {
                    if (type == RecordType.AUDIO) {
                        if (audioTrackId >= 0) {
                            Log.e(TAG, "writeAudioSampleData: ${bufferInfo.presentationTimeUs}")
                            writeSampleData(audioTrackId, data, bufferInfo)
                        }
                    } else if (type == RecordType.VIDEO) {
                        if (videoTrackId >= 0) {
                            Log.e(TAG, "writeVideoSampleData: ${bufferInfo.presentationTimeUs}")
                            writeSampleData(videoTrackId, data, bufferInfo)
                        }
                    }
                }

            }
        }
    }

    override fun onRecordEnd(type: RecordType) {
        Log.e(TAG, "onRecordEnd")
        lock(lock) {
            if (type == RecordType.AUDIO) {
                audioTrackId = -1
            } else if (type == RecordType.VIDEO) {
                videoTrackId = -1
            }
            if (audioTrackId < 0 && videoTrackId < 0 && muxerStart) {
                stoping = false
                release()
                muxerStart = false
                condition.signalAll()
            }
        }
    }

    enum class RecordType {
        AUDIO, VIDEO
    }

    class MuxerThread

}

interface AVRecorderListener {

    fun onRecordStart(type: RecordType, format: MediaFormat)

    fun onRecordData(type: RecordType, data: ByteBuffer, bufferInfo: MediaCodec.BufferInfo)

    fun onRecordEnd(type: RecordType)
}