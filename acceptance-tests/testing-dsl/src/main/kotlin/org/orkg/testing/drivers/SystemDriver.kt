package org.orkg.testing.drivers

import org.orkg.testing.drivers.registration.RegistrationDriver
import org.orkg.world.World

/**
 * The system driver provides access to all other driver instances, and takes care of tearing them down.
 */
class SystemDriver {
    private val drivers: MutableMap<String, Driver> = mutableMapOf()
    private val world: World = World.controlledSystem()

    val registrationDriver: RegistrationDriver
        get() = provideDriverInstance("registration") { RegistrationDriver(world.environment) }

    fun tearDown() {
        drivers.forEach { (name, driver) ->
            driver.stop()
            drivers.remove(name)
        }
        require(drivers.isEmpty()) { "Error while tearing down drivers. Some drivers were not shut down properly: $drivers." }
    }

    private fun <T : Driver> provideDriverInstance(key: String, driverInstance: () -> T): T {
        if (drivers[key] == null) {
            val driver = drivers.getOrPut(key, driverInstance)
            driver.start()
        }
        @Suppress("UNCHECKED_CAST")
        return drivers[key]!! as T
    }
}

interface Driver {
    fun start() = Unit

    fun stop() = Unit
}
