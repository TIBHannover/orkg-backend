import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("org.asciidoctor.jvm.convert")
    id("org.asciidoctor.jvm.gems")
}

val asciidoctor by configurations.creating
