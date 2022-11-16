#!/usr/bin/env kscript

import java.io.*
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.util.*
import java.util.regex.*

val root : String = args[0]
val queryRegex = Regex("""@Query\(\s*?(?:value\s*?=\s*?)?("{1,3}[\s\S]*?"{1,3})(?:\s*?,\s*?countQuery\s*?=\s*?("{1,3}[\s\S]*?"{1,3})\s*?)?\s*?\)\s*?fun [\w0-9<,>]+?\(\s*?([\w0-9<,>`?:\s]*?)\s*?\):""")
val queryContainsParameterRegex = Regex("""^[\s\S]*?\{\d+\}[\s\S]*?$""")
val parameterValsEntryPattern = Pattern.compile("""\n(?:interface|(?:data\s+)?class)\s""") // TODO: if ever used again: move entry point below imports
val functionParameterPattern = Pattern.compile("""([\w0-9`]+?)\s*:\s*[\w0-9<,>?]*?\s*(?:,|${'$'})""")
var patchedQueries = 0
var patchedParameters = 0

File(root).walk().forEach {
    if (it.isFile) {
        parameterizeQueries(it)
    }
}

println("Patched Queries: $patchedQueries")
println("Patched Parameters: $patchedParameters")

fun parameterizeQueries(file: File) {
    var source = file.readText()
    val matches = queryRegex.findAll(source)
    val parameters = mutableSetOf<String>()

    for (match in matches) {
        val queryMatch = match.groups.get(1)?.value!!
        if (queryMatch.matches(queryContainsParameterRegex)) {
            val functionParameters = extractParameterList(match.groups.get(3)?.value!!)
            val newQuery = updateQuery(queryMatch, functionParameters, parameters)
            source = source.replace(queryMatch, newQuery)
            patchedQueries++
            if (match.groups.get(2) != null) {
                val countQueryMatch = match.groups.get(2)?.value!!
                val newCountQuery = updateQuery(countQueryMatch, functionParameters, parameters)
                source = source.replace(countQueryMatch, newCountQuery)
                patchedQueries++
            }
        }
    }

    if (parameters.isNotEmpty()) {
        val parameterValsEntryMatcher = parameterValsEntryPattern.matcher(source)
        if (!parameterValsEntryMatcher.find()) {
            throw RuntimeException("Could not find parameter vals entry")
        }
        val builder = StringBuilder(source)
        val parameterValsEntryIndex = parameterValsEntryMatcher.start()
        for (parameter in parameters) {
            builder.insert(parameterValsEntryIndex, "private const val $parameter = \"${'$'}{'${'$'}'}$parameter\"\n")
        }
        builder.insert(parameterValsEntryIndex, "\n")
        source = builder.toString()
        patchedParameters += parameters.size
        file.writeText(source)
        println("Patched ${file}")
    }
}

fun updateQuery(query: String, functionParameters: List<String>, parameters: MutableSet<String>): String {
    var result = query
    for ((index, parameter) in functionParameters.withIndex()) {
        if (result.contains("{$index}")) {
            result = result.replace("{$index}", "${'$'}$parameter")
            parameters.add(parameter)
        }
    }
    return result
}

fun extractParameterList(parameterString: String): List<String> {
    val matcher = functionParameterPattern.matcher(parameterString)
    val result = mutableListOf<String>()
    while (matcher.find()) {
        result.add(matcher.group(1)!!)
    }
    return result
}
