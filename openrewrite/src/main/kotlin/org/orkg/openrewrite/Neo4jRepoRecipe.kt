package org.orkg.openrewrite

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import org.openrewrite.*
import org.openrewrite.java.tree.*
import org.openrewrite.kotlin.KotlinIsoVisitor
import org.openrewrite.kotlin.tree.K
import org.openrewrite.marker.Markers

class Neo4jRepoRecipe : Recipe {
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
        return SplitPageQueryMethodVisitor()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Neo4jRepoRecipe

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

    inner class SplitPageQueryMethodVisitor : KotlinIsoVisitor<ExecutionContext>() {
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
            val queryAnnotation = methodDecl.leadingAnnotations.find { it.simpleName == "Query" }
                ?: return methodDecl

            val queryAnnotationArgs: MutableList<Expression> = queryAnnotation.arguments.orEmpty().toMutableList()

            if (queryAnnotationArgs.size < 2) {
                return methodDecl
            }

            val contentQueryAssignment = queryAnnotationArgs.find {
                it is K.StringTemplate || it is J.Assignment && with(it.variable as J.Identifier) { simpleName == "value" }
            }
            val countQueryAssignment = queryAnnotationArgs.find {
                it is J.Assignment && with(it.variable as J.Identifier) { simpleName == "countQuery" }
            }

            queryAnnotationArgs.remove(countQueryAssignment)

            return methodDecl.withLeadingAnnotations(methodDecl.leadingAnnotations.map {
                if (it == queryAnnotation) {
                    queryAnnotation.withArguments(queryAnnotationArgs)
                } else {
                    it
                }
            })
        }
    }
}
