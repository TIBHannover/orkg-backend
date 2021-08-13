package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface StatementRepository {

    fun save(statement: GeneralStatement)

    fun delete(statement: GeneralStatement)

    fun count(): Long

    fun countByIdRecursive(id: String): Long

    fun fetchAsBundle(rootId: String, configuration: Map<String, Any>): List<GeneralStatement>

    fun findAll(): Iterable<GeneralStatement> // only required by RDF dump

    fun findAll(pagination: Pageable): Iterable<GeneralStatement>

    fun findById(statementId: StatementId): Optional<GeneralStatement>

    fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement>

    fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement>

    fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement>

    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateAndLabel(predicateId: PredicateId, literal: String, pagination: Pageable): Page<GeneralStatement>

    fun findAllByPredicateAndLabelAndSubjectClass(predicateId: PredicateId, literal: String, subjectClass: ClassId, pagination: Pageable): Page<GeneralStatement>
}