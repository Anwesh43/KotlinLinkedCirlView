package com.anwesh.uiprojects.kotlinlinkedcirlview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linkedcirlview.LinkedCirlView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedCirlView.create(this)
    }
}
