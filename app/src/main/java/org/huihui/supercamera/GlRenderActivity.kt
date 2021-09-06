package org.huihui.supercamera

import android.opengl.GLES20
import android.opengl.GLES30
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.huihui.supercamera.databinding.ActivityGlRenderBinding
import org.huihui.supercamera.library.camera.filter.utils.OpenGLUtils
import org.huihui.supercamera.library.camera.render.AbsRender
import org.huihui.supercamera.library.camera.render.IRender
import org.huihui.supercamera.library.util.loadAssertString
import java.nio.FloatBuffer

class GlRenderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityGlRenderBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.sr.setRender(object : AbsRender() {
            var programId = 0
            var vao = intArrayOf(GLES20.GL_NONE)
            var vbo = intArrayOf(GLES20.GL_NONE)
            var pos = floatArrayOf(
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
            )

            override fun onGLCreated() {
                autoRender = true
                programId = OpenGLUtils.createProgram(
                    loadAssertString("triangle.vs"),
                    loadAssertString("triangle.fs")
                )
                GLES30.glGenVertexArrays(1, vao, 0)
                GLES30.glGenBuffers(1, vbo, 0)

                GLES30.glBindVertexArray(vao[0])
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
                GLES30.glBufferData(
                    GLES30.GL_ARRAY_BUFFER, pos.size * 4,
                    FloatBuffer.wrap(pos), GLES30.GL_STATIC_DRAW
                )
                GLES30.glVertexAttribPointer(
                    0, 3, GLES30.GL_FLOAT, false,
                    0, 0
                )
                GLES30.glEnableVertexAttribArray(0)
                OpenGLUtils.checkGlError("")
            }

            override fun onSurfaceCreated() {

            }

            override fun onSurfaceChanged(width: Int, height: Int) {

            }

            override fun onDrawFrame() {
                GLES20.glViewport(0, 0, viewWidth, viewHeight)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glUseProgram(programId)
                GLES30.glBindVertexArray(vao[0])
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
                OpenGLUtils.checkGlError("")
            }

            override fun beforeSurfaceDestory() {
                TODO("Not yet implemented")
            }

            override fun onSurfaceDestory() {
                TODO("Not yet implemented")
            }

            override fun onGLDestroy() {

            }

            override var renderListener: IRender.IRenderListener? = null
        })
    }
}