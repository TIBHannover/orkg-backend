package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionsUpdateValidator(
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator
) : UpdateLiteratureListAction {
    constructor(resourceRepository: ResourceRepository) : this(AbstractLiteratureListSectionValidator(resourceRepository))

    override fun invoke(
        command: UpdateLiteratureListCommand,
        state: UpdateLiteratureListState
    ): UpdateLiteratureListState {
        command.sections?.let { sections ->
            val validIds = state.literatureList!!.sections.filterIsInstance<ListSection>()
                .flatMapTo(mutableSetOf()) { it.entries.map { entry -> entry.value.id } }
            sections.forEach { section ->
                abstractLiteratureListSectionValidator.validate(section, validIds)
            }
        }
        return state
    }
}
