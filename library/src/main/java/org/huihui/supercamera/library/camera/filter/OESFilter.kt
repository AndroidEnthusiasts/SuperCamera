package org.huihui.supercamera.library.camera.filter

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 22:25
 */
class OESFilter : FBOBaseFilter() {

    override fun onDrawFrame(textureId: Int): Int {

        return outputTexture[0]
    }
}