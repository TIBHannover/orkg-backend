package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.CreateLiteratureListAction.State
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionsCreateValidator(
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator,
) : CreateLiteratureListAction {
    constructor(resourceRepository: ResourceRepository) : this(AbstractLiteratureListSectionValidator(resourceRepository))

    override fun invoke(command: CreateLiteratureListCommand, state: State): State {
        val validationCache = mutableSetOf<ThingId>()
        command.sections.forEach { section ->
            abstractLiteratureListSectionValidator.validate(section, validationCache)
        }
        return state
    }
}
