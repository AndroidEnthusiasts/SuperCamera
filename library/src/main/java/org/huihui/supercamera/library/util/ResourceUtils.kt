package org.huihui.supercamera.library.util

import android.app.Application
import android.content.Context

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/11 21:51
 */

lateinit var gContex: Application

fun init(context: Context) {
    gContex = context.applicationContext as Application
}

fun loadRawString(resId: Int): String {
    return gContex.resources.openRawResource(resId).readBytes().toString(Charsets.UTF_8)
}

fun loadAssertString(path: String): String {
    return gContex.assets.open(path).readBytes().toString(Charsets.UTF_8)
}