import com.google.cloud.tools.jib.gradle.JibExtension
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    id("com.google.cloud.tools.jib")
}

extensions.configure<JibExtension> {
    val baseImageName = "gcr.io/distroless/java21"
    from.image = baseImageName
    to.image = "registry.gitlab.com/tibhannover/orkg/orkg-backend"
    container {
        val customLabels = mutableMapOf(
            // Standardized tags by OCI
            "org.opencontainers.image.vendor" to "Open Research Knowledge Graph (ORKG) <info@orkg.org>",
            "org.opencontainers.image.authors" to listOf(
                "Manuel Prinz <manuel.prinz@tib.eu>",
                "Marcel Konrad <marcel.konrad@tib.eu>",
            ).joinToString(", "),
            "org.opencontainers.image.created" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            "org.opencontainers.image.source" to "https://gitlab.com/TIBHannover/orkg/orkg-backend",
            "org.opencontainers.image.base.name" to baseImageName,
            // "org.opencontainers.image.base.digest" to "",
        )
        if (System.getenv("GITLAB_CI") == "true") {
            customLabels += "org.opencontainers.image.revision" to System.getenv("CI_COMMIT_SHA")
            if (System.getenv("CI_COMMIT_TAG") != null) {
                customLabels += "org.opencontainers.image.version" to System.getenv("CI_COMMIT_TAG")
            }
            customLabels += "org.orkg.component.rest-api.ci-build" to System.getenv("CI_PIPELINE_URL")
            // Overwrite key in CI builds with pipeline information
            customLabels += "org.opencontainers.image.created" to System.getenv("CI_PIPELINE_CREATED_AT")
        }
        labels.putAll(customLabels)
    }
}
