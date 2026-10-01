package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChireidenTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void allRoomsGalleriesAndUndercroftConnectToOldCapitalOnFoot() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);
        List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room room:Chireiden.create().rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x,room.y,room.z));
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(Chireiden.create().rooms().size()>=56);
        assertTrue(reached.contains(Chireiden.local(0,4,-18)));
        assertTrue(reached.contains(Chireiden.local(0,28,-52)));
        assertTrue(reached.contains(Chireiden.local(0,-10,64)));
    }
    @Test public void bothBedroomsStorageAndWorkplacesRemainUsable() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);int beds=0,chests=0,work=0;
        for(int x=-87;x<=87;x++)for(int z=-58;z<=58;z++)for(int y=-10;y<=38;y++) {
            BlockPos p=Chireiden.local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()!=Blocks.BED && s.getBlock()!=Blocks.CHEST && s.getBlock()!=Blocks.CRAFTING_TABLE && s.getBlock()!=Blocks.FURNACE)continue;
            assertTrue("support "+p+" "+s,g.at(p.down()).isFullCube());assertFalse("headroom "+p,g.solid(p.up()));
            boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p+" "+s,access);
            if(s.getBlock()==Blocks.BED) {
                beds++;EnumFacing f=s.getValue(BlockBed.FACING);IBlockState other=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?f.getOpposite():f));
                assertSame(Blocks.BED,other.getBlock());assertNotEquals(s.getValue(BlockBed.PART),other.getValue(BlockBed.PART));
            }else if(s.getBlock()==Blocks.CHEST)chests++;else work++;
        }
        assertEquals(4,beds);assertTrue("chests "+chests,chests>=100);assertTrue("workplaces "+work,work>=25);
        System.out.println("Chireiden furniture: bed halves="+beds+", chests="+chests+", workplaces="+work);
    }
    @Test public void bathsAreSealedLightsAreHungAndPalaceFitsBelowRock() {
        Generated g=new Generated(12345);int water=0,lamps=0;
        for(int x=-87;x<=87;x++)for(int z=-58;z<=58;z++)for(int y=4;y<=38;y++) {
            BlockPos p=Chireiden.local(x,y,z);IBlockState s=g.at(p);
            if(s.getMaterial()==Material.WATER) {
                water++;assertTrue(g.at(p.down()).isFullCube());assertSame("water surface "+p,Blocks.AIR,g.at(p.up()).getBlock());
                for(EnumFacing f:EnumFacing.HORIZONTALS)assertTrue("water wall "+p,g.at(p.offset(f)).isFullCube() || g.at(p.offset(f)).getMaterial()==Material.WATER);
            }
            if(s.getBlock()==ModBlocks.LIBRARY_LAMP) {
                lamps++;assertSame("hanging lamp "+p,Blocks.IRON_BARS,g.at(p.up()).getBlock());
                if(Math.abs(x)>32)assertTrue("ceiling "+p,g.at(p.up(4)).isFullCube());
            }
        }
        assertEquals(120,water);assertTrue(lamps>=170);
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Generated other=new Generated(seed);
            for(int x:new int[]{-96,0,96})for(int z:new int[]{-76,0,66}) {
                assertTrue("rock roof",other.at(Chireiden.local(x,155,z)).isFullCube());
                assertSame(Blocks.BEDROCK,other.at(new BlockPos(OldHellWorld.ORIGIN.x+x,255,OldHellWorld.ORIGIN.z+Chireiden.Z+z)).getBlock());
            }
            assertTrue("road",other.walkable(OldHellWorld.local(0,1,320)));
            assertTrue("front",other.walkable(Chireiden.local(0,4,-73)));
            assertSame(Blocks.AIR,other.at(Chireiden.local(0,78,0)).getBlock());
        }
        assertSame(Blocks.LAVA,g.at(Chireiden.local(0,-39,85)).getBlock());
        assertSame(Blocks.IRON_BARS,g.at(Chireiden.local(0,-10,72)).getBlock());
        for(int y=42;y<=54;y++)for(int z=-68;z<=-60;z++)assertSame("rose window sight line",Blocks.AIR,g.at(Chireiden.local(0,y,z)).getBlock());
    }
    @Test public void lootAndOrdinaryPetsUseNormalChunkSaving() {
        TestWorld world=new TestWorld();OldHellGenerator generator=new OldHellGenerator(world,12345);LootTableManager manager=new LootTableManager(null);
        for(String name:new String[]{"palace_books","palace_household","palace_pet_care"}) {
            LootTable table=manager.getLootTableFromLocation(new ResourceLocation("lostfantasy","chests/"+name));assertNotSame(LootTable.EMPTY_LOOT_TABLE,table);
            assertFalse(table.generateLootForPools(new Random(18),new LootContext(0,null,manager,null,null,null)).isEmpty());
        }
        BlockPos p=Chireiden.local(-67,4,-53);Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
        TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);
        NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());assertEquals("lostfantasy:chests/palace_pet_care",saved.getString("LootTable"));
        TileEntityChest restored=new TileEntityChest();restored.readFromNBT(saved);assertEquals(saved,restored.writeToNBT(new NBTTagCompound()));
        restored.setLootTable(null,0);restored.setInventorySlotContents(4,new ItemStack(Items.FISH,3));saved=restored.writeToNBT(new NBTTagCompound());
        TileEntityChest reopened=new TileEntityChest();reopened.readFromNBT(saved);assertEquals(3,reopened.getStackInSlot(4).getCount());
        Set<Long> chunks=new HashSet<>();for(int[] xyz:ChireidenPets.POSITIONS){BlockPos q=Chireiden.local(xyz[0],xyz[1],xyz[2]);chunks.add(GensokyoAtlas.key(q.getX()>>4,q.getZ()>>4));}
        int pets=0;Set<UUID> ids=new HashSet<>();
        for(long key:chunks) {
            Chunk c=generator.generateChunk((int)(key>>32),(int)key);
            for(Iterable<Entity> list:c.getEntityLists())for(Entity e:list) {
                assertTrue(e instanceof EntityOcelot || e instanceof EntityRabbit);pets++;assertTrue(ids.add(e.getUniqueID()));
                EntityLiving animal=(EntityLiving)e;assertTrue(animal.isNoDespawnRequired());assertTrue(c.getBlockState(e.getPosition().down()).isFullCube());
                EntityLiving loaded=e instanceof EntityOcelot?new EntityOcelot(world):new EntityRabbit(world);
                loaded.readFromNBT(e.writeToNBT(new NBTTagCompound()));assertEquals(e.getUniqueID(),loaded.getUniqueID());assertTrue(loaded.isNoDespawnRequired());
            }
        }
        assertEquals(6,pets);
    }
    @Test public void palaceChunkSlicesDoNotDependOnGenerationOrder() {
        OldHellGenerator g=new OldHellGenerator(null,12345);
        for(int[] xz:new int[][]{{-80,-40},{0,0},{80,40},{0,85}}) {
            BlockPos p=Chireiden.local(xz[0],0,xz[1]);int cx=p.getX()>>4,cz=p.getZ()>>4;
            ChunkPrimer first=g.primer(cx,cz);g.primer(cx-1,cz+1);ChunkPrimer second=g.primer(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(first.getBlockState(x,y,z),second.getBlockState(x,y,z));
        }
    }
    private static Set<BlockPos> walk(Generated g) {
        BlockPos start=OldHellWorld.local(0,1,264);Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();assertTrue("start",g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);int x=n.getX()-OldHellWorld.ORIGIN.x,z=n.getZ()-OldHellWorld.ORIGIN.z,y=n.getY()-OldHellWorld.ORIGIN.y;
                if(x<-102 || x>102 || z<264 || z>Chireiden.Z+73 || y<-11 || y>39 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        private final OldHellGenerator generator;private final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new OldHellGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER && at(p).getMaterial()!=Material.LAVA;}
    }
}
