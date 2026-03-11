package org.orkg.contenttypes.adapter.input.rest.jats

@JvmInline
value class TableId(val value: Int) {
    override fun toString(): String = "table-$value"
}
