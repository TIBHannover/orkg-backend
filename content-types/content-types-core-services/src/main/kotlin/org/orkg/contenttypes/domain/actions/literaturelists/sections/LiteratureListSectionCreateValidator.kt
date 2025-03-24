package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionValidator
import org.orkg.contenttypes.domain.actions.literaturelists.sections.CreateLiteratureListSectionAction.State
import org.orkg.contenttypes.input.AbstractLiteratureListSectionCommand
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionCreateValidator(
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator,
) : CreateLiteratureListSectionAction {
    constructor(
        resourceRepository: ResourceRepository,
    ) : this(
        AbstractLiteratureListSectionValidator(resourceRepository)
    )

    override fun invoke(command: CreateLiteratureListSectionCommand, state: State): State {
        abstractLiteratureListSectionValidator.validate(
            section = command as AbstractLiteratureListSectionCommand,
            validIds = mutableSetOf()
        )
        return state
    }
}
