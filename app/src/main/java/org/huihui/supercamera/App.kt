package org.huihui.supercamera

import android.app.Application
import org.huihui.supercamera.library.util.init

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/8/11 22:02
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
    }
}