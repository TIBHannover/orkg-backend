package org.orkg.graph.testing.asciidoc

import org.orkg.common.ThingId
import org.orkg.graph.domain.PUBLISHABLE_CLASSES
import org.orkg.testing.toAsciidoc

object Asciidoc {
    /** Formats the set of publishable classes to use in Asciidoc documents. */
    fun formatPublishableClasses() = PUBLISHABLE_CLASSES.map(ThingId::value).sorted().toAsciidoc()
}
