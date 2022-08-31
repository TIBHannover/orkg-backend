package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Literal

interface CreateLiteralUseCase {
    // legacy methods:
    fun create(label: String, datatype: String = "xsd:string"): LiteralRepresentation
    fun create(userId: ContributorId, label: String, datatype: String = "xsd:string"): LiteralRepresentation
}

interface UpdateLiteralUseCase {
    // legacy methods:
    fun update(literal: Literal): LiteralRepresentation
}

interface DeleteLiteralUseCase {
    // legacy methods:
    fun removeAll()
}
