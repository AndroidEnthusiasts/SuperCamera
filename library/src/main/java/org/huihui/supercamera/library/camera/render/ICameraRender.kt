package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/3 0:01
 */
interface ICameraRender : IRender {


    fun getInputSurfaceTexture(): SurfaceTexture

    fun setTextureSize(textureWidth: Int, textureHeight: Int)

}