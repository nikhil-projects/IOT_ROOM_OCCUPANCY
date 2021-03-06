/* mbed Microcontroller Library
 * Copyright (c) 2006-2013 ARM Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __READ_SENSOR_SERVICE_H__
#define __READ_SENSOR_SERVICE_H__

class ReadSensorService {
public:
    const static uint16_t READ_SERVICE_UUID              = 0xA004;
    const static uint16_t READ_SERVICE_CHARACTERISTIC_UUID = 0xB005;

    ReadSensorService(BLE &_ble, bool buttonPressedInitial) :
        ble(_ble), buttonState(READ_SERVICE_CHARACTERISTIC_UUID, &buttonPressedInitial, GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY)
    {
        GattCharacteristic *charTable[] = {&buttonState};
        GattService         readSensorService(ReadSensorService::READ_SERVICE_UUID, charTable, sizeof(charTable) / sizeof(GattCharacteristic *));
        ble.gattServer().addService(readSensorService);
    }

    void updateButtonState(bool newState) {
        ble.gattServer().write(buttonState.getValueHandle(), (uint8_t *)&newState, sizeof(bool));
    }

private:
    BLE                              &ble;
    ReadOnlyGattCharacteristic<bool>  buttonState;
};

#endif /* #ifndef __READ_SENSOR_SERVICE_H__ */