package org.orkg.dataimport.input.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.testing.fixtures.Assets.turtle
import org.orkg.dataimport.domain.rdf.RdfFile.Format
import org.orkg.dataimport.input.CreateRdfFileUseCase

fun createRdfFileCommand() = CreateRdfFileUseCase.CreateCommand(
    contributorId = ContributorId("e7de95a8-d1f5-4837-9a1f-a2eb8b45a254"),
    name = "aliceKnowsBob.ttl",
    data = turtle("aliceKnowsBob"),
    baseUri = IRI.create("http://example.org"),
    format = Format.TURTLE,
)
