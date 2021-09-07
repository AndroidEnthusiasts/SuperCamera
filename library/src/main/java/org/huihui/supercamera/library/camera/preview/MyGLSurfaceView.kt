package org.huihui.supercamera.library.camera.preview

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import org.huihui.supercamera.library.camera.camera.Camera2
import org.huihui.supercamera.library.camera.camera.CameraFilter
import org.huihui.supercamera.library.util.OpenGLUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 作者：丰雷
 * 时间：2021/8/4:4:07 下午
 * 说明：
 */
@RequiresApi(Build.VERSION_CODES.R)
class MyGLSurfaceView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : GLSurfaceView(context, attributeSet),
    GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private var mCamera: Camera2? = null
    private var mCameraFilter: CameraFilter? = null
    private var mTextureId: Int = -1
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mRatioWidth: Int = 0
    private var mRatioHeight: Int = 0

    init {
        init(context)
    }

    private fun init(context: Context){
        mCamera = Camera2()
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mTextureId = OpenGLUtils.getExternalOESTextureID()
        mSurfaceTexture = SurfaceTexture(mTextureId)
        mSurfaceTexture!!.setOnFrameAvailableListener(this)
        mCameraFilter = CameraFilter()
        mCamera!!.openCamera(context as Activity, width, height)
        mCamera!!.setPreviewSurface(mSurfaceTexture!!)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val previewWidth = mCamera!!.getPreviewWidth()
        val previewHeight = mCamera!!.getPreviewHeight()
        if (width > height) {
            setAspectRatio(previewWidth, previewHeight)
        } else {
            setAspectRatio(previewHeight, previewWidth)
        }
        GLES30.glViewport(0, 0, width, height)
    }


    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        mCamera!!.closeCamera()
    }

    private fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        mRatioWidth = width
        mRatioHeight = height
        // 保证执行在主线程
        post {
            requestLayout()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        mSurfaceTexture!!.updateTexImage()
        mCameraFilter!!.draw(mTextureId, mCamera!!.isFrontCamera())
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        requestRender()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (mRatioWidth == 0 || mRatioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth)
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height)
            }
        }
    }

}