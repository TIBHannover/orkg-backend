package org.orkg.contenttypes.domain.testing.fixtures

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.PublishingService
import org.orkg.graph.domain.Classes

fun dummyPublishCommand(): PublishingService.PublishCommand =
    PublishingService.PublishCommand(
        id = ThingId("R123"),
        title = "Paper title",
        contributorId = ContributorId("257d6a0e-92c2-4a91-8c44-5d33366df441"),
        subject = "Paper subject",
        description = "Description of the paper",
        url = URI.create("https://example.org"),
        creators = listOf(
            Author(
                id = ThingId("147"),
                name = "Josiah Stinkney Carberry",
                identifiers = mapOf(
                    "orcid" to "0000-0002-1825-0097"
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
        resourceType = Classes.paper,
        relatedIdentifiers = listOf("10.1000/183")
    )
