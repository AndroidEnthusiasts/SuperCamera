package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture
import android.opengl.GLES10
import android.opengl.GLES10Ext
import android.opengl.GLES11Ext
import android.opengl.GLES20
import androidx.annotation.CallSuper
import org.huihui.supercamera.library.camera.filter.DisplayFilter
import org.huihui.supercamera.library.camera.filter.IFilter
import org.huihui.supercamera.library.camera.filter.OESFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10Ext

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/28 22:33
 */
class CameraRender : AbsRender(), ICameraRender {

    private val mInputSurfaceTexture = SurfaceTexture(0)
    private var oesTextureId = intArrayOf(GLES20.GL_NONE)

    protected var textureWidth: Int = 0
    protected var textureHeight: Int = 0

    private val oesFilter: IFilter
    private val displayFilter: IFilter


    init {
        mInputSurfaceTexture.detachFromGLContext()

        oesFilter = OESFilter()

        displayFilter = DisplayFilter()
    }

    override fun getInputSurfaceTexture(): SurfaceTexture {
        return mInputSurfaceTexture
    }

    override fun setTextureSize(textureWidth: Int, textureHeight: Int) {
        this.textureWidth = textureWidth
        this.textureHeight = textureHeight
        notifySizeChange()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        GLES20.glGenTextures(1, oesTextureId, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId[0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        mInputSurfaceTexture.attachToGLContext(oesTextureId[0])

        oesFilter.onInit()
        displayFilter.onInit()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        notifySizeChange()
    }

    private fun notifySizeChange() {
        if (viewWidth != 0 && viewHeight != 0 && textureWidth != 0 && textureHeight != 0) {
            oesFilter.onSizeChanged(viewWidth, viewHeight, textureWidth, textureHeight)
            displayFilter.onSizeChanged(viewWidth, viewHeight, textureWidth, textureHeight)
        }
    }

    override fun onSurfaceDestory() {
        super.onSurfaceDestory()
        mInputSurfaceTexture.detachFromGLContext()
        textureHeight = 0
        textureWidth = 0
        oesFilter.onDestroy()
        displayFilter.onDestroy()
    }


    override fun onDrawFrame(gl: GL10?) {
        var curTextureId: Int = oesFilter.onDrawFrame(oesTextureId[0])

        displayFilter.onDrawFrame(curTextureId)
    }

}