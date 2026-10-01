package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.TestWorld;
import dev.lostfantasy.entity.EntityCablecar;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.Unsafe;
import static org.junit.Assert.*;

public class RopewayRideTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void dismountRelocatesAfterVanillaAndDoesNotUndoOtherTravel() throws Exception {
        for(int mode=0;mode<3;mode++) {
            Landing world=new Landing();Visitor p=visitor(world);EntityCablecar car=new EntityCablecar(world);car.begin(p,0);p.mount=car;
            BlockPos at=RopewayPath.landing(0);world.blocks.put(at.down(),Blocks.STONE.getDefaultState());RopewayRide ride=new RopewayRide();
            EntityMountEvent event=exitEvent(p,car,world);ride.leave(event);assertFalse(event.isCanceled());assertEquals(0,((Connection)p.connection).calls);
            p.mount=null;p.posY+=3;
            if(mode==1)p.world=new Landing();if(mode==2)p.posX+=100;
            ride.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));Connection c=(Connection)p.connection;
            assertEquals(mode==0?1:0,c.calls);if(mode==0) {assertEquals(at.getY(),c.y,0);assertEquals(0,p.fallDistance,0);}
            ride.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));assertEquals(mode==0?1:0,c.calls);
        }
    }
    @Test public void blockedStationKeepsPassengerMountedAndLogoutLeavesVanillaSaveAlone() throws Exception {
        Landing world=new Landing();Visitor p=visitor(world);EntityCablecar car=new EntityCablecar(world);car.begin(p,0);p.mount=car;RopewayRide ride=new RopewayRide();
        EntityMountEvent event=exitEvent(p,car,world);ride.leave(event);assertTrue(event.isCanceled());
        world.blocks.put(RopewayPath.landing(0).down(),Blocks.STONE.getDefaultState());
        ride.logout(new PlayerEvent.PlayerLoggedOutEvent(p));assertTrue(car.loggingOut);
        event=exitEvent(p,car,world);ride.leave(event);assertFalse(event.isCanceled());p.mount=null;
        ride.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));assertEquals(0,((Connection)p.connection).calls);
    }
    private static EntityMountEvent exitEvent(Visitor p,EntityCablecar car,Landing world) {
        // Forge's Event transformer normally installs this from the class's @Cancelable annotation.
        return new EntityMountEvent(p,car,world,false) {@Override public boolean isCancelable() {return true;}};
    }
    private static Visitor visitor(Landing world) throws Exception {
        Visitor p=allocate(Visitor.class);p.world=world;p.id=UUID.randomUUID();p.alive=true;p.connection=allocate(Connection.class);p.fallDistance=12;return p;
    }
    private static <T> T allocate(Class<T> type) throws Exception {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return type.cast(((Unsafe)f.get(null)).allocateInstance(type));}
    private static final class Landing extends TestWorld {@Override public Chunk getChunk(BlockPos p) {return null;}}
    private static final class Visitor extends EntityPlayerMP {
        UUID id;Entity mount;boolean alive;
        Visitor() {super(null,null,null,null);}
        @Override public UUID getUniqueID() {return id;}
        @Override public Entity getRidingEntity() {return mount;}
        @Override public boolean isRiding() {return mount!=null;}
        @Override public boolean isEntityAlive() {return alive;}
    }
    private static final class Connection extends NetHandlerPlayServer {
        int calls;double y;Connection() {super(null,null,null);}
        @Override public void setPlayerLocation(double x,double y,double z,float yaw,float pitch) {calls++;this.y=y;}
    }
}
