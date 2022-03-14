package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.iss.ga.model.norm.nonregimented.StayHomeNorm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

public class TestStayHomeNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        norm = new StayHomeNorm();
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        testOnceIncreasedTrustDecreasesAttitude();
    }
}
