package org.orkg.testing.spring

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.restdocs.DocumentationBuilder
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.ResultActions
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private const val DOCUMENTATION_ENDPOINT_PATH = "/open-api-doc-test"

@WebMvcTest
@ContextConfiguration(classes = [ExceptionTestConfiguration::class, FixedClockConfig::class])
abstract class MockMvcOpenApiBaseTest : MockMvcBaseTest("open-api-doc") {
    @MockkBean
    private lateinit var testController: TestController

    protected fun <T : Any> document(value: T, builder: DocumentationBuilder.() -> Unit): ResultActions {
        every { testController.document() } returns value

        val result = documentedGetRequestTo(DOCUMENTATION_ENDPOINT_PATH)
            .perform()
            .andDocument(builder)

        verify(exactly = 1) { testController.document() }

        return result
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping(DOCUMENTATION_ENDPOINT_PATH)
        fun document(): Any = throw RuntimeException("If you see this message, the test has failed!")
    }
}
