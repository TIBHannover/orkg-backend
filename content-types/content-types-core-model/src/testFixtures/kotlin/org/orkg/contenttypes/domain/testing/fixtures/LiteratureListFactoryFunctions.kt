package org.orkg.contenttypes.domain.testing.fixtures

import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PublishedVersion
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.domain.TextSection
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

fun createDummyLiteratureList() = LiteratureList(
    id = ThingId("R658946"),
    title = "Dummy Literature List Title",
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
        TextSection(
            id = ThingId("R154686"),
            heading = "Heading",
            headingSize = 2,
            text = "text section contents"
        ),
        ListSection(
            id = ThingId("R456351"),
            entries = listOf(
                ResourceReference(
                    id = ThingId("R154686"),
                    label = "Paper",
                    classes = setOf(Classes.paper)
                ),
                ResourceReference(
                    id = ThingId("R6416"),
                    label = "Comparison",
                    classes = setOf(Classes.comparison)
                )
            )
        )
    )
)
