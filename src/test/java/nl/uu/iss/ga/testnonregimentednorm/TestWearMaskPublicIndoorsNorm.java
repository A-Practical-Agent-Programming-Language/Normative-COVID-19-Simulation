package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.iss.ga.model.norm.modal.WearMaskPublicIndoorsNorm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;


@DisplayName("Norm: Wear Mask Public Indoors")
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class TestWearMaskPublicIndoorsNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        norm = new WearMaskPublicIndoorsNorm();
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
