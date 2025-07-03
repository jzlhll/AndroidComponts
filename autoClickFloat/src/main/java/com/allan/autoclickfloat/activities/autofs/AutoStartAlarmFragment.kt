package com.allan.autoclickfloat.activities.autofs

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.autoclickfloat.databinding.FragmentAutoStartupNewBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.logd
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmBottomDialog
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.toast.ToastUtil
import com.au.module_cached.AppDataStore
import com.au.module_nested.decoration.VertPaddingItemDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * @author allan
 * @date :2024/9/24 11:25
 * @description:
 */
class AutoStartAlarmFragment : BindingFragment<FragmentAutoStartupNewBinding>(), ActionDialog.IAction {

    private var mLastActionId:String? = null
    private val adapter = AutoStartAlarmAdapter {
        mLastActionId = it
        ActionDialog.pop(this)
    }

    override fun onNotify(mode: ActionDialog.ActionMode) {
        val autoFsId = mLastActionId ?: return
        when (mode) {
            ActionDialog.ActionMode.Edit -> {
                deleteAlarm(autoFsId)
            }
            ActionDialog.ActionMode.Delete -> {
                ConfirmCenterDialog.show(childFragmentManager,
                    "删除闹钟", "确定删除闹钟吗？", getString(com.au.module_android.R.string.ok)) {
                    AutoFsObj.removeAlarmUi(Globals.app, autoFsId)
                    it.dismissAllowingStateLoss()
                }
            }
        }
    }

    private fun deleteAlarm(autoFsId: String) {
        if (AutoFsObj.isAlarmExpired(autoFsId)) {
            Globals.topActivity?.asOrNull<FragmentShellActivity>()?.let { ac ->
                ConfirmBottomDialog.show2(
                    ac.supportFragmentManager,
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
        } else {
            AutoStartAlarmDialog.edit(Globals.topActivity as FragmentShellActivity, autoFsId)
        }
    }

    private var mIgonePasswordCount = 0

    private fun initRcv() {
        binding.rcv.adapter = adapter
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.setHasFixedSize(true)
        binding.rcv.addItemDecoration(VertPaddingItemDecoration(0, 0, 12.dp))
    }

    override fun isAutoHideIme(): Boolean {
        return true
    }

    private fun initEdit() {
        val savePsWd = AppDataStore.readBlocked("autoFsInputPassword", "")

        binding.inputHost.onClick {  } //必须添加用来阻止到被覆盖的界面

        binding.inputTitle.onClick {
            mIgonePasswordCount++
            if (mIgonePasswordCount >= 5) {
                binding.inputHost.gone()
                hideImeNew(requireActivity().window, binding.inputHost)
            }
        }

        if (savePsWd.isEmpty()) {
            binding.inputTitle.text = "首次使用，请设置密码:"
            binding.inputOkBtn.onClick {
                val t = binding.inputEdit.text.toString()
                if (t == binding.inputEdit2.text.toString() && t.isNotEmpty()) {
                    AppDataStore.save("autoFsInputPassword" to t)
                    binding.inputHost.gone()
                    hideImeNew(requireActivity().window, binding.inputHost)
                }
            }
        } else {
            binding.inputTitle.text = "请输入密码:"
            binding.inputEdit2.gone()
            binding.inputOkBtn.onClick {
                val t = binding.inputEdit.text.toString()
                if (t == savePsWd && t.isNotEmpty()) {
                    binding.inputHost.gone()
                    hideImeNew(requireActivity().window, binding.inputHost)
                }
            }
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        initBtns()
        initAlarm()
        initEdit()
        initCurrentTime()
        initRcv()

        initTimer()
    }

    private fun initDing() {
        val lastTime = AppDataStore.readBlocked("autoFSDingInfoLastShowTime", 0L)
        val cur = System.currentTimeMillis()
        if (cur - lastTime > 7 * 24 * 3600 * 1000L) { //每七天显示一次
            binding.dingInfoHost.visible()
            binding.dingInfoHost.onClick {
                binding.dingInfoHost.tag = "isClicked"
                if (goToAutoStartSettings(requireContext())) {
                    binding.dingInfoHost.gone()
                } else {
                    ToastUtil.toastOnTop("暂时无法跳转到自启动设置界面，请手动去系统中设置。")
                }
            }
        } else {
            binding.dingInfoHost.gone()
        }
    }

    override fun onStart() {
        super.onStart()
        initDing()
    }

    override fun onStop() {
        super.onStop()
        if (binding.dingInfoHost.tag == "isClicked") {
            AppDataStore.save("autoFSDingInfoLastShowTime" to System.currentTimeMillis())
        }
    }

    private fun initTimer() {
        lifecycleScope.launch {
            while (true) {
                delay(15 * 1000)
                onTargetTsListChanged(AutoFsObj.targetTsListData.realValue ?: listOf())
            }
        }
    }

    private fun initBtns() {
        binding.addBtn.onClick {
            AutoStartAlarmDialog.show(this)
        }
    }

    private fun initCurrentTime() {
        lifecycleScope.launch {
            while (true) {
                binding.currentTime.text = TimeUtil.timeYMHMS(Calendar.getInstance())
                delay(1000)
            }
        }
    }

    private fun initAlarm() {
        val oldPendingIntent = AutoFsObj.fetchPendingIntent(requireContext())
        if (oldPendingIntent == null) {
            logd { "allan-alarm old pending Intent is null" }
            AutoFsObj.cancelAlarmOnly(requireContext(), null)
        } else {
            logd { "allan-alarm old pending is not null" }
            val isNotExist = AutoFsObj.targetTsListData.realValue.isNullOrEmpty()
            if (isNotExist) { //有pendingIntent但是没有保存数据？那么就可以用户清理了存储。我们也清理掉任务好了。
                AutoFsObj.cancelAlarmOnly(requireContext(), oldPendingIntent)
            }
        }

        AutoFsObj.targetTsListData.observe(this) { targetTsList->
            onTargetTsListChanged(targetTsList)
        }
    }

    private fun timeToColor(targetTs:Long, current:Long) : ColorMode {
        val delta = targetTs - current
        if (delta < 0) {
            return ColorMode.IsOver
        }

        if (delta < 5 * 60 * 1000) {
            return ColorMode.AlmostClose
        }

        val calendar = Calendar.getInstance().also { it.timeInMillis = current }
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayStart = calendar.timeInMillis + 1
        return if (targetTs <= dayStart) {
            ColorMode.Today
        } else {
            ColorMode.Later
        }
    }

    private fun onTargetTsListChanged(targetTsList: List<TargetTs>) {
        val cur = System.currentTimeMillis()
        val list = targetTsList.map {
            AutoStartRcvBean(it.autoFsId,
                it.targetTs,
                it.offsetMinute,
                it.isClose,
                it.isLoop,
                color = timeToColor(it.targetTs, cur),
                TimeUtil.fmtLeftTimeStr(it.targetTs - cur))
        }
        adapter.submitList(list, false)
        if (list.isEmpty()) {
            binding.addBtn.visible()
            binding.emptyText.visible()
        } else {
            binding.emptyText.gone()
        }
    }
}
