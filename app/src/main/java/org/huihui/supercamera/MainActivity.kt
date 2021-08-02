package org.huihui.supercamera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.huihui.supercamera.databinding.ActivityMainBinding
import org.huihui.supercamera.library.camera.CameraEngine

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var engine: CameraEngine
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        engine = CameraEngine(preview = binding.gls, lifecycleOwner = this)
        binding.start.setOnClickListener {
            engine.startPreview()
        }
    }
}