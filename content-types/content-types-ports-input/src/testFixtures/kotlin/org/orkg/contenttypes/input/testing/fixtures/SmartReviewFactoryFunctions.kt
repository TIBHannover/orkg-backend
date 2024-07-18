package org.orkg.contenttypes.input.testing.fixtures

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.input.CreateSmartReviewUseCase
import org.orkg.contenttypes.input.SmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.SmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.SmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.SmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionCommand
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod

fun dummyCreateSmartReviewCommand() = CreateSmartReviewUseCase.CreateCommand(
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    title = "Dummy SmartReview Label",
    researchFields = listOf(ThingId("R12")),
    authors = listOf(
        Author(
            id = ThingId("R123"),
            name = "Author with id"
        ),
        Author(
            name = "Author with orcid",
            identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333"))
        ),
        Author(
            id = ThingId("R456"),
            name = "Author with id and orcid",
            identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444"))
        ),
        Author(
            name = "Author with homepage",
            homepage = URI.create("http://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    ),
    sustainableDevelopmentGoals = setOf(ThingId("SDG_3")),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    extractionMethod = ExtractionMethod.MANUAL,
    sections = listOf(
        dummySmartReviewComparisonSectionDefinition(),
        dummySmartReviewVisualizationSectionDefinition(),
        dummySmartReviewResourceSectionDefinition(),
        dummySmartReviewPredicateSectionDefinition(),
        dummySmartReviewOntologySectionDefinition(),
        dummySmartReviewTextSectionDefinition()
    ),
    references = listOf(
        "@misc{R123456,title = {Fancy title of a super important paper}",
        "@misc{R456789,title = {Another super important paper}"
    )
)

fun dummySmartReviewComparisonSectionDefinition(): SmartReviewComparisonSectionCommand =
    SmartReviewComparisonSectionCommand(
        heading = "comparison section heading",
        comparison = ThingId("R6416")
    )

fun dummySmartReviewVisualizationSectionDefinition(): SmartReviewVisualizationSectionCommand =
    SmartReviewVisualizationSectionCommand(
        heading = "visualization section heading",
        visualization = ThingId("R215648")
    )

fun dummySmartReviewResourceSectionDefinition(): SmartReviewResourceSectionCommand =
    SmartReviewResourceSectionCommand(
        heading = "resource section heading",
        resource = ThingId("R14565")
    )

fun dummySmartReviewPredicateSectionDefinition(): SmartReviewPredicateSectionCommand =
    SmartReviewPredicateSectionCommand(
        heading = "predicate section heading",
        predicate = ThingId("R15696541")
    )

fun dummySmartReviewOntologySectionDefinition(): SmartReviewOntologySectionCommand =
    SmartReviewOntologySectionCommand(
        heading = "ontology section heading",
        entities = listOf(ThingId("R1"), ThingId("P1")),
        predicates = listOf(ThingId("P1"))
    )

fun dummySmartReviewTextSectionDefinition(): SmartReviewTextSectionCommand =
    SmartReviewTextSectionCommand(
        heading = "Heading",
        `class` = Classes.introduction,
        text = "text section contents"
    )

fun SmartReviewSection.toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
    when (this) {
        is SmartReviewComparisonSection -> toSmartReviewComparisonSectionDefinition()
        is SmartReviewVisualizationSection -> toSmartReviewVisualizationSectionDefinition()
        is SmartReviewResourceSection -> toSmartReviewResourceSectionDefinition()
        is SmartReviewPredicateSection -> toSmartReviewPredicateSectionDefinition()
        is SmartReviewOntologySection -> toSmartReviewOntologySectionDefinition()
        is SmartReviewTextSection -> toSmartReviewTextSectionDefinition()
    }

fun SmartReviewComparisonSection.toSmartReviewComparisonSectionDefinition(): SmartReviewComparisonSectionCommand =
    SmartReviewComparisonSectionCommand(heading, comparison?.id)

fun SmartReviewVisualizationSection.toSmartReviewVisualizationSectionDefinition(): SmartReviewVisualizationSectionCommand =
    SmartReviewVisualizationSectionCommand(heading, visualization?.id)

fun SmartReviewResourceSection.toSmartReviewResourceSectionDefinition(): SmartReviewResourceSectionCommand =
    SmartReviewResourceSectionCommand(heading, resource?.id)

fun SmartReviewPredicateSection.toSmartReviewPredicateSectionDefinition(): SmartReviewPredicateSectionCommand =
    SmartReviewPredicateSectionCommand(heading, predicate?.id)

fun SmartReviewOntologySection.toSmartReviewOntologySectionDefinition(): SmartReviewOntologySectionCommand =
    SmartReviewOntologySectionCommand(heading, entities.map { it.id!! }, predicates.map { it.id })

fun SmartReviewTextSection.toSmartReviewTextSectionDefinition(): SmartReviewTextSectionCommand =
    SmartReviewTextSectionCommand(heading, classes.filter { it in SmartReviewTextSection.types }.single(), text)
