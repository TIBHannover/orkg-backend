package org.orkg.contenttypes.testing.fixtures

import java.net.URI
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.PublishingService
import org.orkg.graph.domain.Classes

fun dummyPublishCommand(): PublishingService.PublishCommand =
    PublishingService.PublishCommand(
        id = ThingId("R123"),
        title = "Paper title",
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
