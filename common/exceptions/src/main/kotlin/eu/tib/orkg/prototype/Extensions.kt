package eu.tib.orkg.prototype

fun String.toSnakeCase(): String =
    if (this.isEmpty()) this else StringBuilder().also {
        this.forEach { c ->
            when (c) {
                in 'A'..'Z' -> {
                    it.append("_")
                    it.append(c.lowercase())
                }
                else -> {
                    it.append(c)
                }
            }
        }
    }.toString()
