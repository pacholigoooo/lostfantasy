package dev.lostfantasy.core;

import dev.lostfantasy.Balance;
import net.minecraftforge.common.config.Configuration;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class SpellBalanceTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();

    @Test public void oldConfigsGainDoubleDamageWithoutOverwritingBaseDamageAndCustomMultiplierPersists() throws Exception {
        float flare=Balance.flareDamage, train=Balance.trainDamage, spear=Balance.spearDamage, multiplier=Balance.spellDamageMultiplier;
        Field home=net.minecraftforge.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);Object oldHome=home.get(null);home.set(null,folder.getRoot());
        try {
            File file=new File(folder.getRoot(),"existing.cfg");
            Configuration config=new Configuration(file);
            config.get("spells","flareDamagePerTick",7).set(7);
            config.get("spells","trainDamage",80).set(80);
            config.get("spells","gungnirDamage",25).set(25);
            config.save();
            Balance.load(file);
            assertEquals(2,Balance.spellDamageMultiplier,0);
            assertEquals(7,Balance.flareDamage,0);assertEquals(80,Balance.trainDamage,0);assertEquals(25,Balance.spearDamage,0);
            Configuration saved=new Configuration(file);saved.load();
            assertEquals(2,saved.getCategory("spells").get("damageMultiplier").getDouble(),0);
            saved.getCategory("spells").get("damageMultiplier").set(1.25);saved.save();
            Balance.load(file);Balance.load(file);
            assertEquals(1.25,Balance.spellDamageMultiplier,0);
            assertEquals(7,Balance.flareDamage,0);assertEquals(80,Balance.trainDamage,0);assertEquals(25,Balance.spearDamage,0);
        }finally {
            Balance.flareDamage=flare;Balance.trainDamage=train;Balance.spearDamage=spear;
            Balance.spellDamageMultiplier=multiplier;home.set(null,oldHome);
        }
    }
}
