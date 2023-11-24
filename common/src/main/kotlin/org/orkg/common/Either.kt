package org.orkg.common

sealed interface Either<L, R> {
    val isLeft: Boolean
    val isRight: Boolean
    fun onLeft(block: (L) -> Unit)
    fun onRight(block: (R) -> Unit)
    fun <T> fold(leftMapper: (L) -> T, rightMapper: (R) -> T): T

    companion object {
        fun <L, R> left(value: L) : Either<L, R> = Left(value)
        fun <L, R> right(value: R) : Either<L, R> = Right(value)
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
}

private data class Right<L, R>(val value: R) : Either<L, R> {
    override val isLeft: Boolean
        get() = false
    override val isRight: Boolean
        get() = true
    override fun onRight(block: (R) -> Unit) = block(value)
    override fun onLeft(block: (L) -> Unit) = Unit
    override fun <T> fold(leftMapper: (L) -> T, rightMapper: (R) -> T): T = rightMapper(value)
}
