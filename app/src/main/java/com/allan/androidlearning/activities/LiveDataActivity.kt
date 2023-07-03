package com.allan.androidlearning.activities

import android.R.string
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ScaleXSpan
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.allan.androidlearning.databinding.ActivityLiveDataBinding
import com.allan.androidlearning.utils.logt
import com.allan.androidlearning.utils.testGsonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LiveDataActivity : AppCompatActivity() {
    private val numbersVM by viewModels<NumbersViewModel>()
    private val TAG = LiveDataActivity::class.java.simpleName

    private val list:MutableList<SpannableString> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLiveDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //liveData 是粘性的。只有有值，一个直接observe进来的监听者立刻得到回调

        numbersVM.listData.observe(this,
            Observer {
                binding.text.text = fromHtml(it.toString())
            })

        binding.changeBtn.setOnClickListener {
            numbersVM.calData()
        }

        testGsonData()
    }

    fun fromHtml(html: String?): Spanned {
        return if (html == null) {
            // return an empty spannable if the html is null
            SpannableString("")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }
}

class NumbersViewModel : ViewModel() {
    val listData = MutableLiveData<SpannableString>()
    val spanData = MutableLiveData<Spanned>()
    fun calData() {
        viewModelScope.launch {
            delay(100)
            logt("allan", "1call span data")
            withContext(Dispatchers.IO) {
                logt("allan", "2call span data")
                var bot = 1
                while (bot <= 8) {
                    var top = 1
                    while (top<bot) {
                        delay(1000)
                        logt("allan", "3call span data $top/$bot")
                        val first = "2"
                        val second = fractionToUnicode(top, bot)
                        val third = "kg"
                        val ss = SpannableString("$first$second$third")
                        ss.setSpan(ScaleXSpan(0.1f), first.length, first.length + second.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        listData.postValue(ss)
                        top++
                    }
                    bot++
                }
            }
        }
    }

    fun calSpanData() {
        viewModelScope.launch {
            logt("allan", "call span data")
            delay(100)
        }
    }

    fun fromHtml(html: String?): Spanned {
        return if (html == null) {
            // return an empty spannable if the html is null
            SpannableString("")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    /**
     * 所有的1-8的分数。
     */
    fun fractionToUnicode(molecular:Int, denominator:Int):String {
        //后面与单位的空格。
        if (molecular == 1) {
            if (denominator == 2) {
                return "\u00BD "
            }
            if (denominator == 3) {
                return "\u2153 "
            }
            if (denominator == 4) {
                return "\u00BC "
            }
            if (denominator == 8) {
                return "\u215B "
            }
            if (denominator == 5) {
                return "\u2155 "
            }
            if (denominator == 6) {
                return "\u2159 "
            }
        } else if (molecular == 2) {
            if (denominator == 3) {
                return "\u2154 "
            }
            if (denominator == 5) {
                return "\u2156 "
            }
        } else if (molecular == 3) {
            if (denominator == 4) {
                return "\u00BE "
            }
            if (denominator == 8) {
                return "\u215C "
            }
            if (denominator == 5) {
                return "\u2157 "
            }
        } else if (molecular == 4) {
            if (denominator == 5) {
                return "\u2158 "
            }
        } else if (molecular == 5) {
            if (denominator == 8) {
                return "\u215D "
            }
            if (denominator == 6) {
                return "\u215A "
            }
        } else if (molecular == 7) {
            if (denominator == 8) {
                return "\u215E "
            }
        }

        return vulgarFraction(molecular, denominator)
    }

    private fun vulgarFraction(numerator: Int, denominator: Int): String {
        return String.format(" %d%c%d ", numerator, '\u2044', denominator)
    }
}
