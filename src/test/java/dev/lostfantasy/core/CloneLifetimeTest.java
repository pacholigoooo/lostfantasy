package dev.lostfantasy.core;

import dev.lostfantasy.Balance;
import net.minecraftforge.common.config.Configuration;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import static org.junit.Assert.*;

public class CloneLifetimeTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();
    @Test public void currentDurationDefaultsToSixtySecondsAndCustomSettingsPersist() throws Exception {
        int saved=Balance.cloneTicks;
        // Configuration only needs Forge's game-directory value; do not start its launcher.
        java.lang.reflect.Field homeField=net.minecraftforge.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);Object oldHome=homeField.get(null);homeField.set(null,folder.getRoot());
        try {
            File file=new File(folder.getRoot(),"current.cfg");
            Balance.cloneTicks=40;Balance.load(file);assertEquals(1200,Balance.cloneTicks);
            Configuration reloaded=new Configuration(file);reloaded.load();
            reloaded.get("spells","cloneLifetimeTicks",1200).set(240);reloaded.save();
            Balance.load(file);assertEquals(240,Balance.cloneTicks);
        } finally {Balance.cloneTicks=saved;homeField.set(null,oldHome);}
    }
}
