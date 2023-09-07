package eu.tib.orkg.prototype.testing

fun Iterable<String>.toAsciidoc(): String = this.joinToString(separator = ", ", prefix = "`", postfix = "`")
