package dev.lostfantasy.entity;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.TestWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.*;

public class KomachiFerryTest {
    @Test public void absoluteTrackingPacketsDoNotSnapTheBoatOrItsPassengers() {
        Bootstrap.register();EntityRiverFerry ferry=new EntityRiverFerry(new TestWorld());
        ferry.setLocationAndAngles(10,63,20,35,0);
        for(boolean absolute:new boolean[]{false,true}) {
            ferry.setPositionAndRotationDirect(11,63,20,37,0,3,absolute);
            assertEquals(10,ferry.posX,0);assertEquals(35,ferry.rotationYaw,0);
        }
    }
    private static EntityPlayer player(TestWorld world) {
        return new EntityPlayer(world,new GameProfile(UUID.randomUUID(),"Passenger")) {
            @Override public boolean isSpectator() {return false;}
            @Override public boolean isCreative() {return false;}
        };
    }
    @Test public void pilotAndPassengerHaveSeparateBerthsAndOtherOwnersCannotJoin() {
        Bootstrap.register();TestWorld world=new TestWorld();EntityPlayer a=player(world),b=player(world);
        EntityRiverFerry ferry=new EntityRiverFerry(world,a.getUniqueID()),second=new EntityRiverFerry(world,b.getUniqueID());
        EntityKomachi pilot=new EntityKomachi(world);assertTrue(pilot.startRiding(ferry,true));
        assertTrue(ferry.canFitPassenger(a));assertFalse(ferry.canFitPassenger(b));assertTrue(second.canFitPassenger(b));assertFalse(second.canFitPassenger(a));
        assertFalse(ferry.canFitPassenger(new EntityKomachi(world)));assertTrue(a.startRiding(ferry,true));assertEquals(2,ferry.getPassengers().size());
        assertFalse(ferry.canFitPassenger(b));
        NBTTagCompound saved=new NBTTagCompound();ferry.writeEntityToNBT(saved);EntityRiverFerry restored=new EntityRiverFerry(world);restored.readEntityFromNBT(saved);
        assertTrue(restored.belongsTo(a));assertFalse(restored.belongsTo(b));
        ferry.setDead();assertTrue(pilot.isDead);assertFalse(a.isDead);
    }
    @Test public void waitingPilotDoesNotMakeUnownedFerryUnavailable() {
        Bootstrap.register();TestWorld world=new TestWorld();EntityRiverFerry ferry=new EntityRiverFerry(world);EntityKomachi pilot=new EntityKomachi(world);
        assertTrue(pilot.startRiding(ferry,true));assertTrue(ferry.available());
        ferry.setPosition(10,63,20);ferry.rotationYaw=90;ferry.updatePassenger(pilot);
        assertEquals(9.10,pilot.posX,.0001);assertEquals(20,pilot.posZ,.0001);
        assertEquals(63.19,pilot.posY,.0001);
        EntityPlayer passenger=player(world);
        EntityRiverFerry occupied=new EntityRiverFerry(world,passenger.getUniqueID());
        pilot=new EntityKomachi(world);
        assertTrue(pilot.startRiding(occupied,true));assertTrue(passenger.startRiding(occupied,true));
        for(float yaw:new float[]{-179,-90,0,90,179}) {
            occupied.rotationYaw=yaw;occupied.updatePassenger(pilot);occupied.updatePassenger(passenger);
            double angle=Math.toRadians(yaw);
            double distanceForward=(pilot.posX-passenger.posX)*-Math.sin(angle)+(pilot.posZ-passenger.posZ)*Math.cos(angle);
            assertEquals("Ferryman remains ahead of the passenger",2.02,distanceForward,.0001);
            assertEquals(-.14,passenger.posY,.0001);
            assertEquals(yaw,passenger.renderYawOffset,.0001);
        }
    }
}
