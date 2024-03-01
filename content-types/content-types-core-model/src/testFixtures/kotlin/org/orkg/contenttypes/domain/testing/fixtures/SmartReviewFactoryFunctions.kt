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
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

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
            createdAt = OffsetDateTime.parse("2024-01-28T12:24:00.959539600+01:00")
        ),
        published = listOf(
            PublishedVersion(
                id = ThingId("R5466"),
                label = "version 2",
                createdAt = OffsetDateTime.parse("2024-01-30T12:24:00.959539600+01:00"),
                changelog = "change 2"
            ),
            PublishedVersion(
                id = ThingId("R13546"),
                label = "version 1",
                createdAt = OffsetDateTime.parse("2024-01-29T12:24:00.959539600+01:00"),
                changelog = "change 1"
            )
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
        SmartReviewTextSection(
            id = ThingId("R154686"),
            heading = "Heading",
            classes = setOf(Classes.introduction),
            text = "text section contents"
        ),
        SmartReviewComparisonSection(
            id = ThingId("R456351"),
            heading = "comparison section heading",
            comparison = ResourceReference(
                id = ThingId("R6416"),
                label = "Comparison",
                classes = setOf(Classes.comparison)
            )
        ),
        SmartReviewVisualizationSection(
            id = ThingId("R6521"),
            heading = "visualization section heading",
            visualization = ResourceReference(
                id = ThingId("R215648"),
                label = "Visualization",
                classes = setOf(Classes.visualization)
            )
        ),
        SmartReviewResourceSection(
            id = ThingId("R14565"),
            heading = "resource section heading",
            resource = ResourceReference(ThingId("R1"), "some resource label", classes = setOf(Classes.problem))
        ),
        SmartReviewPredicateSection(
            id = ThingId("R15696541"),
            heading = "predicate section heading",
            predicate = PredicateReference(ThingId("P1"), "some predicate label", "some predicate description")
        ),
        SmartReviewOntologySection(
            id = ThingId("R16532"),
            heading = "ontology section heading",
            entities = listOf(
                ResourceReference(ThingId("R1"), "some resource label", classes = setOf(Classes.problem)),
                PredicateReference(ThingId("P1"), "some predicate label", "some predicate description")
            ),
            predicates = listOf(PredicateReference(ThingId("P1"), "some predicate label", "some predicate description"))
        )
    ),
    references = listOf("@misc{R615465, title = {reference 1}}", "@misc{R615465, title = {reference 2}}")
)
