package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES20
import android.opengl.Matrix
import androidx.annotation.CallSuper
import org.huihui.supercamera.library.camera.filter.utils.OpenGLUtils

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 17:57
 */
abstract class BaseFilter(
    private var vertexShander: String, private var fragmentShader: String
) : IFilter {
    companion object {
        val VERTEX_POS = floatArrayOf(
            -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f
        )
        val TEXTURE_COORD = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        )
    }

    protected var viewWidth: Int = 0

    protected var viewHeight: Int = 0

    protected var textureWidth: Int = 0

    protected var textureHeight: Int = 0

    protected var programId: Int = 0

    protected var inputTextureLocation: Int = -1

    protected val view = FloatArray(16)
    protected val model = FloatArray(16)
    protected val projection = FloatArray(16)
    protected val mvp = FloatArray(16)
//
//    protected var vertexPosLocation: Int = -1
//
//    protected var textureCoordinateLocation: Int = -1

    @CallSuper
    override fun onInit() {
        programId = OpenGLUtils.createProgram(vertexShander, fragmentShader)
        Matrix.setIdentityM(view, 0)
        Matrix.setIdentityM(model, 0)
        Matrix.setIdentityM(projection, 0)
        Matrix.setIdentityM(mvp, 0)
    }

    @CallSuper
    override fun onSizeChanged(width: Int, height: Int, textureWidth: Int, textureHeight: Int) {
        viewWidth = width
        viewHeight = height
        this.textureWidth = textureWidth
        this.textureHeight = textureHeight
    }

    override fun inputTextureType():Int {
        return GLES20.GL_TEXTURE_2D
    }

    @CallSuper
    override fun release() {
        viewWidth = 0
        viewHeight = 0
        textureWidth = 0
        textureHeight = 0
        GLES20.glDeleteProgram(programId)
    }


}