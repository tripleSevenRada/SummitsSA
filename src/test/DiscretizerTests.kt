package test

import org.junit.Test
import service.DiscretizingService

class DiscretizerTests {
    @Test
    fun sanityTestDiscretizingService(){
        val mockRoute = RouteFactory().getRouteTwoPoints()
        val discretizedLocationsList = DiscretizingService().discretizeParallel(mockRoute)
        println("discretizedLocationsList.size = ${discretizedLocationsList.size}")
        assert(discretizedLocationsList.size == 129)
    }
}