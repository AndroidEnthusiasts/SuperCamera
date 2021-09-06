package org.huihui.supercamera.library.util

import java.util.concurrent.locks.Lock

/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/9/2 14:01
 */
inline fun <R> lock(lock: Lock, block: () -> R): R {
    try {
        lock.lock()
        return block()
    } finally {
        lock.unlock()
    }
}