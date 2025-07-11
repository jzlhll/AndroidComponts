package com.allan.androidlearning.activities

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.view.updatePadding
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentRgb2HexBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.ImeHelper
import com.au.module_android.utils.hideImeNew
import kotlinx.coroutines.flow.combine
import okhttp3.internal.toHexString

@EntryFrgName(priority = 10)
class RgbToHexFragment : BindingFragment<FragmentRgb2HexBinding>() {
    private var doOncePaddingTop = false

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        requireActivity().title = "RGB to Hex"
        ImeHelper.assist(requireActivity())?.let {
            it.setOnImeListener { imeOffset,_,statusBarHeight,_->
                binding.root.translationY = -imeOffset.toFloat()

                if (!doOncePaddingTop) {
                    requireActivity().window.decorView.updatePadding(top = statusBarHeight)
                    doOncePaddingTop = true
                }
            }
        }

        binding.intToBitsBtn.onClick {
            val num = binding.intToBitsEdit.text.toString().toInt()
            //将num转成8bit的二进制
            val binaryString = Integer.toBinaryString(num)
            hideImeNew(requireActivity().window, binding.intToBitsBtn)
            binding.intToBitsEdit.setText(binaryString)
        }

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

    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo()
    }

    override fun isPaddingStatusBar(): Boolean {
        return true
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