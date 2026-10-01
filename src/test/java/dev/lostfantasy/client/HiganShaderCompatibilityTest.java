package dev.lostfantasy.client;

import dev.lostfantasy.Balance;
import java.util.function.BooleanSupplier;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiganShaderCompatibilityTest {
    public static final class ConfigFixture {
        static boolean active;
        public static boolean isShaders() {return active;}
    }
    public static final class BrokenFixture {
        public static boolean isShaders() {throw new IllegalStateException("Test bridge failure");}
    }
    private ClassLoader withoutOptifine() {
        return new ClassLoader(getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
                if(name.equals("Config"))throw new ClassNotFoundException(name);
                return super.loadClass(name,resolve);
            }
        };
    }
    @Test public void absentOptifineLeavesLocalRenderingEnabled() {
        assertFalse(HiganShaderCompatibility.probe(withoutOptifine()).getAsBoolean());
    }
    @Test public void followsShaderPackTogglesWithoutRequiringOptifineAtCompileTime() {
        BooleanSupplier probe=HiganShaderCompatibility.shaderState(ConfigFixture.class);
        ConfigFixture.active=false;assertFalse(probe.getAsBoolean());
        ConfigFixture.active=true;assertTrue(probe.getAsBoolean());
        ConfigFixture.active=false;assertFalse(probe.getAsBoolean());
    }
    @Test public void manualCompatibilitySettingAndUnreadableBridgeYieldToExternalRendering() {
        assertTrue(HiganShaderCompatibility.shaderState(BrokenFixture.class).getAsBoolean());
        assertTrue(HiganShaderCompatibility.shaderState(Object.class).getAsBoolean());
        boolean old=Balance.higanShaderCompatibility;
        try {Balance.higanShaderCompatibility=true;assertTrue(HiganShaderCompatibility.externalAtmosphere());}
        finally {Balance.higanShaderCompatibility=old;}
    }
}
