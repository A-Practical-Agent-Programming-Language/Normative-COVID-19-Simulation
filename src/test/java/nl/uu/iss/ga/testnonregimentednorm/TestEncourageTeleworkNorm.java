package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.iss.ga.model.norm.nonregimented.EncourageTeleworkNorm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

@DisplayName("Norm: Encourage Telework")
public class TestEncourageTeleworkNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        norm = new EncourageTeleworkNorm();
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        testOnceIncreasedTrustDecreasesAttitude();
    }

    @RepeatedTest(100)
    void testAttitudeDecreasesWhenOthersSymptomatic() {
        testOnceMoreSymptomaticSeenDecreasesAttitude();
//        testOnceMoreMasksSeenDecreasesAttitude();
    }

}
