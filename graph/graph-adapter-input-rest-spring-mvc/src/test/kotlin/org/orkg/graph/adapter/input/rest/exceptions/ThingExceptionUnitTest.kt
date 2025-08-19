package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.ThingAlreadyExists
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ThingExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun thingAlreadyExists() {
        documentedGetRequestTo(ThingAlreadyExists(ThingId("S123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A thing with id "S123" already exists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
