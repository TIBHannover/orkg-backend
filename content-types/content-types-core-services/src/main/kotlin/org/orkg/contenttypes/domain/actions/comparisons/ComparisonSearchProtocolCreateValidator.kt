package org.orkg.contenttypes.domain.actions.comparisons

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.InvalidOriginallyReturnedStudyCount
import org.orkg.contenttypes.domain.InvalidRetainedStudyCount
import org.orkg.contenttypes.domain.InvalidStudyCounts
import org.orkg.contenttypes.domain.SearchEngineEntityNotFound
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.output.ThingRepository

class ComparisonSearchProtocolCreateValidator(
    private val thingRepository: ThingRepository,
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        command.searchProtocol.inclusionCriteria?.also {
            Description.ofOrNull(it) ?: throw InvalidDescription("search_protocol.inclusion_criteria")
        }
        command.searchProtocol.exclusionCriteria?.also {
            Description.ofOrNull(it) ?: throw InvalidDescription("search_protocol.exclusion_criteria")
        }
        command.searchProtocol.searchStrings.forEachIndexed { index, searchString ->
            Description.ofOrNull(searchString) ?: throw InvalidDescription("search_protocol.search_strings[$index]")
        }
        command.searchProtocol.researchQuestions.forEachIndexed { index, researchQuestion ->
            Description.ofOrNull(researchQuestion) ?: throw InvalidDescription("search_protocol.research_questions[$index]")
        }
        val studiesReturned = command.searchProtocol.numberOfStudiesOriginallyReturned
        if (studiesReturned != null && studiesReturned < 0) {
            throw InvalidOriginallyReturnedStudyCount(studiesReturned)
        }
        val studiesRetained = command.searchProtocol.numberOfStudiesRetained
        if (studiesRetained != null && studiesRetained < 0) {
            throw InvalidRetainedStudyCount(studiesRetained)
        }
        if (studiesReturned != null && studiesRetained != null && studiesReturned < studiesRetained) {
            throw InvalidStudyCounts(studiesReturned, studiesRetained)
        }
        if (command.searchProtocol.searchEngines.isNotEmpty() && !thingRepository.existsAllById(command.searchProtocol.searchEngines.toSet())) {
            throw SearchEngineEntityNotFound(command.searchProtocol.searchEngines.toSet())
        }
        return state
    }
}
