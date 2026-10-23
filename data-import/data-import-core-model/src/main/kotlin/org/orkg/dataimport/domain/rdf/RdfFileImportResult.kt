package org.orkg.dataimport.domain.rdf

data class RdfFileImportResult(
    val classCount: Int,
    val predicateCount: Int,
    val resourceCount: Int,
    val statementCount: Int,
)
