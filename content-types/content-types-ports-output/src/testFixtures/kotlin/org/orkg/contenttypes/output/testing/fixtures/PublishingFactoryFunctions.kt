package org.orkg.contenttypes.output.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.output.DoiService
import org.orkg.contenttypes.output.HandleService
import org.orkg.graph.domain.Classes
import java.net.URI

fun createRegisterDoiCommand(): DoiService.RegisterCommand =
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
                    "orcid" to listOf("0000-0002-1825-0097")
                ),
                homepage = ParsedIRI.create("https://example.org")
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

fun registerHandleCommand(): HandleService.RegisterCommand =
    HandleService.RegisterCommand(
        suffix = "182",
        url = URI.create("https://example.org"),
    )
