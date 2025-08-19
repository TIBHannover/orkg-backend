package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.NotACurator
import org.orkg.testing.MockUserId
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class CommonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidLabel_withDefaultProperty() {
        documentedGetRequestTo(InvalidLabel())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_label")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/label""")))
            .andDocumentWithValidationExceptionResponseFields()
    }

    @Test
    fun invalidLabel_withProperty() {
        get(InvalidLabel("title"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_label")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/title""")))
    }

    @Test
    fun invalidDescription_withDefaultProperty() {
        documentedGetRequestTo(InvalidDescription())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_description")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A description must not be blank and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/description""")))
            .andDocumentWithValidationExceptionResponseFields()
    }

    @Test
    fun invalidDescription_withProperty() {
        get(InvalidDescription("contents"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_description")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A description must not be blank and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/contents""")))
    }

    @Test
    fun neitherOwnerNorCurator_withContributorId() {
        documentedGetRequestTo(NeitherOwnerNorCurator(ContributorId(MockUserId.USER)))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:neither_owner_nor_curator")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Contributor <b7c81eed-52e1-4f7a-93bf-e6d331b8df7b> does not own the entity to be deleted and is not a curator.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun neitherOwnerNorCurator_cannotChangeVisibility() {
        documentedGetRequestTo(NeitherOwnerNorCurator.cannotChangeVisibility(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:neither_owner_nor_curator")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Insufficient permissions to change visibility of entity "R123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun notACurator_withContributorId() {
        documentedGetRequestTo(NotACurator(ContributorId(MockUserId.USER)))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:not_a_curator")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Contributor <b7c81eed-52e1-4f7a-93bf-e6d331b8df7b> is not a curator.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun notACurator_cannotChangeVerifiedStatus() {
        documentedGetRequestTo(NotACurator.cannotChangeVerifiedStatus(ContributorId(MockUserId.USER)))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:not_a_curator")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Cannot change verified status: Contributor <b7c81eed-52e1-4f7a-93bf-e6d331b8df7b> is not a curator.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
