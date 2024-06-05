package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.LiteratureListSectionTypeMismatch
import org.orkg.contenttypes.domain.TextSection
import org.orkg.contenttypes.domain.UnrelatedLiteratureListSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionValidator
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionUpdateValidator(
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator
) : UpdateLiteratureListSectionAction {
    constructor(resourceRepository: ResourceRepository) : this(AbstractLiteratureListSectionValidator(resourceRepository))

    override fun invoke(
        command: UpdateLiteratureListSectionCommand,
        state: UpdateLiteratureListSectionState
    ): UpdateLiteratureListSectionState {
        val section = state.literatureList!!.sections.singleOrNull { it.id == command.literatureListSectionId }
            ?: throw UnrelatedLiteratureListSection(command.literatureListId, command.literatureListSectionId)
        if (command is UpdateLiteratureListSectionUseCase.UpdateListSectionCommand) {
            if (section !is ListSection) {
                throw LiteratureListSectionTypeMismatch.mustBeListSection()
            }
            abstractLiteratureListSectionValidator.validate(
                section = command as LiteratureListSectionDefinition,
                validIds = section.entries.map { it.value.id }.toMutableSet()
            )
        } else if (command is UpdateLiteratureListSectionUseCase.UpdateTextSectionCommand) {
            if (section !is TextSection) {
                throw LiteratureListSectionTypeMismatch.mustBeTextSection()
            }
            abstractLiteratureListSectionValidator.validate(
                section = command as LiteratureListSectionDefinition,
                validIds = mutableSetOf()
            )
        }
        return state
    }
}
