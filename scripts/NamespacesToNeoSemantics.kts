#!/usr/bin/env kscript

import java.io.File
import java.io.FileInputStream
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader
import kotlin.system.exitProcess

if (args.isEmpty()) {
    System.err.println("ERROR: No RDF file provided.")
    exitProcess(1)
}

val namespaces = mutableMapOf<String, String>()

val input = File(args[0])

val reader: XMLStreamReader =
    XMLInputFactory.newInstance()
        .createXMLStreamReader(FileInputStream(input))

while (reader.hasNext()) {
    if (reader.next() == XMLStreamConstants.START_ELEMENT) {
        val qName = reader.name
        if (qName?.prefix.isNeitherNullNorEmpty())
            namespaces[qName.prefix] = qName.namespaceURI
    }
}

val cypherStatement = buildString {
    append("CREATE (:NamespacePrefixDefinition ")
    append(
        namespaces.map {
            "`${it.value}`: '${it.key}'"
        }.joinToString(separator = ", ", prefix = "{", postfix = "}")
    )
    append(")")
}

println(cypherStatement)

// End of script. Helper functions follow.

fun String?.isNeitherNullNorEmpty() =
    this != null //&& this != ""
