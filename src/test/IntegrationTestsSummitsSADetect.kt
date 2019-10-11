package test

import SummitsSA
import data.DetectResult
import org.junit.Test

class IntegrationTestsSummitsSADetect {

    @Test
    fun testPresenceNoValueElev(){
        val routeExpectFailure = RouteFactory().getRouteContainingNoValueElevation()
        val result = SummitsSA().detect(routeExpectFailure)
        assert(result is DetectResult.Failure)
        println("result:")
        println("$result")
    }

}