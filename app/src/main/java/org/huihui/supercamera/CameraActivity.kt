package org.huihui.supercamera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.huihui.supercamera.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.gls.onResume()
    }

    // TODO BUG 待解决
    override fun onPause() {
        super.onPause()
        binding.gls.onPause()
    }

}