package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES10
import android.opengl.GLES20
import androidx.annotation.CallSuper
import org.huihui.supercamera.library.camera.filter.utils.OpenGLUtils

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 23:09
 */
abstract class FBOBaseFilter(vertexShander: String, fragmentShader: String) : BaseFilter(vertexShander, fragmentShader) {
    protected var frameWidth: Int = 0
    protected var frameHeight: Int = 0

    protected var outputTexture = intArrayOf(GLES20.GL_NONE)
    protected var frameBuffer = intArrayOf(GLES20.GL_NONE)

    protected var viewLocation = -1
    protected var modelLocation = -1
    protected var projectionLocation = -1
    protected var mvpLocation = -1

    @CallSuper
    override fun onInit() {
        super.onInit()
        GLES10.glGenTextures(1, outputTexture, 0)

        GLES20.glGenFramebuffers(1, frameBuffer, 0)

    }

    override fun onSizeChanged(width: Int, height: Int, textureWidth: Int, textureHeight: Int) {
        super.onSizeChanged(width, height, textureWidth, textureHeight)
        frameWidth = width
        frameHeight = height

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, outputTexture[0])

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureWidth, textureHeight, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])

        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
            outputTexture[0], 0
        )

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

    }

    private fun bindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])

    }

    private fun unbindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

    }

    private fun drawFrameBuffer(textureId: Int) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(inputTextureType(), textureId)
        OpenGLUtils.checkGlError("")
        GLES20.glUniform1i(inputTextureLocation, 0)
        OpenGLUtils.checkGlError("")
        onDraw()
        GLES20.glBindTexture(inputTextureType(), 0)
    }

    abstract fun onDraw()

    @CallSuper
    override fun onDrawFrame(textureId: Int): Int {
        GLES20.glUseProgram(programId)
//        GLES20.glUniformMatrix4fv(mvpLocation, 1, false, mvp, 0)
//        GLES20.glUniformMatrix4fv(modelLocation, 1, false, model, 0)
//        GLES20.glUniformMatrix4fv(viewLocation, 1, false, view, 0)
//        GLES20.glUniformMatrix4fv(projectionLocation, 1, false, projection, 0)
        GLES20.glViewport(0, 0, textureWidth, textureHeight)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        bindFrameBuffer()
        drawFrameBuffer(textureId)
        unbindFrameBuffer()
        GLES20.glUseProgram(0)
        return outputTexture[0]
    }

    @CallSuper
    override fun release() {
        super.release()
        GLES20.glDeleteTextures(1, outputTexture, 0)
        GLES20.glDeleteFramebuffers(1, frameBuffer, 0)
    }
}