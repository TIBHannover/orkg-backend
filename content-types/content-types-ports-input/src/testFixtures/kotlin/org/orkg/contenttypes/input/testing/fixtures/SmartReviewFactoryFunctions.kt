package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
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
import org.orkg.contenttypes.input.CreateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.CreateSmartReviewUseCase
import org.orkg.contenttypes.input.DeleteSmartReviewSectionUseCase
import org.orkg.contenttypes.input.PublishSmartReviewUseCase
import org.orkg.contenttypes.input.SmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.SmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.SmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.SmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionCommand
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionCommand
import org.orkg.contenttypes.input.UpdateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.UpdateSmartReviewUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility
import java.util.UUID

fun createSmartReviewCommand() = CreateSmartReviewUseCase.CreateCommand(
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
            homepage = ParsedIRI("https://example.org/author")
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
        smartReviewComparisonSectionDefinition(),
        smartReviewVisualizationSectionDefinition(),
        smartReviewResourceSectionDefinition(),
        smartReviewPredicateSectionDefinition(),
        smartReviewOntologySectionDefinition(),
        smartReviewTextSectionDefinition()
    ),
    references = listOf(
        "@misc{R123456,title = {Fancy title of a super important paper}",
        "@misc{R456789,title = {Another super important paper}"
    )
)

fun updateSmartReviewCommand() = UpdateSmartReviewUseCase.UpdateCommand(
    smartReviewId = ThingId("R123"),
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
            homepage = ParsedIRI("https://example.org/author")
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
        smartReviewComparisonSectionDefinition(),
        smartReviewVisualizationSectionDefinition(),
        smartReviewResourceSectionDefinition(),
        smartReviewPredicateSectionDefinition(),
        smartReviewOntologySectionDefinition(),
        smartReviewTextSectionDefinition()
    ),
    references = listOf(
        "@misc{R123456,title = {Fancy title of a super important paper}",
        "@misc{R456789,title = {Another super important paper}"
    ),
    visibility = Visibility.DEFAULT
)

fun createSmartReviewComparisonSectionCommand() =
    CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand(
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        index = null,
        heading = "comparison section heading",
        comparison = ThingId("R6416")
    )

fun createSmartReviewVisualizationSectionCommand() =
    CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand(
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        index = null,
        heading = "visualization section heading",
        visualization = ThingId("R215648")
    )

fun createSmartReviewResourceSectionCommand() =
    CreateSmartReviewSectionUseCase.CreateResourceSectionCommand(
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        index = null,
        heading = "resource section heading",
        resource = ThingId("R14565")
    )

fun createSmartReviewPredicateSectionCommand() =
    CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand(
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        index = null,
        heading = "predicate section heading",
        predicate = ThingId("R15696541")
    )

fun createSmartReviewOntologySectionCommand() =
    CreateSmartReviewSectionUseCase.CreateOntologySectionCommand(
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        index = null,
        heading = "ontology section heading",
        entities = listOf(ThingId("R1"), ThingId("P1")),
        predicates = listOf(ThingId("P1"))
    )

fun createSmartReviewTextSectionCommand() =
    CreateSmartReviewSectionUseCase.CreateTextSectionCommand(
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        index = null,
        heading = "Heading",
        `class` = Classes.introduction,
        text = "text section contents"
    )

fun updateSmartReviewComparisonSectionCommand() =
    UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand(
        smartReviewSectionId = ThingId("R456"),
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        heading = "comparison section heading",
        comparison = ThingId("R6416")
    )

fun updateSmartReviewVisualizationSectionCommand() =
    UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand(
        smartReviewSectionId = ThingId("R456"),
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        heading = "visualization section heading",
        visualization = ThingId("R215648")
    )

fun updateSmartReviewResourceSectionCommand() =
    UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand(
        smartReviewSectionId = ThingId("R456"),
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        heading = "resource section heading",
        resource = ThingId("R14565")
    )

fun updateSmartReviewPredicateSectionCommand() =
    UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand(
        smartReviewSectionId = ThingId("R456"),
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        heading = "predicate section heading",
        predicate = ThingId("R15696541")
    )

fun updateSmartReviewOntologySectionCommand() =
    UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand(
        smartReviewSectionId = ThingId("R456"),
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        heading = "ontology section heading",
        entities = listOf(ThingId("R1"), ThingId("P1")),
        predicates = listOf(ThingId("P1"))
    )

fun updateSmartReviewTextSectionCommand() =
    UpdateSmartReviewSectionUseCase.UpdateTextSectionCommand(
        smartReviewSectionId = ThingId("R456"),
        contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
        smartReviewId = ThingId("R123"),
        heading = "Heading",
        `class` = Classes.introduction,
        text = "text section contents"
    )

fun smartReviewComparisonSectionDefinition(): SmartReviewComparisonSectionCommand =
    SmartReviewComparisonSectionCommand(
        heading = "comparison section heading",
        comparison = ThingId("R6416")
    )

fun smartReviewVisualizationSectionDefinition(): SmartReviewVisualizationSectionCommand =
    SmartReviewVisualizationSectionCommand(
        heading = "visualization section heading",
        visualization = ThingId("R215648")
    )

fun smartReviewResourceSectionDefinition(): SmartReviewResourceSectionCommand =
    SmartReviewResourceSectionCommand(
        heading = "resource section heading",
        resource = ThingId("R14565")
    )

fun smartReviewPredicateSectionDefinition(): SmartReviewPredicateSectionCommand =
    SmartReviewPredicateSectionCommand(
        heading = "predicate section heading",
        predicate = ThingId("R15696541")
    )

fun smartReviewOntologySectionDefinition(): SmartReviewOntologySectionCommand =
    SmartReviewOntologySectionCommand(
        heading = "ontology section heading",
        entities = listOf(ThingId("R1"), ThingId("P1")),
        predicates = listOf(ThingId("P1"))
    )

fun smartReviewTextSectionDefinition(): SmartReviewTextSectionCommand =
    SmartReviewTextSectionCommand(
        heading = "Heading",
        `class` = Classes.introduction,
        text = "text section contents"
    )

fun deleteSmartReviewSectionCommand() = DeleteSmartReviewSectionUseCase.DeleteCommand(
    contributorId = ContributorId(UUID.fromString("341995ab-1498-4d34-bac5-d39d866ce00e")),
    smartReviewId = ThingId("R123"),
    sectionId = ThingId("R456")
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
    SmartReviewTextSectionCommand(heading, classes.single { it in SmartReviewTextSection.types }, text)

fun publishSmartReviewCommand() = PublishSmartReviewUseCase.PublishCommand(
    smartReviewId = ThingId("R123"),
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    changelog = "new release",
    assignDOI = true,
    description = "review about important topic"
)
