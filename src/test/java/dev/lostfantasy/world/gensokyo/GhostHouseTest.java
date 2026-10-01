package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.entity.EntityHouseSpirit;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GhostHouseTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.GHOST_HOUSE;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void hallGalleryStorageAndFormerBedroomsHaveWalkingRoutes() {
        Generated g=new Generated();Set<BlockPos> reached=walk(g,local(0,1,38));GensokyoBlueprint plan=new GensokyoBlueprint();GhostHouse.build(plan);
        assertEquals(14,plan.rooms().size());List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:plan.rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name);
        assertTrue(missing.toString(),missing.isEmpty());
        for(int z=2;z<=8;z++)assertTrue("stair "+z,reached.contains(local(0,12-z,z)));
        for(int x=-23;x<=23;x++)for(int z=-19;z<=17;z++) {
            assertTrue(g.at(local(x,2,z)).isFullCube());
            if(x>=-7 && x<=7 && z>=2 && z<=10)continue;
            assertTrue("upper floor "+x+","+z,g.at(local(x,9,z)).isFullCube());
        }
        for(int x=-23;x<=23;x++)for(int z=-19;z<=17;z++) {
            if(x>=15 && x<=18 && z>=-15 && z<=-12)continue;
            assertTrue("ceiling "+x+","+z,g.at(local(x,16,z)).isFullCube());
        }
    }
    @Test public void racksBedsLightsAndWindowBoardsHaveSupportAndClearances() {
        Generated g=new Generated();Set<BlockPos> reached=walk(g,local(0,1,38));int beds=0,chests=0,lamps=0;
        for(int x=-24;x<=24;x++)for(int z=-20;z<=18;z++)for(int y=3;y<=16;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED || s.getBlock()==Blocks.CHEST) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("headroom "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
                if(s.getBlock()==Blocks.CHEST)chests++;
                else {beds++;EnumFacing f=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?f.getOpposite():f));assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));}
            } else if(s.getBlock()==ModBlocks.RED_LANTERN) {lamps++;assertTrue("suspension "+p,g.at(p.up()).getMaterial().blocksMovement());}
        }
        assertEquals(4,beds);assertEquals(16,chests);assertEquals(9,lamps);
        for(int[] p:new int[][]{{-19,5,18},{-15,5,18},{24,12,5},{24,12,9}})assertTrue(g.at(local(p[0],p[1],p[2])).isFullCube());
    }
    @Test public void initialSpiritsBelongToThreeSourceChunksAndKeepTheirAnchors() {
        TestWorld world=new TestWorld();GensokyoGenerator generator=new GensokyoGenerator(world,12345);Set<BlockPos> anchors=new HashSet<>();int spirits=0;
        for(int cx=(SITE.x-32)>>4;cx<=(SITE.x+32)>>4;cx++)for(int cz=(SITE.z-24)>>4;cz<=(SITE.z+24)>>4;cz++) {
            Chunk chunk=generator.generateChunk(cx,cz);
            for(net.minecraft.util.ClassInheritanceMultiMap<Entity> list:chunk.getEntityLists())for(Entity e:list) {
                assertTrue(e instanceof EntityHouseSpirit);spirits++;
                assertEquals(cx,(int)Math.floor(e.posX)>>4);assertEquals(cz,(int)Math.floor(e.posZ)>>4);
                NBTTagCompound saved=e.writeToNBT(new NBTTagCompound());assertTrue(saved.getBoolean("Anchored"));
                anchors.add(new BlockPos(saved.getDouble("HomeX"),saved.getDouble("HomeY"),saved.getDouble("HomeZ")));
                EntityHouseSpirit loaded=new EntityHouseSpirit(world);loaded.readFromNBT(saved);
                assertEquals(saved.getDouble("HomeX"),loaded.writeToNBT(new NBTTagCompound()).getDouble("HomeX"),0);
            }
        }
        assertEquals(3,spirits);assertEquals(3,anchors.size());
        for(int[] p:GhostHouse.SPIRITS)assertTrue(anchors.contains(local(p[0],p[1],p[2])));
    }
    @Test public void crateContentsAndTheTransportNoteUseSavedLootTables() {
        int[][] positions={{-11,3,15},{-21,3,-18},{-15,3,-18}};String[] names={"ghost_storehouse","kappa_tools","kappa_parts"};LootTableManager manager=new LootTableManager(null);
        for(int i=0;i<names.length;i++) {
            BlockPos p=local(positions[i][0],positions[i][1],positions[i][2]);Chunk c=new GensokyoGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)c.getTileEntityMap().get(p);assertNotNull(chest);NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());
            ResourceLocation id=new ResourceLocation("lostfantasy","chests/"+names[i]);assertEquals(id.toString(),saved.getString("LootTable"));
            TileEntityChest loaded=new TileEntityChest();loaded.readFromNBT(saved);assertEquals(saved,loaded.writeToNBT(new NBTTagCompound()));
            LootTable loot=manager.getLootTableFromLocation(id);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            List<ItemStack> items=loot.generateLootForPools(new Random(1),new LootContext(0,null,manager,null,null,null));assertFalse(items.isEmpty());
            if(i==0) {
                ItemStack book=items.stream().filter(s->s.getItem()==Items.WRITTEN_BOOK).findFirst().orElseThrow(AssertionError::new);
                assertEquals("收存清单",book.getTagCompound().getString("title"));
                ITextComponent page=ITextComponent.Serializer.jsonToComponent(book.getTagCompound().getTagList("pages",8).getStringTagAt(0));assertTrue(page.getUnformattedText().contains("置行堀"));
            }
        }
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static Set<BlockPos> walk(Generated g,BlockPos start) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();assertTrue("start "+start,g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-30 || n.getX()>SITE.x+34 || n.getZ()<SITE.z-22 || n.getZ()>SITE.z+40 || n.getY()<SITE.y || n.getY()>SITE.y+14 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        private final GensokyoGenerator generator=new GensokyoGenerator(null,12345);private final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
