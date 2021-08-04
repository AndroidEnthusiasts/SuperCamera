package org.huihui.supercamera.util.permissions

/**
 * 作者：丰雷
 * 时间：2021/8/3:5:43 下午
 * 说明：
 */
sealed class PermissionResult {
    // 申请成功
    object Grant: PermissionResult()
    // 被拒绝的权限
    class Rationale(val permissions: Array<String>) : PermissionResult()
    // 被拒绝且不再提醒的权限
    class Deny(val permissions: Array<String>) : PermissionResult()
}