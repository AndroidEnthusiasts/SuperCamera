package org.huihui.supercamera.library.camera.camera

import android.content.Context

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:55
 */
abstract class AbsCamera(var context: Context) : ICamera {
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
    private var displayOrientation = 0

    override fun getDisplayOrientation(): Int {
        return displayOrientation
    }

    protected fun setDisplayOrientation(orientaion: Int) {
        displayOrientation = orientaion
    }
//   var textureFrameListener: ICamera.TextureFrameListener? = null


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