package org.huihui.supercamera.library.camera.preview

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.*
import org.huihui.supercamera.library.camera.render.IRender
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10


/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/28 21:55
 */
class SurfaceRenderView : SurfaceView, IPreview, LifecycleEventObserver, SurfaceHolder.Callback {
    private var render: IRender? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        holder.addCallback(this)
    }

    override fun setRender(render: IRender) {
        this.render = render
    }

    override fun bindLifeCycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        render?.surfaceCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        render?.surfaceChange(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        render?.surfaceDestroy()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {

    }


}