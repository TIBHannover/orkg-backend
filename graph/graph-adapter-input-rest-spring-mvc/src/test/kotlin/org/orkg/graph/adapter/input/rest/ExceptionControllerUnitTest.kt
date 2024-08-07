package org.orkg.graph.adapter.input.rest

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.adapter.input.rest.ExceptionControllerUnitTest.FakeExceptionController
import org.orkg.graph.domain.CannotResetURI
import org.orkg.graph.domain.ClassAlreadyExists
import org.orkg.graph.domain.ClassNotAllowed
import org.orkg.graph.domain.ClassNotModifiable
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExternalClassNotFound
import org.orkg.graph.domain.ExternalPredicateNotFound
import org.orkg.graph.domain.ExternalResourceNotFound
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.InvalidStatement
import org.orkg.graph.domain.ListInUse
import org.orkg.graph.domain.LiteralNotModifiable
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.NotACurator
import org.orkg.graph.domain.PredicateNotModifiable
import org.orkg.graph.domain.ResourceNotModifiable
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementNotModifiable
import org.orkg.graph.domain.ThingAlreadyExists
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
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
    fun invalidLabelWithProperty() {
        get("/invalid-label")
            .param("property", "title")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("title"))
            .andExpect(jsonPath("$.errors[0].message").value("""A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-label"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidDescription() {
        get("/invalid-description")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("description"))
            .andExpect(jsonPath("$.errors[0].message").value("""A description must not be blank and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-description"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidDescriptionWithProperty() {
        get("/invalid-description")
            .param("property", "contents")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("contents"))
            .andExpect(jsonPath("$.errors[0].message").value("""A description must not be blank and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-description"))
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

    @Test
    fun neitherOwnerNorCuratorDelete() {
        val contributorId = ContributorId(MockUserId.USER)

        get("/neither-owner-nor-creator-delete")
            .param("contributorId", contributorId.toString())
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/neither-owner-nor-creator-delete"))
            .andExpect(jsonPath("$.message").value("""Contributor <$contributorId> does not own the entity to be deleted and is not a curator."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun neitherOwnerNorCuratorVisibility() {
        val id = "R123"

        get("/neither-owner-nor-creator-visibility")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/neither-owner-nor-creator-visibility"))
            .andExpect(jsonPath("$.message").value("""Insufficient permissions to change visibility of entity "$id". User must be a curator or the owner of the entity."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidStatementIsListElement() {
        get("/invalid-statement-is-list-element")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-statement-is-list-element"))
            .andExpect(jsonPath("$.message").value("A list element statement cannot be managed using the statements endpoint. Please see the documentation on how to manage lists."))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidStatementRosettaStoneStatementResource() {
        get("/invalid-statement-rosetta-stone-statement-resource")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-statement-rosetta-stone-statement-resource"))
            .andExpect(jsonPath("$.message").value("A rosetta stone statement resource cannot be managed using statements endpoint. Please see the documentation on how to manage rosetta stone statements."))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun thingAlreadyExists() {
        val id = "R123"

        get("/thing-already-exists")
            .param("id", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/thing-already-exists"))
            .andExpect(jsonPath("$.message").value("""A thing with id "$id" already exists."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun listInUse() {
        val id = "R123"

        get("/list-in-use")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/list-in-use"))
            .andExpect(jsonPath("$.message").value("""Unable to delete list "$id" because it is used in at least one statement."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun externalResourceNotFound() {
        val id = "R123"
        val ontologyId = "skos"

        get("/external-resource-not-found")
            .param("id", id)
            .param("ontologyId", ontologyId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/external-resource-not-found"))
            .andExpect(jsonPath("$.message").value("""External resource "$id" for ontology "$ontologyId" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun externalPredicateNotFound() {
        val id = "R123"
        val ontologyId = "skos"

        get("/external-predicate-not-found")
            .param("id", id)
            .param("ontologyId", ontologyId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/external-predicate-not-found"))
            .andExpect(jsonPath("$.message").value("""External predicate "$id" for ontology "$ontologyId" not found."""))
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

    @Test
    fun notACurator() {
        val contributorId = "62bfeea3-4210-436d-b953-effa5a07ed64"

        get("/not-a-curator")
            .param("contributorId", contributorId)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/not-a-curator"))
            .andExpect(jsonPath("$.message").value("""Contributor <$contributorId> is not a curator."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLiteralLabelTooLong() {
        get("/invalid-literal-label-too-long")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("A literal must be at most $MAX_LABEL_LENGTH characters long."))
            .andExpect(jsonPath("$.path").value("/invalid-literal-label-too-long"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLiteralLabelConstraintViolation() {
        val label = "not a number"
        val datatype = "xsd:decimal"

        get("/invalid-literal-label-constraint-violation")
            .param("label", label)
            .param("datatype", datatype)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("""Literal value "$label" is not a valid "$datatype"."""))
            .andExpect(jsonPath("$.path").value("/invalid-literal-label-constraint-violation"))
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

        @GetMapping("/invalid-label", params = ["property"])
        fun invalidLabel(@RequestParam property: String) {
            throw InvalidLabel(property)
        }

        @GetMapping("/invalid-description")
        fun invalidInvalidDescription() {
            throw InvalidDescription()
        }

        @GetMapping("/invalid-description", params = ["property"])
        fun invalidInvalidDescription(@RequestParam property: String) {
            throw InvalidDescription(property)
        }

        @GetMapping("/uri-already-in-use")
        fun uriAlreadyInUse(@RequestParam id: ThingId, @RequestParam uri: ParsedIRI) {
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

        @GetMapping("/neither-owner-nor-creator-delete")
        fun neitherOwnerNorCuratorDelete(@RequestParam contributorId: ContributorId) {
            throw NeitherOwnerNorCurator(contributorId)
        }

        @GetMapping("/neither-owner-nor-creator-visibility")
        fun neitherOwnerNorCuratorVisibility(@RequestParam id: ThingId) {
            throw NeitherOwnerNorCurator.changeVisibility(id)
        }

        @GetMapping("/invalid-statement-is-list-element")
        fun invalidStatementIsListElement() {
            throw InvalidStatement.isListElementStatement()
        }

        @GetMapping("/invalid-statement-rosetta-stone-statement-resource")
        fun invalidStatementRosettaStoneStatementResource() {
            throw InvalidStatement.includesRosettaStoneStatementResource()
        }

        @GetMapping("/thing-already-exists")
        fun thingAlreadyExists(@RequestParam id: ThingId) {
            throw ThingAlreadyExists(id)
        }

        @GetMapping("/list-in-use")
        fun listInUse(@RequestParam id: ThingId) {
            throw ListInUse(id)
        }

        @GetMapping("/external-resource-not-found")
        fun externalResourceNotFound(@RequestParam ontologyId: String, @RequestParam id: String) {
            throw ExternalResourceNotFound(ontologyId, id)
        }

        @GetMapping("/external-predicate-not-found")
        fun externalPredicateNotFound(@RequestParam ontologyId: String, @RequestParam id: String) {
            throw ExternalPredicateNotFound(ontologyId, id)
        }

        @GetMapping("/external-class-not-found")
        fun externalClassNotFound(@RequestParam ontologyId: String, @RequestParam id: String) {
            throw ExternalClassNotFound(ontologyId, id)
        }

        @GetMapping("/not-a-curator")
        fun notACurator(@RequestParam contributorId: ContributorId) {
            throw NotACurator(contributorId)
        }

        @GetMapping("/invalid-literal-label-too-long")
        fun invalidLiteralLabelTooLong() {
            throw InvalidLiteralLabel()
        }

        @GetMapping("/invalid-literal-label-constraint-violation")
        fun invalidLiteralLabelConstraintViolation(@RequestParam label: String, @RequestParam datatype: String) {
            throw InvalidLiteralLabel(label, datatype)
        }
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
