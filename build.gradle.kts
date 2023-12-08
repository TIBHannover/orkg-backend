import org.orkg.gradle.plugins.PrintCoverageTask

plugins {
    id("com.diffplug.spotless") version "6.2.1" apply false
    id("com.diffplug.spotless-changelog") version "2.4.0"

    base // required, otherwise report aggregation plugins will not work
    id("test-report-aggregation")
    id("jacoco-report-aggregation")
    id("org.orkg.print-coverage")
}

dependencies {
    // Configure all projects for test and coverage aggregation. Ignore parent projects, or those without (tested) code.
    val parentProjects = subprojects
        .map(Project::getPath)
        .filter { path -> path.count { it == ':' } < 2 }
        .filterNot { it == ":rest-api-server" } // Not a parent project
        .toSet()
    val ignoredProjects = setOf(":platform", ":testing:kotest", ":testing:spring") union parentProjects
    subprojects
        .filterNot { it.path in ignoredProjects }
        .onEach(::jacocoAggregation)
        .onEach(::testReportAggregation)
}

// Test reports
reporting {
    reports {
        val testAggregateTestReport by creating(AggregateTestReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

// JaCoCo Code Coverage Report
reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

val testCodeCoverageReport by tasks.getting(JacocoReport::class) {
    reports {
        xml.required.set(true)
    }
}

tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
    finalizedBy(tasks.named<PrintCoverageTask>("printCoverage"))
}
