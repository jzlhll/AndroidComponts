package com.allan.autoclickfloat.activities.autofs

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.allan.autoclickfloat.databinding.FragmentAutoStartupNewHolderItemBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.dialogs.ConfirmBottomDialog
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import java.util.Calendar

enum class ColorMode {
    IsOver,
    AlmostClose,
    Today,
    Later
}

class AutoStartRcvBean(val autoFsId:String,
                       val targetTs:Long,
                       val offsetMinute:Int,
                       val isClose:Boolean,
                       val isLoop:Boolean,
                       val color:ColorMode,
                       var leftTimeStr:String?)

class AutoStartAlarmAdapter(val rootClick:(autoFsId:String)->Unit) : BindRcvAdapter<AutoStartRcvBean, AutoStartAlarmItemHolder>() {

    private val switchClick = {autoFsId:String, isClose:Boolean ->
        val r = AutoFsObj.switchAlarmUi(Globals.app, autoFsId, isClose)
        if (r == "expired") {
            Globals.mainHandler.post {
                Globals.topActivity?.asOrNull<FragmentShellActivity>()?.let { ac->
                    ConfirmBottomDialog.show2(ac.supportFragmentManager,
                        "闹钟已过期",
                        "请选择接下来的操作。",
                        "编辑",
                        "删除",
                        sureClick = {
                            AutoStartAlarmDialog.edit(ac, autoFsId)
                            it.dismissAllowingStateLoss()
                        },
                        cancelClick = {
                            AutoFsObj.removeAlarmUi(Globals.app, autoFsId)
                            it.dismissAllowingStateLoss()
                        })
                }
            }
        }
        r == ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoStartAlarmItemHolder {
        return AutoStartAlarmItemHolder(switchClick, rootClick, create(parent))
    }
}

/**
 * switchClick的返回值如果不是true，我就需要将内容回弹。
 */
class AutoStartAlarmItemHolder(switchClick:(autoFsId:String, isClose:Boolean)->Boolean,
                               rootClick:(autoFsId:String)->Unit,
                               binding: FragmentAutoStartupNewHolderItemBinding)
        : BindViewHolder<AutoStartRcvBean, FragmentAutoStartupNewHolderItemBinding>(binding) {
    val orangeColor = "#FFA500".toColorInt()
    val defaultColor = Globals.getColor(com.au.module_androidcolor.R.color.color_text_normal)
    val descColor = Globals.getColor(com.au.module_androidcolor.R.color.color_text_desc)

    init {
        binding.switchBtn.valueCallback = {
            binding.switchBtn.abort = true
            currentData?.autoFsId?.let { it1 ->
                val isAccept = switchClick(it1, binding.switchBtn.isClosed)
                if (!isAccept) {
                    binding.switchBtn.setValue(!binding.switchBtn.isClosed) //不会触发回调
                }
            }
            binding.switchBtn.abort = false
        }
        binding.root.onClick {
            currentData?.autoFsId?.let { it1 -> rootClick(it1) }
        }
    }

    override fun bindData(bean: AutoStartRcvBean) {
        super.bindData(bean)
        val c = Calendar.getInstance()
        c.timeInMillis = bean.targetTs
        val (ymd, time) = TimeUtil.timeDayAndTimeStrs(c)
        binding.timeTv.text = time
        if (bean.isLoop) {
            binding.descTv.text = "每天"
        } else {
            val dayOfWeekStr = TimeUtil.formatDayOfWeek(c.timeInMillis)
            binding.descTv.text = "$ymd ($dayOfWeekStr)"
        }
        binding.leftTv.text = "(" + bean.leftTimeStr + " ±${bean.offsetMinute})"

        when (bean.color) {
            ColorMode.AlmostClose -> {
                binding.timeTv.setTextColor(Color.RED)
                binding.descTv.setTextColor(defaultColor)
            }
            ColorMode.Today -> {
                binding.timeTv.setTextColor(orangeColor)
                binding.descTv.setTextColor(defaultColor)
            }
            ColorMode.Later -> {
                binding.timeTv.setTextColor(defaultColor)
                binding.descTv.setTextColor(defaultColor)
            }
            ColorMode.IsOver -> {
                binding.timeTv.setTextColor(descColor)
                binding.descTv.setTextColor(descColor)
            }
        }

        if (binding.switchBtn.isInit) {
            binding.switchBtn.setValue(bean.isClose)
        } else {
            binding.switchBtn.initValue(bean.isClose)
        }
    }
}