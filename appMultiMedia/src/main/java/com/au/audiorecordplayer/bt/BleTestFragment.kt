package com.au.audiorecordplayer.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.annotation.EmptySuper
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import com.au.audiorecordplayer.bt.ble.BleFragment
import com.au.audiorecordplayer.bt.trandition.BtClassicFragment
import com.au.audiorecordplayer.databinding.BtBleBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

class BleTestFragment : BindingFragment<BtBleBinding>() {
    companion object {
        const val IS_BLE = "is_ble"

        fun start(context: Context, isBle: Boolean) {
            FragmentShellActivity.start(context, BleTestFragment::class.java,
                bundleOf(IS_BLE to isBle)
            )
        }
    }

    private val isBle by lazy {
        arguments?.getBoolean(IS_BLE) ?: false
    }

    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo(title = if(isBle) "BLE蓝牙" else "经典蓝牙")
    }

    val blePermissionHelp = BtPermissionHelp(this)

    val bluetoothAdapter: BluetoothAdapter? by lazy { (Globals.app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter }

    lateinit var subFragment: AbsSubBleTestFragment

    @SuppressLint("MissingPermission")
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        subFragment = AbsSubBleTestFragment.build(isBle)

        if (isBle) {
            binding.classicStartServerBtn.gone()
            binding.classicCloseTransferBtn.gone()
            binding.classicStartTransferBtn.gone()
        } else {
            binding.classicStartServerBtn.visible()
            binding.classicCloseTransferBtn.visible()
            binding.classicStartTransferBtn.visible()
            binding.classicStartServerBtn.onClick {
                subFragment.onClassicClick("startServer")
            }
            binding.classicCloseTransferBtn.onClick {
                subFragment.onClassicClick("closeTransfer")
            }
            binding.classicStartTransferBtn.onClick {
                subFragment.onClassicClick("clientStartTransfer")
            }
        }

        childFragmentManager.beginTransaction().add(subFragment, "").commit()
        binding.startScan.onClick {
            subFragment.onStartScan()
        }
        binding.stopScan.onClick {
            subFragment.onStopScan()
        }
    }

}

abstract class AbsSubBleTestFragment : androidx.fragment.app.Fragment() {
    companion object {
        fun build(isBle: Boolean) : AbsSubBleTestFragment {
            return if (isBle) BleFragment() else BtClassicFragment()
        }
    }

    val blePermissionHelp:BtPermissionHelp
        get() = (parentFragment as BleTestFragment).blePermissionHelp

    val bluetoothAdapter: BluetoothAdapter?
        get() = (parentFragment as BleTestFragment).bluetoothAdapter

    @MainThread
    fun updateInfo(info:String) {
        (parentFragment as BleTestFragment).binding.textView.text = info
    }

    abstract fun onStartScan()
    abstract fun onStopScan()

    @EmptySuper
    open fun onClassicClick(buttonType:String) {}
}