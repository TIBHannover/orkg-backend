package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing

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
        context?.setMixInAnnotations(Thing::class.java, ThingMixin::class.java)
        context?.setMixInAnnotations(Class::class.java, ClassMixin::class.java)
        context?.setMixInAnnotations(Literal::class.java, LiteralMixin::class.java)
        context?.setMixInAnnotations(Resource::class.java, ResourceMixin::class.java)
        context?.setMixInAnnotations(Predicate::class.java, PredicateMixin::class.java)
        context?.setMixInAnnotations(GeneralStatement::class.java, StatementMixin::class.java)
    }
}
