package performance

import com.intuit.karate.gatling.KarateProtocol
import com.intuit.karate.gatling.PreDef._
import io.gatling.core.Predef._
import io.gatling.core.structure._

import scala.concurrent.duration._
import scala.language.postfixOps

// Adapted from https://intuit.github.io/karate/karate-gatling/#usage
class SimpleSimulation extends Simulation {

    // Declare URL patterns used in features so that they "aggregate" correctly in the report.
    val protocol: KarateProtocol = karateProtocol(
        "/" -> Nil
    )

    // (optional), but "useful for teams that need more control over the “segregation” of requests"
    //noinspection ScalaUnusedSymbol
    protocol.nameResolver = (req, ctx) => req.getHeader("karate-name")

    // karateFeature() declares (a) feature(s) as a "flow".
    val simple: ScenarioBuilder = scenario("simple").exec(karateFeature("classpath:performance/simple.feature"))

    setUp(
        simple.inject(rampUsers(10) during (5 seconds)).protocols(protocol)
    )
}
