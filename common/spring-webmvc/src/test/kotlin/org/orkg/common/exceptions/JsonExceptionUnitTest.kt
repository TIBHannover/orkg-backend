package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.exceptions.JsonExceptionUnitTest.TestController
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@ContextConfiguration(classes = [CommonSpringConfig::class, TestController::class])
internal class JsonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun jsonMissingFieldException() {
        val type = "orkg:problem:mismatched_json_input"
        documentedPostRequestTo("/errors/json")
            .content("""{"field": "abc", "nested": {}}""")
            .perform()
            .andExpectErrorResponse("/errors/json")
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Field "$.nested.field" is either missing, "null", of invalid type, or contains "null" values.""")
            .andExpect(jsonPath("$.pointer", `is`("#/nested/field")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("pointer").description("A JSON Pointer that describes the location of the problem within the request's content.")
                    )
                )
            )
    }

    @Test
    fun jsonUnknownFieldException() {
        val type = "orkg:problem:unknown_json_field"
        documentedPostRequestTo("/errors/json")
            .content("""{"field": "abc", "nested": {"unknown": 1, "field": "def", "list": []}}""")
            .perform()
            .andExpectErrorResponse("/errors/json")
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown field "$.nested.unknown".""")
            .andExpect(jsonPath("$.pointer", `is`("#/nested/unknown")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("pointer").description("A JSON Pointer that describes the location of the problem within the request's content.")
                    )
                )
            )
    }

    @Test
    fun jsonTypeMismatchException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"field": [], "list": []}}""")
            .perform()
            .andExpectErrorResponse("/errors/json")
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:mismatched_json_input")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Field "$.nested.field" is either missing, "null", of invalid type, or contains "null" values.""")
            .andExpect(jsonPath("$.pointer", `is`("#/nested/field")))
    }

    @Test
    fun jsonTypeMismatchArrayException() {
        post("/errors/json")
            .content("""{"field": "abc", "array": [{"unknown": 1}] "nested": {"field": "def", "list": []}}""")
            .perform()
            .andExpectErrorResponse("/errors/json")
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:mismatched_json_input")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Field "$.array[0].field" is either missing, "null", of invalid type, or contains "null" values.""")
            .andExpect(jsonPath("$.pointer", `is`("#/array/0/field")))
    }

    @Test
    fun jsonNullValueInCollectionException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"field": "def", "list": [null]}}""")
            .perform()
            .andExpectErrorResponse("/errors/json")
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:mismatched_json_input")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Field "$.nested.list" is either missing, "null", of invalid type, or contains "null" values.""")
            .andExpect(jsonPath("$.pointer", `is`("#/nested/list")))
    }

    @Test
    fun jsonNullValueException() {
        post("/errors/json")
            .content("""{"field": null, "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpectErrorResponse("/errors/json")
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:mismatched_json_input")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Field "$.field" is either missing, "null", of invalid type, or contains "null" values.""")
            .andExpect(jsonPath("$.pointer", `is`("#/field")))
    }

    @Test
    fun jsonMalformedException() {
        val type = "orkg:problem:invalid_json"
        documentedPostRequestTo("/errors/json")
            .content("""{"field": "abc" "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpectErrorResponse("/errors/json")
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unexpected character ('"' (code 34)): was expecting comma to separate Object entries""")
            .andDocumentWithDefaultExceptionResponseFields(type)
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
