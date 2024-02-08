package com.yoyojia.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.yoyojia.timer.CountdownView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<CountdownView>(R.id.countdown)
            .start(System.currentTimeMillis() + 100000) {
                ToastUtils.showShort("倒计时结束")
            }
    }
}