package org.huihui.supercamera.library.camera.camera

import android.graphics.SurfaceTexture
import android.hardware.Camera
import java.util.*
import kotlin.math.abs


/*
 * @Description: 
 * @version 1.0
 * @author 陈松辉
 * @date 2021/7/22 17:32
 */
class Camera1 : AbsCamera(), Camera.PreviewCallback {

    private var mCameraId = -1

    private var mCamera: Camera? = null

    //    surfaceTexture: SurfaceTexture
    init {
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
    }

    override fun openCamera() {
        if (mCamera != null) {
            closeCamera()
        }
        mCamera = Camera.open(mCameraId)
    }

    private fun caculatePreviewSize(expectWidth: Int, expectHeight: Int): Camera.Size {
        mCamera!!.apply {
            val sizes = parameters.supportedPreviewSizes
            val calculateType = CalculateType.Lower
            val ratio = expectWidth.toFloat() / expectHeight
            sortList(sizes) // 根据宽度进行排序

            // 根据当前期望的宽高判定

            // 根据当前期望的宽高判定
            val bigEnough: MutableList<Camera.Size> = ArrayList()
            val noBigEnough: MutableList<Camera.Size> = ArrayList()
            for (size in sizes) {
                if (size.height * expectWidth / expectHeight == size.width) {
                    if (size.width > expectWidth && size.height > expectHeight) {
                        bigEnough.add(size)
                    } else {
                        noBigEnough.add(size)
                    }
                }
            }
            // 根据计算类型判断怎么如何计算尺寸
            // 根据计算类型判断怎么如何计算尺寸
            var perfectSize: Camera.Size? = null
            when (calculateType) {
                CalculateType.Min ->                 // 不大于期望值的分辨率列表有可能为空或者只有一个的情况，
                    // Collections.min会因越界报NoSuchElementException
                    if (noBigEnough.size > 1) {
                        perfectSize = Collections.min(noBigEnough, CompareAreaSize())
                    } else if (noBigEnough.size == 1) {
                        perfectSize = noBigEnough[0]
                    }
                CalculateType.Max ->                 // 如果bigEnough只有一个元素，使用Collections.max就会因越界报NoSuchElementException
                    // 因此，当只有一个元素时，直接使用该元素
                    if (bigEnough.size > 1) {
                        perfectSize = Collections.max(bigEnough, CompareAreaSize())
                    } else if (bigEnough.size == 1) {
                        perfectSize = bigEnough[0]
                    }
                CalculateType.Lower ->                 // 优先查找比期望尺寸小一点的，否则找大一点的，接受范围在0.8左右
                    if (noBigEnough.size > 0) {
                        val size = Collections.max(noBigEnough, CompareAreaSize())
                        if (size.width.toFloat() / expectWidth >= 0.8
                            && size.height.toFloat() / expectHeight > 0.8
                        ) {
                            perfectSize = size
                        }
                    } else if (bigEnough.size > 0) {
                        val size = Collections.min(bigEnough, CompareAreaSize())
                        if (expectWidth.toFloat() / size.width >= 0.8
                            && (expectHeight / size.height).toFloat() >= 0.8
                        ) {
                            perfectSize = size
                        }
                    }
                CalculateType.Larger ->                 // 优先查找比期望尺寸大一点的，否则找小一点的，接受范围在0.8左右
                    if (bigEnough.size > 0) {
                        val size = Collections.min(bigEnough, CompareAreaSize())
                        if (expectWidth.toFloat() / size.width >= 0.8
                            && (expectHeight / size.height).toFloat() >= 0.8
                        ) {
                            perfectSize = size
                        }
                    } else if (noBigEnough.size > 0) {
                        val size = Collections.max(noBigEnough, CompareAreaSize())
                        if (size.width.toFloat() / expectWidth >= 0.8
                            && size.height.toFloat() / expectHeight > 0.8
                        ) {
                            perfectSize = size
                        }
                    }
            }
            // 如果经过前面的步骤没找到合适的尺寸，则计算最接近expectWidth * expectHeight的值
            // 如果经过前面的步骤没找到合适的尺寸，则计算最接近expectWidth * expectHeight的值
            if (perfectSize == null) {
                var result: Camera.Size = sizes[0]
                var widthOrHeight = false // 判断存在宽或高相等的Size
                // 辗转计算宽高最接近的值
                for (size in sizes) {
                    // 如果宽高相等，则直接返回
                    if (size.width == expectWidth && size.height == expectHeight && size.height.toFloat() / size.width.toFloat() == ratio) {
                        result = size
                        break
                    }
                    // 仅仅是宽度相等，计算高度最接近的size
                    if (size.width == expectWidth) {
                        widthOrHeight = true
                        if (abs(result.height - expectHeight) > abs(size.height - expectHeight)
                            && size.height.toFloat() / size.width.toFloat() == ratio
                        ) {
                            result = size
                            break
                        }
                    } else if (size.height == expectHeight) {
                        widthOrHeight = true
                        if (abs(result.width - expectWidth) > abs(size.width - expectWidth)
                            && size.height.toFloat() / size.width.toFloat() == ratio
                        ) {
                            result = size
                            break
                        }
                    } else if (!widthOrHeight) {
                        if (abs(result.width - expectWidth) > abs(size.width - expectWidth) && abs(result.height - expectHeight) > abs(
                                size.height - expectHeight
                            ) && size.height.toFloat() / size.width.toFloat() == ratio
                        ) {
                            result = size
                        }
                    }
                }
                perfectSize = result
            }
            return perfectSize
        }
    }

    override fun closeCamera() {
        mCamera?.apply {
            this@Camera1.stopPreview()
            release()
        }
        mCamera = null
    }

    override fun startPreview(surfaceTexture: SurfaceTexture) {
        mCamera?.apply {
            val size = caculatePreviewSize(getPreviewWidth(), getPreviewHeight())
            setPreviewHeight(size.height)
            setPreviewWidth(size.width)
            val params = parameters.apply {
                setPreviewSize(size.width, size.height)
            }
            parameters = params
            setPreviewTexture(surfaceTexture)
            setPreviewCallback(this@Camera1)
            startPreview()
        }
    }

    override fun stopPreview() {
        mCamera?.apply {
            stopPreview()
            setPreviewTexture(null)
            setPreviewCallback(null)
        }
    }

    override fun getPreviewFormat() {
    }

    override fun switchCamera(surfaceTexture: SurfaceTexture) {
        closeCamera()
        toogleFont()
        mCameraId = if (isFont()) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
        openCamera()
        startPreview(surfaceTexture)
    }

    override fun takePicture() {
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        data?.apply {
            frameListener?.onFrameAvailable(data)
        }
    }

    /**
     * 分辨率由大到小排序
     * @param list
     */
    private fun sortList(list: List<Camera.Size>) {
        Collections.sort(list, CompareAreaSize())
    }

    /**
     * 比较器
     */
    class CompareAreaSize : Comparator<Camera.Size> {
        override fun compare(pre: Camera.Size, after: Camera.Size): Int {
            return java.lang.Long.signum(
                pre.width.toLong() * pre.height -
                        after.width.toLong() * after.height
            )
        }
    }

    internal enum class CalculateType {
        Min,  // 最小
        Max,  // 最大
        Larger,  // 大一点
        Lower
        // 小一点
    }


}