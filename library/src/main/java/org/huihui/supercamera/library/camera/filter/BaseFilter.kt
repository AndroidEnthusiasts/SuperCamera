package org.huihui.supercamera.library.camera.filter

import android.opengl.GLES10
import android.opengl.GLES30
import androidx.annotation.CallSuper

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/2 17:57
 */
abstract class BaseFilter : IFilter {

    protected var viewWidth: Int = 0

    protected var viewHeight: Int = 0

    protected var textureWidth: Int = 0

    protected var textureHeight: Int = 0

    @CallSuper
    override fun onSizeChanged(width: Int, height: Int, textureWidth: Int, textureHeight: Int) {
        viewWidth = width
        viewHeight = height
        this.textureWidth = textureWidth
        this.textureHeight = textureWidth
    }

    @CallSuper
    override fun onDestroy() {
        viewWidth = 0
        viewHeight = 0
        textureWidth = 0
        textureHeight = 0
    }


}