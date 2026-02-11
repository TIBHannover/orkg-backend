package org.orkg.dataimport.adapter.input.rest

import org.junit.jupiter.api.Test
import org.orkg.dataimport.testing.fixtures.configuration.DataImportControllerUnitTestConfiguration
import org.orkg.dataimport.testing.fixtures.existingPredicateContributionStatementResponseFields
import org.orkg.dataimport.testing.fixtures.newPredicateContributionStatementResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.testing.spring.MockMvcOpenApiBaseTest
import org.orkg.testing.spring.restdocs.oneOf
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [DataImportControllerUnitTestConfiguration::class])
internal class OpenApiDocTest : MockMvcOpenApiBaseTest() {
    @Test
    fun contributionStatement() {
        document(createExistingPredicateContributionStatementRepresentation()) {
            responseFields<ContributionStatementRepresentation>(
                oneOf(
                    ExistingPredicateContributionStatementRepresentation::class,
                    NewPredicateContributionStatementRepresentation::class,
                )
            )
        }
    }

    @Test
    fun existingPredicateContributionStatement() {
        document(createExistingPredicateContributionStatementRepresentation()) {
            responseFields<ExistingPredicateContributionStatementRepresentation>(
                existingPredicateContributionStatementResponseFields()
            )
        }
    }

    @Test
    fun newPredicateContributionStatement() {
        document(createNewPredicateContributionStatementRepresentation()) {
            responseFields<NewPredicateContributionStatementRepresentation>(
                newPredicateContributionStatementResponseFields()
            )
        }
    }

    private fun createNewPredicateContributionStatementRepresentation() =
        NewPredicateContributionStatementRepresentation(
            predicateLabel = "has subfield",
            `object` = createTypedValueRepresentation(),
        )

    private fun createExistingPredicateContributionStatementRepresentation() =
        ExistingPredicateContributionStatementRepresentation(
            predicateId = Predicates.hasSubfield,
            `object` = createTypedValueRepresentation(),
        )

    private fun createTypedValueRepresentation(): TypedValueRepresentation =
        TypedValueRepresentation(namespace = "resource", value = "Field", type = Classes.resource)
}
