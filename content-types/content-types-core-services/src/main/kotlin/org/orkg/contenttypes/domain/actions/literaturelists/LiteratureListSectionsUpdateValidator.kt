package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.UpdateLiteratureListAction.State
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionsUpdateValidator(
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator,
) : UpdateLiteratureListAction {
    constructor(resourceRepository: ResourceRepository) : this(AbstractLiteratureListSectionValidator(resourceRepository))

    override fun invoke(command: UpdateLiteratureListCommand, state: State): State {
        command.sections?.let { sections ->
            val validIds = state.literatureList!!.sections.filterIsInstance<LiteratureListListSection>()
                .flatMapTo(mutableSetOf()) { it.entries.map { entry -> entry.value.id } }
            sections.forEach { section ->
                abstractLiteratureListSectionValidator.validate(section, validIds)
            }
        }
        return state
    }
}
