package org.orkg.graph.adapter.input.rest.exceptions

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.CannotResetURI
import org.orkg.graph.domain.ClassAlreadyExists
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.ClassNotModifiable
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExternalClassNotFound
import org.orkg.graph.domain.ReservedClassId
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class ClassExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun classNotModifiable() {
        val type = "orkg:problem:class_not_modifiable"
        documentedGetRequestTo(ClassNotModifiable(ThingId("C123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Class "C123" is not modifiable.""")
            .andExpect(jsonPath("$.class_id").value("C123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("class_id").description("The id of the class."),
                    )
                )
            )
    }

    @Test
    fun uriAlreadyInUse() {
        val type = "orkg:problem:uri_already_in_use"
        documentedGetRequestTo(URIAlreadyInUse(ParsedIRI.create("https://example.org/C123"), ThingId("C123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""The URI <https://example.org/C123> is already assigned to class with ID "C123".""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("#/uri")))
            .andDocumentWithValidationExceptionResponseFields(type)
    }

    @Test
    fun uriNotAbsolute() {
        val type = "orkg:problem:uri_not_absolute"
        documentedGetRequestTo(URINotAbsolute(ParsedIRI.create("invalid")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""The URI <invalid> is not absolute.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("#/uri")))
            .andDocumentWithValidationExceptionResponseFields(type)
    }

    @Test
    fun classNotAllowed() {
        val type = "orkg:problem:reserved_class_id"
        documentedGetRequestTo(ReservedClassId(Classes.list))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Class id "${Classes.list}" is reserved.""")
            .andExpect(jsonPath("$.class_id").value(Classes.list.value))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("class_id").description("The id of the class."),
                    )
                )
            )
    }

    @Test
    fun classAlreadyExists() {
        val type = "orkg:problem:class_already_exists"
        documentedGetRequestTo(ClassAlreadyExists(Classes.list))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Class "${Classes.list}" already exists.""")
            .andExpect(jsonPath("$.class_id").value(Classes.list.value))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("class_id").description("The id of the class."),
                    )
                )
            )
    }

    @Test
    fun cannotResetURI() {
        val type = "orkg:problem:cannot_reset_uri"
        documentedGetRequestTo(CannotResetURI(Classes.list))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""The class "${Classes.list}" already has a URI. It is not allowed to change URIs.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("#/uri")))
            .andDocumentWithValidationExceptionResponseFields(type)
    }

    @Test
    fun classNotFound_withId() {
        val type = "orkg:problem:class_not_found"
        documentedGetRequestTo(ClassNotFound.withThingId(ThingId("C123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Class "C123" not found.""")
            .andExpect(jsonPath("$.class_id", `is`("C123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("class_id").description("The id of the class. (optional, either `class_id` or `class_uri` is present)"),
                        fieldWithPath("class_uri").type("URI").description("The uri of the class. (optional, either `class_id` or `class_uri` is present)").optional(),
                    )
                )
            )
    }

    @Test
    fun classNotFound_withURI() {
        get(ClassNotFound.withURI(ParsedIRI.create("https://example.org/C123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Class with URI "https://example.org/C123" not found.""")
            .andExpect(jsonPath("$.class_uri", `is`("https://example.org/C123")))
    }

    @Test
    fun externalClassNotFound() {
        val type = "orkg:problem:external_class_not_found"
        documentedGetRequestTo(ExternalClassNotFound("skos", "R123"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""External class "R123" for ontology "skos" not found.""")
            .andExpect(jsonPath("$.class_id").value("R123"))
            .andExpect(jsonPath("$.ontology_id").value("skos"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("class_id").description("The id of the class. (optional, either `class_id` or `class_uri` is present)"),
                        fieldWithPath("class_uri").type("URI").description("The uri of the class. (optional, either `class_id` or `class_uri` is present)").optional(),
                        fieldWithPath("ontology_id").description("The id of the class ontology."),
                    )
                )
            )
    }

    @Test
    fun reservedClassId() {
        val type = "orkg:problem:reserved_class_id"
        documentedGetRequestTo(ReservedClassId(ThingId("C123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Class id "C123" is reserved.""")
            .andExpect(jsonPath("$.class_id", `is`("C123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("class_id").description("The id of the class."),
                    )
                )
            )
    }
}
