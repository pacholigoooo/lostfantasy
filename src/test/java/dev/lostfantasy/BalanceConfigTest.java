package dev.lostfantasy;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraftforge.common.config.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class BalanceConfigTest {
    @Rule public final TemporaryFolder temporary = new TemporaryFolder();

    @Test public void saturatedThresholdsRemainOrderedAndWithinTheConfiguredLimit() throws Exception {
        Map<Field, Object> saved = new LinkedHashMap<>();
        for (Field field : Balance.class.getFields()) if (Modifier.isStatic(field.getModifiers())) {
            Object value = field.get(null);
            saved.put(field, value instanceof int[] ? ((int[]) value).clone() : value);
        }
        Field home = net.minecraftforge.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        Object previousHome = home.get(null);
        home.set(null, temporary.getRoot());
        try {
            File file = temporary.newFile("balance.cfg");
            Configuration config = new Configuration(file);
            for (int i = 3; i <= 6; i++) config.get("progression", "capacity"+i+"XP", 0).set(10000000);
            config.get("progression", "youkaiStage2", 0).set(10000000);
            config.get("progression", "vampireStage2XP", 0).set(10000000);
            config.save();
            Balance.load(file);
            assertArrayEquals(new int[]{9999997,9999998,9999999,10000000}, Balance.spiritThresholds);
            assertEquals(9999999, Balance.youkaiSecond);
            assertEquals(10000000, Balance.youkaiThird);
            assertEquals(9999999, Balance.vampireSecond);
            assertEquals(10000000, Balance.vampireThird);
        } finally {
            for (Map.Entry<Field,Object> entry : saved.entrySet()) entry.getKey().set(null, entry.getValue());
            home.set(null, previousHome);
        }
    }
}
