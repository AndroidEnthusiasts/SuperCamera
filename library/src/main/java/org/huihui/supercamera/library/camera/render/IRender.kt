package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture
import android.view.Surface
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:40
 */
interface IRender {
    fun initRender()

    fun surfaceCreated(surface: Surface)

    fun surfaceCreated(surfaceTexture: SurfaceTexture)

    fun surfaceChange(width: Int, height: Int)

    fun surfaceDestroy()

    fun requestRender()

    fun enqueueTask(runnable: Runnable)

    fun onGLCreated()

    fun onSurfaceCreated()

    fun onSurfaceChanged(width: Int, height: Int)

    fun onDrawFrame()

    fun beforeSurfaceDestory()

    fun onSurfaceDestory()

    /**
     * glcontex 销毁
     */
    fun onGLDestroy()

    fun release()


    var renderListener: RenderListener?

    interface RenderListener {

        fun onSurfaceCreated()

        fun onSurfaceDestroy()

        fun onDestroy()
    }
}