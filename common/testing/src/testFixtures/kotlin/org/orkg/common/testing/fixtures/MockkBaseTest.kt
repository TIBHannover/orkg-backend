package org.orkg.common.testing.fixtures

import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

interface MockkBaseTest {
    @BeforeEach
    fun resetMocks() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        checkUnnecessaryStub()
        confirmVerified()
    }
}
