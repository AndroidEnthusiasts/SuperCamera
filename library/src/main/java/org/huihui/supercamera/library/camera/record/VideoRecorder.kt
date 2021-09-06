package org.huihui.supercamera.library.camera.record

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.EGLContext
import android.opengl.GLES20
import android.opengl.GLES30
import android.os.Build
import android.os.SystemClock
import android.util.Log
import org.huihui.supercamera.library.camera.egl.EglCore
import org.huihui.supercamera.library.camera.egl.EglCore.FLAG_RECORDABLE
import org.huihui.supercamera.library.camera.egl.EglCore.FLAG_TRY_GLES3
import org.huihui.supercamera.library.camera.egl.WindowSurface
import org.huihui.supercamera.library.camera.filter.DisplayFilter
import org.huihui.supercamera.library.camera.filter.utils.OpenGLUtils
import org.huihui.supercamera.library.util.loadAssertString
import org.huihui.supercamera.library.util.lock
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/29 10:47
 */
class VideoRecorder {
    companion object {
        const val TAG = "VideoRecorder"

        // 录制视频的类型
        const val MIME_TYPE = "video/avc"

        // 帧率
        const val FRAME_RATE = 25

        // I帧时长
        const val I_FRAME_INTERVAL = 1

        /**
         * 16*1000 bps：可视电话质量
         * 128-384 * 1000 bps：视频会议系统质量
         * 1.25 * 1000000 bps：VCD质量（使用MPEG）
         * 5 * 1000000 bps：DVD质量（使用MPEG2压缩）
         * 8-15 * 1000000 bps：高清晰度电视（HDTV） 质量（使用H.264压缩）
         * 29.4  * 1000000 bps：HD DVD质量
         * 40 * 1000000 bps：蓝光光碟质量（使用MPEG2、H.264或VC-1压缩）
         */
        //    public static final int BIT_RATE = 15 * 1000000;
        // 与抖音相同的视频比特率
        const val BIT_RATE = 6693560 // 1280 * 720

        const val BIT_RATE_LOW = 3921332 // 576 * 1024

    }

    private var recording = false
    private var stoping = false

    private var lock = ReentrantLock()
    private var stopCondition = lock.newCondition()
    private var startCondition = lock.newCondition()
    private var frameAvailabeCondition = lock.newCondition()
    private var frameAvailable = false
    private var frameTimeStamp = 0L

    private var bufferInfo = MediaCodec.BufferInfo()
    private var startTime = 0L

    private var videoParam: VideoParam? = null
    private var textureId = 0
    private var videoThread: VideoThread? = null
    var recorderListener: AVRecorderListener? = null

    fun start(videoParam: VideoParam) {
        waitForStop()
        lock(lock) {
            this.videoParam = videoParam
            recording = true
            videoThread = VideoThread().apply {
                start()
            }
        }
        waitForStart()
    }

    fun frameAvaliable(texture: Int) {
        lock(lock) {
            if (recording) {
                textureId = texture
                frameAvailable = true
                frameTimeStamp = SystemClock.elapsedRealtimeNanos()
                if (startTime == 0L) {
                    startTime = frameTimeStamp
                }
                frameAvailabeCondition.signal()
            }
        }
    }

    private fun waitForStart() {
        lock(lock) {
            startCondition.await()
        }
    }

    private fun waitForStop() {
        lock(lock) {
            while (stoping) {
                stopCondition.await(10, TimeUnit.MILLISECONDS)
            }
        }
    }

    fun stop(wait: Boolean) {
        lock(lock) {
            if (recording) {
                videoParam = null
                recording = false
                stoping = true
                startTime = 0
                frameAvailabeCondition.signal()
            }
        }
        if (wait) {
            waitForStop()
        }
    }

    fun drainEncoder(mediaCodec: MediaCodec, endOfStream: Boolean) {
        mediaCodec.apply {
            if (endOfStream) {
                mediaCodec.signalEndOfInputStream()
            }
            var outputIndex = 0
            while (outputIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                outputIndex = dequeueOutputBuffer(bufferInfo, 0)
                Log.e(TAG, "dequeueOutputBuffer $outputIndex  ${bufferInfo.flags}")
                if (outputIndex >= 0) {
                    val encodedData: ByteBuffer = getOutputBuffer(outputIndex) ?: break
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0 && bufferInfo.size != 0) {
                        releaseOutputBuffer(outputIndex, false)
                    } else {
                        Log.e(TAG, "gain buffer ${bufferInfo.offset}\tsize: ${bufferInfo.size}\tpts: ${bufferInfo.presentationTimeUs}")
                        recorderListener?.onRecordData(CameraRecoder.RecordType.VIDEO, encodedData, bufferInfo)
                        releaseOutputBuffer(outputIndex, false)
                    }
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    Log.e(TAG, "INFO_OUTPUT_FORMAT_CHANGED $outputFormat ")
                    recorderListener?.onRecordStart(CameraRecoder.RecordType.VIDEO, outputFormat)
                }
            }

        }


    }

    inner class VideoThread : Thread("VideoThread") {
        override fun run() {
            val param = videoParam!!
            val egl = EglCore(param.glContex, FLAG_TRY_GLES3 or FLAG_RECORDABLE)
            // 设置编码格式

            // 设置编码格式
            val videoWidth = param.textureWidth

            val videoHeight = param.textureHeight

            val format = MediaFormat.createVideoFormat(param.mineType, videoWidth, videoHeight)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_BIT_RATE, param.bitRate)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            if (Build.VERSION.SDK_INT >= 21) {
                var profile = 0
                var level = 0
                if (param.mineType == "video/avc") {
                    profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh
                    level = if (videoWidth * videoHeight >= 1920 * 1080) {
                        MediaCodecInfo.CodecProfileLevel.AVCLevel4
                    } else {
                        MediaCodecInfo.CodecProfileLevel.AVCLevel31
                    }
                } else if (param.mineType == "video/hevc") {
                    profile = MediaCodecInfo.CodecProfileLevel.HEVCProfileMain
                    level = if (videoWidth * videoHeight >= 1920 * 1080) {
                        MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel4
                    } else {
                        MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel31
                    }
                }
                format.setInteger(MediaFormat.KEY_PROFILE, profile)
                // API 23以后可以设置AVC的编码level，低于23设置了但不生效
                if (Build.VERSION.SDK_INT >= 23) {
                    format.setInteger(MediaFormat.KEY_LEVEL, level)
                }
            }
            val mediaCodec = MediaCodec.createEncoderByType(param.mineType)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = mediaCodec.createInputSurface()
            mediaCodec.start()
            val glSurface = WindowSurface(egl, surface, true)
            glSurface.makeCurrent()
            val displayFilter = DisplayFilter()
            displayFilter.onInit()
            displayFilter.onSizeChanged(
                param.textureWidth, param.textureHeight,
                param.textureWidth, param.textureHeight
            )

            var timestamp = 0L


            lock(lock) {
                startCondition.signal()
            }
            while (recording) {
                lock(lock) {

                    while (!frameAvailable && recording) {
                        frameAvailabeCondition.await(10, TimeUnit.MILLISECONDS)
                    }
                    frameAvailable = false
                    timestamp = frameTimeStamp
                }
                if (recording) {
                    val pts = timestamp - startTime
                    glSurface.setPresentationTime(pts)
                    displayFilter.onDrawFrame(textureId)
                    GLES20.glFinish()

                    glSurface.swapBuffers()
                    drainEncoder(mediaCodec, false)
                }
            }
            drainEncoder(mediaCodec, true)
            mediaCodec.stop()
            mediaCodec.release()
            glSurface.releaseEglSurface()
            egl.release()
            videoThread = null
            lock(lock) {
                stoping = false
                stopCondition.signalAll()
            }
            recorderListener?.onRecordEnd(CameraRecoder.RecordType.VIDEO)
            Log.e(TAG, "VideoThread exit")
        }
    }

    class VideoParam(
        var glContex: EGLContext, var textureWidth: Int, var textureHeight: Int,
        var bitRate: Int, var mineType: String
    )
}