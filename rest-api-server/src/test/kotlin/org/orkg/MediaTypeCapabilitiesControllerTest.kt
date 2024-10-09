package org.orkg

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.MediaTypeCapabilitiesControllerTest.NonProducingFakeController
import org.orkg.MediaTypeCapabilitiesControllerTest.ProducingFakeController
import org.orkg.MediaTypeCapabilitiesControllerTest.TestConfiguration
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.MediaTypeCapabilityRegistry
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.testing.fixtures.FORMATTED_LABEL_CAPABILITY
import org.orkg.common.testing.fixtures.INCOMING_STATEMENTS_COUNT_CAPABILITY
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class)
@ContextConfiguration(
    classes = [
        ProducingFakeController::class,
        NonProducingFakeController::class,
        ExceptionHandler::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class,
        MediaTypeCapabilityRegistry::class,
        TestConfiguration::class
    ]
)
@WebMvcTest
internal class MediaTypeCapabilitiesControllerTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `Given rest controller, when an endpoint produces a media type that supports capabilities, then media type capabilities are parsed correctly`() {
        val result = get("/capabilities")
            .accept("application/test+json;formatted-label=true;incoming-statements-count=true")
            .perform()
            .responseContentsAsMap()

        result shouldNotBe null
        result.size shouldBe 2
        result["formatted-label"] shouldBe true
        result["incoming-statements-count"] shouldBe true
    }

    @Test
    fun `Given rest controller, when an endpoint produces a media type that supports capabilities but specified parameter value is malformed, then it throws an exception`() {
        get("/capabilities")
            .accept("application/test+json;formatted-label=NotABoolean")
            .perform()
            .andExpect(status().isNotAcceptable)
    }

    @Test
    fun `Given rest controller, when an endpoint produces a media type that supports capabilities but not all are specified, then the default media type capability values are passed`() {
        val result = get("/capabilities")
            .accept("application/test+json;formatted-label=true")
            .perform()
            .responseContentsAsMap()

        result shouldNotBe null
        result.size shouldBe 2
        result["formatted-label"] shouldBe true
        result["incoming-statements-count"] shouldBe false
    }

    @Test
    fun `Given rest controller, when an endpoint does not produce a media type that supports capabilities, then media type capabilities are not parsed`() {
        val result = get("/capabilities-non-producing")
            .accept("application/test+json;formatted-label=true")
            .perform()
            .responseContentsAsMap()

        result shouldNotBe null
        result.size shouldBe 0
    }

    @Test
    fun `Given rest controller, when an endpoint produces multiple media types and specified media type supports capabilities, then media type capabilities are parsed correctly`() {
        val result = get("/capabilities-produces-multi")
            .accept("application/test+json;formatted-label=true")
            .perform()
            .responseContentsAsMap()

        result shouldNotBe null
        result.size shouldBe 2
        result["formatted-label"] shouldBe true
        result["incoming-statements-count"] shouldBe false
    }

    @Test
    fun `Given rest controller, when an endpoint produces multiple media types and specified media type does not support capabilities, then media type capabilities are not parsed`() {
        val result = get("/capabilities-produces-multi")
            .accept("application/json;formatted-label=true")
            .perform()
            .responseContentsAsMap()

        result shouldNotBe null
        result.size shouldBe 0
    }

    @Test
    fun `Given rest controller, when an a controller class produces a media type that supports capabilities, then media type capabilities are parsed correctly`() {
        val result = get("/capabilities-declared-by-class")
            .accept("application/test+json;formatted-label=true;incoming-statements-count=true")
            .perform()
            .responseContentsAsMap()

        result shouldNotBe null
        result.size shouldBe 2
        result["formatted-label"] shouldBe true
        result["incoming-statements-count"] shouldBe true
    }

    @TestComponent
    internal class TestConfiguration(mediaTypeCapabilityRegistry: MediaTypeCapabilityRegistry) {
        init {
            mediaTypeCapabilityRegistry.register("application/test+json", FORMATTED_LABEL_CAPABILITY)
            mediaTypeCapabilityRegistry.register("application/test+json", INCOMING_STATEMENTS_COUNT_CAPABILITY)
        }
    }

    @TestComponent
    @RestController
    internal class NonProducingFakeController {
        @GetMapping("/capabilities", produces = ["application/test+json"])
        fun capabilities(capabilities: MediaTypeCapabilities) =
            capabilities.toParameterMap()

        @GetMapping("/capabilities-non-producing")
        fun capabilitiesNonProducing(capabilities: MediaTypeCapabilities) =
            capabilities.toParameterMap()

        @GetMapping("/capabilities-produces-multi", produces = ["application/test+json", MediaType.APPLICATION_JSON_VALUE])
        fun capabilitiesProducesMulti(capabilities: MediaTypeCapabilities) =
            capabilities.toParameterMap()
    }

    @TestComponent
    @RestController
    @RequestMapping(produces = ["application/test+json"])
    internal class ProducingFakeController {
        @GetMapping("/capabilities-declared-by-class")
        fun capabilitiesDeclaredByClass(capabilities: MediaTypeCapabilities) =
            capabilities.toParameterMap()
    }

    private fun ResultActions.responseContentsAsMap() = andReturn().response
        .let { objectMapper.readValue(it.contentAsString, object : TypeReference<Map<String, Any>>() {}) }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private fun MediaTypeCapabilities.toParameterMap() =
    keys.associateWith { getOrDefault(it) }
        .mapKeys { it.key.parameterName }
