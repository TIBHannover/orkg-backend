package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionValidator
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.graph.output.ResourceRepository

class LiteratureListSectionCreateValidator(
    private val abstractLiteratureListSectionValidator: AbstractLiteratureListSectionValidator
) : CreateLiteratureListSectionAction {
    constructor(
        resourceRepository: ResourceRepository
    ) : this(AbstractLiteratureListSectionValidator(resourceRepository))

    override fun invoke(
        command: CreateLiteratureListSectionCommand,
        state: CreateLiteratureListSectionState
    ): CreateLiteratureListSectionState =
        state.also {
            abstractLiteratureListSectionValidator.validate(
                section = command as LiteratureListSectionDefinition,
                validIds = mutableSetOf()
            )
        }
}
