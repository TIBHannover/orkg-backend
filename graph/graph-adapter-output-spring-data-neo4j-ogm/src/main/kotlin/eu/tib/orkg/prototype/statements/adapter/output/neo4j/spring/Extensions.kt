package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClass
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRelation
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRelationRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatement
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassSubclassRelation
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId

internal fun ClassSubclassRelation.toNeo4jClassRelation(
    neo4jRepository: Neo4jClassRelationRepository,
    neo4jClassRepository: Neo4jClassRepository
) =
    neo4jRepository.findByChildId(child.id).orElse(Neo4jClassRelation()).apply {
        child = this@toNeo4jClassRelation.child.toNeo4jClass(neo4jClassRepository)
        parent = this@toNeo4jClassRelation.parent.toNeo4jClass(neo4jClassRepository)
        createdBy = this@toNeo4jClassRelation.createdBy
        createdAt = this@toNeo4jClassRelation.createdAt
    }

internal fun Class.toNeo4jClass(neo4jRepository: Neo4jClassRepository): Neo4jClass =
    neo4jRepository.findById(id).orElse(Neo4jClass()).apply {
        id = this@toNeo4jClass.id
        label = this@toNeo4jClass.label
        uri = this@toNeo4jClass.uri?.toString()
        createdBy = this@toNeo4jClass.createdBy
        createdAt = this@toNeo4jClass.createdAt
    }

internal fun Literal.toNeo4jLiteral(neo4jRepository: Neo4jLiteralRepository) =
    neo4jRepository.findById(id).orElse(Neo4jLiteral()).apply {
        id = this@toNeo4jLiteral.id
        label = this@toNeo4jLiteral.label
        datatype = this@toNeo4jLiteral.datatype
        createdAt = this@toNeo4jLiteral.createdAt
        createdBy = this@toNeo4jLiteral.createdBy
    }

internal fun Predicate.toNeo4jPredicate(neo4jRepository: Neo4jPredicateRepository) =
    neo4jRepository.findById(id).orElse(Neo4jPredicate()).apply {
        id = this@toNeo4jPredicate.id
        label = this@toNeo4jPredicate.label
        createdBy = this@toNeo4jPredicate.createdBy
        createdAt = this@toNeo4jPredicate.createdAt
    }

internal fun Resource.toNeo4jResource(neo4jRepository: Neo4jResourceRepository) =
    // We need to fetch the original resource, so "resources" is set properly.
    neo4jRepository.findById(id).orElse(Neo4jResource()).apply {
        id = this@toNeo4jResource.id
        label = this@toNeo4jResource.label
        createdBy = this@toNeo4jResource.createdBy
        createdAt = this@toNeo4jResource.createdAt
        observatoryId = this@toNeo4jResource.observatoryId
        extractionMethod = this@toNeo4jResource.extractionMethod
        verified = this@toNeo4jResource.verified
        visibility = this@toNeo4jResource.visibility
        organizationId = this@toNeo4jResource.organizationId
        classes = this@toNeo4jResource.classes
        unlistedBy = this@toNeo4jResource.unlistedBy
    }

internal fun Neo4jStatement.toStatement(
    neo4jPredicateRepository: Neo4jPredicateRepository,
): GeneralStatement = GeneralStatement(
    id = statementId!!,
    subject = subject!!.toThing(),
    predicate = neo4jPredicateRepository.findById(predicateId!!).get().toPredicate(),
    `object` = `object`!!.toThing(),
    createdAt = createdAt!!,
    createdBy = createdBy,
    index = index
)

internal fun Neo4jStatement.toStatement(lookupTable: PredicateLookupTable): GeneralStatement = GeneralStatement(
    id = statementId!!,
    subject = subject!!.toThing(),
    predicate = lookupTable[ThingId(predicateId!!.value)]
        ?: throw IllegalStateException("Predicate $predicateId not found in lookup table. This is a bug."),
    `object` = `object`!!.toThing(),
    createdAt = createdAt!!,
    createdBy = createdBy,
    index = index
)

internal fun GeneralStatement.toNeo4jStatement(
    neo4jStatementRepository: Neo4jStatementRepository,
    neo4jClassRepository: Neo4jClassRepository,
    neo4jLiteralRepository: Neo4jLiteralRepository,
    neo4jPredicateRepository: Neo4jPredicateRepository,
    neo4jResourceRepository: Neo4jResourceRepository
): Neo4jStatement =
    // Need to fetch the internal ID of a (possibly) existing entity to prevent creating a new one.
    neo4jStatementRepository.findByStatementId(id!!).orElse(Neo4jStatement()).apply {
        statementId = this@toNeo4jStatement.id
        subject = this@toNeo4jStatement.subject.toNeo4jThing(
            neo4jClassRepository,
            neo4jLiteralRepository,
            neo4jPredicateRepository,
            neo4jResourceRepository
        )
        `object` = this@toNeo4jStatement.`object`.toNeo4jThing(
            neo4jClassRepository,
            neo4jLiteralRepository,
            neo4jPredicateRepository,
            neo4jResourceRepository
        )
        predicateId = this@toNeo4jStatement.predicate.id
        createdBy = this@toNeo4jStatement.createdBy
        createdAt = this@toNeo4jStatement.createdAt
        index = this@toNeo4jStatement.index
    }

internal fun Thing.toNeo4jThing(
    neo4jClassRepository: Neo4jClassRepository,
    neo4jLiteralRepository: Neo4jLiteralRepository,
    neo4jPredicateRepository: Neo4jPredicateRepository,
    neo4jResourceRepository: Neo4jResourceRepository
): Neo4jThing =
    when (this) {
        is Class -> neo4jClassRepository.findById(id).get()
        is Literal -> neo4jLiteralRepository.findById(id).get()
        is Predicate -> neo4jPredicateRepository.findById(id).get()
        is Resource -> neo4jResourceRepository.findById(id).get()
    }
