package org.orkg.graph.adapter.input.rest.json

import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import tools.jackson.databind.module.SimpleDeserializers
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.module.SimpleSerializers

class GraphJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(
            SimpleSerializers().apply {
                addSerializer(StatementId::class.java, StatementIdSerializer())
            }
        )
        context?.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(StatementId::class.java, StatementIdDeserializer())
                addDeserializer(Resource::class.java, ResourceDeserializer())
            }
        )
        context?.setMixIn(Thing::class.java, ThingMixin::class.java)
        context?.setMixIn(Class::class.java, ClassMixin::class.java)
        context?.setMixIn(Literal::class.java, LiteralMixin::class.java)
        context?.setMixIn(Resource::class.java, ResourceMixin::class.java)
        context?.setMixIn(Predicate::class.java, PredicateMixin::class.java)
        context?.setMixIn(GeneralStatement::class.java, StatementMixin::class.java)
    }
}
