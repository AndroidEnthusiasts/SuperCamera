package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES20

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 22:25
 */
class OESFilter : FBOBaseFilter() {
    override fun onInit() {
        super.onInit()


    }

    override fun onDrawFrame(textureId: Int): Int {

        return GLES20.GL_NONE
    }
}