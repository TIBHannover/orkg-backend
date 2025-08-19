package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.ListInUse
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ListExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun listInUse() {
        documentedGetRequestTo(ListInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:list_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete list "R123" because it is used in at least one statement.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
