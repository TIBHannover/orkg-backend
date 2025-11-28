package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.NotACurator
import org.orkg.testing.MockUserId
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class CommonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidLabel_withDefaultProperty() {
        val type = "orkg:problem:invalid_label"
        documentedGetRequestTo(InvalidLabel())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A label must not be blank or contain newlines or NULL characters and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/label""")))
            .andDocumentWithValidationExceptionResponseFields(InvalidLabel::class, type)
    }

    @Test
    fun invalidLabel_withProperty() {
        get(InvalidLabel("title"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_label")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A label must not be blank or contain newlines or NULL characters and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/title""")))
    }

    @Test
    fun invalidDescription_withDefaultProperty() {
        val type = "orkg:problem:invalid_description"
        documentedGetRequestTo(InvalidDescription())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A description must not be blank or contain NULL characters and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/description""")))
            .andDocumentWithValidationExceptionResponseFields(InvalidDescription::class, type)
    }

    @Test
    fun invalidDescription_withProperty() {
        get(InvalidDescription("contents"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_description")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A description must not be blank or contain NULL characters and must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/contents""")))
    }

    @Test
    fun neitherOwnerNorCurator_defaultConstructor() {
        val type = "orkg:problem:neither_owner_nor_curator"
        documentedGetRequestTo(NeitherOwnerNorCurator(ContributorId(MockUserId.CURATOR), ContributorId(MockUserId.USER), ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Contributor <b7c81eed-52e1-4f7a-93bf-e6d331b8df7b> does not own the entity to be deleted and is not a curator.""")
            .andExpect(jsonPath("$.owner_id").value("c4e9e7c2-cd5f-4385-af09-071674304e37"))
            .andExpect(jsonPath("$.contributor_id").value("b7c81eed-52e1-4f7a-93bf-e6d331b8df7b"))
            .andExpect(jsonPath("$.thing_id").value("R123"))
            .andDocument {
                responseFields<NeitherOwnerNorCurator>(
                    fieldWithPath("owner_id").description("The id of the owner."),
                    fieldWithPath("contributor_id").description("The id of the contributor."),
                    fieldWithPath("thing_id").description("The id of thing."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun neitherOwnerNorCurator_cannotChangeVisibility() {
        get(NeitherOwnerNorCurator.cannotChangeVisibility(ContributorId(MockUserId.CURATOR), ContributorId(MockUserId.USER), ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:neither_owner_nor_curator")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Insufficient permissions to change visibility of entity "R123".""")
            .andExpect(jsonPath("$.owner_id").value("c4e9e7c2-cd5f-4385-af09-071674304e37"))
            .andExpect(jsonPath("$.contributor_id").value("b7c81eed-52e1-4f7a-93bf-e6d331b8df7b"))
            .andExpect(jsonPath("$.thing_id").value("R123"))
    }

    @Test
    fun notACurator_defaultConstructor() {
        val type = "orkg:problem:not_a_curator"
        documentedGetRequestTo(NotACurator(ContributorId(MockUserId.USER)))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Contributor <b7c81eed-52e1-4f7a-93bf-e6d331b8df7b> is not a curator.""")
            .andExpect(jsonPath("$.contributor_id").value("b7c81eed-52e1-4f7a-93bf-e6d331b8df7b"))
            .andDocument {
                responseFields<NotACurator>(
                    fieldWithPath("contributor_id").description("The id of the contributor."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun notACurator_cannotChangeVerifiedStatus() {
        get(NotACurator.cannotChangeVerifiedStatus(ContributorId(MockUserId.USER)))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:not_a_curator")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Cannot change verified status: Contributor <b7c81eed-52e1-4f7a-93bf-e6d331b8df7b> is not a curator.""")
            .andExpect(jsonPath("$.contributor_id").value("b7c81eed-52e1-4f7a-93bf-e6d331b8df7b"))
    }
}
