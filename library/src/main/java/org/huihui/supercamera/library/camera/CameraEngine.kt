package org.huihui.supercamera.library.camera

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.huihui.supercamera.library.camera.camera.Camera1
import org.huihui.supercamera.library.camera.camera.ICamera
import org.huihui.supercamera.library.camera.preview.IPreview
import org.huihui.supercamera.library.camera.record.CameraRecoder
import org.huihui.supercamera.library.camera.record.VideoRecorder
import org.huihui.supercamera.library.camera.record.VideoRecorder.Companion.BIT_RATE
import org.huihui.supercamera.library.camera.record.VideoRecorder.Companion.FRAME_RATE
import org.huihui.supercamera.library.camera.record.VideoRecorder.Companion.MIME_TYPE
import org.huihui.supercamera.library.camera.render.CameraRender
import org.huihui.supercamera.library.camera.render.ICameraRender
import org.huihui.supercamera.library.camera.render.IRender

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:39
 */
class CameraEngine(
    context: Context,
    camera: ICamera = Camera1(context),
    render: ICameraRender = CameraRender(),
    preview: IPreview,
    lifecycleOwner: LifecycleOwner
) : IRender.IRenderListener, LifecycleEventObserver {

    companion object {
        const val TAG = "CameraEngine"
    }

    private var mCamera: ICamera = camera

    private var mRender: ICameraRender = render

    private var mPreview: IPreview = preview

    private var cameraRecorder = CameraRecoder()
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var isPreview = false

    private var previewRunning = false

    private var isRecord = false

    /**
     * 自动开启预览
     */
    var autoPreView = true

    init {
        mRender.renderListener = this
        mPreview.bindLifeCycle(lifecycleOwner)
        mPreview.setRender(this.mRender)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun startPreview() {
        if (!isPreview) {
            mCamera.openCamera()
            mCamera.startPreview(mRender.getInputSurfaceTexture())
            if (mCamera.getDisplayOrientation() == 90 || mCamera.getDisplayOrientation() == 180) {
                mRender.setTextureSize(mCamera.getPreviewHeight(), mCamera.getPreviewWidth())
                textureWidth = mCamera.getPreviewHeight()
                textureHeight = mCamera.getPreviewWidth()
            } else {
                mRender.setTextureSize(mCamera.getPreviewWidth(), mCamera.getPreviewHeight())
                textureWidth = mCamera.getPreviewWidth()
                textureHeight = mCamera.getPreviewHeight()
            }
            isPreview = true
        }
    }

    fun stopPreview() {
        if (isPreview) {
            mCamera.stopPreview()
            isPreview = false
        }
    }

    fun startRecord(path: String) {
        if (isPreview && previewRunning && !isRecord) {
            cameraRecorder.startRecord(
                path,
                VideoRecorder.VideoParam(
                    mRender.getEGLContex()!!, textureWidth, textureHeight,
                    BIT_RATE, MIME_TYPE
                )
            )
            isRecord = true
        }
    }

    fun stopRecord() {
        if (isRecord) {
            cameraRecorder.stopRecord()
            isRecord = false
        }
    }

    override fun onGLCreated() {

    }

    override fun onSurfaceCreated() {
        previewRunning = true
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
    }

    override fun onDrawFrame(textureId: Int) {
        if (isRecord) {
            cameraRecorder.videoFrameAvailable(textureId)
        }
    }

    override fun beforeSurfaceDestory() {
    }

    override fun onSurfaceDestory() {
        previewRunning = false
    }

    override fun onGLDestroy() {
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
//            startPreview()
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            stopPreview()
        }
    }

//    class Builder {
////        enum class CAMERAAPI {
////            API1, API2, APIX
////        }
////
////        val api = CAMERAAPI.API1
//
//    }

}