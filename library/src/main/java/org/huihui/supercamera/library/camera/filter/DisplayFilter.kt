package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES20
import android.opengl.GLES30
import org.huihui.supercamera.library.camera.filter.utils.OpenGLUtils
import org.huihui.supercamera.library.util.loadAssertString
import java.nio.FloatBuffer

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 23:07
 */
class DisplayFilter : BaseFilter(
    loadAssertString("filter/displayFilter.vs"),
    loadAssertString("filter/displayFilter.fs")
) {
    private var viewLocation = -1
    private var modelLocation = -1
    private var projectionLocation = -1
    private var mvpLocation = -1

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

    override fun onSizeChanged(width: Int, height: Int, textureWidth: Int, textureHeight: Int) {
        super.onSizeChanged(width, height, textureWidth, textureHeight)
        vertexPos = OpenGLUtils.createFloatBuffer(VERTEX_POS)
        textureCoord = OpenGLUtils.createFloatBuffer(TEXTURE_COORD)

        GLES30.glBindVertexArray(vao[0])

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])

        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexPos.limit() * 4, vertexPos, GLES30.GL_STATIC_DRAW)

        GLES30.glVertexAttribPointer(vPosLocation, 3, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glEnableVertexAttribArray(vPosLocation)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[1])


        GLES20.glUseProgram(programId)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, textureCoord.limit() * 4, textureCoord, GLES30.GL_STATIC_DRAW)

        GLES30.glVertexAttribPointer(vTexCoord, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glEnableVertexAttribArray(vTexCoord)

//        GLES20.glUniformMatrix4fv(modelLocation, 1, false, model, 0)
//
//        GLES20.glUniformMatrix4fv(viewLocation, 1, false, view, 0)
//
//        GLES20.glUniformMatrix4fv(projectionLocation, 1, false, projection, 0)

        GLES20.glUniformMatrix4fv(mvpLocation, 1, false, mvp, 0)

    }

    override fun onDrawFrame(textureId: Int): Int {
        GLES20.glUseProgram(programId)
        GLES20.glViewport(0, 0, textureWidth, textureHeight)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glUniform1i(inputTextureLocation, 0)

        GLES30.glBindVertexArray(vao[0])

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glUseProgram(0)
        return GLES20.GL_NONE
    }
    override fun release() {
        super.release()
        GLES30.glDeleteBuffers(2, vbo, 0)
        GLES30.glDeleteVertexArrays(1, vao, 0)
    }
}