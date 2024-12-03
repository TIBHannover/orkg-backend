package org.orkg.openrewrite

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import java.util.function.Consumer
import org.openrewrite.*
import org.openrewrite.java.tree.*
import org.openrewrite.kotlin.KotlinIsoVisitor
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.kotlin.KotlinTemplate
import org.openrewrite.marker.Markers

class SayHelloRecipe : Recipe {
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
        return SayHelloVisitor()
    }

    inner class SayHelloVisitor : KotlinIsoVisitor<ExecutionContext>() {
        private val helloTemplate: KotlinTemplate =
            run {
                val constructor = KotlinTemplate::class.java.getDeclaredConstructor(
                    Boolean::class.java,
                    KotlinParser.Builder::class.java,
                    String::class.java,
                    Set::class.java,
                    Consumer::class.java,
                    Consumer::class.java
                )
                constructor.isAccessible = true
                constructor.newInstance(
                    true,
                    KotlinParser.builder(),
                    "fun hello(): String {\nreturn \"Hello from com.yourorg.FooBar!\"\n }",
                    mutableSetOf<String>(),
                    Consumer<String> { },
                    Consumer<String> { }
                )
            }

        override fun visitClassDeclaration(
            classDecl: J.ClassDeclaration,
            executionContext: ExecutionContext
        ): J.ClassDeclaration {
            // Don't make changes to classes that don't match the fully qualified name
            if (classDecl.type == null || classDecl.type?.fullyQualifiedName != fullyQualifiedClassName) {
                return classDecl
            }

            // Check if the class already has a method named "hello".
            val helloMethodExists = classDecl.body.statements
                .filterIsInstance<J.MethodDeclaration>()
                .map { J.MethodDeclaration::class.java.cast(it) }
                .any { it.name.simpleName == "hello" }

            // If the class already has a `hello()` method, don't make any changes to it.
            if (helloMethodExists) {
                return classDecl
            }

            val methodType = JavaType.Method(
                /* managedReference = */ null,
                /* flagsBitMap = */ Flag.Public.bitMask or Flag.Final.bitMask,
                /* declaringType = */ JavaType.Class(
                    /* managedReference = */ null,
                    /* flagsBitMap = */ Flag.Public.bitMask or Flag.Final.bitMask,
                    /* fullyQualifiedName = */ "com.yourorg.FooBar",
                    /* kind = */ JavaType.FullyQualified.Kind.Class,
                    /* typeParameters = */ null,
                    /* supertype = */ JavaType.buildType("kotlin.Any") as JavaType.Class,
                    /* owningClass = */ null,
                    /* annotations = */ null,
                    /* interfaces = */ null,
                    /* members = */ null,
                    /* methods = */ null
                ),
                /* name = */ "hello",
                /* returnType = */ JavaType.buildType("kotlin.String"),
                /* parameterNames = */ null,
                /* parameterTypes = */ null,
                /* thrownExceptions = */ null,
                /* annotations = */ null
            )

            /**
             *  \-------J.MethodDeclaration | "MethodDeclaration{com.yourorg.FooBar{name=hello,return=kotlin.String,parameters=[]}}"
             *          |---J.Modifier | "final"
             *          |---J.Modifier | "languageextension"
             *          |---J.Identifier | "String"
             *          |---J.Identifier | "hello"
             *          |-----------J.Empty
             *          \---J.Block
             *              \-------K.Return | "return """
             *                      \---J.Return | "return """
             *                          \---J.Literal | """"
             */
//            classDecl.body.m
            val methodDecl = J.MethodDeclaration(
                /* id = */ UUID.randomUUID(),
                /* prefix = */ Space.EMPTY,
                /* markers = */ Markers.EMPTY,
                /* leadingAnnotations = */ emptyList(),
                /* modifiers = */ listOf(
                    J.Modifier(
                        UUID.randomUUID(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        "final",
                        J.Modifier.Type.Final,
                        emptyList()
                    ),
                    J.Modifier(
                        UUID.randomUUID(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        "fun",
                        J.Modifier.Type.LanguageExtension,
                        emptyList()
                    )
                ),
                /* typeParameters = */ null,

                /*
                J.TypeParameters(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    emptyList(),
                    emptyList()
                )
                 */

                /* returnTypeExpression = */ J.Identifier(
                    /* id = */ UUID.randomUUID(),
                    /* prefix = */ Space.SINGLE_SPACE,
                    /* markers = */ Markers(UUID.randomUUID(), emptyList()),
                    /* annotations = */ emptyList(),
                    /* simpleName = */ "String",
                    /* type = */ JavaType.buildType("kotlin.String"),
                    /* fieldType = */ null
                ),
                /* name = */ J.MethodDeclaration.IdentifierWithAnnotations(
                    J.Identifier(
                        /* id = */ UUID.randomUUID(),
                        /* prefix = */ Space.SINGLE_SPACE,
                        /* markers = */ Markers.EMPTY,
                        /* annotations = */ emptyList(),
                        /* simpleName = */ "hello",
                        /* type = */ methodType,
                        /* fieldType = */ null
                    ),
                    emptyList()
                ),
                /* parameters = */ JContainer.empty(),
                /* throwz = */ null,
                /* body = */ null,
                /* defaultValue = */ null,
                /* methodType = */ methodType
            )

            return classDecl.withBody(
                helloTemplate.apply(
                    Cursor(cursor, classDecl.body),
                    classDecl.body.coordinates.lastStatement()
                )
            )

            return classDecl.withBody(
                classDecl.body.withStatements(classDecl.body.statements + methodDecl)
            )

            // Interpolate the fullyQualifiedClassName into the template and use the resulting LST to update the class body
//            return classDecl
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SayHelloRecipe

        return fullyQualifiedClassName == other.fullyQualifiedClassName
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + fullyQualifiedClassName.hashCode()
        return result
    }

    override fun toString(): String {
        return "SayHelloRecipe(fullyQualifiedClassName='$fullyQualifiedClassName')"
    }
}
