package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES10
import android.opengl.GLES20
import android.opengl.GLES30
import androidx.annotation.CallSuper

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 23:09
 */
abstract class FBOBaseFilter : BaseFilter() {
    protected var outputTexture = intArrayOf(GLES20.GL_NONE)

    @CallSuper
    override fun onInit() {
        GLES10.glGenTextures(1, outputTexture, 0)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        GLES10.glDeleteTextures(1, outputTexture, 0)
    }
}