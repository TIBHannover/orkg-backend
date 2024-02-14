package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label

class PublicationInfoValidator<T, S>(
    private val valueSelector: (T) -> PublicationInfoDefinition?
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        valueSelector(command)?.also { publicationInfo ->
            publicationInfo.publishedMonth?.also { publishedMonth ->
                if (publishedMonth < 1 || publishedMonth > 12) {
                    throw InvalidMonth(publishedMonth)
                }
            }
            publicationInfo.publishedIn?.also { publishedIn ->
                Label.ofOrNull(publishedIn) ?: throw InvalidLabel()
            }
        }
        return state
    }
}
