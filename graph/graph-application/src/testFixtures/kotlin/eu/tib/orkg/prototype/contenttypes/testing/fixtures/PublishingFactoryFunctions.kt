package eu.tib.orkg.prototype.contenttypes.testing.fixtures

import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.services.PublishingService
import eu.tib.orkg.prototype.contenttypes.spi.DoiService
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI

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

fun dummyRegisterDoiCommand(): DoiService.RegisterCommand =
    DoiService.RegisterCommand(
        suffix = "182",
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
        resourceType = Classes.paper.value,
        resourceTypeGeneral = "Dataset",
        relatedIdentifiers = listOf("10.48366/r609337")
    )
