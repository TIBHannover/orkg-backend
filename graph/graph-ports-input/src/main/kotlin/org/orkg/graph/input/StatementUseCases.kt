package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.PredicateUsageCount
import org.orkg.graph.domain.StatementId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.Optional

interface UnsafeStatementUseCases :
    CreateStatementUseCase,
    UpdateStatementUseCase,
    DeleteStatementUseCase

interface StatementUseCases :
    CreateStatementUseCase,
    RetrieveStatementUseCase,
    UpdateStatementUseCase,
    DeleteStatementUseCase

interface RetrieveStatementUseCase {
    fun existsById(id: StatementId): Boolean

    fun findAll(
        pageable: Pageable,
        subjectClasses: Set<ThingId> = emptySet(),
        subjectId: ThingId? = null,
        subjectLabel: String? = null,
        predicateId: ThingId? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        objectClasses: Set<ThingId> = emptySet(),
        objectId: ThingId? = null,
        objectLabel: String? = null,
    ): Page<GeneralStatement>

    fun findById(statementId: StatementId): Optional<GeneralStatement>

    fun count(): Long

    fun countStatementsInPaperSubgraph(paperId: ThingId): Long

    fun fetchAsBundle(thingId: ThingId, configuration: BundleConfiguration, includeFirst: Boolean, sort: Sort): Bundle

    fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount>

    fun countIncomingStatementsById(id: ThingId): Long

    fun countAllIncomingStatementsById(ids: Set<ThingId>): Map<ThingId, Long>

    fun findAllDescriptionsById(ids: Set<ThingId>): Map<ThingId, String>
}

interface CreateStatementUseCase {
    fun create(command: CreateCommand): StatementId

    data class CreateCommand(
        val id: StatementId? = null,
        val contributorId: ContributorId,
        val subjectId: ThingId,
        val predicateId: ThingId,
        val objectId: ThingId,
        val modifiable: Boolean = true,
    )
}

interface UpdateStatementUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val statementId: StatementId,
        val contributorId: ContributorId,
        val subjectId: ThingId? = null,
        val predicateId: ThingId? = null,
        val objectId: ThingId? = null,
        val modifiable: Boolean? = null,
    )
}

interface DeleteStatementUseCase {
    fun deleteById(statementId: StatementId)

    fun deleteAllById(statementIds: Set<StatementId>)

    // For tests only!
    fun deleteAll()
}
