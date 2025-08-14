### BLE
https://blog.csdn.net/weixin_37794278/article/details/149506749

```java
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

#### 经典蓝牙

经典蓝牙适合 “一对一数据传输” 场景（如蓝牙串口通讯），开发流程为 “开启蓝牙→扫描设备→配对连接→数据传输→断开连接”。

经典蓝牙依赖android.bluetooth包下的类：

BluetoothAdapter：蓝牙适配器，核心入口；

​	startDiscovery/cancelDiscovery

BluetoothDevice：远程设备，通过 MAC 地址或名称标识；可通过adapter和字符串地址创建出来。
BluetoothSocket：数据传输的 socket（类似 TCP socket）；
BluetoothServerSocket：作为服务端时监听连接的 socket。

#### ble

BLE 适合 “低功耗、频繁小数据传输” 场景（如传感器数据上报），与经典蓝牙的核心区别是**基于 GATT 协议**，通过 “服务（Service）” 和 “特征（Characteristic）” 定义数据结构。

```kotlin
if (scanFilterList.isEmpty()) {
    leScanner.startScan(scanCallback)
} else {
    leScanner.startScan(scanFilterList, getScanSettings(), scanCallback)
}
```

> 大约持续12秒。并且大概2分钟4次。不能过于频繁调用，避免用户无响应。

GATT：通用属性配置文件，定义了设备间数据交互的规则；
Service：服务，包含多个特征（如 “心率服务” 包含 “心率测量特征”）；
Characteristic：特征，数据的最小单元（可读写、通知）；
    service?.getCharacteristic(UUID_WRITE_CHARACTERISTIC)
    service?.getCharacteristic(UUID_NOTIFICATION_CHARACTERISTIC)
    mNotifyChar 用于接收数据
    mWriteChar 用于发送数据
UUID：服务和特征的唯一标识（如心率测量特征 UUID：00002a37-0000-1000-8000-00805f9b34fb）；
Advertising：BLE 设备广播自身信息（如名称、服务 UUID），供其他设备扫描。
核心 API：

BluetoothLeScanner：BLE 扫描工具（替代经典蓝牙的BluetoothAdapter扫描）；
BluetoothGatt：GATT 客户端，负责连接和数据交互；
BluetoothGattCallback：GATT 操作的回调（连接状态、数据接收等）。
