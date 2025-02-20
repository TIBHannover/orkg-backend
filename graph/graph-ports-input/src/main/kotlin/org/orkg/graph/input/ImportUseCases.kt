package org.orkg.graph.input

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface ImportUseCases :
    ImportResourceUseCase,
    ImportPredicateUseCase,
    ImportClassUseCase

interface ImportResourceUseCase {
    fun importResourceByShortForm(contributorId: ContributorId, ontologyId: String, shortForm: String): ThingId

    fun importResourceByURI(contributorId: ContributorId, ontologyId: String, uri: ParsedIRI): ThingId
}

interface ImportPredicateUseCase {
    fun importPredicateByShortForm(contributorId: ContributorId, ontologyId: String, shortForm: String): ThingId

    fun importPredicateByURI(contributorId: ContributorId, ontologyId: String, uri: ParsedIRI): ThingId
}

interface ImportClassUseCase {
    fun importClassByShortForm(contributorId: ContributorId, ontologyId: String, shortForm: String): ThingId

    fun importClassByURI(contributorId: ContributorId, ontologyId: String, uri: ParsedIRI): ThingId
}
