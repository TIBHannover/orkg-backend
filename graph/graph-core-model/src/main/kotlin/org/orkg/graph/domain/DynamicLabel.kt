package org.orkg.graph.domain

import kotlin.collections.List
import org.orkg.graph.domain.DynamicLabel.Component.Companion.formatValue

data class DynamicLabel(
    val template: String
) {
    val components by lazy { parse(template) }

    fun render(valueMap: Map<String, List<String>>): String =
        components.mapNotNull { it.render(valueMap) }
            .joinToString(" ")
            .replace(Regex("""\s+"""), " ")

    companion object {
        private fun parse(template: String): List<Component> {
            val components = mutableListOf<Component>()
            val reader = StringReader(template)

            while (reader.canRead()) {
                val component = reader.readComponent()

                if (component is TextComponent) {
                    val last = components.lastOrNull()
                    if (last is TextComponent) {
                        components.set(components.lastIndex, last.append(component))
                    } else {
                        components.add(component)
                    }
                } else if (component != null) {
                    components.add(component)
                }
            }

            return components
        }

        private fun StringReader.readComponent(): Component? {
            val char = peek()
            if (char == '{') {
                val placeholderComponent = readPlaceholderComponent()

                if (placeholderComponent != null) {
                    return placeholderComponent
                }
            } else if (char == '[') {
                val sectionComponent = readSectionComponent()

                if (sectionComponent != null) {
                    return sectionComponent
                }
            }
            return readTextComponent()
        }

        private fun StringReader.readTextComponent(): TextComponent? {
            val skipFirst = peek() == '[' || peek() == '{'
            val text = readStringUntil('[', '{', skipFirst = skipFirst)
            if (text.isEmpty()) {
                return null
            }
            return TextComponent(text)
        }

        private fun StringReader.readPlaceholderComponent(): PlaceholderComponent? {
            if (!canRead() || peek() != '{') {
                return null
            }
            val cursorIn = cursor
            skip()
            val key = readStringUntil('}').trim()

            if (key.isEmpty() || !canRead() || read() != '}') {
                cursor = cursorIn
                return null
            }

            return PlaceholderComponent(key)
        }

        private fun StringReader.readSectionComponent(): SectionComponent? {
            if (!canRead() || peek() != '[') {
                return null
            }

            val cursorIn = cursor
            skip()
            val preposition = readStringUntil('{').trim()

            if (!canRead() || read() != '{') {
                cursor = cursorIn
                return null
            }

            val key = readStringUntil('}').trim()

            if (key.isEmpty() || !canRead() || read() != '}') {
                cursor = cursorIn
                return null
            }

            val postposition = readStringUntil(']').trim()

            if (!canRead() || read() != ']') {
                cursor = cursorIn
                return null
            }

            return SectionComponent(key, preposition, postposition)
        }

        private fun StringReader.readStringUntil(vararg terminators: Char, skipFirst: Boolean = false): String {
            val builder = StringBuilder()
            if (skipFirst && canRead()) {
                builder.append(read())
            }
            var escaped = false
            while (canRead()) {
                val char = peek()
                when {
                    escaped -> {
                        if (char !in terminators && char != '\\') {
                            builder.append('\\')
                        }
                        builder.append(char)
                        escaped = false
                    }
                    char == '\\' -> escaped = true
                    char in terminators -> return builder.toString()
                    else -> builder.append(char)
                }
                skip()
            }
            return builder.toString()
        }
    }

    sealed interface Component {
        fun render(valueMap: Map<String, List<String>>): String?

        companion object {
            fun formatValue(values: List<String>): String? =
                when {
                    values.isEmpty() -> null
                    values.size == 1 -> values.first()
                    else -> values.dropLast(1).joinToString() + " and " + values.last()
                }
        }
    }

    data class TextComponent(val text: String) : Component {
        override fun render(valueMap: Map<String, List<String>>): String? {
            return text
        }

        fun append(other: TextComponent): TextComponent =
            TextComponent(text + other.text)
    }

    data class PlaceholderComponent(
        val key: String
    ) : Component {
        override fun render(valueMap: Map<String, List<String>>): String? {
            return valueMap[key]?.let(::formatValue) ?: "{$key}"
        }
    }

    data class SectionComponent(
        val key: String,
        val preposition: String,
        val postposition: String
    ) : Component {
        override fun render(valueMap: Map<String, List<String>>): String? {
            return valueMap[key]
                ?.takeIf { it.isNotEmpty() }
                ?.let { value -> (preposition + " " + formatValue(value) + " " + postposition).trim() }
        }
    }
}
