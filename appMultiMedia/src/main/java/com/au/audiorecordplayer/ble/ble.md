### BLE

```
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
//            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            val receiver = BtBroadcastReceiver()
            context.registerReceiverFix(receiver, intentFilter)
```

广播注册的结果，并非一定是由你自己发起的bluetoothAdapter.startDiscovery()。系统定期扫描或者开启蓝牙瞬间的扫描，也会触发广播的回调。

```
getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
bluetoothAdapter?.startDiscovery()
```

#### bluetoothAdapter?.startDiscovery()

大约持续12秒。并且大概2分钟4次。所以你调用并不能保险。
