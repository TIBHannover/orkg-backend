package eu.tib.orkg.prototype.statements.domain.model

data class Bundle(
    val rootId: String
) {
    var bundle: MutableList<BundleStatement> = mutableListOf()

    fun addStatement(statement: GeneralStatement, level: Int) {
        bundle.add(BundleStatement(statement, level))
    }
}

data class BundleStatement(
    val statement: GeneralStatement,
    val level: Int
)
