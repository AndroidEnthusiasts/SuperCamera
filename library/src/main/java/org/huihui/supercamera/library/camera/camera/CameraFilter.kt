package org.huihui.supercamera.library.camera.camera

import android.opengl.GLES11Ext
import android.opengl.GLES30
import org.huihui.supercamera.library.util.OpenGLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 作者：丰雷
 * 时间：2021/8/4:4:36 下午
 * 说明：
 */
class CameraFilter {

    private val VERTEX_SHADER = "" +
            "attribute vec4 vPosition;" +
            "attribute vec2 inputTextureCoordinate;" +
            "varying vec2 textureCoordinate;" +
            "void main()" +
            "{" +
            "gl_Position = vPosition;" +
            "textureCoordinate = inputTextureCoordinate;" +
            "}"

    private val FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES s_texture;\n" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
            "}"

    private var mVertexBuffer: FloatBuffer? = null
    private var mBackTextureBuffer: FloatBuffer? = null
    private var mFrontTextureBuffer: FloatBuffer? = null
    private var mDrawListBuffer: ByteBuffer? = null
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mTextureHandle = 0

    private val VERTEX_ORDER = byteArrayOf(0, 1, 2, 3)
    private val VERTEX_SIZE = 2
    private val VERTEX_STRIDE = VERTEX_SIZE * 4

    private val VERTEXES = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f,
        1.0f, 1.0f
    )

    // 后置摄像头使用的纹理坐标
    private val TEXTURE_BACK = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f
    )

    // 前置摄像头使用的纹理坐标
    private val TEXTURE_FRONT = floatArrayOf(
        1.0f, 1.0f,
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )

    init {
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEXES.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mVertexBuffer!!.put(VERTEXES).position(0)

        mBackTextureBuffer =
            ByteBuffer.allocateDirect(TEXTURE_BACK.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        mBackTextureBuffer!!.put(TEXTURE_BACK).position(0)
        mFrontTextureBuffer =
            ByteBuffer.allocateDirect(TEXTURE_FRONT.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        mFrontTextureBuffer!!.put(TEXTURE_FRONT).position(0)

        mDrawListBuffer =
            ByteBuffer.allocateDirect(VERTEX_ORDER.size).order(ByteOrder.nativeOrder())
        mDrawListBuffer!!.put(VERTEX_ORDER).position(0)

        val shaderId = OpenGLUtils.compileVertexShader(VERTEX_SHADER)
        val fragmentId = OpenGLUtils.compileFragmentShader(FRAGMENT_SHADER)
        mProgram = OpenGLUtils.linkProgram(shaderId, fragmentId)
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")
        mTextureHandle = GLES30.glGetAttribLocation(mProgram, "inputTextureCoordinate")
    }

    fun draw(texture: Int, isFrontCamera: Boolean) {
        GLES30.glUseProgram(mProgram) // 指定使用的program
        GLES30.glEnable(GLES30.GL_CULL_FACE) // 启动剔除
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture) // 绑定纹理
        GLES30.glEnableVertexAttribArray(mPositionHandle)
        GLES30.glVertexAttribPointer(
            mPositionHandle,
            VERTEX_SIZE,
            GLES30.GL_FLOAT,
            false,
            VERTEX_STRIDE,
            mVertexBuffer
        )
        GLES30.glEnableVertexAttribArray(mTextureHandle)
        if (isFrontCamera) {
            GLES30.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mFrontTextureBuffer)
        } else {
            GLES30.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mBackTextureBuffer)
        }
        // 真正绘制的操作
        GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, VERTEX_ORDER.size, GLES30.GL_UNSIGNED_BYTE, mDrawListBuffer)
        GLES30.glDisableVertexAttribArray(mPositionHandle)
        GLES30.glDisableVertexAttribArray(mTextureHandle)
    }
}