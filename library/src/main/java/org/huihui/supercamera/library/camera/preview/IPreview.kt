package org.huihui.supercamera.library.camera.preview

import androidx.lifecycle.LifecycleOwner
import org.huihui.supercamera.library.camera.render.IRender

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/28 21:17
 */
interface IPreview {

    fun setRender(render: IRender)

    fun bindLifeCycle(lifecycleOwner: LifecycleOwner)
}