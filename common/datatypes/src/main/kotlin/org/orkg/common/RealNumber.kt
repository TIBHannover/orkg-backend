package org.orkg.common

import java.math.BigDecimal

class RealNumber(value: String) : BigDecimal(validate(value)) {
    constructor(value: Byte) : this(value.toString())
    constructor(value: Short) : this(value.toString())
    constructor(value: Int) : this(value.toString())
    constructor(value: Long) : this(value.toString())
    constructor(value: Float) : this(value.toString())
    constructor(value: Double) : this(value.toString())

    override fun toByte(): Byte = toInt().toByte()

    override fun toShort(): Short = toInt().toShort()

    companion object {
        private fun validate(value: String): String {
            if (isValidNumber(value)) {
                return value
            } else {
                throw IllegalArgumentException(""""$value" is not a valid number.""")
            }
        }

        private fun isValidNumber(value: String): Boolean {
            if (value.isEmpty()) {
                // value is empty
                return false
            }

            val reader = StringReader(value)
            val hasSign = reader.peek() == '+' || reader.peek() == '-'

            if (hasSign) {
                reader.skip()
            }

            if (!reader.canRead()) {
                // value is just a sign
                return false
            }

            while (reader.canRead() && reader.peek() in '0'..'9') {
                reader.skip()
            }

            when {
                !reader.canRead() && reader.cursor > 0 -> return true // value has been fully read and there is at least one number
                reader.canRead() && reader.peek() == '.' -> reader.skip()
                else -> return false // value does not contain a '.' character after first block of digits
            }

            while (reader.canRead() && reader.peek() in '0'..'9') {
                reader.skip()
            }

            // value has been fully read and there is at least one number
            return !reader.canRead() && reader.cursor > if (hasSign) 2 else 1
        }
    }
}

fun String.toRealNumberOrNull(): RealNumber? =
    try {
        RealNumber(this)
    } catch (e: Exception) {
        null
    }

fun String.toRealNumber(): RealNumber = RealNumber(this)
