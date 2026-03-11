package org.orkg.contenttypes.adapter.input.rest.jats

@JvmInline
value class FigureId(val value: Int) {
    override fun toString(): String = "fig-$value"
}
