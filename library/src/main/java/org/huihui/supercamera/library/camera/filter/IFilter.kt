package org.huihui.supercamera.library.camera.filter

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 16:54
 */
interface IFilter {

    fun onInit()

    fun inputTextureType():Int

    fun onDrawFrame(textureId: Int): Int

    fun onSizeChanged(width: Int, height: Int, textureWidth: Int, textureHeight: Int)

    fun release()
}