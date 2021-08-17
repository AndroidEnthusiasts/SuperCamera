package org.huihui.supercamera.library.camera

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import org.huihui.supercamera.library.camera.camera.Camera1
import org.huihui.supercamera.library.camera.camera.ICamera
import org.huihui.supercamera.library.camera.preview.IPreview
import org.huihui.supercamera.library.camera.render.CameraRender
import org.huihui.supercamera.library.camera.render.ICameraRender

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:39
 */
class CameraEngine(
    camera: ICamera = Camera1(),
    render: ICameraRender = CameraRender(),
    preview: IPreview,
    lifecycleOwner: LifecycleOwner
) : IPreview.PreviewListener {

    companion object {
        const val TAG = "CameraEngine"
    }

    private var mCamera: ICamera = camera

    private var mRender: ICameraRender = render

    private var mPreview: IPreview = preview

    private var previewReady = false
    private var isPreview = false

    /**
     * 自动开启预览
     */
    var autoPreView = true

    private var pendingPreview = false

    init {
        mPreview.bindLifeCycle(lifecycleOwner)
        mPreview.previewListner = this
        mPreview.setRender(this.mRender)
        this.mRender.getInputSurfaceTexture().setOnFrameAvailableListener {
            mPreview.requestRender()
        }
    }

    fun startPreview() {
        if (!previewReady) {
            pendingPreview = true
            return
        }
        if (!isPreview) {
            mCamera.openCamera()
            mCamera.startPreview(mRender.getInputSurfaceTexture())
            mRender.setTextureSize(mCamera.getPreviewWidth(), mCamera.getPreviewHeight())
            isPreview = true
        }
    }

    fun stopPreview() {
        if (isPreview) {
            mCamera.stopPreview()
            isPreview= false
        }
    }

    fun startRecord() {

    }

    fun stopRecord() {

    }

    override fun onPreviewReady() {
        if (autoPreView || pendingPreview) {
            previewReady = true
            pendingPreview = false
            startPreview()
        }
    }

    override fun onPreviewDestroy() {
        previewReady = false
        stopPreview()
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