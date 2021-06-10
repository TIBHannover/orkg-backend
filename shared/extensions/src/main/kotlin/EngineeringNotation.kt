package org.orkg.extensions

/** Parses a string with a number in [engineering notation](https://en.wikipedia.org/wiki/Engineering_notation) to a number. */
fun String.parseEngineeringNotation(): Long? {
    val exponent =
        when (this.last()) {
            'k' -> 3
            'M' -> 6
            'G' -> 9
            'T' -> 12
            'P' -> 15
            'E' -> 18
            in '0'..'9' -> 0
            else -> return null
        }
    val number =
        if (exponent == 0) {
            this
        } else {
            this.substring(0 until this.lastIndex) + "E" + exponent
        }
    return number.toBigDecimalOrNull()?.toLong()
}
