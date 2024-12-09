package org.orkg.contenttypes.adapter.output.simcomp.internal

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import org.orkg.common.ThingId
import org.springframework.stereotype.Component

@Component
class InMemorySimCompThingRepositoryAdapter(
    private val objectMapper: ObjectMapper,
    private val idGenerator: () -> UUID = UUID::randomUUID,
    private val clock: Clock = Clock.systemDefaultZone()
) : SimCompThingRepository {
    private val entities: MutableMap<ThingType, MutableMap<ThingId, BaseThing>> = mutableMapOf()

    override fun findById(id: ThingId, type: ThingType): Optional<BaseThing> {
        return entities[type]?.get(id).let { Optional.ofNullable(it) }
    }

    override fun save(id: ThingId, type: ThingType, data: Any, config: Any) {
        val now = LocalDateTime.now(clock)
        entities.getOrPut(type, ::mutableMapOf)[id] = BaseThing(
            id = idGenerator(),
            createdAt = now,
            updatedAt = now,
            thingType = type,
            thingKey = id,
            config = objectMapper.valueToTree(config),
            data = objectMapper.valueToTree(data),
        )
    }

    override fun update(id: ThingId, type: ThingType, data: Any, config: Any) {
        val map = entities.getOrPut(type, ::mutableMapOf)
        val found = map[id] ?: throw IllegalStateException("Thing with id $id was not found.")
        map[id] = found.copy(
            updatedAt = LocalDateTime.now(clock),
            config = objectMapper.valueToTree(config),
            data = objectMapper.valueToTree(data),
        )
    }
}
