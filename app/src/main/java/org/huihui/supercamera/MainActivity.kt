package org.huihui.supercamera

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.lifecycle.Observer
import org.huihui.supercamera.databinding.ActivityMainBinding
import org.huihui.supercamera.library.camera.CameraEngine
import org.huihui.supercamera.library.camera.record.AudioRecorder
import org.huihui.supercamera.library.camera.record.CameraRecoder
import org.huihui.supercamera.util.permissions.PermissionCheckUtil
import org.huihui.supercamera.util.permissions.PermissionResult
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var engine: CameraEngine? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.start.setOnClickListener {
            engine?.startPreview()
        }
        binding.stop.setOnClickListener {
            engine?.stopPreview()
        }
        val file = File(
            Environment.getExternalStorageDirectory().absolutePath,
            "/superCamera"
        )
        file.mkdirs()


        binding.record.setOnClickListener {
            val path = File(file, "/test_${System.currentTimeMillis()}.mp4").absolutePath
            engine?.startRecord(path)
        }
        binding.stoprecord.setOnClickListener {
            engine?.stopRecord()
        }
        requestPermissions()
    }

    private fun requestPermissions() {
        PermissionCheckUtil(this).request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).observe(this, Observer {
            when (it) {
                is PermissionResult.Grant -> {
                    // 权限允许
                    engine = CameraEngine(applicationContext, preview = binding.gls, lifecycleOwner = this)
                    println("权限获取成功")
                }
                is PermissionResult.Rationale -> {
                    // 权限拒绝
                    it.permissions.forEach { s ->
                        println("拒绝的权限:${s}")
                    }
                }
                is PermissionResult.Deny -> {
                    // 权限拒绝并勾选了不再询问
                    it.permissions.forEach { s ->
                        println("权限拒绝并勾选了不再询问:${s}")
                    }
                }
            }
        })
    }
}