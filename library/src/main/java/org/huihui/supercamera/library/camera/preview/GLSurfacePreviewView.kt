package org.huihui.supercamera.library.camera.preview

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.SurfaceHolder
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
class GLSurfacePreviewView : GLSurfaceView, IPreview, GLSurfaceView.Renderer, LifecycleEventObserver {
    private var render: IRender? = null

    private var destory = true
    private var destroyLock = ReentrantLock()
    private var destroyCondition = destroyLock.newCondition()
    override var previewListner: IPreview.PreviewListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        super.setPreserveEGLContextOnPause(true)
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun setPreserveEGLContextOnPause(preserveOnPause: Boolean) {
    }

    override fun setRender(render: IRender) {
        this.render = render
    }

    override fun bindLifeCycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        destory = false
        render?.onSurfaceCreated(gl, config)
        previewListner?.onPreviewReady()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        render?.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        render?.onDrawFrame(gl)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        render?.onSurfaceDestory()
    }

    private fun onBeforeGLContexDestory() {
        if (!destory) {
            previewListner?.onPreviewDestroy()
            render?.onDestroy()
            destory = true
        }
    }

    override fun onDetachedFromWindow() {
        queueEvent {
            onBeforeGLContexDestory()
            try {
                destroyLock.lock()
                destroyCondition.signalAll()
            } catch (e: Exception) {
            } finally {
                destroyLock.unlock()
            }
        }
        try {
            destroyLock.lock()
            destroyCondition.await()
        } catch (e: Exception) {
        } finally {
            destroyLock.unlock()
        }
        super.onDetachedFromWindow()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            onPause()
        }
    }

    override fun requestRender() {
        super.requestRender()
    }

//    private var mEGLContextClientVersion = 0

//    override fun setEGLContextClientVersion(version: Int) {
//        super.setEGLContextClientVersion(version)
//        mEGLContextClientVersion = version
//    }

//    inner class GLContextFactory : EGLContextFactory {
//        val EGL_CONTEXT_CLIENT_VERSION = 0x3098
//
//        override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext? {
//            val attrib_list = intArrayOf(
//                EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
//                EGL10.EGL_NONE
//            )
//            return egl.eglCreateContext(
//                display, config, EGL10.EGL_NO_CONTEXT,
//                if (mEGLContextClientVersion != 0) attrib_list else null
//            )
//        }
//
//        override fun destroyContext(
//            egl: EGL10, display: EGLDisplay,
//            context: EGLContext
//        ) {
//            if (!egl.eglDestroyContext(display, context)) {
//                Log.e("DefaultContextFactory", "display:$display context: $context")
//                if (GLSurfaceView.LOG_THREADS) {
//                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().id)
//                }
//                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError())
//            }
//        }
//
//
//    }
}