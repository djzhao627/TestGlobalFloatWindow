package cn.djzhao.testglobalfloatwindow.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cn.djzhao.testglobalfloatwindow.R
import cn.djzhao.testglobalfloatwindow.service.FloatingService
import cn.djzhao.testglobalfloatwindow.service.FloatingService.Companion.TYPE_FLOATING_BUTTON
import cn.djzhao.testglobalfloatwindow.service.FloatingService.Companion.TYPE_FLOATING_IMAGE
import cn.djzhao.testglobalfloatwindow.service.FloatingService.Companion.TYPE_FLOATING_VIDEO
import cn.djzhao.testglobalfloatwindow.utils.ToastUtil

class MainActivity : AppCompatActivity() {

    private lateinit var registerForActivityResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerForActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        ToastUtil.show("授权失败")
                    }
                }
            }
    }

    fun doFloat(view: View) {
        if (!checkPermission()) {
            requestPermission()
            return
        }
        when (view.id) {
            R.id.float_bth -> startFloat(TYPE_FLOATING_BUTTON)
            R.id.float_img -> startFloat(TYPE_FLOATING_IMAGE)
            R.id.float_video -> startFloat(TYPE_FLOATING_VIDEO)
        }
    }

    @SuppressLint("InlinedApi")
    private fun requestPermission() {
        ToastUtil.show("请授权允许显示在其他应用上层")
        val intent =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        registerForActivityResult.launch(intent)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        return Settings.canDrawOverlays(this)
    }

    private fun startFloat(type: Int) {
        val intent = Intent(this, FloatingService::class.java)
        intent.putExtra("type", type)
        startService(intent)
    }
}