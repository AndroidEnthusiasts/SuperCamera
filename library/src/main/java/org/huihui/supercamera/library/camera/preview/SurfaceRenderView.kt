package org.huihui.supercamera.library.camera.preview

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.huihui.supercamera.library.camera.render.IRender


/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/28 21:55
 */
class SurfaceRenderView : SurfaceView, IPreview, LifecycleEventObserver, SurfaceHolder.Callback {
    private var render: IRender? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        holder.addCallback(this)
    }

    override fun setRender(render: IRender) {
        this.render = render
        render.initRender()
    }

    override fun bindLifeCycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        render?.surfaceCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        render?.surfaceChange(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        render?.surfaceDestroy()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {

    }


}