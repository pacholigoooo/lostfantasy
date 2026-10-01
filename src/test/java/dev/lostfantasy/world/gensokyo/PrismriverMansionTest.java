package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.block.RehearsalInstrument;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
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

public class PrismriverMansionTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.RUINED_MANSION;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void approachStageGalleriesAndAllRoomsConnectWithHeadroom() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);
        GensokyoBlueprint plan=new GensokyoBlueprint();PrismriverMansion.build(plan);assertEquals(20,plan.rooms().size());
        List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:plan.rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name);
        assertTrue(missing.toString(),missing.isEmpty());
        for(int z=2;z<=9;z++)assertTrue("stair "+z,reached.contains(local(0,13-z,z)));
        for(int x=-31;x<=31;x++)for(int z=-17;z<=17;z++) {
            assertTrue("ground floor "+x+","+z,g.at(local(x,2,z)).isFullCube());
            if(!(x>=-5 && x<=5 && z>=2 && z<=15))assertTrue("upper floor "+x+","+z,g.at(local(x,10,z)).isFullCube());
            assertTrue("ceiling "+x+","+z,g.at(local(x,18,z)).isFullCube());
        }
    }
    @Test public void furnitureIsSupportedAndReachableAndLightsAreSuspended() {
        Generated g=new Generated(12345);Set<BlockPos> reached=walk(g);int beds=0,chests=0,instruments=0,lamps=0;
        for(int x=-32;x<=32;x++)for(int z=-18;z<=27;z++)for(int y=3;y<=18;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED || s.getBlock()==Blocks.CHEST || s.getBlock() instanceof RehearsalInstrument) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("clear above "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
                if(s.getBlock()==Blocks.BED) {
                    beds++;EnumFacing f=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?f.getOpposite():f));
                    assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));
                } else if(s.getBlock()==Blocks.CHEST)chests++;
                else {instruments++;assertFalse(s.getBlock().hasTileEntity(s));assertEquals(0,s.getLightValue());}
            } else if(s.getBlock()==Blocks.GLOWSTONE && g.at(p.up()).getBlock()==Blocks.IRON_BARS) {
                lamps++;BlockPos support=p.up();while(g.at(support).getBlock()==Blocks.IRON_BARS)support=support.up();
                assertTrue("anchored chain "+p,g.at(support).getMaterial().blocksMovement());
            }
        }
        assertEquals(8,beds);assertEquals(17,chests);assertEquals(6,instruments);assertEquals(14,lamps);
        assertSame(ModBlocks.VIOLIN_STAND,g.at(local(-27,4,-14)).getBlock());
        assertSame(ModBlocks.TRUMPET_STAND,g.at(local(-13,4,-14)).getBlock());
        assertSame(ModBlocks.REHEARSAL_KEYBOARD,g.at(local(-20,4,-15)).getBlock());
    }
    @Test public void musicMaterialsAndOneRehearsalBookSurviveChestSaving() {
        LootTableManager manager=new LootTableManager(null);Set<BlockPos> seen=new HashSet<>();int noteChests=0;
        TestWorld world=new TestWorld();world.loaded=false;GensokyoGenerator generator=new GensokyoGenerator(world,12345);
        for(int cx=(SITE.x-32)>>4;cx<=(SITE.x+32)>>4;cx++)for(int cz=(SITE.z-18)>>4;cz<=(SITE.z+27)>>4;cz++) {
            Chunk chunk=generator.generateChunk(cx,cz);
            for(Map.Entry<BlockPos,net.minecraft.tileentity.TileEntity> e:chunk.getTileEntityMap().entrySet())if(e.getValue() instanceof TileEntityChest) {
                assertTrue(seen.add(e.getKey()));TileEntityChest chest=(TileEntityChest)e.getValue();
                NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());ResourceLocation id=new ResourceLocation(saved.getString("LootTable"));
                TileEntityChest loaded=new TileEntityChest();loaded.readFromNBT(saved);assertEquals(saved,loaded.writeToNBT(new NBTTagCompound()));
                LootTable table=manager.getLootTableFromLocation(id);assertNotSame(id.toString(),LootTable.EMPTY_LOOT_TABLE,table);
                List<ItemStack> items=table.generateLootForPools(new Random(8),new LootContext(0,null,manager,null,null,null));assertFalse(items.isEmpty());
                if(id.getPath().equals("chests/prismriver_rehearsal")) {
                    noteChests++;ItemStack book=items.stream().filter(s->s.getItem()==Items.WRITTEN_BOOK).findFirst().orElseThrow(AssertionError::new);
                    assertEquals("练习簿",book.getTagCompound().getString("title"));
                    assertTrue(ITextComponent.Serializer.jsonToComponent(book.getTagCompound().getTagList("pages",8).getStringTagAt(1)).getUnformattedText().contains("右键"));
                }
            }
        }
        assertEquals(17,seen.size());assertEquals(1,noteChests);
    }
    @Test public void constructionStaysInsideItsChunksAndDoesNotDependOnSeed() {
        Generated first=new Generated(12345),second=new Generated(0);List<BlockPos> points=new ArrayList<>();
        for(int x=-33;x<=33;x+=2)for(int z=-19;z<=29;z+=2)for(int y=0;y<=32;y++)points.add(local(x,y,z));
        Map<BlockPos,IBlockState> expected=new HashMap<>();for(BlockPos p:points)expected.put(p,first.at(p));
        Collections.reverse(points);for(BlockPos p:points)assertEquals("seed/order "+p,expected.get(p),second.at(p));
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(0,1,58);
        assertTrue("arrival",g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos q=p.offset(f).up(dy);
                if(q.getX()<SITE.x-37 || q.getX()>SITE.x+39 || q.getZ()<SITE.z-20 || q.getZ()>SITE.z+60 || q.getY()<SITE.y || q.getY()>SITE.y+15 || reached.contains(q))continue;
                if(!g.walkable(q) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(q.up(2)))continue;
                reached.add(q);queue.add(q);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {return at(p).getMaterial().blocksMovement() && at(p).getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && !at(p).getMaterial().isLiquid();}
    }
}
