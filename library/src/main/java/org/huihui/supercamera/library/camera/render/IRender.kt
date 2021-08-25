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

    fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)

    fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)

    fun onDrawFrame(gl: GL10?)

    fun onSurfaceDestory()

    /**
     * glcontex 销毁
     */
    fun onDestroy()

    fun release()


    var renderListener: RenderListener?

    interface RenderListener {

        fun onSurfaceCreated()

        fun onSurfaceDestroy()

        fun onDestroy()
    }
}