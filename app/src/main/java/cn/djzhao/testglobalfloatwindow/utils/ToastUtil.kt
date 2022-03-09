package cn.djzhao.testglobalfloatwindow.utils

import android.content.Context
import android.widget.Toast
import java.lang.ref.WeakReference

/**
 * Toast工具
 *
 * @author djzhao
 * @date 22/03/03
 */
class ToastUtil {
    companion object {
        private var context: WeakReference<Context>? = null
        private var mToast: Toast? = null

        fun init(context: Context) {
            this.context = WeakReference(context)
        }

        private fun dismiss() {
            mToast?.cancel()
        }

        fun show(msg: String) {
            dismiss()
            mToast = Toast.makeText(context?.get(), msg, Toast.LENGTH_SHORT)
            mToast?.show()
        }
    }
}