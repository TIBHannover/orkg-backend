package org.orkg.dataimport.domain.internal

internal class RecordParsingException(val causes: List<Throwable>) : Exception("""Failed to parse ${causes.size} records.""")
