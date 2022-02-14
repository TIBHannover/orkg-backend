package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.net.URI

interface CreateClassUseCase {
    // legacy methods:
    fun create(label: String): Class
    fun create(userId: ContributorId, label: String): Class
    fun create(request: CreateClassRequest): Class
    fun create(userId: ContributorId, request: CreateClassRequest): Class
    fun createIfNotExists(id: ClassId, label: String, uri: URI?)
}

interface UpdateClassUseCase {
    // legacy methods:
    fun update(`class`: Class): Class
}

interface DeleteClassUseCase {
    // legacy methods:
    fun removeAll()
}
