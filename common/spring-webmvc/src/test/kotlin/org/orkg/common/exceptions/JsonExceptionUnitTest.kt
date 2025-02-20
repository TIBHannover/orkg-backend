package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.exceptions.JsonExceptionUnitTest.TestController
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [ExceptionHandler::class, TestController::class, CommonSpringConfig::class, FixedClockConfig::class])
internal class JsonExceptionUnitTest : MockMvcBaseTest("errors") {
    @Test
    fun jsonMissingFieldException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.nested.field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonUnknownFieldException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"unknown": 1, "field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Unknown field "$.nested.unknown".""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonTypeMismatchException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"field": [], "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.nested.field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonTypeMismatchArrayException() {
        post("/errors/json")
            .content("""{"field": "abc", "array": [{"unknown": 1}] "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.array[0].field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonNullValueInCollectionException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"field": "def", "list": [null]}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.nested.list" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonNullValueException() {
        post("/errors/json")
            .content("""{"field": null, "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonMalformedException() {
        post("/errors/json")
            .content("""{"field": "abc" "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Unexpected character ('"' (code 34)): was expecting comma to separate Object entries""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @PostMapping("/errors/json")
        fun json(
            @RequestBody request: JsonRequest,
        ): Nothing = throw NotImplementedError()

        @Suppress("ArrayInDataClass")
        data class JsonRequest(
            val field: String,
            val nested: Nested,
            val array: Array<Nested>,
        ) {
            data class Nested(
                val field: String,
                val list: List<String>,
            )
        }
    }
}
