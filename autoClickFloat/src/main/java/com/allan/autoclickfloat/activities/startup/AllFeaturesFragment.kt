package com.allan.autoclickfloat.activities.startup

import android.app.AlarmManager
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.core.net.toUri
import com.allan.autoclickfloat.activities.autofs.AutoFsScreenOnFragment
import com.allan.autoclickfloat.activities.autofs.AutoStartAlarmFragment
import com.allan.autoclickfloat.activities.autofs.canWrite
import com.allan.autoclickfloat.activities.autofs.goToManageSetting
import com.allan.autoclickfloat.activities.autooneclick.AutoContinuousClickActivityFragment
import com.allan.autoclickfloat.activities.coverscreen.CoverScreenFloatingView
import com.allan.autoclickfloat.activities.nongyao.NongyaoFragment
import com.allan.autoclickfloat.activities.recordprojects.RecordProjectsAllFragment
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.databinding.AllFeaturesFragmentBinding
import com.au.logsystem.LogSystemFragment
import com.au.logsystem.oncelog.OnceLogViewFragment
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.permissions.hasFloatWindowPermission
import com.au.module_android.permissions.hasPermission
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.FragmentShellOrientationActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_android.utils.startActivityFix
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import java.io.File

class AllFeaturesFragment : BindingFragment<AllFeaturesFragmentBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.autoClickButton.onClick {
            FragmentShellOrientationActivity.start(requireActivity(), AutoContinuousClickActivityFragment::class.java)
        }

        binding.coverScreenBtn.onClick {
            if (requireContext().hasFloatWindowPermission()) {
                CoverScreenFloatingView.getInstance().show(0, 0, Const.rotationLiveData.value!!)
            } else {
                ConfirmCenterDialog.show(childFragmentManager,
                    "请授予悬浮窗权限",
                    "请点击确定，跳转去设置，找到应用「AShoot辅助点击方案」开启悬浮窗权限。",
                    "确定") {
                    requireActivity().gotoFloatWindowPermission()
                    it.dismissAllowingStateLoss()
                }
            }
        }

        binding.recordModeBtn.onClick {
            FragmentShellOrientationActivity.start(requireActivity(), RecordProjectsAllFragment::class.java)
        }

        binding.nongyaoBtn.onClick {
            FragmentShellOrientationActivity.start(requireActivity(), NongyaoFragment::class.java)
        }

        binding.debugBtn.onClick {
            FragmentShellActivity.start(requireActivity(), AutoFsScreenOnFragment::class.java)
        }

        binding.logSysBtn.onClick {
            FragmentShellActivity.start(requireActivity(), LogSystemFragment::class.java)
        }

        Const.autoOnePoint.autoOnePointOpenLiveData.observe(viewLifecycleOwner) {
            if (it) {
                binding.autoClickButton.text = "自动点击（开启中）"
            } else {
                binding.autoClickButton.text = "自动点击"
            }
        }

        binding.autoFsBtn.onClick(paddingTime = 50) {
            val tag = binding.autoFsBtn.tag
            if (tag == null) {
                binding.autoFsBtn.tag = 1
                return@onClick
            }

            if (tag == 1) {
                binding.autoFsBtn.tag = 2
                return@onClick
            }

            binding.autoFsBtn.tag = null

            val alarmManager: AlarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val hasBootReceiverPermission = hasPermission(android.Manifest.permission.RECEIVE_BOOT_COMPLETED)
            logd { "allanAlarm hasBootReceiverPermission $hasBootReceiverPermission" }

            if (alarmManager.canScheduleExactAlarms()) {
                if (canWrite(requireContext())) {
                    val packageName: String = requireContext().packageName
                    val pm = requireContext().getSystemService(POWER_SERVICE) as PowerManager
                    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                        ConfirmCenterDialog.show(childFragmentManager, "设置", "本功能需要“无限制 - 不采取任何限制措施”的电池管理设置，给予授权。", "OK", "取消",
                            cancelBlock = {
                            }, sureClick = {
                                val intent = Intent()
                                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.setData("package:$packageName".toUri())
                                startActivityFix(intent)
                                it.dismiss()
                            })
                    } else {
                        FragmentShellActivity.start(requireContext(), AutoStartAlarmFragment::class.java)
                    }
                } else {
                    ConfirmCenterDialog.show(childFragmentManager, "设置", "本功能需要调节亮度，即将跳转到系统设置，给予授权。", "OK", "取消",
                        cancelBlock = {
                        }, sureClick = {
                            goToManageSetting(requireActivity())
                            it.dismiss()
                        })
                }
            } else {
                ConfirmCenterDialog.show(childFragmentManager, "设置", "本功能需要精确闹钟权限，即将跳转到系统设置，给予授权。", "OK", "取消",
                    cancelBlock = {
                    }, sureClick = {
                        startActivityFix(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        it.dismiss()
                    })
            }
        }
    }
}