package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import org.huihui.supercamera.library.camera.filter.DisplayFilter
import org.huihui.supercamera.library.camera.filter.IFilter
import org.huihui.supercamera.library.camera.filter.OESFilter
import org.huihui.supercamera.library.camera.filter.utils.OpenGLUtils

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/28 22:33
 */
class CameraRender : AbsRender(), ICameraRender {
    companion object {
        const val TAG = "CameraRender"
    }

    private val mInputSurfaceTexture = SurfaceTexture(0)

    private var oesTextureId = intArrayOf(GLES20.GL_NONE)

    private var textureWidth: Int = 0
    private var textureHeight: Int = 0

    private val oesFilter: IFilter
    private val displayFilter: IFilter


    init {
        mInputSurfaceTexture.setOnFrameAvailableListener {
            requestRender()
        }
        oesFilter = OESFilter()

        displayFilter = DisplayFilter()
    }

    override var renderListener: IRender.IRenderListener? = null

    override fun getInputSurfaceTexture(): SurfaceTexture {
        return mInputSurfaceTexture
    }

    override fun setTextureSize(textureWidth: Int, textureHeight: Int) {
        this.textureWidth = textureWidth
        this.textureHeight = textureHeight
        mInputSurfaceTexture.setDefaultBufferSize(textureWidth, textureHeight)
        notifySizeChange()
    }

    override fun renderReady(): Boolean {
        return super.renderReady() && textureWidth > 0 && textureHeight > 0
    }

    override fun onGLCreated() {
        oesFilter.onInit()
        displayFilter.onInit()
        renderListener?.onSurfaceCreated()
        OpenGLUtils.checkGlError("")

    }

    override fun onSurfaceCreated() {
        GLES20.glGenTextures(1, oesTextureId, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId[0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        // 这里会销毁原来的texture
        mInputSurfaceTexture.detachFromGLContext()
        mInputSurfaceTexture.attachToGLContext(oesTextureId[0])
        OpenGLUtils.checkGlError("")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        notifySizeChange()
        renderListener?.onSurfaceChanged(width, height)
    }

    private fun notifySizeChange() {
        if (viewWidth != 0 && viewHeight != 0 && textureWidth != 0 && textureHeight != 0) {
            enqueueTask {
                oesFilter.onSizeChanged(viewWidth, viewHeight, textureWidth, textureHeight)
                displayFilter.onSizeChanged(viewWidth, viewHeight, textureWidth, textureHeight)
                OpenGLUtils.checkGlError("")
            }

        }
    }


    override fun onDrawFrame() {
        mInputSurfaceTexture.updateTexImage()
        (oesFilter as OESFilter).apply {
            mInputSurfaceTexture.getTransformMatrix(oesMatrixArray)
        }
        var curTextureId: Int = oesFilter.onDrawFrame(oesTextureId[0])
        renderListener?.onDrawFrame(curTextureId)
        displayFilter.onDrawFrame(curTextureId)
    }

    override fun beforeSurfaceDestory() {
        renderListener?.beforeSurfaceDestory()
    }

    override fun onSurfaceDestory() {
        renderListener?.onSurfaceDestory()
    }

    override fun release() {
        textureHeight = 0
        textureWidth = 0
        oesFilter.release()
        displayFilter.release()
    }

    override fun onGLDestroy() {
        //  detachFromContext: invalid current EGLDisplay 无法解决
//        mInputSurfaceTexture.detachFromGLContext()
        textureHeight = 0
        textureWidth = 0
        renderListener?.onGLDestroy()

    }


}