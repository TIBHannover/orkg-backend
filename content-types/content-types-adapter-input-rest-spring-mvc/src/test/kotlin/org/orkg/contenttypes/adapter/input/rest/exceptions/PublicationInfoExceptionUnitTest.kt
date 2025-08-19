package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class PublicationInfoExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidMonth() {
        documentedGetRequestTo(InvalidMonth(-1))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_month")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid month "-1". Must be in range [1..12].""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
