plugins {
    id("org.asciidoctor.jvm.convert")
    // id("org.asciidoctor.jvm.gems") // FIXME: Disabled because of Asciidoctor issue, but not used anyway
    id("com.epages.restdocs-api-spec")
}

val asciidoctor by configurations.creating
