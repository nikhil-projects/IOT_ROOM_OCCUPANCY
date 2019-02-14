/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.blinky.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.blinky.profile.callback.BlinkyButton2DataCallback;
import no.nordicsemi.android.blinky.profile.callback.BlinkyButtonDataCallback;
import no.nordicsemi.android.blinky.profile.callback.BlinkyLedDataCallback;
import no.nordicsemi.android.blinky.profile.data.BlinkyLED;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
    /**
     * Nordic Blinky Service UUID.
     */
    public final static UUID LBS_UUID_SERVICE = UUID.fromString("0000A000-0000-1000-8000-00805F9B34FB");
    public final static UUID LBS2_UUID_SERVICE = UUID.fromString("0000A003-0000-1000-8000-00805F9B34FB");


    /**
     * BUTTON characteristic UUID.
     */
    private final static UUID LBS_UUID_BUTTON_CHAR = UUID.fromString("0000A001-0000-1000-8000-00805F9B34FB");
    private final static UUID LBS_UUID_BUTTON_CHAR2 = UUID.fromString("0000B808-0000-1000-8000-00805F9B34FB");

    /**
     * LED characteristic UUID.
     */
    private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("0000A005-0000-1000-8000-00805F9B34FB");

    private BluetoothGattCharacteristic mButtonCharacteristic, mLedCharacteristic, mButtonCharacteristic2;
    private LogSession mLogSession;
    private boolean mLedOn;

    public BlinkyManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * Sets the log session to be used for low level logging.
     *
     * @param session the session, or null, if nRF Logger is not installed.
     */
    public void setLogger(@Nullable final LogSession session) {
        this.mLogSession = session;
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        // The priority is a Log.X constant, while the Logger accepts it's log levels.
        Logger.log(mLogSession, LogContract.Log.Level.fromPriority(priority), message);
    }



    private final BlinkyButtonDataCallback mButtonCallback = new BlinkyButtonDataCallback() {
        @Override
        public void onButtonStateChanged(@NonNull final BluetoothDevice device,
                                         final boolean pressed) {
            log(LogContract.Log.Level.APPLICATION, "Button " + (pressed ? "pressed" : "released"));
            System.out.println("Button 1 state changed");
            mCallbacks.onButtonStateChanged(device, pressed);
        }


        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };



    private final BlinkyButton2DataCallback mButtonCallback2 = new BlinkyButton2DataCallback() {

        @Override
        public void onButton2StateChanged(@NonNull BluetoothDevice device, boolean pressed) {
            log(LogContract.Log.Level.APPLICATION, "Button 2" + (pressed ? "pressed" : "released"));
            System.out.println("Button 2 state changed");
            mCallbacks.onButton2StateChanged(device, pressed);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
            log(Log.WARN, "Invalid data received 2 : " + data);
        }
    };




    private final BlinkyLedDataCallback mLedCallback = new BlinkyLedDataCallback() {
        @Override
        public void onLedStateChanged(@NonNull final BluetoothDevice device,
                                      final boolean on) {
            mLedOn = on;
            log(LogContract.Log.Level.APPLICATION, "LED " + (on ? "ON" : "OFF"));
            mCallbacks.onLedStateChanged(device, on);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            // Data can only invalid if we read them. We assume the app always sends correct data.
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    /**
     * BluetoothGatt callbacks object.
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            setNotificationCallback(mButtonCharacteristic).with(mButtonCallback);
            setNotificationCallback(mButtonCharacteristic2).with(mButtonCallback2);

            readCharacteristic(mButtonCharacteristic).with(mButtonCallback).enqueue();
            readCharacteristic(mButtonCharacteristic2).with(mButtonCallback2).enqueue();

            enableNotifications(mButtonCharacteristic).enqueue();
            enableNotifications(mButtonCharacteristic2).enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
            final BluetoothGattService service2 = gatt.getService(LBS2_UUID_SERVICE);
            //:TODO Fix this to not just return true blindly
            if (service != null) {
                mButtonCharacteristic = service.getCharacteristic(LBS_UUID_BUTTON_CHAR);
                mButtonCharacteristic2 = service2.getCharacteristic(LBS_UUID_BUTTON_CHAR2);
            }

            boolean writeRequest = true;
            //mSupported = mButtonCharacteristic != null && mLedCharacteristic != null && writeRequest;
            return true;
        }



        @Override
        protected void onDeviceDisconnected() {
            mButtonCharacteristic = null;
            mButtonCharacteristic2 = null;
            mLedCharacteristic = null;
        }
    };



    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return true;
    }

    /**
     * Sends a request to the device to turn the LED on or off.
     *
     * @param on true to turn the LED on, false to turn it off.
     */
    public void send(final boolean on) {
        // Are we connected?
        if (mLedCharacteristic == null)
            return;

        // No need to change?
        if (mLedOn == on)
            return;

        log(Log.VERBOSE, "Turning LED " + (on ? "ON" : "OFF") + "...");
        writeCharacteristic(mLedCharacteristic, on ? BlinkyLED.turnOn() : BlinkyLED.turnOff())
                .with(mLedCallback).enqueue();
    }
}

