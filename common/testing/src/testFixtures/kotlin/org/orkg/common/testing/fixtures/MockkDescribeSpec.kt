package org.orkg.common.testing.fixtures

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.confirmVerified

abstract class MockkDescribeSpec(
    body: DescribeSpec.() -> Unit = {}
) : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf
    body()
    beforeSpec { // mocks are reset before each context block
        clearAllMocks()
    }
    afterEach { // mocks are verified after every it block
        checkUnnecessaryStub()
        confirmVerified()
    }
})
