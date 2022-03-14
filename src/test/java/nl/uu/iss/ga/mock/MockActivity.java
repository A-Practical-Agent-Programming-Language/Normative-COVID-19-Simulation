package nl.uu.iss.ga.mock;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.data.dictionary.LocationEntry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockActivity {

    public static Activity getMockActivity() {
        Activity activity = mock(Activity.class);
        LocationEntry locationEntry = mock(LocationEntry.class);
        when(locationEntry.getLocationID()).thenReturn(1L);
        when(locationEntry.getDesignation()).thenReturn(Designation.none);
        when(activity.getLocation()).thenReturn(locationEntry);

        return activity;
    }
}
