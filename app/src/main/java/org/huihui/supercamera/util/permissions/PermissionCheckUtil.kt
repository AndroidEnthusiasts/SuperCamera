package org.huihui.supercamera.util.permissions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData

/**
 * 作者：丰雷
 * 时间：2021/8/3:5:48 下午
 * 说明：创建 fragment 并申请权限
 */
class PermissionCheckUtil {

    companion object {
        const val TAG = "permissions"
    }

    @Volatile
    private var permissionFragment: PermissionFragment? = null

    constructor(activity: AppCompatActivity) {
        permissionFragment = getInstance(activity.supportFragmentManager)
    }

    constructor(fragment: Fragment) {
        permissionFragment = getInstance(fragment.childFragmentManager)
    }

    private fun getInstance(fragmentManager: FragmentManager) =
        permissionFragment ?: synchronized(this) {
            permissionFragment ?: if (fragmentManager.findFragmentByTag(TAG) == null) PermissionFragment().run {
                    fragmentManager.beginTransaction().add(this, TAG).commitNow()
                    this
                } else fragmentManager.findFragmentByTag(TAG) as PermissionFragment
        }

    fun request(vararg permissions: String): MutableLiveData<PermissionResult> {
        return this.requestArray(permissions)
    }

    fun requestArray(permissions: Array<out String>): MutableLiveData<PermissionResult> {
        permissionFragment!!.requestPermissions(permissions)
        return permissionFragment!!.liveData
    }
}