package org.huihui.supercamera.library.camera

import androidx.lifecycle.LifecycleOwner
import org.huihui.supercamera.library.camera.camera.Camera1
import org.huihui.supercamera.library.camera.camera.ICamera
import org.huihui.supercamera.library.camera.preview.IPreview
import org.huihui.supercamera.library.camera.render.AbsRender
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
    camera: ICamera = Camera1(),
    render: ICameraRender = CameraRender(),
    preview: IPreview,
    lifecycleOwner: LifecycleOwner
) : IRender.RenderListener {
    private var mCamera = camera

    private var IRender = render

    private var IPreview = preview

    private var canPreview = false
    private var isPreview = false

    init {
        IPreview.bindLifeCycle(lifecycleOwner)
        IRender.setRenderListener(this@CameraEngine)
        preview.setRender(IRender)
    }

    fun startPreview() {
        if (canPreview && !isPreview) {
            mCamera.startPreview(IRender.getInputSurfaceTexture())
            IRender.setTextureSize(mCamera.getPreviewWidth(), mCamera.getPreviewHeight())
            isPreview = true
        }
    }

    fun stopPreview() {
        if (isPreview) {
            mCamera.stopPreview()
        }
    }

    fun startRecord() {

    }

    fun stopRecord() {

    }

    override fun onRenderReady() {
        canPreview = true
        startPreview()
    }


    override fun onRenderDestory() {
        canPreview = false
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