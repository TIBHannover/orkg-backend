package org.orkg.common.exceptions

fun escapeJsonPointerReferenceToken(referenceToken: String): String =
    referenceToken.replace("~", "~0").replace("/", "~1")

fun jsonFieldPathToJsonPointerReference(fieldPath: String): String =
    fieldPath.split(".", "[", "]")
        .map(::escapeJsonPointerReferenceToken)
        .joinToString(prefix = "#/", separator = "/")
        .replace(Regex("/+"), "/")
