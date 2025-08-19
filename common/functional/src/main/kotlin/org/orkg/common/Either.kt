package org.orkg.common

import java.io.Serial
import java.io.Serializable

sealed interface Either<L, R> : Serializable {
    val isLeft: Boolean
    val isRight: Boolean

    fun onLeft(block: (L) -> Unit)

    fun onRight(block: (R) -> Unit)

    fun <T> fold(leftMapper: (L) -> T, rightMapper: (R) -> T): T

    fun <T> mapLeft(leftMapper: (L) -> T): Either<T, R>

    fun <T> mapRight(rightMapper: (R) -> T): Either<L, T>

    fun <S, T> map(leftMapper: (L) -> S, rightMapper: (R) -> T): Either<S, T>

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)

        fun <L, R> right(value: R): Either<L, R> = Right(value)

        fun <T> Either<T, T>.merge() = fold({ it }, { it })
    }
}

private data class Left<L, R>(val value: L) : Either<L, R> {
    override val isLeft: Boolean
        get() = true
    override val isRight: Boolean
        get() = false

    override fun onRight(block: (R) -> Unit) = Unit

    override fun onLeft(block: (L) -> Unit) = block(value)

    override fun <T> fold(leftMapper: (L) -> T, rightMapper: (R) -> T): T = leftMapper(value)

    override fun <T> mapLeft(leftMapper: (L) -> T): Either<T, R> = Left(leftMapper(value))

    override fun <T> mapRight(rightMapper: (R) -> T): Either<L, T> = Left(value)

    override fun <S, T> map(leftMapper: (L) -> S, rightMapper: (R) -> T): Either<S, T> = Left(leftMapper(value))

    companion object {
        @Serial
        private const val serialVersionUID: Long = 4215803670109274228L
    }
}

private data class Right<L, R>(val value: R) : Either<L, R> {
    override val isLeft: Boolean
        get() = false
    override val isRight: Boolean
        get() = true

    override fun onRight(block: (R) -> Unit) = block(value)

    override fun onLeft(block: (L) -> Unit) = Unit

    override fun <T> fold(leftMapper: (L) -> T, rightMapper: (R) -> T): T = rightMapper(value)

    override fun <T> mapLeft(leftMapper: (L) -> T): Either<T, R> = Right(value)

    override fun <T> mapRight(rightMapper: (R) -> T): Either<L, T> = Right(rightMapper(value))

    override fun <S, T> map(leftMapper: (L) -> S, rightMapper: (R) -> T): Either<S, T> = Right(rightMapper(value))

    companion object {
        @Serial
        private const val serialVersionUID: Long = 642768739619297750L
    }
}
