package com.allan.androidlearning.activities

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentFontTestBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.UIStepExecutor
import com.au.module_android.utils.logd

@EntryFrgName
class FontTestFragment : BindingFragment<FragmentFontTestBinding>() {
    var index = 0

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.jumpBtn.onClick {
            FragmentShellActivity.start(requireActivity(), ActivityJumpFragment::class.java,
                enterAnim = com.au.module_android.R.anim.dialog_bottom_in_p,
                exitAnim = com.au.module_android.R.anim.dialog_bottom_out_p)
        }

        logd { "alland onCreate1" }
//        while(index++ <= 199999) {
//            binding.text.text = "changed: $index"
//        }
        logd { "alland onCreate2" }

        UIStepExecutor(Globals.mainHandler).apply {
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 20000) {
                        binding.text.text = "changed: $index"
                    }
                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 40000) {
                        binding.text.text = "changed: $index"
                    }
                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 60000) {
                        binding.text.text = "changed: $index"
                    }
                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 80000) {
                        binding.text.text = "changed: $index"
                    }

                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 100000) {
                        binding.text.text = "changed: $index"
                    }

                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 120000) {
                        binding.text.text = "changed: $index"
                    }

                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 140000) {
                        binding.text.text = "changed: $index"
                    }

                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 160000) {
                        binding.text.text = "changed: $index"
                    }

                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 180000) {
                        binding.text.text = "changed: $index"
                    }

                }
            })
            addStep(object : UIStepExecutor.Step() {
                override fun onExecute() {
                    while(index++ <= 200000) {
                        binding.text.text = "changed: $index"
                    }
                    logd { "alland init over!!" }
                }
            })
        }.start()
    }

    override fun onResume() {
        super.onResume()
        logd { "alland onResume!!" }
    }
}