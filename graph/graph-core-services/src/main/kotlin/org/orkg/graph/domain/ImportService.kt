package org.orkg.graph.domain

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ImportUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ExternalClassService
import org.orkg.graph.output.ExternalPredicateService
import org.orkg.graph.output.ExternalResourceService
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class ImportService(
    private val externalClassRepositories: MutableList<ExternalClassService>,
    private val externalResourceRepositories: MutableList<ExternalResourceService>,
    private val externalPredicateRepositories: MutableList<ExternalPredicateService>,
    private val statementService: StatementUseCases,
    private val unsafeStatementService: UnsafeStatementUseCases,
    private val resourceService: ResourceUseCases,
    private val classService: ClassUseCases,
    private val predicateService: PredicateUseCases,
    private val literalService: LiteralUseCases,
) : ImportUseCases {
    init {
        externalClassRepositories.sortBy { it.supportsMultipleOntologies() }
        externalResourceRepositories.sortBy { it.supportsMultipleOntologies() }
        externalPredicateRepositories.sortBy { it.supportsMultipleOntologies() }
    }

    override fun importResourceByShortForm(
        contributorId: ContributorId,
        ontologyId: String,
        shortForm: String,
    ): ThingId {
        externalResourceRepositories.forEach { repository ->
            if (repository.supportsOntology(ontologyId)) {
                val externalThing = repository.findResourceByShortForm(ontologyId, shortForm)
                if (externalThing != null) {
                    val statements = statementService.findAll(
                        pageable = PageRequests.SINGLE,
                        subjectClasses = setOf(Classes.resource),
                        predicateId = Predicates.sameAs,
                        objectLabel = externalThing.uri.toString(),
                        objectClasses = setOf(Classes.literal)
                    )
                    if (!statements.isEmpty) {
                        return statements.single().subject.id
                    }
                    return createExternalResource(contributorId, externalThing)
                }
            }
        }
        throw ExternalResourceNotFound(ontologyId, shortForm)
    }

    override fun importResourceByURI(
        contributorId: ContributorId,
        ontologyId: String,
        uri: ParsedIRI,
    ): ThingId {
        val statements = statementService.findAll(
            pageable = PageRequests.SINGLE,
            subjectClasses = setOf(Classes.resource),
            predicateId = Predicates.sameAs,
            objectLabel = uri.toString(),
            objectClasses = setOf(Classes.literal)
        )
        if (!statements.isEmpty) {
            return statements.single().subject.id
        }
        externalResourceRepositories.forEach { repository ->
            if (repository.supportsOntology(ontologyId)) {
                val externalThing = repository.findResourceByURI(ontologyId, uri)
                if (externalThing != null) {
                    return createExternalResource(contributorId, externalThing)
                }
            }
        }
        throw ExternalResourceNotFound(ontologyId, uri)
    }

    override fun importPredicateByShortForm(
        contributorId: ContributorId,
        ontologyId: String,
        shortForm: String,
    ): ThingId {
        externalPredicateRepositories.forEach { repository ->
            if (repository.supportsOntology(ontologyId)) {
                val externalThing = repository.findPredicateByShortForm(ontologyId, shortForm)
                if (externalThing != null) {
                    val statements = statementService.findAll(
                        pageable = PageRequests.SINGLE,
                        subjectClasses = setOf(Classes.predicate),
                        predicateId = Predicates.sameAs,
                        objectLabel = externalThing.uri.toString(),
                        objectClasses = setOf(Classes.literal)
                    )
                    if (!statements.isEmpty) {
                        return statements.single().subject.id
                    }
                    return createExternalPredicate(contributorId, externalThing)
                }
            }
        }
        throw ExternalPredicateNotFound(ontologyId, shortForm)
    }

    override fun importPredicateByURI(
        contributorId: ContributorId,
        ontologyId: String,
        uri: ParsedIRI,
    ): ThingId {
        val statements = statementService.findAll(
            pageable = PageRequests.SINGLE,
            subjectClasses = setOf(Classes.predicate),
            predicateId = Predicates.sameAs,
            objectLabel = uri.toString(),
            objectClasses = setOf(Classes.literal)
        )
        if (!statements.isEmpty) {
            return statements.single().subject.id
        }
        externalPredicateRepositories.forEach { repository ->
            if (repository.supportsOntology(ontologyId)) {
                val externalThing = repository.findPredicateByURI(ontologyId, uri)
                if (externalThing != null) {
                    return createExternalPredicate(contributorId, externalThing)
                }
            }
        }
        throw ExternalPredicateNotFound(ontologyId, uri)
    }

    override fun importClassByShortForm(
        contributorId: ContributorId,
        ontologyId: String,
        shortForm: String,
    ): ThingId {
        externalClassRepositories.forEach { repository ->
            if (repository.supportsOntology(ontologyId)) {
                val externalThing = repository.findClassByShortForm(ontologyId, shortForm)
                if (externalThing != null) {
                    val existingClass = classService.findByURI(externalThing.uri)
                    if (existingClass.isPresent) {
                        return existingClass.get().id
                    }
                    return createExternalClass(contributorId, externalThing)
                }
            }
        }
        throw ExternalClassNotFound(ontologyId, shortForm)
    }

    override fun importClassByURI(
        contributorId: ContributorId,
        ontologyId: String,
        uri: ParsedIRI,
    ): ThingId {
        val existingClass = classService.findByURI(uri)
        if (existingClass.isPresent) {
            return existingClass.get().id
        }
        externalClassRepositories.forEach { repository ->
            if (repository.supportsOntology(ontologyId)) {
                val externalThing = repository.findClassByURI(ontologyId, uri)
                if (externalThing != null) {
                    return createExternalClass(contributorId, externalThing)
                }
            }
        }
        throw ExternalClassNotFound(ontologyId, uri)
    }

    private fun createExternalResource(contributorId: ContributorId, externalThing: ExternalThing): ThingId {
        val resourceId = resourceService.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = externalThing.label,
            )
        )
        createDescription(externalThing, contributorId, resourceId)
        createSameAsLiteral(contributorId, externalThing, resourceId)
        return resourceId
    }

    private fun createExternalPredicate(contributorId: ContributorId, externalThing: ExternalThing): ThingId {
        val predicateId = predicateService.create(
            CreatePredicateUseCase.CreateCommand(
                contributorId = contributorId,
                label = externalThing.label,
            )
        )
        createDescription(externalThing, contributorId, predicateId)
        createSameAsLiteral(contributorId, externalThing, predicateId)
        return predicateId
    }

    private fun createExternalClass(contributorId: ContributorId, externalThing: ExternalThing): ThingId {
        val classId = classService.create(
            CreateClassUseCase.CreateCommand(
                contributorId = contributorId,
                label = externalThing.label,
                uri = externalThing.uri
            )
        )
        createDescription(externalThing, contributorId, classId)
        return classId
    }

    private fun createSameAsLiteral(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId,
    ) {
        val sameAsLiteralId = literalService.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = externalThing.uri.toString(),
                datatype = Literals.XSD.URI.prefixedUri
            )
        )
        unsafeStatementService.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.sameAs,
                objectId = sameAsLiteralId
            )
        )
    }

    private fun createDescription(
        externalThing: ExternalThing,
        contributorId: ContributorId,
        subjectId: ThingId,
    ) {
        externalThing.description?.let { description ->
            val descriptionId = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
            unsafeStatementService.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.description,
                    objectId = descriptionId
                )
            )
        }
    }
}
