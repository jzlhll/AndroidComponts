package com.allan.androidlearning.activities

import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentRgb2HexBinding
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.hideImeNew
import okhttp3.internal.toHexString

@EntroFrgName(priority = 10)
class RgbToHexFragment : BindingFragment<FragmentRgb2HexBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.rgbBtn.onClick {
            hideImeNew(requireActivity().window, binding.hexStartBtn)
            val c = getArgb(binding.editA.text.toString(),
                binding.editR.text.toString().toInt(),
                binding.editG.text.toString().toInt(),
                binding.editB.text.toString().toInt())

            binding.editHex.setText("#" + c.toHexString())

            binding.blockView.setBackgroundColor(c)
            binding.textTv.setBackgroundColor(c)
        }

        binding.hexStartBtn.onClick {
            hideImeNew(requireActivity().window, binding.rgbBtn)

            val c = Color.parseColor(binding.editHex.text.toString())

            val color = Color.valueOf(c)
            binding.editA.setText("" + (color.alpha() * 255).toInt())
            binding.editR.setText("" + (color.red() * 255).toInt())
            binding.editG.setText("" + (color.green() * 255).toInt())
            binding.editB.setText("" + (color.blue() * 255).toInt())

            binding.blockView.setBackgroundColor(c)
            binding.textTv.setBackgroundColor(c)
        }

        binding.backF0Btn.isChecked = true
        binding.textColorBlackCheck.isChecked = true

        binding.backRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.backNullBtn -> {
                    binding.backHost.setBackgroundColor(Color.TRANSPARENT)
                }
                R.id.backFFFBtn -> {
                    binding.backHost.setBackgroundColor(Color.parseColor("#cccccc"))
                }
                R.id.back000Btn -> {
                    binding.backHost.setBackgroundColor(Color.parseColor("#000000"))
                }
                R.id.backF0Btn -> {
                    binding.backHost.setBackgroundColor(Color.parseColor("#f0f0f0"))
                }
            }
        }

        binding.radioGroupText.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.textColorWhiteCheck -> {
                    binding.textTv.setTextColor(Color.parseColor("#ffffff"))
                }
                R.id.textColorBlackCheck -> {
                    binding.textTv.setTextColor(Color.parseColor("#000000"))
                }
                R.id.textColorCCCCheck -> {
                    binding.textTv.setTextColor(Color.parseColor("#cccccc"))
                }
            }
        }
    }

    @ColorInt
    fun getArgb(alphaStr:String, r:Int, g:Int, b:Int) : Int{
        val alpha = if (alphaStr.endsWith("%")) {
            val percent = alphaStr.substring(0, alphaStr.length - 1).toInt()
            percent * 255 / 100
        } else {
            alphaStr.toInt()
        }

        return Color.argb(alpha, r, g, b)
    }

}