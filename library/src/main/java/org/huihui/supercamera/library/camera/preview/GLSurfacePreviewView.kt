package org.huihui.supercamera.library.camera.preview

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.lifecycle.*
import org.huihui.supercamera.library.camera.render.IRender
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/28 21:55
 */
class GLSurfacePreviewView : GLSurfaceView, IPreview, GLSurfaceView.Renderer, LifecycleEventObserver {

    private var render: IRender? = null

    private var destory = true

    constructor(context: Context) : super(context, null) {
        setRenderer(this)
    }


    override fun setRender(render: IRender) {
        this.render = render
    }

    override fun bindLifeCycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        destory = true
        render?.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        render?.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        render?.onDrawFrame(gl)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!destory) {
            render?.onSurfaceDestory()
            destory = true
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            onPause()
            if (!destory) {
                render?.onSurfaceDestory()
                destory = true
            }
        }
    }

}