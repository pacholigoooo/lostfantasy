package dev.lostfantasy.world.gensokyo;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.Balance;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.item.HakureiCharm;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.Unsafe;
import static org.junit.Assert.*;

public class GensokyoTravelTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    private EntityPlayer player(TestWorld world) {
        return new EntityPlayer(world,new GameProfile(UUID.randomUUID(),"visitor")) {
            @Override public boolean isSpectator() {return false;}
            @Override public boolean isCreative() {return false;}
            // Keep Entity's real ForgeData save/load path. Vanilla inventory data fixing needs a running server.
            @Override public void writeEntityToNBT(NBTTagCompound tag) {}
            @Override public void readEntityFromNBT(NBTTagCompound tag) {}
        };
    }
    @Test public void currentPlayerSaveAndDeathCloneKeepIndependentReturnPositions() {
        EntityPlayer first=player(new TestWorld()),second=player(new TestWorld());
        NBTTagCompound anchor=new NBTTagCompound();anchor.setLong("position",new BlockPos(-243,91,741).toLong());anchor.setFloat("yaw",-72);
        GensokyoTravel.persisted(first).setTag("lostfantasyGensokyoReturn",anchor);
        NBTTagCompound saved=first.writeToNBT(new NBTTagCompound());
        EntityPlayer rejoined=player(new TestWorld());rejoined.readFromNBT(saved);
        assertEquals(anchor,GensokyoTravel.persisted(rejoined).getCompoundTag("lostfantasyGensokyoReturn"));
        assertFalse(GensokyoTravel.persisted(second).hasKey("lostfantasyGensokyoReturn"));
        EntityPlayer respawned=player(new TestWorld());
        new GensokyoTravel().clone(new PlayerEvent.Clone(respawned,rejoined,true));
        NBTTagCompound copy=GensokyoTravel.persisted(respawned).getCompoundTag("lostfantasyGensokyoReturn");
        assertEquals(anchor,copy);copy.setFloat("yaw",90);
        assertEquals(-72,GensokyoTravel.persisted(rejoined).getCompoundTag("lostfantasyGensokyoReturn").getFloat("yaw"),0);
    }
    @Test public void onlyEmptyHandSneakInteractionWithFrontToriiPlinthReturns() {
        TestWorld world=new TestWorld();EntityPlayer p=player(world);p.dimension=Balance.gensokyoDimensionId;
        BlockPos plinth=local(10,1,71);world.blocks.put(plinth,Blocks.STONEBRICK.getDefaultState());
        p.setPosition(plinth.getX()-.5,plinth.getY(),plinth.getZ()+.5);
        assertFalse(interact(p,plinth).isCanceled());
        p.setSneaking(true);assertTrue(interact(p,plinth).isCanceled());
        p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(Items.STICK));assertFalse(interact(p,plinth).isCanceled());
        p.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);p.dimension=0;assertFalse(interact(p,plinth).isCanceled());
        p.dimension=Balance.gensokyoDimensionId;world.blocks.put(plinth,Blocks.AIR.getDefaultState());assertFalse(interact(p,plinth).isCanceled());
        world.blocks.put(plinth,Blocks.STONEBRICK.getDefaultState());p.setPosition(plinth.getX()+20,plinth.getY(),plinth.getZ());assertFalse(interact(p,plinth).isCanceled());
        assertFalse(GensokyoTravel.returnPlinth(local(10,1,45)));
        assertFalse(GensokyoTravel.returnPlinth(GensokyoWorld.arrival()));
    }
    private PlayerInteractEvent.RightClickBlock interact(EntityPlayer p,BlockPos pos) {
        PlayerInteractEvent.RightClickBlock e=new PlayerInteractEvent.RightClickBlock(p,EnumHand.MAIN_HAND,pos,EnumFacing.WEST,new net.minecraft.util.math.Vec3d(pos)) {
            @Override public boolean isCancelable() {return true;}
        };
        new GensokyoTravel().interact(e);return e;
    }
    @Test public void shrineArrivalAndBothReturnPlinthsExistInActualGeneratedChunks() {
        GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        BlockPos start=GensokyoWorld.arrival();ChunkPrimer arrival=generator.primer(start.getX()>>4,start.getZ()>>4);
        assertTrue(arrival.getBlockState(start.getX()&15,start.getY()-1,start.getZ()&15).isFullCube());
        assertSame(Blocks.AIR,arrival.getBlockState(start.getX()&15,start.getY(),start.getZ()&15).getBlock());
        assertSame(Blocks.AIR,arrival.getBlockState(start.getX()&15,start.getY()+1,start.getZ()&15).getBlock());
        for(int x:new int[]{-11,11}) {
            BlockPos pos=local(x,1,72);ChunkPrimer chunk=generator.primer(pos.getX()>>4,pos.getZ()>>4);
            assertTrue(GensokyoTravel.returnPlinth(pos));
            assertSame(Blocks.STONEBRICK,chunk.getBlockState(pos.getX()&15,pos.getY(),pos.getZ()&15).getBlock());
            BlockPos standing=pos.south();
            assertSame(Blocks.AIR,chunk.getBlockState(standing.getX()&15,standing.getY(),standing.getZ()&15).getBlock());
        }
        BlockPos sign=local(11,2,73);
        Chunk generated=new GensokyoGenerator(new TestWorld(),12345).generateChunk(sign.getX()>>4,sign.getZ()>>4);
        net.minecraft.tileentity.TileEntitySign tile=(net.minecraft.tileentity.TileEntitySign)generated.getTileEntityMap().get(sign);
        assertNotNull(tile);assertEquals("归途",tile.signText[1].getUnformattedText());
        assertEquals("空手潜行右键石座",tile.signText[2].getUnformattedText());
    }
    @Test public void exactSafeLandingWinsAndBlockedLandingCanSearchAnUnloadedAdjacentChunk() {
        LandingWorld world=new LandingWorld();BlockPos at=new BlockPos(15,64,15);
        world.blocks.put(at.down(),Blocks.STONE.getDefaultState());
        assertEquals(at,RealmPassage.findLanding(world,at));assertEquals(1,world.loadedChunks.size());
        world.blocks.put(at,Blocks.STONE.getDefaultState());world.blocks.put(at.up(),Blocks.STONE.getDefaultState());
        world.blocks.put(at.up(2),Blocks.STONE.getDefaultState()); // no false lower landing in this column
        for(int y=67;y<=77;y++)world.blocks.put(new BlockPos(15,y,15),Blocks.STONE.getDefaultState());
        BlockPos nearby=new BlockPos(16,64,15);world.blocks.put(nearby.down(),Blocks.STONE.getDefaultState());
        assertEquals(nearby,RealmPassage.findLanding(world,at));assertTrue(world.loadedChunks.contains(GensokyoAtlas.key(1,0)));
        world.blocks.clear();assertNull(RealmPassage.findLanding(world,at));
        world.getWorldBorder().setTransition(10);int loaded=world.loads;
        assertNull(RealmPassage.findLanding(world,at));assertEquals(loaded,world.loads);
    }
    @Test public void canceledOrRedirectedTransferDoesNotOverwriteLocationMotionOrCooldown() throws Exception {
        WorldServer target=allocate(WorldServer.class);Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);
        provider.set(target,new WorldProviderSurface() {@Override public long getSeed() {return 0;}});
        target.provider.setDimension(Balance.gensokyoDimensionId);
        for(int mode:new int[]{0,1,2}) {
            TransferPlayer p=allocate(TransferPlayer.class);p.world=new TestWorld();p.dimension=0;p.target=target;p.mode=mode;
            p.motionX=2;p.motionY=3;p.motionZ=4;p.fallDistance=5;p.timeUntilPortal=7;
            RecordingConnection connection=allocate(RecordingConnection.class);p.connection=connection;
            boolean success=RealmPassage.transfer(p,target,new BlockPos(4,70,8),180);
            assertEquals(mode==2,success);assertEquals(1,p.attempts);
            if(success) {assertEquals(1,connection.calls);assertEquals(4.5,connection.x,0);assertEquals(0,p.motionY,0);assertEquals(80,p.timeUntilPortal);}
            else {assertEquals(0,connection.calls);assertEquals(3,p.motionY,0);assertEquals(5,p.fallDistance,0);assertEquals(7,p.timeUntilPortal);}
        }
    }
    @Test public void charmUsesNormalHoldingAndCannotOpenTravelFromHiganOrAnotherRealm() {
        HakureiCharm charm=new HakureiCharm();assertEquals(40,charm.getMaxItemUseDuration(ItemStack.EMPTY));
        assertEquals(EnumAction.NONE,charm.getItemUseAction(ItemStack.EMPTY));
        EntityPlayer p=player(new TestWorld());p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(charm));
        for(int dimension:new int[]{Balance.gensokyoDimensionId,Balance.kasenDimensionId,-1}) {
            p.dimension=dimension;assertEquals(EnumActionResult.FAIL,charm.onItemRightClick(p.world,p,EnumHand.MAIN_HAND).getType());
        }
        p.dimension=0;assertEquals(EnumActionResult.SUCCESS,charm.onItemRightClick(p.world,p,EnumHand.MAIN_HAND).getType());
        assertEquals(1,p.getHeldItemMainhand().getCount());
    }
    private static BlockPos local(int x,int y,int z) {GensokyoAtlas s=GensokyoAtlas.HAKUREI;return new BlockPos(s.x+x,s.y+y,s.z+z);}
    private static final class LandingWorld extends TestWorld {
        final Set<Long> loadedChunks=new HashSet<>();int loads;
        @Override public Chunk getChunk(BlockPos p) {loadedChunks.add(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4));loads++;return null;}
        @Override protected boolean isChunkLoaded(int x,int z,boolean empty) {return loadedChunks.contains(GensokyoAtlas.key(x,z));}
    }
    private static <T> T allocate(Class<T> type) throws Exception {
        Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return type.cast(((Unsafe)f.get(null)).allocateInstance(type));
    }
    private static final class TransferPlayer extends EntityPlayerMP {
        WorldServer target;int mode,attempts;
        TransferPlayer() {super((MinecraftServer)null,(WorldServer)null,null,(PlayerInteractionManager)null);}
        @Override public Entity changeDimension(int dimension,net.minecraftforge.common.util.ITeleporter teleporter) {
            attempts++;if(mode>0)this.dimension=dimension;if(mode==2)world=target;return this;
        }
    }
    private static final class RecordingConnection extends NetHandlerPlayServer {
        int calls;double x;
        RecordingConnection() {super(null,null,null);}
        @Override public void setPlayerLocation(double x,double y,double z,float yaw,float pitch) {calls++;this.x=x;}
    }
}
