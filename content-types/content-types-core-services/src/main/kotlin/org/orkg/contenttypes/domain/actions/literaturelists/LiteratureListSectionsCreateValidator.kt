package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionsCreateValidator(
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator,
) : CreateLiteratureListAction {
    constructor(resourceRepository: ResourceRepository) : this(AbstractLiteratureListSectionValidator(resourceRepository))

    override fun invoke(
        command: CreateLiteratureListCommand,
        state: CreateLiteratureListState,
    ): CreateLiteratureListState {
        val validIds = mutableSetOf<ThingId>()
        command.sections.forEach { section ->
            abstractLiteratureListSectionValidator.validate(section, validIds)
        }
        return state
    }
}
