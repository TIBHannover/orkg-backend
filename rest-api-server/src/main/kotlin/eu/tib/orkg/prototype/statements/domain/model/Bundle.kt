package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Bundle(
    @JsonProperty("root")
    val rootId: String,
    @JsonProperty("statements")
    var bundle: MutableList<StatementRepresentation> = mutableListOf()
) {
    private fun addStatement(statement: StatementRepresentation) {
        bundle.add(statement)
    }

    operator fun contains(statement: GeneralStatement): Boolean {
        return this.bundle.any { it.id == statement.id }
    }

    operator fun plus(other: Bundle): Bundle {
        val newBundle = this.copy()
        other.bundle
            .filter { it !in this.bundle }
            .forEach { newBundle.addStatement(it) }
        // TODO: This is sorting descending to conform to issue (#309)
        newBundle.bundle.sortByDescending { it.createdAt }
        return newBundle
    }
}
