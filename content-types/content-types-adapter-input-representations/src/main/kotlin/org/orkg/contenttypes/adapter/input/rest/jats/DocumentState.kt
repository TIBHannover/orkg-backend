package org.orkg.contenttypes.adapter.input.rest.jats

import java.util.concurrent.atomic.AtomicInteger

class DocumentState(
    private val figureCount: AtomicInteger = AtomicInteger(0),
    private val tableCount: AtomicInteger = AtomicInteger(0),
) {
    fun nextFigureId(): FigureId = FigureId(figureCount.incrementAndGet())

    fun nextTableId(): TableId = TableId(tableCount.incrementAndGet())
}
