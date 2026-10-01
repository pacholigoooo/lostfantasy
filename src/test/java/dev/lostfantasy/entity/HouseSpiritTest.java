package dev.lostfantasy.entity;

import dev.lostfantasy.TestWorld;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HouseSpiritTest {
    @BeforeClass public static void bootstrap() {Bootstrap.register();}
    @Test public void boundedDriftNeedsNoBlockLookupsAndDoesNotPushOrTakeDamage() {
        TestWorld world=new TestWorld();EntityHouseSpirit spirit=new EntityHouseSpirit(world);spirit.anchor(new BlockPos(-65,100,927),2.1);world.loaded=false;
        for(int i=0;i<40000;i++) {
            double x=spirit.posX,y=spirit.posY,z=spirit.posZ;spirit.drift();
            assertTrue(spirit.getDistanceSq(x,y,z)<=.045*.045+.0000001);
            assertTrue(Math.abs(spirit.posX+64.5)<=3.001);assertTrue(Math.abs(spirit.posZ-927.5)<=2.401);assertTrue(Math.abs(spirit.posY-100)<=.551);
        }
        assertFalse(spirit.canBeCollidedWith());assertFalse(spirit.canBePushed());assertTrue(spirit.noClip);assertTrue(spirit.hasNoGravity());
        assertFalse(spirit.attackEntityFrom(DamageSource.GENERIC,100));assertFalse(spirit.isDead);
        assertTrue(spirit.isInRangeToRenderDist(47*47));assertFalse(spirit.isInRangeToRenderDist(49*49));
    }
    @Test public void savedHomeAndPhaseContinueTheSameMotionAfterReload() {
        TestWorld world=new TestWorld();EntityHouseSpirit original=new EntityHouseSpirit(world);original.anchor(new BlockPos(-65,100,927),4.2);
        for(int i=0;i<314;i++)original.drift();
        NBTTagCompound saved=original.writeToNBT(new NBTTagCompound());EntityHouseSpirit loaded=new EntityHouseSpirit(world);loaded.readFromNBT(saved);
        for(int i=0;i<300;i++) {
            original.drift();loaded.drift();assertEquals(original.posX,loaded.posX,.000001);assertEquals(original.posY,loaded.posY,.000001);assertEquals(original.posZ,loaded.posZ,.000001);
        }
        EntityHouseSpirit summoned=new EntityHouseSpirit(world);summoned.setPosition(20,80,30);summoned.drift();
        assertTrue(summoned.getDistanceSq(20,80,30)<.01);
    }
}
