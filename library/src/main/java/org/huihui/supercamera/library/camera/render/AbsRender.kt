package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLContext
import android.opengl.EGLSurface
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Surface
import androidx.annotation.CallSuper
import org.huihui.supercamera.library.camera.egl.EglCore
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:40
 */
abstract class AbsRender : IRender {
    companion object {
        const val TAG = "AbsRender"

        const val MSG_TASK = 0x00
        const val MSG_SURFACE_CREATE = 0x01
        const val MSG_SURFACE_CHANGE = 0x02
        const val MSG_SURFACE_DESTROY = 0x03
        const val MSG_RENDER = 0x04
    }

    protected var autoRender = false
    protected var viewWidth: Int = 0
    protected var viewHeight: Int = 0
    protected var eglCore: EglCore? = null
    protected var eglSurface: EGLSurface? = null
    private var taskQueue = LinkedList<Runnable>()
    private var renderLock = ReentrantLock()
    private var renderCondition = renderLock.newCondition()

    @Volatile
    protected var thread: GLThread? = null
    protected var renderHander: RenderHandler? = null
    protected var haveSurface = false
    override fun initRender() {
        if (thread != null) {
            return
        }
        thread = GLThread().apply {
            start()
            renderHander = RenderHandler(looper)
        }
    }

    override fun setRenderMode(auto: Boolean) {
        autoRender = auto
    }

    override fun getEGLContex(): EGLContext? {
        return eglCore?.eglContext
    }

    override fun surfaceCreated(surface: Surface) {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_CREATE, surface))
        }
    }

    @CallSuper
    override fun surfaceChange(width: Int, height: Int) {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_CHANGE, width, height))
        }
    }

    @CallSuper
    override fun surfaceCreated(surfaceTexture: SurfaceTexture) {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_CREATE, surfaceTexture))
        }
        renderLock.lock()
        while (!haveSurface) {
            renderCondition.await()
        }
        renderLock.unlock()
    }

    @CallSuper
    override fun surfaceDestroy() {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_DESTROY))
        }
        //这里需要等待glsurface销毁 不然会有gl错误
        renderLock.lock()
        while (haveSurface) {
            renderCondition.await()
        }
        renderLock.unlock()

    }

    override fun requestRender() {
        renderHander?.apply {
            if (renderReady()) {
                sendMessage(Message.obtain(this, MSG_RENDER))
            }
        }
    }
//    @CallSuper
//    override fun onGLCreated() {
//        renderListener?.onGLCreated()
//    }
//    @CallSuper
//    override fun onSurfaceChanged(width: Int, height: Int) {
//        renderListener?.onSurfaceChanged(width,height)
//    }
//    @CallSuper
//    override fun onSurfaceDestory() {
//        renderListener?.onSurfaceDestory()
//    }
//    @CallSuper
//    override fun beforeSurfaceDestory() {
//        renderListener?.beforeSurfaceDestory()
//    }
//
//    override fun onSurfaceCreated() {
//        renderListener?.onSurfaceCreated()
//    }
//
//    override fun onGLDestroy() {
//        renderListener?.onGLDestroy()
//    }

    override fun enqueueTask(runnable: Runnable) {
        if (Thread.currentThread() == thread) {
            runnable.run()
            return
        }
        renderHander?.apply {
            synchronized(this) {
                taskQueue.add(runnable)
                sendEmptyMessage(MSG_TASK)
            }
        }

    }

    private fun handleTask() {
        synchronized(this) {
            while (!taskQueue.isEmpty()) {
                taskQueue.pollFirst()?.run()
            }

        }
    }

    override fun release() {
        try {
            thread?.apply {
                quit()
                join()
            }
            thread = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun renderReady(): Boolean {
        return viewHeight > 0 && viewWidth > 0 && haveSurface
    }

    inner class RenderHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            handleTask()
            eglCore?.apply {
                when (msg.what) {
                    MSG_SURFACE_CREATE -> {
                        eglSurface = createWindowSurface(msg.obj)
                        makeCurrent(eglSurface)
                        haveSurface = true
                        renderLock.lock()
                        renderCondition.signal()
                        renderLock.unlock()
                        onSurfaceCreated()
                    }
                    MSG_SURFACE_CHANGE -> {
                        viewWidth = msg.arg1
                        viewHeight = msg.arg2
                        onSurfaceChanged(viewWidth, viewHeight)
                        if (autoRender) {
                            requestRender()
                        }
                    }
                    MSG_SURFACE_DESTROY -> {
                        beforeSurfaceDestory()
                        releaseSurface(eglSurface)
                        makeCurrent(EGL14.EGL_NO_SURFACE)
                        renderLock.lock()
                        renderCondition.signal()
                        renderLock.unlock()
                        eglSurface = null
                        viewWidth = 0
                        viewHeight = 0
                        haveSurface = false
                        onSurfaceDestory()
                    }
                    MSG_RENDER -> {
                        if (renderReady()) {
                            onDrawFrame()
                            swapBuffers(eglSurface)
                            if (autoRender) {
                                requestRender()
                            }
                        }
                    }
                }
            }

        }
    }


    inner class GLThread : HandlerThread("GLThread") {
        override fun run() {
            eglCore = EglCore(
                null, EglCore.FLAG_TRY_GLES3
                        or EglCore.FLAG_RECORDABLE
            ).apply {
                makeCurrent(EGL14.EGL_NO_SURFACE)
            }
            onGLCreated()
            super.run()
            onGLDestroy()
            eglCore?.release()
            eglCore = null
        }
    }
}