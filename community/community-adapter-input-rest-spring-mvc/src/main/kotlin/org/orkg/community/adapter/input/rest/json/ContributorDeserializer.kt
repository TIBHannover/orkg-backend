package org.orkg.community.adapter.input.rest.json

import io.ipfs.multibase.Base16
import io.ipfs.multihash.Multihash
import io.ipfs.multihash.Multihash.Type
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.time.OffsetDateTime

class ContributorDeserializer : ValueDeserializer<Contributor>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Contributor {
        val node = ctxt.readTree(p)
        return Contributor(
            id = ContributorId(node["id"].asString()),
            name = node["display_name"].asString(),
            joinedAt = OffsetDateTime.parse(node["joined_at"].asString()),
            organizationId = OrganizationId(node["organization_id"].asString()),
            observatoryId = ObservatoryId(node["observatory_id"].asString()),
            emailHash = node["gravatar_id"].asString().let { gravatarId ->
                val hash = Base16.decode(gravatarId)
                if (hash.size == Type.md5.length) {
                    Multihash(Type.md5, hash)
                } else {
                    Multihash(Type.sha2_256, hash)
                }
            }
        )
    }
}
