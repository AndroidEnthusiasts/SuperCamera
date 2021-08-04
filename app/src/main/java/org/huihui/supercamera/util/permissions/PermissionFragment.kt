package org.huihui.supercamera.util.permissions

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData

/**
 * 作者：丰雷
 * 时间：2021/8/3:5:48 下午
 * 说明：权限申请 fragment
 */
class PermissionFragment : Fragment() {

    private val PERMISSIONS_RESULT_CODE = 200

    lateinit var liveData: MutableLiveData<PermissionResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        returnTransition = true
    }

    /**
     * 获取权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(permissions: Array<out String>) {
        liveData = MutableLiveData()
        val tempPermissions = ArrayList<String>()
        permissions.forEach {
            // 逐个权限是否已被申请
            if (activity?.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                tempPermissions.add(it)
            }
        }
        if (tempPermissions.isEmpty()) {
            liveData.value = PermissionResult.Grant
        } else {
            requestPermissions(tempPermissions.toTypedArray(), PERMISSIONS_RESULT_CODE)
        }
    }

    /**
     * 获取权限的回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_RESULT_CODE) {
            val denyPermissions = ArrayList<String>()
            val rationalePermissions = ArrayList<String>()
            for ((index, value) in grantResults.withIndex()) {
                if (value == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(permissions[index])) {
                        rationalePermissions.add(permissions[index])
                    } else {
                        denyPermissions.add(permissions[index])
                    }
                }
            }
            if (denyPermissions.isEmpty() && rationalePermissions.isEmpty()) {
                liveData.value = PermissionResult.Grant
            } else {
                if (rationalePermissions.isNotEmpty()) {
                    liveData.value = PermissionResult.Rationale(rationalePermissions.toTypedArray())
                } else if (denyPermissions.isNotEmpty()) {
                    liveData.value = PermissionResult.Deny(denyPermissions.toTypedArray())
                }
            }
        }
    }
}