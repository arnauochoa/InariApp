package com.inari.team.core.utils.extensions

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.inari.team.core.App
import com.inari.team.core.di.component.AppComponent
import org.jetbrains.anko.intentFor


val Activity.app: App get() = application as App
val Fragment.app: App get() = activity?.application as App
val Service.app: App get() = application as App
val resources: Resources get() = App.getAppContext().resources
val context: Context get() = App.getAppContext()

fun AppCompatActivity.getAppComponent(): AppComponent = (app).appComponent
fun Fragment.getAppComponent(): AppComponent = (app).appComponent
fun Service.getAppComponent(): AppComponent = (app).appComponent

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this.context, message, duration).show()
}

fun Activity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

inline fun <reified T : Activity> Activity.startActivityAfterDelay(delay: Long = 2000L) {
    Handler().postDelayed({
        startActivity(intentFor<T>())
        finish()
    }, delay)
}

fun enableFullScreen(window: Window) {
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

fun Activity.showKeyBoard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
}

fun Activity.hideKeyBoard(view: View) {
    val imm =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(
        view.windowToken,
        0
    )
}

fun inflate(@LayoutRes resourceLayout: Int, viewGroup: ViewGroup? = null, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(resourceLayout, viewGroup, attachToRoot)
}


val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()