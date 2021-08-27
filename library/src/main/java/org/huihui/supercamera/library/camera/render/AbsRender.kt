package org.huihui.supercamera.library.camera.render

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLSurface
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.view.Surface
import org.huihui.supercamera.library.camera.egl.EglCore
import java.util.*

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/23 9:40
 */
abstract class AbsRender : IRender {
    companion object {
        const val MSG_TASK = 0x00
        const val MSG_SURFACE_CREATE = 0x01
        const val MSG_SURFACE_CHANGE = 0x02
        const val MSG_SURFACE_DESTROY = 0x03
        const val MSG_RENDER = 0x04
    }

    protected var viewWidth: Int = 0
    protected var viewHeight: Int = 0
    protected var eglCore: EglCore? = null
    protected var eglSurface: EGLSurface? = null
    private var taskQueue = LinkedList<Runnable>()

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

    override fun surfaceCreated(surface: Surface) {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_CREATE, surface))
        }
    }

    override fun surfaceChange(width: Int, height: Int) {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_CHANGE, width, height))
        }
    }

    override fun surfaceCreated(surfaceTexture: SurfaceTexture) {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_CREATE, surfaceTexture))
        }
    }

    override fun surfaceDestroy() {
        renderHander?.apply {
            sendMessage(Message.obtain(this, MSG_SURFACE_DESTROY))
        }
    }

    override fun requestRender() {
        renderHander?.apply {
            if (haveSurface) {
                sendMessage(Message.obtain(this, MSG_RENDER))
            }
        }
    }

    override fun onGLCreated() {

    }

    override fun onSurfaceChanged(width: Int, height: Int) {

    }

    override fun onSurfaceDestory() {

    }

    override fun beforeSurfaceDestory() {
    }

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

    inner class RenderHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            handleTask()
            eglCore?.apply {
                when (msg.what) {
                    MSG_SURFACE_CREATE -> {
                        eglSurface = createWindowSurface(msg.obj)
                        makeCurrent(eglSurface)
                        haveSurface = true
                        onSurfaceCreated()
                    }
                    MSG_SURFACE_CHANGE -> {
                        viewWidth = msg.arg1
                        viewHeight = msg.arg2
                        onSurfaceChanged(viewWidth, viewHeight)
                    }
                    MSG_SURFACE_DESTROY -> {
                        beforeSurfaceDestory()
                        makeCurrent(EGL14.EGL_NO_SURFACE)
                        releaseSurface(eglSurface)
                        eglSurface = null
                        viewWidth = 0
                        viewHeight = 0
                        haveSurface = false
                        onSurfaceDestory()
                    }
                    MSG_RENDER -> {
                        if (haveSurface) {
                            onDrawFrame()
                            swapBuffers(eglSurface)
                        }
                    }
                }
            }

        }
    }

    inner class GLThread : HandlerThread("GLThread") {
        override fun run() {
            eglCore = EglCore(null, EglCore.FLAG_TRY_GLES3).apply {
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