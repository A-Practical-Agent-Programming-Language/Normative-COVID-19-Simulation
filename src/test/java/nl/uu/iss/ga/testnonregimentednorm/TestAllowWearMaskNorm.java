package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.iss.ga.model.norm.modal.AllowWearMaskNorm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Norm: Allow Wear Mask")
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class TestAllowWearMaskNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        norm = new AllowWearMaskNorm();
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        testOnceIncreasedTrustDecreasesAttitude();
    }

    @RepeatedTest(100)
    void testAttitudeLowWhenOthersWearMask() {
        testOnceMoreMasksSeenDecreasesAttitude();
    }

}
