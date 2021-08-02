package org.huihui.supercamera.library.camera.render

import org.huihui.supercamera.library.camera.filter.DisplayFilter
import org.huihui.supercamera.library.camera.filter.IFilter
import org.huihui.supercamera.library.camera.filter.OESFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/28 22:33
 */
class CameraRender : AbsRender() {
    private val oesFilter: IFilter
    private val displayFilter: IFilter

    init {
        oesFilter = OESFilter()

        displayFilter = DisplayFilter()

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        notifySizeChange()
    }

    private fun notifySizeChange() {
        if (viewWidth != 0 && viewHeight != 0 && textureWidth != 0 && textureHeight != 0) {
            oesFilter.onSizeChanged(viewWidth, viewHeight, textureWidth, textureHeight)
        }
    }

    override fun onSurfaceDestory() {
        oesFilter.onDestroy()
    }

    override fun setTextureSize(textureWidth: Int, textureHeight: Int) {
        super.setTextureSize(textureWidth, textureHeight)
        notifySizeChange()
    }

    override fun onDrawFrame(gl: GL10?) {
        oesFilter

    }

}