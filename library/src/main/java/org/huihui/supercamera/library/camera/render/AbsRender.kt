package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture
import androidx.annotation.CallSuper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:40
 */
abstract class AbsRender : IRender {
    private val renderListener: IRender.RenderListener? = null
    private val mInputSurfaceTexture = SurfaceTexture(0)

    protected var textureWidth: Int = 0
    protected var textureHeight: Int = 0
    protected var viewWidth: Int = 0
    protected var viewHeight: Int = 0

    init {
        mInputSurfaceTexture.detachFromGLContext()
    }

    override fun setRenderListener(renderListener: IRender.RenderListener) {

    }

    override fun getInputSurfaceTexture(): SurfaceTexture {
        return mInputSurfaceTexture
    }

    @CallSuper
    override fun setTextureSize(textureWidth: Int, textureHeight: Int) {
        this.textureWidth = textureWidth
        this.textureHeight = textureHeight
    }
    @CallSuper
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

    @CallSuper
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        renderListener?.onRenderReady()
    }

}