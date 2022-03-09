package cn.djzhao.testglobalfloatwindow.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.ImageView
import cn.djzhao.testglobalfloatwindow.R
import cn.djzhao.testglobalfloatwindow.utils.ToastUtil

/**
 * 悬浮按钮服务
 *
 * @author djzhao
 * @date 22/03/03
 */
class FloatingService : Service() {

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var button: Button? = null
    private var imageView: ImageView? = null
    private var surfaceView: SurfaceView? = null
    private var mediaPlayer: MediaPlayer? = null

    private var floatingHandler: Handler? = null
    private var isFloating = false

    private var onTouchListener: View.OnTouchListener? = null

    private var viewX = 100f
    private var viewY = 100f

    private var imageIndex = 0
    private val imagesRes =
        arrayOf(R.drawable.p_0, R.drawable.p_1, R.drawable.p_2, R.drawable.p_3)

    private var currentType = TYPE_FLOATING_BUTTON

    companion object {
        const val TYPE_FLOATING_BUTTON = 0
        const val TYPE_FLOATING_IMAGE = 1
        const val TYPE_FLOATING_VIDEO = 2
    }

    override fun onCreate() {
        super.onCreate()

        initWindowManger()
        initTouchEvent()
    }

    private fun resetViews() {
        if (button != null) {
            if (isFloating) {
                windowManager?.removeView(button)
            }
            button = null
        } else if (imageView != null) {
            if (isFloating) {
                windowManager?.removeView(imageView)
            }
            imageView = null
        } else if (surfaceView != null) {
            if (isFloating) {
                windowManager?.removeView(surfaceView)
            }
            surfaceView = null
            mediaPlayer = null
        }
        imageIndex = 0
        isFloating = false
        floatingHandler?.removeCallbacksAndMessages(null)
        floatingHandler = null
    }

    private fun initTouchEvent() {
        onTouchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    viewX = event.rawX
                    viewY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX
                    val newY = event.rawY
                    layoutParams?.x = layoutParams?.x?.plus((newX - viewX).toInt())
                    layoutParams?.y = layoutParams?.y?.plus((newY - viewY).toInt())
                    viewX = newX
                    viewY = newY
                    windowManager?.updateViewLayout(v, layoutParams)
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                }
            }
            false
        }
    }

    private fun getScreenWith(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager?.currentWindowMetrics?.bounds
            bounds!!.width()
        } else {
            val point = Point()
            windowManager?.defaultDisplay?.getSize(point)
            point.x
        }
    }

    private fun initButton() {
        button = LayoutInflater.from(this).inflate(R.layout.layout_float_button, null) as Button
        layoutParams?.width = 400
        layoutParams?.height = 200
        button?.setOnClickListener {
            ToastUtil.show("Talk!!!\nWhat are u want?")
        }
        button?.setOnTouchListener(onTouchListener)
    }

    private fun initImageView() {
        imageView =
            LayoutInflater.from(this).inflate(R.layout.layout_float_image, null) as ImageView
        layoutParams?.width = getScreenWith() * 2 / 3
        layoutParams?.height = layoutParams!!.width * 2 / 3
        imageView?.setOnTouchListener(onTouchListener)
        initHandler()
    }

    private fun initHandler() {
        floatingHandler = Handler(Looper.getMainLooper()) {
            when (it.what) {
                1 -> {
                    imageIndex = ++imageIndex % 4
                    imageView?.setImageResource(imagesRes[imageIndex])
                    floatingHandler?.sendEmptyMessageDelayed(1, 2000)
                }
            }
            false
        }
    }

    private fun initVideo() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        surfaceView = SurfaceView(this)
        val holder = surfaceView?.holder
        holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mediaPlayer?.setDisplay(holder)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {

            }

        })
        mediaPlayer?.setOnPreparedListener {
            mediaPlayer?.start()
        }

        mediaPlayer?.setDataSource("https://media.w3.org/2010/05/sintel/trailer.mp4")
        mediaPlayer?.isLooping = true
        mediaPlayer?.prepareAsync()
        surfaceView?.setOnTouchListener(onTouchListener)
        layoutParams?.width = getScreenWith() * 2 / 3
        layoutParams?.height = layoutParams!!.width * 2 / 3

    }

    private fun initWindowManger() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        layoutParams = WindowManager.LayoutParams()
        // 通过layout的type来指定窗口类型
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            // Android8以上使用
            layoutParams?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // Android8以下使用
            layoutParams?.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams?.format = PixelFormat.RGBA_8888
        layoutParams?.gravity = Gravity.START or Gravity.TOP
        layoutParams?.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams?.x = viewX.toInt()
        layoutParams?.y = viewY.toInt()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = intent?.getIntExtra("type", TYPE_FLOATING_BUTTON) ?: TYPE_FLOATING_BUTTON

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            ToastUtil.show("暂无悬浮窗权限")
            return super.onStartCommand(intent, flags, startId)
        }
        if (currentType != type) {
            resetViews()
            currentType = type
        }
        when (type) {
            TYPE_FLOATING_BUTTON -> {
                showFloatButton()
            }
            TYPE_FLOATING_IMAGE -> {
                showFloatImage()
            }
            TYPE_FLOATING_VIDEO -> {
                showFloatVideo()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun showFloatButton() {
        if (button == null) {
            initButton()
        }
        if (isFloating) {
            windowManager?.removeView(button)
            isFloating = false
            return
        }
        /*button? = Button(applicationContext)
        button?.text = "Floating Button"
        button?.setBackgroundColor(Color.parseColor("#88336699"))*/
        windowManager?.addView(button, layoutParams)
        isFloating = true
    }

    private fun showFloatImage() {
        if (imageView == null) {
            initImageView()
            initHandler()
            floatingHandler?.sendEmptyMessageDelayed(1, 2000)
        }
        if (isFloating) {
            windowManager?.removeView(imageView)
            isFloating = false
            return
        }
        windowManager?.addView(imageView, layoutParams)
        isFloating = true
    }

    private fun showFloatVideo() {
        if (surfaceView == null) {
            initVideo()
        }
        if (isFloating) {
            windowManager?.removeView(surfaceView)
            isFloating = false
            return
        }
        windowManager?.addView(surfaceView, layoutParams)
        isFloating = true
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingHandler = null
    }
}