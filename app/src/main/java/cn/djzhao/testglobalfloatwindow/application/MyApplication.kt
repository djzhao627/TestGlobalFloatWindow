package cn.djzhao.testglobalfloatwindow.application

import android.app.Application
import cn.djzhao.testglobalfloatwindow.utils.ToastUtil

/**
 * Application
 *
 * @author djzhao
 * @date 22/03/03
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ToastUtil.init(this)
    }
}