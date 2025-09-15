package org.orkg.contenttypes.domain.actions.tables.cells

import org.orkg.contenttypes.domain.CannotDeleteTableHeader
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.actions.UpdateTableCellCommand
import org.orkg.contenttypes.domain.actions.tables.cells.UpdateTableCellAction.State
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository

class TableCellValueValidator(
    private val thingRepository: ThingRepository,
) : UpdateTableCellAction {
    override fun invoke(command: UpdateTableCellCommand, state: State): State {
        if (command.id != null) {
            val thing = thingRepository.findById(command.id!!).orElseThrow { ThingNotFound(command.id!!) }
            if (command.rowIndex == 0 && thing !is Literal) {
                throw TableHeaderValueMustBeLiteral(command.columnIndex)
            }
        } else if (command.rowIndex == 0) {
            throw CannotDeleteTableHeader()
        }
        return state
    }
}
