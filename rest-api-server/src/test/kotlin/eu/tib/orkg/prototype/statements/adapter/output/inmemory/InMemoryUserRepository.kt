package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import java.util.*
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class InMemoryUserRepository : UserRepository {
    override fun findByEmail(email: String): Optional<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun findById(id: UUID): Optional<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun findByObservatoryId(id: UUID): Iterable<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun findByOrganizationId(id: UUID): Iterable<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun findByIdIn(ids: Array<UUID>): List<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> save(entity: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> saveAll(entities: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun existsById(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun findAll(sort: Sort): MutableList<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> findAll(example: Example<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> findAll(example: Example<S>, sort: Sort): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> findAll(example: Example<S>, pageable: Pageable): Page<S> {
        TODO("Not yet implemented")
    }

    override fun findAllById(ids: MutableIterable<UUID>): MutableList<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> count(example: Example<S>): Long {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: UUID) {
        TODO("Not yet implemented")
    }

    override fun delete(entity: UserEntity) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entities: MutableIterable<UserEntity>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> findOne(example: Example<S>): Optional<S> {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> exists(example: Example<S>): Boolean {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun <S : UserEntity?> saveAndFlush(entity: S): S {
        TODO("Not yet implemented")
    }

    override fun deleteInBatch(entities: MutableIterable<UserEntity>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch() {
        TODO("Not yet implemented")
    }

    override fun getOne(id: UUID): UserEntity {
        TODO("Not yet implemented")
    }
}
