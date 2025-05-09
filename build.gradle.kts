import com.autonomousapps.tasks.BuildHealthTask
import org.orkg.gradle.plugins.PrintCoverageTask

plugins {
    id("org.orkg.gradle.root")

    base // required, otherwise report aggregation plugins will not work
    id("test-report-aggregation")
    id("jacoco-report-aggregation")
    id("org.orkg.gradle.print-coverage")
}

dependencies {
    testReportAggregation(project(":rest-api-server"))
    jacocoAggregation(project(":rest-api-server"))
}

// Test reports
reporting {
    reports {
        val testAggregateTestReport by creating(AggregateTestReport::class) {
            testSuiteName = "test"
        }
    }
}

// JaCoCo Code Coverage Report
reporting {
    reports {
        val testCodeCoverageReport by registering(JacocoCoverageReport::class) {
            testSuiteName = "test"
        }
    }
}

val testCodeCoverageReport by tasks.getting(JacocoReport::class) {
    reports {
        xml.required.set(true)
    }
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
            onUnusedDependencies {
                // This dependency gets on the classpath of integration and container tests, and causes issue with the
                // build health. It is not an issue, so we just exclude it from the analysis.
                exclude("org.junit.jupiter:junit-jupiter")
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
    dependsOn(tasks.named<BuildHealthTask>("buildHealth"))
    finalizedBy(tasks.named<PrintCoverageTask>("printCoverage"))
}
