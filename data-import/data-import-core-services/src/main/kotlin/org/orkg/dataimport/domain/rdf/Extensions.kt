package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.orkg.dataimport.output.RdfTripleRepository
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import java.util.UUID

private val languageComparator = Comparator<Literal> { a, b ->
    if (a.language.isPresent && b.language.isPresent) {
        if (a.language.get() != b.language.get()) {
            if (a.language.get() == "en") {
                return@Comparator Int.MIN_VALUE
            }
            if (b.language.get() == "en") {
                return@Comparator Int.MAX_VALUE
            }
        }
    } else if (a.language.isPresent && a.language.get() == "en") {
        return@Comparator Int.MIN_VALUE
    } else if (b.language.isPresent && b.language.get() == "en") {
        return@Comparator Int.MIN_VALUE
    }
    return@Comparator a.label.compareTo(b.label)
}

internal fun RdfTripleRepository.findLabel(rdfFileId: UUID, input: Resource): String? =
    findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId, input, RDFS.LABEL)
        .sortedWith(languageComparator)
        .firstOrNull()
        ?.stringValue()
        ?.replace("\n", " ")
        ?.take(MAX_LABEL_LENGTH)
