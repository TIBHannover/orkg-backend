package eu.tib.orkg.prototype.statements.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.AlreadyInUse
import eu.tib.orkg.prototype.statements.api.ClassLabelUpdateProblem
import eu.tib.orkg.prototype.statements.api.ClassNotFound
import eu.tib.orkg.prototype.statements.api.ClassURIUpdateProblem
import eu.tib.orkg.prototype.statements.api.ClassUpdateProblem
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.InvalidLabel
import eu.tib.orkg.prototype.statements.api.InvalidURI
import eu.tib.orkg.prototype.statements.api.UpdateNotAllowed
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.api.ClassRepresentation
import eu.tib.orkg.prototype.statements.api.UpdateClassUseCase
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.net.URI
import java.net.URISyntaxException
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ClassService(
    private val repository: ClassRepository
) : ClassUseCases {

    override fun create(label: String): ClassRepresentation = create(ContributorId.createUnknownContributor(), label)

    override fun create(userId: ContributorId, label: String): ClassRepresentation {
        val classId = repository.nextIdentity()
        val newClass = Class(
            id = classId,
            label = label,
            createdAt = OffsetDateTime.now(),
            createdBy = userId,
            uri = null,
        )
        repository.save(newClass)
        return newClass.toClassRepresentation()
    }

    override fun create(request: CreateClassRequest): ClassRepresentation =
        create(ContributorId.createUnknownContributor(), request)

    override fun create(userId: ContributorId, request: CreateClassRequest): ClassRepresentation {
        val id = request.id ?: repository.nextIdentity()
        val newClass = Class(
            id = id,
            label = request.label,
            createdAt = OffsetDateTime.now(),
            createdBy = userId,
            uri = request.uri,
        )
        repository.save(newClass)
        return newClass.toClassRepresentation()
    }

    @Transactional(readOnly = true)
    override fun exists(id: ClassId): Boolean = repository.exists(id)

    override fun findAll(pageable: Pageable): Page<ClassRepresentation> =
        repository.findAll(pageable).map(Class::toClassRepresentation)

    override fun findById(id: ClassId): Optional<ClassRepresentation> =
        repository.findByClassId(id).map(Class::toClassRepresentation)

    override fun findAllByLabel(label: String): Iterable<ClassRepresentation> =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString())
            .map(Class::toClassRepresentation) // TODO: See declaration

    override fun findAllByLabel(pageable: Pageable, label: String): Page<ClassRepresentation> =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable)
            .map(Class::toClassRepresentation) // TODO: See declaration

    override fun findAllByLabelContaining(part: String): Iterable<ClassRepresentation> =
        repository.findAllByLabelMatchesRegex(part.toSearchString())
            .map(Class::toClassRepresentation) // TODO: See declaration

    override fun findAllByLabelContaining(pageable: Pageable, part: String): Page<ClassRepresentation> =
        repository.findAllByLabelMatchesRegex(part.toSearchString(), pageable)
            .map(Class::toClassRepresentation) // TODO: See declaration

    override fun replace(id: ClassId, command: UpdateClassUseCase.ReplaceCommand): Result<Unit, ClassUpdateProblem> {
        val label = Label.ofOrNull(command.label) ?: return Failure(InvalidLabel)
        val found = repository.findByClassId(id).orElse(null) ?: return Failure(ClassNotFound)
        if (found.uri != command.uri && found.uri != null) return Failure(UpdateNotAllowed)
        command.uri?.let {
            val possiblyUsed = findByURI(it).orElse(null)
            if (possiblyUsed != null && possiblyUsed.id != found.id) return Failure(AlreadyInUse)
        }
        repository.save(found.copy(id = id, label = label.value, uri = command.uri))
        return Success(Unit)
    }

    override fun updateLabel(id: ClassId, newLabel: String): Result<Unit, ClassLabelUpdateProblem> {
        val label = Label.ofOrNull(newLabel) ?: return Failure(InvalidLabel)
        val found = repository.findByClassId(id).orElse(null) ?: return Failure(ClassNotFound)
        if (found.label != label.value) repository.save(found.copy(label = label.value))
        return Success(Unit)
    }

    override fun updateURI(id: ClassId, with: String): Result<Unit, ClassURIUpdateProblem> {
        val uri = with.toURIOrNull() ?: return Failure(InvalidURI)
        val found = repository.findByClassId(id).orElse(null) ?: return Failure(ClassNotFound)
        if (found.uri != null) return Failure(UpdateNotAllowed)
        val possiblyUsed = findByURI(uri).orElse(null)
        if (possiblyUsed != null && possiblyUsed.id != found.id) return Failure(AlreadyInUse)
        repository.save(found.copy(uri = uri))
        return Success(Unit)
    }

    override fun removeAll() = repository.deleteAll()

    override fun findByURI(uri: URI): Optional<ClassRepresentation> =
        repository.findByUri(uri.toString()).map(Class::toClassRepresentation)

    override fun createIfNotExists(id: ClassId, label: String, uri: URI?) {
        // Checking if URI is null
        if (uri == null) {
            // check only for ID
            val found = repository.findByClassId(id)
            if (found.isEmpty) {
                repository.save(
                    Class(
                        id = id,
                        label = label,
                        uri = uri,
                        createdAt = OffsetDateTime.now(),
                        createdBy = ContributorId.createUnknownContributor()
                    )
                )
            }
        } else {
            val oClassByURI = repository.findByUri(uri.toString())
            val oClassById = repository.findByClassId(id)
            when {
                oClassById.isEmpty && oClassByURI.isEmpty -> repository.save(
                    Class(
                        id = id,
                        label = label,
                        uri = uri,
                        createdAt = OffsetDateTime.now(),
                        createdBy = ContributorId.createUnknownContributor()
                    )
                )
                // Throwing an exception if IDs are different
                oClassById.isPresent && oClassByURI.isPresent && oClassById.get().id != oClassByURI.get().id -> throw Exception(
                    "ID mismatch for class ID: ${oClassById.get().id}"
                )
            }
        }
    }

    private fun String.toURIOrNull(): URI? = try {
        URI(this)
    } catch (_: URISyntaxException) {
        null
    }

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}

fun Class.toClassRepresentation(): ClassRepresentation = object : ClassRepresentation {
    override val id: ClassId = this@toClassRepresentation.id!!
    override val label: String = this@toClassRepresentation.label
    override val uri: URI? = this@toClassRepresentation.uri
    override val description: String? = this@toClassRepresentation.description
    override val jsonClass: String = "class"
    override val createdAt: OffsetDateTime = this@toClassRepresentation.createdAt
    override val createdBy: ContributorId = this@toClassRepresentation.createdBy
}
