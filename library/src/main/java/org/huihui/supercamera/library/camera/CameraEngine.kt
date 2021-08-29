package org.huihui.supercamera.library.camera

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.huihui.supercamera.library.camera.camera.Camera1
import org.huihui.supercamera.library.camera.camera.ICamera
import org.huihui.supercamera.library.camera.preview.IPreview
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
) : IRender.RenderListener, LifecycleEventObserver {

    companion object {
        const val TAG = "CameraEngine"
    }

    private var mCamera: ICamera = camera

    private var mRender: ICameraRender = render

    private var mPreview: IPreview = preview

    private var isPreview = false

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
            } else {
                mRender.setTextureSize(mCamera.getPreviewWidth(), mCamera.getPreviewHeight())
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

    fun startRecord() {

    }

    fun stopRecord() {

    }

    override fun onSurfaceCreated() {
    }

    override fun onSurfaceDestroy() {

    }

    override fun onDestroy() {

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            startPreview()
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