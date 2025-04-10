package org.orkg.testing.dsl.junit

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInfo
import org.orkg.testing.drivers.SystemDriver
import org.orkg.testing.dsl.api.GraphAPI
import org.orkg.testing.dsl.api.PublicAPI
import org.orkg.testing.dsl.api.RegistrationAPI
import org.orkg.world.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base class for test cases using the Test DSL.
 *
 * This class handles initialization and shutdown.
 * It ensures fresh driver and test context are created for each test.
 */
@Tag("acceptance-test")
abstract class DslTestCase {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val systemDriver: SystemDriver = SystemDriver()
    private val world: World = World.controlledSystem("ORKG_ACCEPTANCE_TESTS_RANDOM_SEED")
    private lateinit var testContext: TestContext

    protected val registrationAPI: RegistrationAPI by lazy { RegistrationAPI(systemDriver, testContext) }
    protected val graphAPI: GraphAPI by lazy { GraphAPI(systemDriver, testContext) }
    protected val publicAPI: PublicAPI by lazy { PublicAPI(systemDriver, testContext) }

    @BeforeEach
    fun beforeEach(info: TestInfo) {
        testContext = TestContext(info, world)
        logger.debug("New empty test context created.")
    }

    @AfterEach
    fun afterEach() = systemDriver.tearDown()
}
