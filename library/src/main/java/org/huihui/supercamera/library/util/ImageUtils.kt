package org.huihui.supercamera.library.util

import android.media.Image

/**
 * 作者：丰雷
 * 时间：2021/8/3:9:17 下午
 * 说明：
 */
object ImageUtils {

    fun getBytesFromImageYV12ToNV21(image: Image?): ByteArray? {
        try {
            val planes = image!!.planes

            /*String TAG = "getBytesFromImageToNV21";
            for (int i = 0; i < planes.length; i++) {
                ByteBuffer iBuffer = planes[i].getBuffer();
                int iSize = iBuffer.remaining();
                Log.i(TAG, "pixelStride = " + planes[i].getPixelStride());
                Log.i(TAG, "rowStride = " + planes[i].getRowStride());
                Log.i(TAG, "iSize = " + iSize);
                Log.i(TAG, "Finished reading data from plane = " + i);
            }
            if (true)return null;*/
            val pictureSize = image.width * image.height
            val nv21 = ByteArray(pictureSize * 3 / 2)
            var index = 0
            // nv21 data like YYYYVUVU, image format is YV12:YYYYVVUU, y placed on planes[0], u on planes[1], v on planes[2]
            // image format decide by DisplayFragment.mFormat
            if (planes.size > 2) {
                // y
                val buffer = planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer[bytes]
                System.arraycopy(bytes, 0, nv21, 0, pictureSize)
                // v pixel stride is equal u pixel stride
                val uvPixelStride = planes[2].pixelStride
                index = pictureSize
                // u
                val uBuffer = planes[1].buffer
                val uBytes = ByteArray(uBuffer.capacity())
                uBuffer[uBytes]

                // v
                val vBuffer = planes[2].buffer
                val vBytes = ByteArray(vBuffer.capacity())
                vBuffer[vBytes]
                // v bytes length is equal u bytes length
                var uv = 0
                while (uv < vBytes.size) {
                    nv21[index++] = vBytes[uv]
                    nv21[index++] = uBytes[uv]
                    uv += uvPixelStride
                }
            }
            return nv21
        } catch (e: Exception) {
            println("ImageUtils:${e.message}")
        } finally {
            image?.close()
        }
        return null
    }

}