package org.orkg.contenttypes.domain.actions

import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.output.StatementRepository

abstract class IdentifierValidator(
    private val statementRepository: StatementRepository
) {
    internal fun validate(identifiers: Map<String, List<String>>, `class`: ThingId, subjectId: ThingId?, exceptionFactory: (String) -> Throwable) {
        Identifiers.paper.parse(identifiers).forEach { (identifier, values) ->
            values.forEach { value ->
                val papers = statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                    predicateId = identifier.predicateId,
                    literal = value,
                    subjectClass = `class`,
                    pageable = PageRequests.SINGLE
                )
                    .content
                    .filter { it.subject.id != subjectId }
                if (papers.isNotEmpty()) {
                    throw exceptionFactory(value)
                }
            }
        }
    }
}
