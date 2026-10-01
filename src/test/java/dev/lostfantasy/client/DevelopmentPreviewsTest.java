package dev.lostfantasy.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class DevelopmentPreviewsTest {
    @Test public void packagedRuntimeNeverChecksFlagsOrLoadsExcludedClasses() {
        DevelopmentPreviews.install(false, key -> {
            fail("Production must ignore preview flags, including flags left in JVM arguments");
            return true;
        }, name -> fail("Production attempted to load " + name));
    }

    @Test public void developmentLoadsOnlyExplicitlySelectedPreviews() {
        List<String> loaded = new ArrayList<>();
        DevelopmentPreviews.install(true, key -> key.equals("lostfantasy.komachiPreview"), loaded::add);
        assertEquals(Arrays.asList("dev.lostfantasy.preview.KomachiPreview"), loaded);
        loaded.clear();
        DevelopmentPreviews.install(true, key -> false, loaded::add);
        assertTrue(loaded.isEmpty());
    }
}
