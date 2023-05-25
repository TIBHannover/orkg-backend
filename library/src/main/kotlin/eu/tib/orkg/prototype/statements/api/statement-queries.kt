package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveStatementUseCase {
    fun exists(id: StatementId): Boolean
    // legacy methods:
    fun findAll(pagination: Pageable): Iterable<StatementRepresentation>
    fun findById(statementId: StatementId): Optional<StatementRepresentation>
    fun findAllBySubject(subjectId: ThingId, pagination: Pageable): Page<StatementRepresentation>
    fun findAllByPredicate(predicateId: ThingId, pagination: Pageable): Page<StatementRepresentation>
    fun findAllByObject(objectId: ThingId, pagination: Pageable): Page<StatementRepresentation>
    fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Iterable<StatementRepresentation>

    fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Iterable<StatementRepresentation>

    fun totalNumberOfStatements(): Long
    fun countStatements(paperId: ThingId): Long
    fun findAllByPredicateAndLabel(
        predicateId: ThingId,
        literal: String,
        pagination: Pageable
    ): Page<StatementRepresentation>

    fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pagination: Pageable
    ): Iterable<StatementRepresentation>

    fun fetchAsBundle(thingId: ThingId, configuration: BundleConfiguration, includeFirst: Boolean): Bundle

    fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount>

    fun countStatementsAboutResource(id: ThingId): Long

    fun countStatementsAboutResources(ids: Set<ThingId>): Map<ThingId, Long>

    data class PredicateUsageCount(
        val id: ThingId,
        val count: Long
    )
}

/**
 * A Bundle configuration class containing the min and max levels to be fetched
 * Also the list of classes to be white-listed or black-listed during the fetch
 * @param minLevel the minimum level to be fetched (if not provided it is set to 0)
 * @param maxLevel the maximum level of statements to be fetched (if not provided, all child statements will be fetched)
 * @param blacklist the list of classes to be black-listed (i.e. not fetched), these classes are checked on the subjects and objects of a statement
 * @param whitelist the list of classes to be white-listed (i.e. the only ones to be fetched), these classes are checked on the subjects and objects of a statement
 */
data class BundleConfiguration(
    val minLevel: Int?,
    val maxLevel: Int?,
    val blacklist: List<ThingId>,
    val whitelist: List<ThingId>
) {
    companion object Factory {
        fun firstLevelConf(): BundleConfiguration =
            BundleConfiguration(minLevel = null, maxLevel = 1, blacklist = emptyList(), whitelist = emptyList())
    }
}
