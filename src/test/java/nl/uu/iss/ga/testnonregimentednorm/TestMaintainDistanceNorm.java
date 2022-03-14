package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.iss.ga.model.norm.modal.MaintainDistanceNorm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

@DisplayName("Norm: Maintain Distance")
public class TestMaintainDistanceNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        norm = new MaintainDistanceNorm();
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        testOnceIncreasedTrustDecreasesAttitude();
    }

    @RepeatedTest(100)
    void testAttitudeLowWhenOthersDistanec() {
        testOnceMoreDistancingSeenDecreasesAttitude();
    }

}
