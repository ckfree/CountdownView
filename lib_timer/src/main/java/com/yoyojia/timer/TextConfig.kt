package com.yoyojia.timer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

open class TextConfig : IConfig {

    override val interval: Long = 1000

    override fun layout(context: Context, root: CountdownView): View =
        LayoutInflater.from(context).inflate(R.layout.view_countdown_text, root, false)

    override fun onTick(root: CountdownView, day: Int, hour: Int, min: Int, sec: Int, millis: Int) {
        root.findViewById<TextView>(R.id.tv_content)?.text = "${day}天${hour}时${min}分${sec}秒"
    }
}