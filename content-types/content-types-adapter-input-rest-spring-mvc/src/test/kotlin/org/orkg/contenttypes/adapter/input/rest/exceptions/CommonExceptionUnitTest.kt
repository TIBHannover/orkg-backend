package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
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
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class CommonExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidMonth() {
        documentedGetRequestTo(InvalidMonth(0))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_month")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid month "0". Must be in range [1..12].""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun sustainableDevelopmentGoalNotFound() {
        documentedGetRequestTo(SustainableDevelopmentGoalNotFound(ThingId("SDG1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:sustainable_development_goal_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Sustainable Development Goal "SDG1" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidBibTeXReference() {
        documentedGetRequestTo(InvalidBibTeXReference("not bibtex"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_bibtex_reference")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid BibTeX reference "not bibtex".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun onlyOneResearchFieldAllowed() {
        documentedGetRequestTo(OnlyOneResearchFieldAllowed())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_research_field_allowed")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ony one research field is allowed.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun onlyOneOrganizationAllowed() {
        documentedGetRequestTo(OnlyOneOrganizationAllowed())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_organization_allowed")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ony one organization is allowed.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun onlyOneObservatoryAllowed() {
        documentedGetRequestTo(OnlyOneObservatoryAllowed())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_observatory_allowed")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Ony one observatory is allowed.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun requiresAtLeastTwoContributions() {
        documentedGetRequestTo(RequiresAtLeastTwoContributions())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:requires_at_least_two_contributions")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""At least two contributions are required.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun thingNotDefined() {
        documentedGetRequestTo(ThingNotDefined("#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_not_defined")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Thing "#temp1" not defined.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun duplicateTempIds() {
        documentedGetRequestTo(DuplicateTempIds(mapOf("#temp1" to 5)))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:duplicate_temp_ids")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Duplicate temp ids: #temp1=5.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidTempId() {
        documentedGetRequestTo(InvalidTempId(":temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_temp_id")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid temp id ":temp1". Requires "#" as prefix.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun thingIsNotAClass() {
        documentedGetRequestTo(ThingIsNotAClass(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_is_not_a_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Thing "R123" is not a class.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun thingIsNotAPredicate() {
        documentedGetRequestTo(ThingIsNotAPredicate("R123"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_is_not_a_predicate")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Thing "R123" is not a predicate.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidStatementSubject() {
        documentedGetRequestTo(InvalidStatementSubject("L123"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement_subject")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid statement subject "L123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun emptyContribution_withIndex() {
        documentedGetRequestTo(EmptyContribution(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:empty_contribution")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Contribution at index "5" does not contain any statements.""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
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
        get(ResearchProblemNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:research_problem_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Research problem "R123" not found.""")
    }

    @Test
    fun researchFieldNotFound() {
        get(ResearchFieldNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:research_field_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Research field "R123" not found.""")
    }
}
