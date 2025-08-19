package org.orkg.community.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorAlreadyExists
import org.orkg.community.domain.ContributorNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ContributorExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun contributorAlreadyExists() {
        documentedGetRequestTo(ContributorAlreadyExists(ContributorId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:contributor_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Contributor "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" already exists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun contributorNotFound() {
        documentedGetRequestTo(ContributorNotFound(ContributorId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:contributor_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Contributor "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
