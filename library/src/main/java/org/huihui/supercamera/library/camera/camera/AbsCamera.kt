package org.huihui.supercamera.library.camera.camera

import android.graphics.SurfaceTexture

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:55
 */
abstract class AbsCamera : ICamera {
//    private val surfaceTexture by lazy {
//        SurfaceTexture(0).apply {
//            initSurfaceTexture(this)
//        }
//    }
//
//    private fun initSurfaceTexture(surfaceTexture:SurfaceTexture) {
//        surfaceTexture.detachFromGLContext()
//        surfaceTexture.setOnFrameAvailableListener {
//            textureFrameListener?.onTextureFrameAvailable()
//        }
//    }

    private var previewWidth = 1280

    private var previewHeight = 720

    private var isFont = false

    var frameListener: ICamera.FrameListener? = null

//    var textureFrameListener: ICamera.TextureFrameListener? = null


    override fun isFont(): Boolean {
        return isFont
    }

    fun toogleFont() {
        isFont = !isFont
    }

    protected fun setPreviewWidth(width: Int) {
        previewWidth = width
    }

    protected fun setPreviewHeight(height: Int) {
        previewHeight = height
    }

    override fun getPreviewWidth(): Int {
        return previewWidth
    }

    override fun getPreviewHeight(): Int {
        return previewHeight
    }

//    override fun getOutputSurfaceTexture(): SurfaceTexture {
//        return surfaceTexture
//    }
}