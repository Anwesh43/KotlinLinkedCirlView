package com.anwesh.uiprojects.kotlinlinkedcirlview

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.linkedcirlview.LinkedCirlView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view  : LinkedCirlView = LinkedCirlView.create(this)
        view.addListener({showShortToast("animation ${it} is completed")}, {showShortToast("animation ${it} is reset")})
        fullScreen()
    }
}

fun MainActivity.showShortToast(text : String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun MainActivity.fullScreen() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}