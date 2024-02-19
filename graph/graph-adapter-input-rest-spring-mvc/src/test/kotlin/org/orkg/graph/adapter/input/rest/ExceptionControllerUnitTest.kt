package org.orkg.graph.adapter.input.rest

import java.net.URI
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.adapter.input.rest.ExceptionControllerUnitTest.FakeExceptionController
import org.orkg.graph.domain.CannotResetURI
import org.orkg.graph.domain.ClassAlreadyExists
import org.orkg.graph.domain.ClassNotAllowed
import org.orkg.graph.domain.ClassNotModifiable
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.LiteralNotModifiable
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.PredicateNotModifiable
import org.orkg.graph.domain.ResourceNotModifiable
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementNotModifiable
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.testing.FixedClockConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [FakeExceptionController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class ExceptionControllerUnitTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun resourceNotModifiable() {
        val id = ThingId("R123")

        get("/resource-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/resource-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Resource "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun classNotModifiable() {
        val id = ThingId("R123")

        get("/class-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/class-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Class "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun predicateNotModifiable() {
        val id = ThingId("R123")

        get("/predicate-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/predicate-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Predicate "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun literalNotModifiable() {
        val id = ThingId("R123")

        get("/literal-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/literal-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Literal "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun statementNotModifiable() {
        val id = StatementId("S123")

        get("/statement-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/statement-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Statement "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLabel() {
        get("/invalid-label")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("""A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-label"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun uriAlreadyInUse() {
        val id = ThingId("C123")
        val uri = "http://example.org/C123"

        get("/uri-already-in-use")
            .param("id", id.value)
            .param("uri", uri)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("""The URI <$uri> is already assigned to class with ID "$id"."""))
            .andExpect(jsonPath("$.path").value("/uri-already-in-use"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun classNotAllowed() {
        val id = Classes.list

        get("/class-not-allowed")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/class-not-allowed"))
            .andExpect(jsonPath("$.message").value("""Class id "$id" is not allowed."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun classAlreadyExists() {
        val id = Classes.list

        get("/class-already-exists")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/class-already-exists"))
            .andExpect(jsonPath("$.message").value("""Class "$id" already exists."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun cannotResetURI() {
        val id = ThingId("C123")

        get("/cannot-reset-uri")
            .param("id", id.value)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/cannot-reset-uri"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("""The class "$id" already has a URI. It is not allowed to change URIs."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/resource-not-modifiable")
        fun resourceNotModifiable(@RequestParam id: ThingId) {
            throw ResourceNotModifiable(id)
        }

        @GetMapping("/class-not-modifiable")
        fun classNotModifiable(@RequestParam id: ThingId) {
            throw ClassNotModifiable(id)
        }

        @GetMapping("/predicate-not-modifiable")
        fun predicateNotModifiable(@RequestParam id: ThingId) {
            throw PredicateNotModifiable(id)
        }

        @GetMapping("/literal-not-modifiable")
        fun literalNotModifiable(@RequestParam id: ThingId) {
            throw LiteralNotModifiable(id)
        }

        @GetMapping("/statement-not-modifiable")
        fun statementNotModifiable(@RequestParam id: StatementId) {
            throw StatementNotModifiable(id)
        }

        @GetMapping("/invalid-label")
        fun invalidLabel() {
            throw InvalidLabel()
        }

        @GetMapping("/uri-already-in-use")
        fun uriAlreadyInUse(@RequestParam id: ThingId, @RequestParam uri: URI) {
            throw URIAlreadyInUse(uri, id)
        }

        @GetMapping("/class-not-allowed")
        fun classNotAllowed(@RequestParam id: ThingId) {
            throw ClassNotAllowed(id)
        }

        @GetMapping("/class-already-exists")
        fun classAlreadyExists(@RequestParam id: ThingId) {
            throw ClassAlreadyExists(id)
        }

        @GetMapping("/cannot-reset-uri")
        fun cannotResetURI(@RequestParam id: ThingId) {
            throw CannotResetURI(id)
        }
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
