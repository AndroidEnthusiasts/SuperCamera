package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES20

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 23:07
 */
class DisplayFilter : BaseFilter() {

    override fun onInit() {

    }

    override fun onDrawFrame(): Int {
        return GLES20.GL_NONE
    }
}