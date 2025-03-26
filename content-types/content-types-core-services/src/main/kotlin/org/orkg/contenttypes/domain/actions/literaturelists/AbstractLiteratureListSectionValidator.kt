package org.orkg.contenttypes.domain.actions.literaturelists

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.input.AbstractLiteratureListListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListTextSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository

private val allowedListSectionEntryClasses = setOf(Classes.paper, Classes.dataset, Classes.software)

class AbstractLiteratureListSectionValidator(
    private val resourceRepository: ResourceRepository,
) {
    internal fun validate(section: AbstractLiteratureListSectionCommand, validationCache: MutableSet<ThingId>) {
        if (section is AbstractLiteratureListListSectionCommand) {
            section.entries.forEach { entry ->
                entry.description?.let { description ->
                    Description.ofOrNull(description) ?: throw InvalidDescription("description")
                }
                if (entry.id !in validationCache) {
                    val resource = resourceRepository.findById(entry.id).orElseThrow { ResourceNotFound.withId(entry.id) }
                    if (allowedListSectionEntryClasses.intersect(resource.classes).isEmpty()) {
                        throw InvalidListSectionEntry(entry.id, allowedListSectionEntryClasses)
                    }
                    validationCache += entry.id
                }
            }
        } else if (section is AbstractLiteratureListTextSectionCommand) {
            Label.ofOrNull(section.heading) ?: throw InvalidLabel("heading")
            Description.ofOrNull(section.text) ?: throw InvalidDescription("text")
            if (section.headingSize < 1) {
                throw InvalidHeadingSize(section.headingSize)
            }
        }
    }
}
