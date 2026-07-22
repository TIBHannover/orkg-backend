package org.orkg.contenttypes.domain.actions.comparisons

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.InvalidOriginallyReturnedStudyCount
import org.orkg.contenttypes.domain.InvalidRetainedStudyCount
import org.orkg.contenttypes.domain.InvalidStudyCounts
import org.orkg.contenttypes.domain.SearchEngineEntityNotFound
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.output.ThingRepository

class ComparisonSearchProtocolUpdateValidator(
    private val thingRepository: ThingRepository,
) : UpdateComparisonAction {
    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        command.searchProtocol?.also { searchProtocol ->
            val oldSearchProtocol = state.comparison!!.searchProtocol
            if (searchProtocol.inclusionCriteria != null && searchProtocol.inclusionCriteria != oldSearchProtocol.inclusionCriteria) {
                Description.ofOrNull(searchProtocol.inclusionCriteria!!) ?: throw InvalidDescription("search_protocol.inclusion_criteria")
            }
            if (searchProtocol.exclusionCriteria != null && searchProtocol.exclusionCriteria != oldSearchProtocol.exclusionCriteria) {
                Description.ofOrNull(searchProtocol.exclusionCriteria!!) ?: throw InvalidDescription("search_protocol.exclusion_criteria")
            }
            if (searchProtocol.searchStrings != oldSearchProtocol.searchStrings) {
                searchProtocol.searchStrings.forEachIndexed { index, searchString ->
                    if (searchString !in oldSearchProtocol.searchStrings) {
                        Description.ofOrNull(searchString) ?: throw InvalidDescription("search_protocol.search_strings[$index]")
                    }
                }
            }
            if (searchProtocol.researchQuestions != oldSearchProtocol.researchQuestions) {
                searchProtocol.researchQuestions.forEachIndexed { index, researchQuestion ->
                    if (researchQuestion !in oldSearchProtocol.researchQuestions) {
                        Description.ofOrNull(researchQuestion) ?: throw InvalidDescription("search_protocol.research_questions[$index]")
                    }
                }
            }
            val studiesReturned = searchProtocol.numberOfStudiesOriginallyReturned
            if (studiesReturned != null && oldSearchProtocol.numberOfStudiesOriginallyReturned != studiesReturned && studiesReturned < 0) {
                throw InvalidOriginallyReturnedStudyCount(studiesReturned)
            }
            val studiesRetained = searchProtocol.numberOfStudiesRetained
            if (studiesRetained != null && oldSearchProtocol.numberOfStudiesRetained != studiesRetained && studiesRetained < 0) {
                throw InvalidRetainedStudyCount(studiesRetained)
            }
            val updatedStudiesReturned = studiesReturned ?: oldSearchProtocol.numberOfStudiesOriginallyReturned
            val updatedStudiesRetained = studiesRetained ?: oldSearchProtocol.numberOfStudiesRetained
            if (
                (studiesReturned != null || studiesRetained != null) &&
                updatedStudiesReturned != null && updatedStudiesRetained != null &&
                updatedStudiesReturned < updatedStudiesRetained
            ) {
                throw InvalidStudyCounts(updatedStudiesReturned, updatedStudiesRetained)
            }
            if (
                searchProtocol.searchEngines.isNotEmpty() &&
                (searchProtocol.searchEngines - oldSearchProtocol.searchEngines.map { it.id }).isNotEmpty() &&
                !thingRepository.existsAllById(searchProtocol.searchEngines.toSet())
            ) {
                throw SearchEngineEntityNotFound(searchProtocol.searchEngines.toSet())
            }
        }
        return state
    }
}
