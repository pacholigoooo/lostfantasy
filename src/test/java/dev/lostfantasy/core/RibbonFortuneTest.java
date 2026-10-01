package dev.lostfantasy.core;

import java.util.UUID;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.*;
import static org.junit.Assert.*;

public class RibbonFortuneTest {
    @BeforeClass public static void bootstrap(){Bootstrap.register();}
    @Test public void goldenLightStartsAfterDrawingAndPersistsOnOnlyTheUsedStack() {
        ItemStack used=new ItemStack(Items.STRING),other=new ItemStack(Items.STRING);
        UUID id=RibbonFortune.mark(used,UUID.randomUUID(),0,100);
        assertEquals(0,RibbonFortune.glow(used,0,171.9),0);
        assertTrue(RibbonFortune.glow(used,0,177)>0);assertEquals(1,RibbonFortune.glow(used,0,182),0);
        ItemStack restored=new ItemStack(used.writeToNBT(new NBTTagCompound()));
        assertTrue(RibbonFortune.matches(restored,id));assertEquals(1,RibbonFortune.glow(restored,0,10000),0);
        assertEquals(1,RibbonFortune.glow(restored,736,0),0);
        assertFalse(RibbonFortune.marked(other));assertEquals(0,RibbonFortune.glow(other,0,10000),0);
        assertFalse(new RiverJourney().fortuneSeparated());
    }
    @Test public void cosmeticSyncDoesNotReequipButRealItemChangesDo() {
        ItemStack plain=new ItemStack(Items.STRING),used=plain.copy();RibbonFortune.mark(used,UUID.randomUUID(),0,0);
        assertTrue(RibbonFortune.onlyLightChanged(plain,used));
        used.setStackDisplayName("kept name");assertFalse(RibbonFortune.onlyLightChanged(plain,used));
        assertFalse(RibbonFortune.onlyLightChanged(plain,new ItemStack(Items.PAPER)));
        assertFalse(RibbonFortune.onlyLightChanged(plain,new ItemStack(Items.STRING,2)));
    }
}
