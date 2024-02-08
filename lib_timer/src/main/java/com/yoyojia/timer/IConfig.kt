package com.yoyojia.timer

import android.content.Context
import android.view.View

interface IConfig {

    val interval: Long
    fun layout(context: Context, root: CountdownView): View
    fun onTick(root: CountdownView, day: Int, hour: Int, min: Int, sec: Int, millis: Int)
}