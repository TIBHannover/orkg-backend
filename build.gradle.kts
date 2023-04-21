import java.nio.file.attribute.FileAttribute

plugins {
    id("jacoco-report-aggregation")
    id("de.jansauer.printcoverage") version "2.0.0"

    id("com.diffplug.spotless") version "6.2.1" apply false
    id("com.diffplug.spotless-changelog") version "2.4.0"
}

dependencies {
    // This is the main dependency. Other modules are included via its dependencies.
    // SDN6 is not added because of classpath pollution.
    jacocoAggregation(project(":rest-api-server"))
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}

tasks.printCoverage {
    // Implement a hack to make the plugin to find the file. It looks at a very specific place.
    val legacyDir = jacoco.reportsDirectory.dir("test").get().asFile
    val legacyReportFile = File(legacyDir, "jacocoTestReport.xml")
    val dest = File("../testCodeCoverageReport/testCodeCoverageReport.xml")
    legacyDir.mkdirs()
    legacyReportFile.delete()
    java.nio.file.Files.createSymbolicLink(legacyReportFile.toPath(), dest.toPath())

    mustRunAfter(tasks.check)
}

tasks.build {
    finalizedBy(tasks.printCoverage)
}
