class BatteryService {
public:
    /**
     * @param[in] _ble
     *               BLE object for the underlying controller.
     * @param[in] level
     *               8bit batterly level. Usually used to represent percentage of batterly charge remaining.
     */
    BatteryService(BLE &_ble, uint8_t level = 100) :
        ble(_ble),
        batteryLevel(level),
        batteryLevelCharacteristic(GattCharacteristic::UUID_BATTERY_LEVEL_CHAR, &batteryLevel, GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY) {

        GattCharacteristic *charTable[] = {&batteryLevelCharacteristic};
        GattService         batteryService(GattService::UUID_BATTERY_SERVICE, charTable, sizeof(charTable) / sizeof(GattCharacteristic *));

        ble.addService(batteryService);
    }

    /**
     * @brief Update the battery level with a new value. Valid values lie between 0 and 100,
     * anything outside this range will be ignored.
     *
     * @param newLevel
     *              Update to battery level.
     */
    void updateBatteryLevel(uint8_t newLevel) {
        batteryLevel = newLevel;
        ble.gattServer().write(batteryLevelCharacteristic.getValueHandle(), &batteryLevel, 1);
    }

protected:
    /**
     * A reference to the underlying BLE instance that this object is attached to.
     * The services and characteristics will be registered in this BLE instance.
     */
    BLE &ble;

    /**
     * The current battery level represented as an integer from 0% to 100%.
     */
    uint8_t    batteryLevel;
    /**
     * A ReadOnlyGattCharacteristic that allows access to the peer device to the
     * batteryLevel value through BLE.
     */
    ReadOnlyGattCharacteristic<uint8_t> batteryLevelCharacteristic;
};

#endif /* #ifndef __BLE_BATTERY_SERVICE_H__*/