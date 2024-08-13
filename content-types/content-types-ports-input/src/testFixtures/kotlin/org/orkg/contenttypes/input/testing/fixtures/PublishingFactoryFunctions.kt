package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.input.PublishPaperUseCase

fun createPaperPublishCommand(): PublishPaperUseCase.PublishCommand =
    PublishPaperUseCase.PublishCommand(
        id = ThingId("R123"),
        contributorId = ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"),
        subject = "Paper subject",
        description = "Description of the paper",
        authors = listOf(
            Author(
                id = ThingId("147"),
                name = "Josiah Stinkney Carberry",
                identifiers = mapOf(
                    "orcid" to listOf("0000-0002-1825-0097")
                ),
                homepage = ParsedIRI("https://example.org")
            ),
            Author(
                id = null,
                name = "Author 2",
                identifiers = emptyMap(),
                homepage = null
            )
        )
    )
