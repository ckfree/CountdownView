package com.yoyojia.timer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.reflect.Constructor

/**
 *
 * 倒计时 未处理visible change事件 日后优化
 *
 * 方式一:
 * <com.yantu.viphd.widgets.countdown.CountdownView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:config="com.yantu.viphd.widgets.countdown.TextConfigLead"
 *     app:start="@{endTimestamp * 1000}" />
 *
 * 方式二:
 * CountdownView().config(ICountdownConfig) 非必须
 * CountdownView().start(endTimestamp,finishCallback)
 * CountdownView().stop() 非必须
 */
class CountdownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var deadline: Long = 0L

    private var counterJob: Job? = null

    private lateinit var config: IConfig

    private var onFinish: (() -> Unit)? = null

    private var layout: View? = null

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CountdownView)
        val configClassName = array.getString(R.styleable.CountdownView_config)
        array.recycle()

        val xmlConfig =
            if (configClassName.isNullOrEmpty()) null else try {
                val configClass = Class.forName(configClassName)
                val constructor: Constructor<*> = configClass.getConstructor()
                constructor.newInstance() as IConfig
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        init(xmlConfig ?: TextConfig())
    }

    /**
     * 非必须 只需要调用一次
     */
    fun init(config: IConfig) {
        layout?.let { removeView(it) }
        this.config = config
        layout = config.layout(context, this)
        addView(layout)
    }

    /**
     * 随便调
     */
    @JvmOverloads
    fun start(deadline: Long, finished: (() -> Unit)? = null) {
        if (System.currentTimeMillis() > deadline) return

        this.deadline = deadline
        this.onFinish = finished
        startFlowInner()
    }

    /**
     * 非必须
     */
    fun stop() {
        counterJob?.cancel()
        onFinish = null
        deadline = 0
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startFlowInner()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        counterJob?.cancel()
    }

    private fun startFlowInner() {
        if (counterJob?.isActive != true && deadline > 0) {

            val scope = findViewTreeLifecycleOwner()?.lifecycleScope ?: return

            counterJob =
                flow {
                    while (true) {
                        emit(Unit)
                        delay(config.interval)
                    }
                }
                    .flowOn(Dispatchers.Main)
                    .onEach { onTick() }
                    .launchIn(scope)
        }
    }

    private fun onTick() {
        if (deadline <= 0) return

        val duration = deadline - System.currentTimeMillis()
        var tmpDuration = duration //毫秒
        val day = tmpDuration / 86400000
        tmpDuration %= 86400000
        val hour = tmpDuration / 3600000
        tmpDuration %= 3600000
        val min = tmpDuration / 60000
        tmpDuration %= 60000
        val sec = tmpDuration / 1000
        val millis = tmpDuration % 1000 / 100

        if (duration < 0) {
            // 结束回调
            config.onTick(this, 0, 0, 0, 0, 0)

            onFinish?.invoke()

            stop()

        } else {
            // UI
            config.onTick(this, day.toInt(), hour.toInt(), min.toInt(), sec.toInt(), millis.toInt())
        }
    }
}