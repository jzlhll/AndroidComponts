package com.au.audiorecordplayer.bt.ble;

import android.bluetooth.BluetoothProfile;

public class BleUtil {
    public static int getTypeValue(int type, int subtype) {
        return (subtype << 2) | type;
    }

    public static int getPackageType(int typeValue) {
        return typeValue & 0b11;
    }

    public static int getSubType(int typeValue) {
        return ((typeValue & 0b11111100) >> 2);
    }

    public static final int PACKAGE_VALUE = 0x01;
    public static final int SUBTYPE_CUSTOM_DATA = 0x13;

    public static final int DIRECTION_OUTPUT = 0;
    public static final int DIRECTION_INPUT = 1;

    private static final int FRAME_CTRL_POSITION_ENCRYPTED = 0;
    private static final int FRAME_CTRL_POSITION_CHECKSUM = 1;
    private static final int FRAME_CTRL_POSITION_DATA_DIRECTION = 2;
    private static final int FRAME_CTRL_POSITION_REQUIRE_ACK = 3;
    private static final int FRAME_CTRL_POSITION_FRAG = 4;

    public static int getFrameCTRLValue(boolean encrypted, boolean checksum, int direction, boolean requireAck, boolean frag) {
        int frame = 0;
        if (encrypted) {
            frame = frame | (1 << FRAME_CTRL_POSITION_ENCRYPTED);
        }
        if (checksum) {
            frame = frame | (1 << FRAME_CTRL_POSITION_CHECKSUM);
        }
        if (direction == DIRECTION_INPUT) {
            frame = frame | (1 << FRAME_CTRL_POSITION_DATA_DIRECTION);
        }
        if (requireAck) {
            frame = frame | (1 << FRAME_CTRL_POSITION_REQUIRE_ACK);
        }
        if (frag) {
            frame = frame | (1 << FRAME_CTRL_POSITION_FRAG);
        }

        return frame;
    }

    public static String bluetoothProfileToString(int state) {
        if (state == BluetoothProfile.STATE_CONNECTED) {
            return "CONNECTED";
        }
        if (state == BluetoothProfile.STATE_CONNECTING) {
            return "CONNECTING";
        }
        if (state == BluetoothProfile.STATE_DISCONNECTED) {
            return "DISCONNECTED";
        }
        if (state == BluetoothProfile.STATE_DISCONNECTING) {
            return "DISCONNECTING";
        }
        return "" + state;
    }
}
