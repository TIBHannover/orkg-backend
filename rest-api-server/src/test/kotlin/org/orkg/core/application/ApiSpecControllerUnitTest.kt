package org.orkg.core.application

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.Assets.responseJson
import org.orkg.common.testing.fixtures.Assets.responseYaml
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_YAML
import org.springframework.http.MediaType.APPLICATION_YAML_VALUE
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class ApiSpecControllerUnitTest {
    @TestPropertySource(properties = ["orkg.api-spec.path=src/test/resources/assets/responses/openapi3.yaml"])
    @ContextConfiguration(classes = [ApiSpecController::class, ExceptionTestConfiguration::class, FixedClockConfig::class])
    @WebMvcTest(controllers = [ApiSpecController::class])
    internal class ApiSpecControllerYamlSpecUnitTest : MockMvcBaseTest("root") {
        @Test
        fun `Given a YAML api spec, when fetched as JSON, then status is 200 OK and the api spec is returned as JSON`() {
            get("/api")
                .accept(APPLICATION_JSON)
                .perform()
                .andExpect(status().isOk)
                .andExpect(header().string(CONTENT_TYPE, startsWith(APPLICATION_JSON_VALUE)))
                .andExpect(MockMvcResultMatchers.content().string(responseJson("openapi3").trim()))
        }

        @Test
        fun `Given a YAML api spec, when fetched as YAML, then status is 200 OK and the api spec is returned as YAML`() {
            get("/api")
                .accept(APPLICATION_YAML)
                .perform()
                .andExpect(status().isOk)
                .andExpect(header().string(CONTENT_TYPE, startsWith(APPLICATION_YAML_VALUE)))
                .andExpect(MockMvcResultMatchers.content().string(responseYaml("openapi3")))
        }
    }

    @TestPropertySource(properties = ["orkg.api-spec.path=src/test/resources/assets/responses/openapi3.json"])
    @ContextConfiguration(classes = [ApiSpecController::class, ExceptionTestConfiguration::class, FixedClockConfig::class])
    @WebMvcTest(controllers = [ApiSpecController::class])
    internal class ApiSpecControllerJsonSpecUnitTest : MockMvcBaseTest("root") {
        @Test
        fun `Given a JSON api spec, when fetched as JSON, then status is 200 OK and the api spec is returned as JSON`() {
            get("/api")
                .accept(APPLICATION_JSON)
                .perform()
                .andExpect(status().isOk)
                .andExpect(header().string(CONTENT_TYPE, startsWith(APPLICATION_JSON_VALUE)))
                .andExpect(MockMvcResultMatchers.content().string(responseJson("openapi3").trim()))
        }

        @Test
        fun `Given a JSON api spec, when fetched as YAML, then status is 200 OK and the api spec is returned as YAML`() {
            get("/api")
                .accept(APPLICATION_YAML)
                .perform()
                .andExpect(status().isOk)
                .andExpect(header().string(CONTENT_TYPE, startsWith(APPLICATION_YAML_VALUE)))
                .andExpect(MockMvcResultMatchers.content().string(responseYaml("openapi3")))
        }
    }

    @TestPropertySource(properties = ["orkg.api-spec.path=src/test/resources/assets/responses/openapi3.yml"])
    @ContextConfiguration(classes = [ApiSpecController::class, ExceptionTestConfiguration::class, FixedClockConfig::class])
    @WebMvcTest(controllers = [ApiSpecController::class])
    internal class ApiSpecControllerYmlSpecUnitTest : MockMvcBaseTest("root") {
        @Test
        fun `Given a YML api spec, when fetched as JSON, then status is 200 OK and the api spec is returned as JSON`() {
            get("/api")
                .accept(APPLICATION_JSON)
                .perform()
                .andExpect(status().isOk)
                .andExpect(header().string(CONTENT_TYPE, startsWith(APPLICATION_JSON_VALUE)))
                .andExpect(MockMvcResultMatchers.content().string(responseJson("openapi3").trim()))
        }

        @Test
        fun `Given a YML api spec, when fetched as YAML, then status is 200 OK and the api spec is returned as YAML`() {
            get("/api")
                .accept(APPLICATION_YAML)
                .perform()
                .andExpect(status().isOk)
                .andExpect(header().string(CONTENT_TYPE, startsWith(APPLICATION_YAML_VALUE)))
                .andExpect(MockMvcResultMatchers.content().string(responseYaml("openapi3")))
        }
    }

    @TestPropertySource(properties = ["orkg.api-spec.path="])
    @ContextConfiguration(classes = [ApiSpecController::class, ExceptionTestConfiguration::class, FixedClockConfig::class])
    @WebMvcTest(controllers = [ApiSpecController::class])
    internal class ApiSpecControllerMissingSpecUnitTest : MockMvcBaseTest("root") {
        @Test
        fun `Given a missing api spec, when fetched as JSON, then status is 404 NOT FOUND`() {
            get("/api")
                .accept(APPLICATION_JSON)
                .perform()
                .andExpectErrorStatus(NOT_FOUND)
                .andExpectType("orkg:problem:not_found")
        }

        @Test
        fun `Given a missing api spec, when fetched as YAML, then status is 404 NOT FOUND`() {
            get("/api")
                .accept(APPLICATION_YAML)
                .perform()
                .andExpectErrorStatus(NOT_FOUND)
                .andExpectType("orkg:problem:not_found")
        }
    }

    @TestPropertySource(properties = ["orkg.api-spec.path=src/test/resources/assets/responses/invalid.json"])
    @ContextConfiguration(classes = [ApiSpecController::class, ExceptionTestConfiguration::class, FixedClockConfig::class])
    @WebMvcTest(controllers = [ApiSpecController::class])
    internal class ApiSpecControllerInvalidSpecUnitTest : MockMvcBaseTest("root") {
        @Test
        fun `Given an invalid api spec, when fetched as JSON, then status is 404 NOT FOUND`() {
            get("/api")
                .accept(APPLICATION_JSON)
                .perform()
                .andExpectErrorStatus(NOT_FOUND)
                .andExpectType("orkg:problem:not_found")
        }

        @Test
        fun `Given an invalid api spec, when fetched as YAML, then status is 404 NOT FOUND`() {
            get("/api")
                .accept(APPLICATION_YAML)
                .perform()
                .andExpectErrorStatus(NOT_FOUND)
                .andExpectType("orkg:problem:not_found")
        }
    }
}
