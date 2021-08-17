package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import org.huihui.supercamera.library.camera.filter.utils.OpenGLUtils
import org.huihui.supercamera.library.util.loadAssertString
import java.nio.FloatBuffer

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 22:25
 */
class OESFilter
    : FBOBaseFilter(
    loadAssertString("filter/oesFilter.vs"),
    loadAssertString("filter/oesFilter.fs")
) {
    private lateinit var vertexPos: FloatBuffer
    private lateinit var textureCoord: FloatBuffer

    private var vPosLocation = -1
    private var vTexCoord = -1

    private var vbo = intArrayOf(GLES30.GL_NONE, GLES30.GL_NONE)
    private var vao = intArrayOf(GLES30.GL_NONE)

    override fun onInit() {
        super.onInit()
        modelLocation = 0
        viewLocation = 1
        projectionLocation = 2
        mvpLocation = 3
        inputTextureLocation = 4

        vPosLocation = 0
        vTexCoord = 1
        GLES30.glGenVertexArrays(1, vao, 0)

        GLES30.glGenBuffers(2, vbo, 0)


    }

    override fun inputTextureType(): Int {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    }

    override fun onSizeChanged(width: Int, height: Int, textureWidth: Int, textureHeight: Int) {
        super.onSizeChanged(width, height, textureWidth, textureHeight)
        vertexPos = OpenGLUtils.createFloatBuffer(VERTEX_POS)
        textureCoord = OpenGLUtils.createFloatBuffer(TEXTURE_COORD)

        GLES30.glBindVertexArray(vao[0])

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])

        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexPos.remaining() * 4, vertexPos, GLES30.GL_STATIC_DRAW)

        GLES30.glVertexAttribPointer(vPosLocation, 3, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glEnableVertexAttribArray(vPosLocation)


        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[1])

        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, textureCoord.remaining() * 4, textureCoord, GLES30.GL_STATIC_DRAW)

        GLES30.glVertexAttribPointer(vTexCoord, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glEnableVertexAttribArray(vTexCoord)

        GLES20.glUseProgram(programId)
//
//        GLES20.glUniformMatrix4fv(modelLocation, 1, false, model, 0)
//
//        GLES20.glUniformMatrix4fv(viewLocation, 1, false, view, 0)
//
//        GLES20.glUniformMatrix4fv(projectionLocation, 1, false, projection, 0)

        GLES20.glUniformMatrix4fv(mvpLocation, 1, false, mvp, 0)


    }

    override fun onDraw() {
        GLES30.glBindVertexArray(vao[0])
        OpenGLUtils.checkGlError("")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        OpenGLUtils.checkGlError("")

    }

    override fun release() {
        super.release()
        GLES30.glDeleteBuffers(2, vbo, 0)
        GLES30.glDeleteVertexArrays(1, vao, 0)

    }
}