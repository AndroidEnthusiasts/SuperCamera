package org.huihui.supercamera

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import org.huihui.supercamera.util.permissions.PermissionCheckUtil
import org.huihui.supercamera.util.permissions.PermissionResult

@RequiresApi(Build.VERSION_CODES.R)
class MainActivity : AppCompatActivity() {

//    lateinit var binding: ActivityMainBinding

    //    lateinit var engine: CameraEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 权限获取
        requestPermissions()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        engine = CameraEngine(preview = binding.gls, lifecycleOwner = this)
//        binding.start.setOnClickListener {
//            engine.startPreview()
//        }

        camera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
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