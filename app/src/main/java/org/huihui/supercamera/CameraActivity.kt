package org.huihui.supercamera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.*
import org.huihui.supercamera.R

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
    }

    override fun onResume() {
        super.onResume()
        gls.onResume()
    }

    // TODO BUG 待解决
    override fun onPause() {
        super.onPause()
        gls.onPause()
    }

}