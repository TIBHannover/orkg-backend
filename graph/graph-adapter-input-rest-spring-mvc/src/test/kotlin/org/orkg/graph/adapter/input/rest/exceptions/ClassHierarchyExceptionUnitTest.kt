package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.EmptyChildIds
import org.orkg.graph.domain.InvalidSubclassRelation
import org.orkg.graph.domain.ParentClassAlreadyExists
import org.orkg.graph.domain.ParentClassAlreadyHasChildren
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ClassHierarchyExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun classNotModifiable() {
        documentedGetRequestTo(InvalidSubclassRelation(ThingId("C123"), ThingId("C456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_subclass_relation")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The class "C123" cannot be a subclass of "C456".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun parentClassAlreadyExists() {
        documentedGetRequestTo(ParentClassAlreadyExists(ThingId("C123"), ThingId("C456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:parent_class_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The class "C123" already has a parent class (C456).""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun emptyChildIds() {
        documentedGetRequestTo(EmptyChildIds())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:empty_child_ids")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The provided list of child classes is empty.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun parentClassAlreadyHasChildren() {
        documentedGetRequestTo(ParentClassAlreadyHasChildren(ThingId("C123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:parent_class_already_has_children")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The class "C123" already has a child classes.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
