package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Bundle(
    @JsonProperty("root")
    val rootId: String,
    @JsonProperty("statements")
    var bundle: MutableList<GeneralStatement> = mutableListOf()
) {
    fun addStatement(statement: GeneralStatement) {
        bundle.add(statement)
    }
}
