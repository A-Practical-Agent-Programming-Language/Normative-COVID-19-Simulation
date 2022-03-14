package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHomeNorm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

@DisplayName("Norm: If Sick Stay Home")
public class TestIfSickStayHomeNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        norm = new IfSickStayHomeNorm();
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        testOnceIncreasedTrustDecreasesAttitude();
    }
}
