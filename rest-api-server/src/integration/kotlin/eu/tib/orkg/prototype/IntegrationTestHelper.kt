package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.CreateClassUseCase
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.net.URI
import java.util.*

// Classes

fun CreateClassUseCase.createClasses(vararg classes: Pair<String, String>) =
    classes.forEach {
        createClass(id = it.first, label = it.second)
    }

fun CreateClassUseCase.createClasses(vararg classes: String) =
    classes.forEach {
        createClass(id = it, label = it)
    }

fun CreateClassUseCase.createClass(
    label: String,
    id: String? = null,
    contributorId: ContributorId? = null,
    uri: URI? = null
): ClassId =
    this.create(CreateClassUseCase.CreateCommand(label, id, contributorId, uri))

// Predicates

fun CreatePredicateUseCase.createPredicates(vararg predicates: Pair<String, String>) =
    predicates.forEach {
        createPredicate(id = it.first, label = it.second)
    }

fun CreatePredicateUseCase.createPredicates(vararg predicates: String) =
    predicates.forEach {
        createPredicate(id = it, label = it)
    }

fun CreatePredicateUseCase.createPredicate(
    label: String,
    id: String? = null
): PredicateId =
    this.create(CreatePredicateUseCase.CreateCommand(label, id, ContributorId.createUnknownContributor()))

// Resources

fun CreateResourceUseCase.createResource(
    classes: Set<String> = setOf(),
    id: String? = null,
    label: String? = null,
    extractionMethod: ExtractionMethod? = null
): ResourceId {
    val request = CreateResourceRequest(
        id = Optional.ofNullable(id).map(::ResourceId).orElse(null),
        label = label ?: "label",
        classes = classes.map(::ClassId).toSet(),
        extractionMethod = extractionMethod ?: ExtractionMethod.UNKNOWN
    )
    return this.create(request).id
}
