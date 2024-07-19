package org.orkg.contenttypes.domain.testing.fixtures

import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PredicateReference
import org.orkg.contenttypes.domain.PublishedVersion
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Visibility
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

fun createDummySmartReview() = SmartReview(
    id = ThingId("R1456"),
    title = "Dummy Smart Review Title",
    researchFields = listOf(
        ObjectIdAndLabel(
            id = ThingId("R456"),
            label = "Research Field 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R789"),
            label = "Research Field 2"
        )
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to listOf("0000-0002-1825-0097")
            ),
            homepage = URI.create("https://example.org")
        ),
        Author(
            id = null,
            name = "Author 2",
            identifiers = emptyMap(),
            homepage = null
        )
    ),
    versions = VersionInfo(
        head = HeadVersion(
            id = ThingId("R1465"),
            label = "head",
            createdAt = OffsetDateTime.parse("2024-01-28T12:24:00.959539600+01:00"),
            createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        ),
        published = listOf(
            PublishedVersion(
                id = ThingId("R5466"),
                label = "version 2",
                createdAt = OffsetDateTime.parse("2024-01-30T12:24:00.959539600+01:00"),
                createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
                changelog = "change 2"
            ),
            PublishedVersion(
                id = ThingId("R13546"),
                label = "version 1",
                createdAt = OffsetDateTime.parse("2024-01-29T12:24:00.959539600+01:00"),
                createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
                changelog = "change 1"
            )
        )
    ),
    sustainableDevelopmentGoals = setOf(
        ObjectIdAndLabel(
            id = ThingId("SDG_1"),
            label = "No poverty"
        ),
        ObjectIdAndLabel(
            id = ThingId("SDG_2"),
            label = "Zero hunger"
        )
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    ),
    extractionMethod = ExtractionMethod.UNKNOWN,
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    visibility = Visibility.DEFAULT,
    unlistedBy = null,
    published = false,
    sections = listOf(
        createDummySmartReviewTextSection(),
        createDummySmartReviewComparisonSection(),
        createDummySmartReviewVisualizationSection(),
        createDummySmartReviewResourceSection(),
        createDummySmartReviewPredicateSection(),
        createDummySmartReviewOntologySection()
    ),
    references = listOf("@misc{R615465, title = {reference 1}}", "@misc{R615465, title = {reference 2}}")
)

fun createDummySmartReviewComparisonSection() = SmartReviewComparisonSection(
    id = ThingId("R456351"),
    heading = "comparison section heading",
    comparison = ResourceReference(
        id = ThingId("R6416"),
        label = "Comparison",
        classes = setOf(Classes.comparison)
    )
)

fun createDummySmartReviewVisualizationSection() = SmartReviewVisualizationSection(
    id = ThingId("R6521"),
    heading = "visualization section heading",
    visualization = ResourceReference(
        id = ThingId("R215648"),
        label = "Visualization",
        classes = setOf(Classes.visualization)
    )
)

fun createDummySmartReviewResourceSection() = SmartReviewResourceSection(
    id = ThingId("R14565"),
    heading = "resource section heading",
    resource = ResourceReference(ThingId("R1"), "some resource label", classes = setOf(Classes.problem))
)

fun createDummySmartReviewPredicateSection() = SmartReviewPredicateSection(
    id = ThingId("R15696541"),
    heading = "predicate section heading",
    predicate = PredicateReference(ThingId("P1"), "some predicate label")
)

fun createDummySmartReviewOntologySection() = SmartReviewOntologySection(
    id = ThingId("R16532"),
    heading = "ontology section heading",
    entities = listOf(
        ResourceReference(ThingId("R1"), "some resource label", classes = setOf(Classes.problem)),
        PredicateReference(ThingId("P1"), "some predicate label")
    ),
    predicates = listOf(PredicateReference(ThingId("P1"), "some predicate label"))
)

fun createDummySmartReviewTextSection() = SmartReviewTextSection(
    id = ThingId("R154686"),
    heading = "Heading",
    classes = setOf(Classes.introduction),
    text = "text section contents"
)

fun SmartReviewSection.toGroupedStatements(): Map<ThingId, List<GeneralStatement>> =
    when (this) {
        is SmartReviewComparisonSection -> toGroupedStatements()
        is SmartReviewVisualizationSection -> toGroupedStatements()
        is SmartReviewResourceSection -> toGroupedStatements()
        is SmartReviewPredicateSection -> toGroupedStatements()
        is SmartReviewOntologySection -> toGroupedStatements()
        is SmartReviewTextSection -> toGroupedStatements()
    }

fun SmartReviewComparisonSection.toGroupedStatements(): Map<ThingId, List<GeneralStatement>> {
    val root = createResource(id, label = heading, classes = setOf(Classes.comparisonSection))
    val statements = listOf(
        createStatement(
            id = StatementId("S1"),
            subject = root,
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(comparison!!.id, classes = setOf(Classes.comparison))
        )
    )
    return statements.groupBy { it.subject.id }
}

fun SmartReviewVisualizationSection.toGroupedStatements(): Map<ThingId, List<GeneralStatement>> {
    val root = createResource(id, label = heading, classes = setOf(Classes.visualizationSection))
    val statements = listOf(
        createStatement(
            id = StatementId("S1"),
            subject = root,
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(visualization!!.id, classes = setOf(Classes.visualization))
        )
    )
    return statements.groupBy { it.subject.id }
}

fun SmartReviewResourceSection.toGroupedStatements(): Map<ThingId, List<GeneralStatement>> {
    val root = createResource(id, label = heading, classes = setOf(Classes.resourceSection))
    val statements = listOf(
        createStatement(
            id = StatementId("S1"),
            subject = root,
            predicate = createPredicate(Predicates.hasLink),
            `object` = createResource(resource!!.id)
        )
    )
    return statements.groupBy { it.subject.id }
}

fun SmartReviewPredicateSection.toGroupedStatements(): Map<ThingId, List<GeneralStatement>> {
    val root = createResource(id, label = heading, classes = setOf(Classes.propertySection))
    val statements = listOf(
        createStatement(
            id = StatementId("S1"),
            subject = root,
            predicate = createPredicate(Predicates.hasLink),
            `object` = createPredicate(predicate!!.id)
        )
    )
    return statements.groupBy { it.subject.id }
}

fun SmartReviewOntologySection.toGroupedStatements(): Map<ThingId, List<GeneralStatement>> {
    val root = createResource(id, label = heading, classes = setOf(Classes.ontologySection))
    val statements = mutableListOf<GeneralStatement>()
    entities.forEach { entity ->
        statements += createStatement(
            id = StatementId("S${statements.size}"),
            subject = root,
            predicate = createPredicate(Predicates.hasEntity),
            `object` = createResource(entity.id!!)
        )
    }
    predicates.forEach { predicate ->
        statements += createStatement(
            id = StatementId("S${statements.size}"),
            subject = root,
            predicate = createPredicate(Predicates.showProperty),
            `object` = createPredicate(predicate.id)
        )
    }
    return statements.groupBy { it.subject.id }
}

fun SmartReviewTextSection.toGroupedStatements(): Map<ThingId, List<GeneralStatement>> {
    val root = createResource(id, label = heading, classes = classes)
    val statements = listOf(
        createStatement(
            id = StatementId("S1"),
            subject = root,
            predicate = createPredicate(Predicates.hasContent),
            `object` = createLiteral(label = text)
        )
    )
    return statements.groupBy { it.subject.id }
}
