package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidBibTeXReference
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.contenttypes.domain.RequiresAtLeastTwoContributions
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.ResearchProblemNotFound
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class CommonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidMonth() {
        val type = "orkg:problem:invalid_month"
        documentedGetRequestTo(InvalidMonth(0))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid month "0". Must be in range [1..12].""")
            .andExpect(jsonPath("$.month").value("0"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("month").description("The month value."),
                    )
                )
            )
    }

    @Test
    fun sustainableDevelopmentGoalNotFound() {
        val type = "orkg:problem:sustainable_development_goal_not_found"
        documentedGetRequestTo(SustainableDevelopmentGoalNotFound(ThingId("SDG1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Sustainable Development Goal "SDG1" not found.""")
            .andExpect(jsonPath("$.sustainable_development_goal_id").value("SDG1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("sustainable_development_goal_id").description("The id of the sustainable development goal."),
                    )
                )
            )
    }

    @Test
    fun invalidBibTeXReference() {
        val type = "orkg:problem:invalid_bibtex_reference"
        documentedGetRequestTo(InvalidBibTeXReference("not bibtex"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid BibTeX reference "not bibtex".""")
            .andExpect(jsonPath("$.bibtex_reference").value("not bibtex"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("bibtex_reference").description("The provided bibtex reference."),
                    )
                )
            )
    }

    @Test
    fun onlyOneResearchFieldAllowed() {
        val type = "orkg:problem:only_one_research_field_allowed"
        documentedGetRequestTo(OnlyOneResearchFieldAllowed())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ony one research field is allowed.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
    }

    @Test
    fun onlyOneOrganizationAllowed() {
        val type = "orkg:problem:only_one_organization_allowed"
        documentedGetRequestTo(OnlyOneOrganizationAllowed())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ony one organization is allowed.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
    }

    @Test
    fun onlyOneObservatoryAllowed() {
        val type = "orkg:problem:only_one_observatory_allowed"
        documentedGetRequestTo(OnlyOneObservatoryAllowed())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ony one observatory is allowed.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
    }

    @Test
    fun requiresAtLeastTwoContributions() {
        val type = "orkg:problem:requires_at_least_two_contributions"
        documentedGetRequestTo(RequiresAtLeastTwoContributions())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""At least two contributions are required.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
    }

    @Test
    fun thingNotDefined() {
        val type = "orkg:problem:thing_not_defined"
        documentedGetRequestTo(ThingNotDefined("#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Thing "#temp1" not defined.""")
            .andExpect(jsonPath("$.thing_id").value("#temp1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("thing_id").description("The id of the thing."),
                    )
                )
            )
    }

    @Test
    fun duplicateTempIds() {
        val type = "orkg:problem:duplicate_temp_ids"
        documentedGetRequestTo(DuplicateTempIds(mapOf("#temp1" to 5)))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Duplicate temp ids: #temp1=5.""")
            .andExpect(jsonPath("$.duplicate_temp_ids['#temp1']").value("5"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        subsectionWithPath("duplicate_temp_ids").description("A map of temp ids to their occurrence count."),
                    )
                )
            )
    }

    @Test
    fun invalidTempId() {
        val type = "orkg:problem:invalid_temp_id"
        documentedGetRequestTo(InvalidTempId(":temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid temp id ":temp1". Requires "#" as prefix.""")
            .andExpect(jsonPath("$.temp_id").value(":temp1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("temp_id").description("The temp id."),
                    )
                )
            )
    }

    @Test
    fun thingIsNotAClass() {
        val type = "orkg:problem:thing_is_not_a_class"
        documentedGetRequestTo(ThingIsNotAClass(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Thing "R123" is not a class.""")
            .andExpect(jsonPath("$.thing_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("thing_id").description("The id of the thing."),
                    )
                )
            )
    }

    @Test
    fun thingIsNotAPredicate() {
        val type = "orkg:problem:thing_is_not_a_predicate"
        documentedGetRequestTo(ThingIsNotAPredicate("R123"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Thing "R123" is not a predicate.""")
            .andExpect(jsonPath("$.thing_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("thing_id").description("The id of the thing."),
                    )
                )
            )
    }

    @Test
    fun invalidStatementSubject() {
        val type = "orkg:problem:invalid_statement_subject"
        documentedGetRequestTo(InvalidStatementSubject("L123"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid statement subject "L123".""")
            .andExpect(jsonPath("$.subject_id").value("L123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("subject_id").description("The id of the subject."),
                    )
                )
            )
    }

    @Test
    fun emptyContribution_withIndex() {
        val type = "orkg:problem:empty_contribution"
        documentedGetRequestTo(EmptyContribution(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Contribution at index "5" does not contain any statements.""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("index").description("Index of the contribution. (optional)")
                    )
                )
            )
    }

    @Test
    fun emptyContribution_withoutIndex() {
        get(EmptyContribution())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:empty_contribution")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Contribution does not contain any statements.""")
    }

    @Test
    fun researchProblemNotFound() {
        val type = "orkg:problem:research_problem_not_found"
        documentedGetRequestTo(ResearchProblemNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Research problem "R123" not found.""")
            .andExpect(jsonPath("$.research_problem_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("research_problem_id").description("The id of the research problem."),
                    )
                )
            )
    }

    @Test
    fun researchFieldNotFound() {
        val type = "orkg:problem:research_field_not_found"
        documentedGetRequestTo(ResearchFieldNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Research field "R123" not found.""")
            .andExpect(jsonPath("$.research_field_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("research_field_id").description("The id of the research field."),
                    )
                )
            )
    }
}
