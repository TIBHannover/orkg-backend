package org.orkg.contenttypes.domain.actions.paper

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.Identifiers
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.PaperState
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.StatementRepository

class PaperExistenceValidator(
    private val resourceService: ResourceUseCases,
    private val statementRepository: StatementRepository
) : PaperAction {
    override fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val resource = resourceService.findAllByTitle(command.title).firstOrNull()
        if (resource != null) {
            throw PaperAlreadyExists.withTitle(resource.label)
        }

        val identifiers = Identifiers.paper.parse(command.identifiers)
        identifiers.forEach { (identifier, value) ->
            val papers = statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = identifier.predicateId,
                literal = value,
                subjectClass = Classes.paper,
                pageable = PageRequests.SINGLE
            )
            if (papers.content.isNotEmpty()) {
                throw PaperAlreadyExists.withIdentifier(value)
            }
        }
        return state
    }
}
