package org.huihui.supercamera.library.camera.preview

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.huihui.supercamera.library.camera.render.IRender

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/9/7 9:28
 */
class TextureRenderView : TextureView, IPreview, LifecycleEventObserver, TextureView.SurfaceTextureListener {
    private var render: IRender? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        surfaceTextureListener = this
    }

    override fun setRender(render: IRender) {
        this.render = render
        render.initRender()
    }

    override fun bindLifeCycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        render?.surfaceCreated(surface)
        render?.surfaceChange(width,height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        render?.surfaceChange(width,height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        render?.surfaceDestroy()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }
}