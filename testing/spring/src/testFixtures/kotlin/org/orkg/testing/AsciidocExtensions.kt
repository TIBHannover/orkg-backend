package org.orkg.testing

fun Iterable<String>.toAsciidoc(): String = this.joinToString(separator = "`, `", prefix = "`", postfix = "`")
