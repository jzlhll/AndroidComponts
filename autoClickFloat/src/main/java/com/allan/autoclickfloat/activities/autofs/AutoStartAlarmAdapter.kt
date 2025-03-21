package com.allan.autoclickfloat.activities.autofs

import android.view.View
import android.view.ViewGroup
import com.allan.autoclickfloat.databinding.FragmentAutoStartupNewHolderItemBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.dialogs.ConfirmBottomDialog
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import java.util.Calendar

class AutoStartRcvBean(val autoFsId:String,
                       val targetTs:Long,
                       val isClose:Boolean,
                       val isLoop:Boolean,
                       var isSelectMode:Boolean,
                       var leftTimeStr:String?)

class AutoStartAlarmAdapter : BindRcvAdapter<AutoStartRcvBean, AutoStartAlarmItemHolder>() {
    private val deleteClick = { autoFsId:String ->
        AutoFsObj.removeAlarmUi(Globals.app, autoFsId)
    }

    private val editClick = { autoFsId:String ->
        AutoStartAlarmDialog.edit(Globals.topActivity as FragmentRootActivity, autoFsId)
    }

    private val switchClick = {autoFsId:String, isClose:Boolean ->
        val r = AutoFsObj.switchAlarmUi(Globals.app, autoFsId, isClose)
        if (r == "expired") {
            Globals.mainHandler.post {
                Globals.topActivity?.asOrNull<FragmentRootActivity>()?.let { ac->
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
        return AutoStartAlarmItemHolder(deleteClick, switchClick, editClick, create(parent))
    }
}

/**
 * switchClick的返回值如果不是true，我就需要将内容回弹。
 */
class AutoStartAlarmItemHolder(deleteClick:(autoFsId:String)->Unit,
                               switchClick:(autoFsId:String, isClose:Boolean)->Boolean,
                               editClick:(autoFsId:String)->Unit,
                               binding: FragmentAutoStartupNewHolderItemBinding)
        : BindViewHolder<AutoStartRcvBean, FragmentAutoStartupNewHolderItemBinding>(binding) {
    init {
        binding.stopBtn.onClick {
            currentData?.autoFsId?.let { it1 -> deleteClick(it1) }
        }
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
            currentData?.autoFsId?.let { it1 -> editClick(it1) }
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
        binding.leftTv.text = "(" + bean.leftTimeStr + ")"
        if (binding.switchBtn.isInit) {
            binding.switchBtn.setValue(bean.isClose)
        } else {
            binding.switchBtn.initValue(bean.isClose)
        }
        binding.switchBtn.visibility = if(bean.isSelectMode) View.GONE else View.VISIBLE
        binding.stopBtn.visibility = if(bean.isSelectMode) View.VISIBLE else View.GONE

        if (bean.isClose && !bean.isSelectMode) {
            binding.root.alpha = 0.8f
        } else {
            binding.root.alpha = 1f
        }
    }
}