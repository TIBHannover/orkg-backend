package eu.tib.orkg.prototype.export.rdf.testing.fixtures

import java.io.StringWriter
import java.io.Writer

fun ((Writer) -> Unit).asString(): String {
    val writer = StringWriter()
    this(writer)
    return writer.toString()
}
