package org.orkg.graph.adapter.input.rest.exceptions

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.adapter.input.rest.exceptions.ClassExceptionUnitTest.TestController
import org.orkg.graph.domain.CannotResetURI
import org.orkg.graph.domain.ClassAlreadyExists
import org.orkg.graph.domain.ClassNotAllowed
import org.orkg.graph.domain.ClassNotModifiable
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExternalClassNotFound
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [TestController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class ClassExceptionUnitTest : MockMvcBaseTest("exceptions") {

    @Test
    fun classNotModifiable() {
        val id = ThingId("R123")

        get("/class-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/class-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Class "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun uriAlreadyInUse() {
        val id = ThingId("C123")
        val uri = "https://example.org/C123"

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
    fun uriNotAbsolute() {
        val uri = "invalid"

        get("/uri-not-absolute")
            .param("uri", uri)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("""The URI <$uri> is not absolute."""))
            .andExpect(jsonPath("$.path").value("/uri-not-absolute"))
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

    @Test
    fun externalClassNotFound() {
        val id = "R123"
        val ontologyId = "skos"

        get("/external-class-not-found")
            .param("id", id)
            .param("ontologyId", ontologyId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/external-class-not-found"))
            .andExpect(jsonPath("$.message").value("""External class "$id" for ontology "$ontologyId" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/class-not-modifiable")
        fun classNotModifiable(@RequestParam id: ThingId) {
            throw ClassNotModifiable(id)
        }

        @GetMapping("/uri-already-in-use")
        fun uriAlreadyInUse(@RequestParam id: ThingId, @RequestParam uri: ParsedIRI) {
            throw URIAlreadyInUse(uri, id)
        }

        @GetMapping("/uri-not-absolute")
        fun uriNotAbsolute(@RequestParam uri: ParsedIRI) {
            throw URINotAbsolute(uri)
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

        @GetMapping("/external-class-not-found")
        fun externalClassNotFound(@RequestParam ontologyId: String, @RequestParam id: String) {
            throw ExternalClassNotFound(ontologyId, id)
        }
    }
}
