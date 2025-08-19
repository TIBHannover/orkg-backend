package org.orkg.community.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorIdentifierAlreadyExists
import org.orkg.community.domain.UnknownIdentifierType
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ContributorIdentifierExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun unknownIdentifierType() {
        documentedGetRequestTo(UnknownIdentifierType("doi"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_identifier_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown identifier type "doi".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun contributorIdentifierAlreadyExists() {
        documentedGetRequestTo(ContributorIdentifierAlreadyExists(ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"), "identifier"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:contributor_identifier_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Identifier "identifier" for contributor "9d791767-245b-46e1-b260-2c00fb34efda" already exists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
