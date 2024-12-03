package org.orkg.openrewrite

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import org.openrewrite.*
import org.openrewrite.java.tree.*
import org.openrewrite.kotlin.KotlinIsoVisitor
import org.openrewrite.marker.Markers

class SuspendFunRecipe : Recipe {
    @Option(
        displayName = "Fully Qualified Class Name",
        description = "A fully qualified class name indicating which class to add a hello() method to.",
        example = "com.yourorg.FooBar"
    )
    var fullyQualifiedClassName: String = ""

    // All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
    @JsonCreator
    constructor(@JsonProperty("fullyQualifiedClassName") fullyQualifiedClassName: String) {
        this.fullyQualifiedClassName = fullyQualifiedClassName
    }

    override fun getDisplayName(): String {
        return "Say Hello"
    }

    override fun getDescription(): String {
        return "Adds a \"hello\" method to the specified class."
    }


    override fun getVisitor(): TreeVisitor<*, ExecutionContext> {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return SuspendFunVisitor()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SuspendFunRecipe

        return fullyQualifiedClassName == other.fullyQualifiedClassName
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + fullyQualifiedClassName.hashCode()
        return result
    }

    override fun toString(): String {
        return "SuspendFunRecipe(fullyQualifiedClassName='$fullyQualifiedClassName')"
    }

    inner class SuspendFunVisitor : KotlinIsoVisitor<ExecutionContext>() {
        private val suspend = J.Modifier(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            "suspend",
            J.Modifier.Type.LanguageExtension,
            emptyList()
        )

        override fun visitMethodDeclaration(
            methodDecl: J.MethodDeclaration,
            executionContext: ExecutionContext
        ): J.MethodDeclaration {
            if (methodDecl.modifiers.any { it.keyword == "suspend" }) {
                return methodDecl
            }

            val modifiers = methodDecl.modifiers.toMutableList()

            if (modifiers.isNotEmpty()) {
                modifiers[0] = modifiers.first().withPrefix(Space.SINGLE_SPACE)
            }

            modifiers.add(0, suspend)

            return methodDecl.withModifiers(modifiers)
        }
    }
}
