package org.orkg.common

data class StringReader(val string: String) {
    var cursor: Int = 0

    fun skip() {
        cursor++
    }

    fun peek(offset: Int = 0): Char = string[cursor + offset]

    fun canRead(offset: Int = 0): Boolean = cursor + offset < string.length

    fun read(): Char = string[cursor++]
}
