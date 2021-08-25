package org.huihui.supercamera.library.camera.camera

import android.graphics.SurfaceTexture

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/22 16:30
 */
interface ICamera {

    fun openCamera()

    fun closeCamera()

    fun startPreview(surfaceTexture: SurfaceTexture)

    fun stopPreview()

    fun getPreviewHeight(): Int

    fun getPreviewWidth(): Int

    fun getPreviewFormat()

    fun isFont(): Boolean

    fun switchCamera(surfaceTexture: SurfaceTexture)

    fun takePicture()

    fun getDisplayOrientation(): Int
//    fun getOutputSurfaceTexture(): SurfaceTexture

    interface FrameListener {
        fun onFrameAvailable(byteArray: ByteArray)
    }

//    interface TextureFrameListener {
//        fun onTextureFrameAvailable()
//    }

}