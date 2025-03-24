package org.orkg.contenttypes.input.testing.fixtures

import org.orkg.common.Either
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.CreateThingsCommand
import org.orkg.graph.domain.Thing

infix fun String.from(command: CreateThingsCommand): Pair<String, Either<CreateThingCommandPart, Thing>> =
    this to Either.left(command.all()[this] ?: throw IllegalStateException("Thing $this is not defined in command."))

infix fun String.from(command: CreatePaperUseCase.CreateCommand): Pair<String, Either<CreateThingCommandPart, Thing>> =
    this from command.contents!!
