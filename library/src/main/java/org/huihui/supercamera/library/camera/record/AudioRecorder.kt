package org.huihui.supercamera.library.camera.record

import android.media.*
import android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM
import android.util.Log
import org.huihui.supercamera.library.util.lock
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/29 10:47
 */
class AudioRecorder {
    private val TAG = "AudioRecorder"
    private val AUDIO_MIME_TYPE = "audio/mp4a-latm"

    private var sampleRate = 44100 // 44.1[KHz] is only setting guaranteed to be available on all devices.

    private var bitRate = 128000
    private var channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private var channelCount = 2
    private var audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var bufferInfo = MediaCodec.BufferInfo()
    private var lock = ReentrantLock()
    private var condition = lock.newCondition()

    @Volatile
    private var recording = false

    @Volatile
    private var stoping = false

    @Volatile
    private var audioThread: AudioThread? = null

    var recorderListener: AVRecorderListener? = null

    fun start() {
        waitForStop()
        lock(lock) {
            if (recording) {
                return
            }
            recording = true
            audioThread = AudioThread().apply {
                start()
            }
        }
    }

    private fun waitForStop() {
        lock(lock) {
            while (stoping) {
                condition.await(10, TimeUnit.MILLISECONDS)
            }
            condition.signalAll()
        }
    }

    fun stop(wait: Boolean) {
        lock(lock) {
            if (!recording || stoping) {
                return
            }
            recording = false
            stoping = true

        }
        if (wait) {
            waitForStop()
        }
    }


    private fun drainEncoder(mediaCodec: MediaCodec, byteArray: ByteArray, size: Int, pts: Long) {
        mediaCodec.apply {
            val inputIndex: Int = dequeueInputBuffer(10)
            if (inputIndex >= 0) {
                val buffer: ByteBuffer = getInputBuffer(inputIndex) ?: return@apply
                buffer.clear()
                if (size < 0) {
                    queueInputBuffer(inputIndex, 0, 0, 0, BUFFER_FLAG_END_OF_STREAM)
                } else {
                    buffer.put(byteArray, 0, size)
                    queueInputBuffer(inputIndex, 0, byteArray.size, pts, 0)
                }
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
                        recorderListener?.onRecordData(CameraRecoder.RecordType.AUDIO, encodedData, bufferInfo)
                        releaseOutputBuffer(outputIndex, false)
                    }
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    Log.e(TAG, "INFO_OUTPUT_FORMAT_CHANGED ${outputFormat.toString()} ")
                    recorderListener?.onRecordStart(CameraRecoder.RecordType.AUDIO, outputFormat)
                }
            }

        }
    }

    inner class AudioThread : Thread("AudioThread") {
        override fun run() {
            Log.e(TAG, "AudioThread in")
            val min_buffer_size = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat
            )
            val audioData = ByteArray(min_buffer_size)

            val mediaFormat =
                MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, sampleRate, channelCount).apply {
                    setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                    setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                    setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, min_buffer_size)

                }

            val mediaCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE).apply {
                configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT, sampleRate, channelConfig,
                audioFormat, min_buffer_size
            )
            audioRecord.startRecording()
            var size: Int
            var byteCount = 0L
            val samplePerByte = 2
            val bytePerSeconds = sampleRate * samplePerByte * channelCount  // byte/s
            while (recording) {
                size = audioRecord.read(audioData, 0, min_buffer_size)
                byteCount += size
                drainEncoder(
                    mediaCodec, audioData, size, byteCount * 1000_000 / bytePerSeconds
                )
            }
            //end of stream
            drainEncoder(
                mediaCodec, audioData, -1, byteCount * 1000_000 / bytePerSeconds
            )
            audioRecord.apply {
                stop()
                release()
            }

            mediaCodec.apply {
                stop()
                release()
            }

            lock(lock) {
                stoping = false
                audioThread = null
                condition.signalAll()
            }

            recorderListener?.onRecordEnd(CameraRecoder.RecordType.AUDIO)
            Log.e(TAG, "AudioThread exit")
        }
    }
}