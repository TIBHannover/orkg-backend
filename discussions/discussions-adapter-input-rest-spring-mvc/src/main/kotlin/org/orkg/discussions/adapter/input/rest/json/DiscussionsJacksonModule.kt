package org.orkg.discussions.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.discussions.domain.DiscussionCommentId

class DiscussionsJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(DiscussionCommentId::class.java, DiscussionCommentIdSerializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(DiscussionCommentId::class.java, DiscussionCommentIdDeserializer())
        })
    }
}
