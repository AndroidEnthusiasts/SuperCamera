package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:40
 */
interface IRender {

    fun setRenderListener(renderListener: RenderListener)

    fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)

    fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)

    fun onSurfaceDestory()

    fun onDrawFrame(gl: GL10?)

    interface RenderListener {

        fun onRenderReady()

        fun onRenderDestory()
    }
}