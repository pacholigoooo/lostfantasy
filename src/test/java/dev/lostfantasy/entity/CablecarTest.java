package dev.lostfantasy.entity;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.world.gensokyo.RopewayPath;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CablecarTest {
    @BeforeClass public static void bootstrap() {Bootstrap.register();}
    private static EntityPlayer visitor(TestWorld world,UUID id) {
        return new EntityPlayer(world,new GameProfile(id,"CablecarVisitor")) {
            @Override public boolean isSpectator() {return false;}
            @Override public boolean isCreative() {return false;}
            @Override public void writeEntityToNBT(NBTTagCompound n) {}
            @Override public void readEntityFromNBT(NBTTagCompound n) {}
        };
    }
    @Test public void realServerTicksAdvanceTheMountedTripAndPauseBeforeObstacles() {
        TestWorld world=new TestWorld();EntityPlayer p=visitor(world,UUID.randomUUID());EntityCablecar car=new EntityCablecar(world);car.begin(p,0);assertTrue(p.startRiding(car,true));
        for(int i=0;i<130;i++)car.onUpdate();assertEquals(130,car.age());car.updatePassenger(p);
        assertEquals(car.posY+CablecarShape.STANDING_Y,p.posY,0);assertEquals(0,p.fallDistance,0);assertFalse(car.shouldRiderSit());
        world.blocks.put(new BlockPos(RopewayPath.ride(0,131)),Blocks.STONE.getDefaultState());car.onUpdate();assertEquals(130,car.age());
        world.blocks.clear();car.onUpdate();assertEquals(131,car.age());
        EntityPlayer other=visitor(world,UUID.randomUUID());assertFalse(other.startRiding(car));
    }
    @Test public void savedOwnerAndTravelAgeContinueTheSameTrip() {
        TestWorld world=new TestWorld();UUID owner=UUID.randomUUID();EntityPlayer p=visitor(world,owner);EntityCablecar car=new EntityCablecar(world);car.begin(p,1);p.startRiding(car,true);
        for(int i=0;i<270;i++)car.onUpdate();NBTTagCompound saved=car.writeToNBT(new NBTTagCompound());
        EntityCablecar restored=new EntityCablecar(world);restored.readFromNBT(saved);EntityPlayer joined=visitor(world,owner);assertTrue(joined.startRiding(restored,true));
        for(int i=0;i<100;i++) {
            car.onUpdate();restored.onUpdate();assertEquals(car.age(),restored.age());assertEquals(car.posX,restored.posX,0);assertEquals(car.posY,restored.posY,0);assertEquals(car.posZ,restored.posZ,0);
        }
        EntityCablecar empty=new EntityCablecar(world);for(int i=0;i<101;i++)empty.onUpdate();assertTrue(empty.isDead);
    }
    @Test public void exportedCabinFitsTheRenderBoundsAndLeavesThePassengerClear() throws Exception {
        net.minecraft.util.math.AxisAlignedBB passenger=new net.minecraft.util.math.AxisAlignedBB(-.3,CablecarShape.STANDING_Y+.00001,-.3,.3,CablecarShape.STANDING_Y+1.8,.3);
        try(java.io.DataInputStream in=new java.io.DataInputStream(new java.io.FileInputStream("src/main/resources/assets/lostfantasy/meshes/cablecar.lfm"))) {
            assertEquals(0x4c464d32,in.readInt());int count=in.readInt();assertEquals(0,count%3);
            for(int i=0;i<count;i+=3) {
                double[] min={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY};
                double[] max={Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
                for(int v=0;v<3;v++) {
                    for(int axis=0;axis<3;axis++) {float p=in.readFloat();min[axis]=Math.min(min[axis],p);max[axis]=Math.max(max[axis],p);}
                    in.readInt();for(int a=0;a<5;a++)in.readFloat();
                }
                net.minecraft.util.math.AxisAlignedBB face=new net.minecraft.util.math.AxisAlignedBB(min[0],min[1],min[2],max[0],max[1],max[2]);
                assertFalse("passenger clipping",face.intersects(passenger));
                assertTrue(min[1]>=0 && max[1]<=8.5);assertTrue(min[0]>=-3 && max[0]<=3 && min[2]>=-3 && max[2]<=3);
            }
            assertEquals(-1,in.read());
        }
    }
}
