package org.huihui.supercamera.library.camera.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import org.huihui.supercamera.library.util.ImageUtils
import java.util.*
import kotlin.math.abs


/**
 * 作者：丰雷
 * 时间：2021/8/3:5:15 下午
 * 说明：
 */
@RequiresApi(Build.VERSION_CODES.R)
class Camera2 {

    private var mActivity: Activity? = null

    /**  Camera 管理类 */
    private var mCameraManager: CameraManager? = null

    /**  Camera 实体对象  */
    private var mCameraDevice: CameraDevice? = null

    /**  相机预览尺寸  */
    private var mPreviewSize: Size? = null

    /**  默认打开后置摄像头 */
    private var mCameraId: String? = null

    /**  处理静态图像的 ImageReader */
    private var mImageReader: ImageReader? = null

    /** 相机预览会话 */
    private var mCaptureSession: CameraCaptureSession? = null

    /** 用于相机预览请求的构造器 */
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    /** 显示的 Surface */
    private var mPreviewSurface: Surface? = null

    /** 是否支持闪光灯 */
    private var mFlashSupported = false

    /** 人脸检测级别 */
    private var mFaceDetectModes: IntArray? = null

    /** 预览格式 */
    private val previewFormat = ImageFormat.JPEG

    /** 用于后台任务的 Handler */
    private var mBackgroundHandler: Handler? = null

    /** 保证运行在单独线程防止UI阻塞 */
    private var mBackgroundThread: HandlerThread? = null

    fun openCamera(activity: Activity, width: Int, height: Int) {
        this.mActivity = activity
        this.mCameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        this.mCameraId = getCameraId(false)
        this.mPreviewSize = getCameraOutPutSizes(
            mCameraId!!,
            SurfaceTexture::class.java,
            width,
            height
        )
        startBackgroundThread()
        openCamera()
    }

    // 开始预览
    fun startPreview() {
        if (mCaptureSession == null || mPreviewRequestBuilder == null) return

        try {
            mCaptureSession!!.setRepeatingRequest(
                mPreviewRequestBuilder!!.build(),
                mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun stopPreview() {
        if (mCaptureSession == null || mPreviewRequestBuilder == null) {
            return
        }
        try {
            mCaptureSession!!.stopRepeating()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun getPreviewFormat() {
        TODO("Not yet implemented")
    }

    fun switchCamera(surfaceTexture: SurfaceTexture) {
        TODO("Not yet implemented")
    }

    fun takePicture() {
        TODO("Not yet implemented")
    }


    fun closeCamera() {
        releaseCamera()
    }

    fun setPreviewSurface(surfaceTexture: SurfaceTexture) {
        surfaceTexture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
        mPreviewSurface = Surface(surfaceTexture)
    }

    fun getPreviewWidth(): Int {
        return if (mPreviewSize == null) 0 else mPreviewSize!!.width
    }

    fun getPreviewHeight(): Int {
        return if (mPreviewSize == null) 0 else mPreviewSize!!.height
    }

    fun isFrontCamera(): Boolean {
        return mCameraId == CameraCharacteristics.LENS_FACING_BACK.toString()
    }

    private fun getCameraId(useFront: Boolean): String? {
        mCameraManager!!.cameraIdList.forEach { cameraId ->
            val cameraCharacteristics = mCameraManager!!.getCameraCharacteristics(cameraId)
            val cameraFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (useFront && cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId
            } else if (!useFront && cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return cameraId
            }
        }
        return null
    }

    /**
     * 打开指定的摄像头
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            if (mImageReader == null) {
                // 创建一个 ImageReader 对象，用于获取摄像头图像数据，maxImages 是 ImageReader 一次可以访问的最大图片数量 这里设为 2
                mImageReader =
                    ImageReader.newInstance(
                        mPreviewSize!!.width,
                        mPreviewSize!!.height,
                        previewFormat,
                        2
                    )
                mImageReader!!.setOnImageAvailableListener(
                    mOnImageAvailableListener,
                    mBackgroundHandler
                )
            }
            mCameraManager!!.openCamera(mCameraId.toString(), mStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
    }


    private fun createCameraPreview() {
        try {
            // 创建预览请求构造器
            mPreviewRequestBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            // 设置预览输出 Surface
            mPreviewRequestBuilder!!.addTarget(mPreviewSurface!!)
            // 设置实时帧数据接收
            mPreviewRequestBuilder!!.addTarget(mImageReader!!.surface)
            // 打开人脸检测
            // mPreviewRequestBuilder!!.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, getFaceDetectMode())
            // 创建预览会话
            mCameraDevice!!.createCaptureSession(
                Arrays.asList(
                    mPreviewSurface!!,
                    mImageReader!!.surface
                ), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        // 相机关闭 不进行处理
                        if (mCameraDevice == null) return
                        // 会话准备就绪，开始预览
                        mCaptureSession = session
                        // 自动连续对焦
                        // 设置连续自动对焦
                        mPreviewRequestBuilder!!.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                        )
                        // 设置自动曝光
                        mPreviewRequestBuilder!!.set(
                            CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                        )
                        // 开始预览
                        startPreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        println("Camera onConfigureFailed")
                    }

                }, mBackgroundHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取支持的最高人脸检测级别
     */
    private fun getFaceDetectMode(): Int {
        return if (mFaceDetectModes == null)
            CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL
        else
            mFaceDetectModes!!.get(mFaceDetectModes!!.size - 1)
    }


    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camrea2 Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        if (mBackgroundThread != null && mBackgroundHandler != null) {
            mBackgroundHandler!!.removeCallbacksAndMessages(null)
            mBackgroundHandler = null

            mBackgroundThread!!.quitSafely()
            try {
                mBackgroundThread!!.join()
                mBackgroundThread = null
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun releaseCamera() {
        // 关闭会话
        if (mCaptureSession != null) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }
        // 关闭当前相机
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
        // 关闭拍照处理器
        if (null != mImageReader) {
            mImageReader!!.close()
            mImageReader = null
        }
        stopBackgroundThread()
    }


    /**
     * [ImageReader]的回调对象。 当静止图像准备保存时，将会调用“onImageAvailable”。
     */
    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        if (reader != null) {
            val image = reader.acquireLatestImage()
            if (image != null) {
                var byteArray: ByteArray? = null
                if (previewFormat == ImageFormat.YV12) {
                    byteArray = ImageUtils.getBytesFromImageYV12ToNV21(image)
                } else {
                    if (image.planes.isNotEmpty()) {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer[bytes]
                        byteArray = bytes.clone()
                    }
                    image.close()
                }
//                 用于回调 图片数据
//                if (previewCallback != null && byteArray != null) {
//                    previewCallback!!.onPreviewCallback(byteArray)
//                }
            }
        }
    }

    /** 摄像头回调 */
    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            // 摄像头打开
            mCameraDevice = camera
            // 创建预览会话
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            // 摄像头关闭
            releaseCamera()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            // 发生错误 关闭摄像头
            releaseCamera()
            println("CameraDevice.StateCallback Error:${error}")
        }

    }


    private val mCaptureCallback: CameraCaptureSession.CaptureCallback =
        object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureProgressed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                partialResult: CaptureResult
            ) {
            }

            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                // 检测到的人脸
//                val faces = result.get(CaptureResult.STATISTICS_FACES)
//                if (faces != null) {
//                    for (f in faces.indices) {
//                        println("人脸[$f]id:${faces[f].id}")
//                        //人脸检测坐标基于相机成像画面尺寸以及坐标原点。此处进行比例换算
//                        println("人脸[$f]bounds:${faces[f].bounds}")
//                        println("人脸[$f]leftEyePosition:${faces[f].leftEyePosition}")
//                        println("人脸[$f]rightEyePosition:${faces[f].rightEyePosition}")
//                        println("人脸[$f]score:${faces[f].score}")
//                    }
//                }
            }
        }

//    解决 TextureView 下的 Camera 变形问题
//    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
//        val context = mSurfaceView.context
//        val rotation = context.display!!.rotation
//        val matrix = Matrix()
//        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
//        val bufferRect = RectF(0f, 0f, mPreviewSize.height.toFloat(), mPreviewSize.width.toFloat())
//        val centerX = viewRect.centerX()
//        val centerY = viewRect.centerY()
//        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
//            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
//            val scale = max(
//                viewHeight.toFloat() / mPreviewSize.height,
//                viewWidth.toFloat() / mPreviewSize.width
//            )
//            matrix.postScale(scale, scale, centerX, centerY)
//            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
//        } else if (Surface.ROTATION_180 == rotation) {
//            matrix.postRotate(180f, centerX, centerY)
//        }
//        // TODO
////        textureView.setTransform(matrix)
//    }


    // TODO 需要测试
    private fun getCameraOutPutSizes(
        cameraId: String,
        clazz: Class<*>,
        width: Int,
        height: Int
    ): Size {
        val characteristics: CameraCharacteristics? =
            mCameraManager?.getCameraCharacteristics(cameraId)
        val maps = characteristics!!.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = (maps!!.getOutputSizes(ImageFormat.JPEG)).toList() as List<Size>

        val totalRotation = getRotation(characteristics)
        val swapRotation = totalRotation == 90 || totalRotation == 270
        val viewWidth: Int = if (swapRotation) height else width
        val viewHeight: Int = if (swapRotation) width else height

        return chooseOptimalSize(
            maps.getOutputSizes(clazz),
            viewWidth,
            viewHeight,
            Collections.max(sizes) { o1, o2 -> o1.width * o1.height - o2.width * o2.height }
        )
    }


    private fun chooseOptimalSize(
        sizes: Array<Size>,
        width: Int,
        height: Int,
        pictureSize: Size
    ): Size {
        var minDelta = Int.MAX_VALUE
        var index = 0
        val aspectRatio: Float = pictureSize.height * 1.0f / pictureSize.width
        for (i in sizes.indices) {
            val size = sizes[i]
            // 先判断比例是否相等
            if (size.width * aspectRatio == size.height.toFloat()) {
                val delta = abs(width - size.width)
                if (delta == 0) {
                    return size
                }
                if (minDelta > delta) {
                    minDelta = delta
                    index = i
                }
            }
        }

        return sizes[index]
    }


    private fun getRotation(cameraCharacteristics: CameraCharacteristics): Int {
        var displayRotation = mActivity!!.windowManager.defaultDisplay.rotation
        when (displayRotation) {
            Surface.ROTATION_0 -> {
                displayRotation = 90
            }
            Surface.ROTATION_90 -> {
                displayRotation = 0
            }
            Surface.ROTATION_180 -> {
                displayRotation = 270
            }
            Surface.ROTATION_270 -> {
                displayRotation = 180
            }
        }
        val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        return (displayRotation + sensorOrientation!! + 270) % 360;
    }
}