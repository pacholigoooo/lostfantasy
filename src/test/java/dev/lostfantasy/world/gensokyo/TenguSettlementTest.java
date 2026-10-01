package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TenguSettlementTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.TENGU;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void everyRoomAndTerracedStreetCanBeReachedOnFoot() {
        Generated g=new Generated();Set<BlockPos> reached=walk(g);List<String> missing=new ArrayList<>();int rooms=0;
        for(GensokyoBlueprint.Room r:GensokyoStructures.create().rooms())if(r.name.startsWith(SITE.title+"·")) {
            rooms++;if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name+" "+new BlockPos(r.x-SITE.x,r.y-SITE.y,r.z-SITE.z));
        }
        assertTrue(missing.toString(),missing.isEmpty());assertTrue("distinct occupied rooms and routes: "+rooms,rooms>=TenguLayout.HOMES.length*4+TenguLayout.PUBLIC.length*2);
        System.out.println("Tengu room and route checkpoints: "+rooms);
        for(TenguLayout.Bridge b:TenguLayout.BRIDGES)for(int x=b.x1;x<=b.x2;x++)
            assertTrue("bridge "+b.name+" at "+x,reached.contains(local(x,b.floor(x)-SITE.y+1,b.z)));
        for(GensokyoBlueprint.Room r:GensokyoStructures.create().rooms())if(r.name.startsWith(SITE.title+"·"))
            assertNotEquals(Material.WATER,g.at(new BlockPos(r.x,r.y,r.z)).getMaterial());
    }
    @Test public void bedsChestsAndWorkFurnitureAreSupportedAndUsable() {
        Generated g=new Generated();Set<BlockPos> reached=walk(g);int beds=0,chests=0,presses=0,cameras=0,lanterns=0;
        for(TenguLayout.Plot plot:TenguLayout.all())for(int x=-plot.w-2;x<=plot.w+2;x++)
                for(int z=-plot.d-2;z<=plot.d+6;z++)for(int y=1;y<=plot.floors*6+5;y++) {
            BlockPos p=plot.local(x,y,z).add(SITE.x,0,SITE.z);IBlockState s=g.at(p);Block b=s.getBlock();
            if(b==Blocks.BED) {
                beds++;assertTrue("bed support "+localLabel(p),g.at(p.down()).isFullCube());assertFalse("bed headroom "+localLabel(p),g.solid(p.up()));
                EnumFacing facing=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?facing.getOpposite():facing));
                assertSame("bed pair "+localLabel(p),Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));
                assertTrue("bed access "+localLabel(p),beside(p,reached));
            } else if(b==Blocks.CHEST) {
                chests++;assertTrue("chest support "+localLabel(p),g.at(p.down()).isFullCube());assertFalse("chest lid "+localLabel(p),g.at(p.up()).isFullCube());
                assertTrue("chest access "+localLabel(p),beside(p,reached));
            } else if(b==ModBlocks.TENGU_PRESS || b==ModBlocks.TENGU_CAMERA) {
                if(b==ModBlocks.TENGU_PRESS)presses++;else cameras++;
                assertTrue("equipment support "+localLabel(p),g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));
                boolean accessible=false;
                // Presses sit on work islands; the outer edge can be reached without climbing the machine.
                for(EnumFacing side:EnumFacing.HORIZONTALS)for(int n=1;n<=3;n++)accessible|=reached.contains(p.offset(side,n).down()) || reached.contains(p.offset(side,n));
                assertTrue("equipment access "+localLabel(p),accessible);
            } else if(b==ModBlocks.RED_LANTERN) {lanterns++;assertTrue("lantern support "+localLabel(p),g.solid(p.up()));}
        }
        assertEquals("two beds per home plus inn and patrol beds, two halves each",TenguLayout.HOMES.length*4+20,beds);
        assertTrue("storage count "+chests,chests>=TenguLayout.HOMES.length*3);
        assertEquals(4,presses);assertEquals(5+Arrays.stream(TenguLayout.HOMES).filter(p->p.floors==3).count(),cameras);assertTrue(lanterns>=40);
        for(TenguLayout.Plot p:TenguLayout.all())for(int x=-p.w+1;x<p.w;x++)for(int z=-p.d+1;z<p.d;z++)
            assertTrue("ceiling "+p.name,g.at(p.local(x,p.floors*6,z).add(SITE.x,0,SITE.z)).isFullCube());
        System.out.println("Tengu furniture: "+beds/2+" beds, "+chests+" chests, "+lanterns+" hanging lanterns");
    }
    @Test public void staticEquipmentRotatesAndTerracesAreIndependentOfChunkOrder() {
        for(Block block:new Block[]{ModBlocks.TENGU_PRESS,ModBlocks.TENGU_CAMERA})for(EnumFacing face:EnumFacing.HORIZONTALS) {
            IBlockState s=block.getDefaultState().withProperty(BlockHorizontal.FACING,face);
            assertEquals(s,block.getStateFromMeta(block.getMetaFromState(s)));
            assertEquals(face.rotateY(),s.withRotation(Rotation.CLOCKWISE_90).getValue(BlockHorizontal.FACING));
            assertFalse(s.isOpaqueCube());assertFalse(block.hasTileEntity(s));assertEquals(0,s.getLightValue());
        }
        Generated one=new Generated(),two=new Generated();
        for(int[] p:new int[][]{{-230,20},{227,20},{20,-137},{-55,117},{315,275},{122,306}}) {
            int cx=(SITE.x+p[0])>>4,cz=(SITE.z+p[1])>>4;ChunkPrimer expected=one.chunk(cx,cz);two.chunk(cx+1,cz-1);ChunkPrimer actual=two.chunk(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=96;y<=245;y++)assertEquals(expected.getBlockState(x,y,z),actual.getBlockState(x,y,z));
        }
    }
    @Test public void printingAndPatrolSuppliesUseRealSavedLootTables() {
        GensokyoGenerator generator=new GensokyoGenerator(new TestWorld(),12345);LootTableManager manager=new LootTableManager(null);
        int[][] positions={{TenguLayout.PRINT.x-29,TenguLayout.PRINT.y-SITE.y+1,TenguLayout.PRINT.z-17},
                {TenguLayout.PATROL.x-12,TenguLayout.PATROL.y-SITE.y+1,TenguLayout.PATROL.z}};String[] tables={"tengu_printing","tengu_patrol"};
        for(int i=0;i<positions.length;i++) {
            int[] c=positions[i];BlockPos p=local(c[0],c[1],c[2]);Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest tile=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(tile);
            NBTTagCompound nbt=tile.writeToNBT(new NBTTagCompound());ResourceLocation key=new ResourceLocation("lostfantasy","chests/"+tables[i]);assertEquals(key.toString(),nbt.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
            LootTable loot=manager.getLootTableFromLocation(key);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            assertFalse(loot.generateLootForPools(new Random(17),new LootContext(0,null,manager,null,null,null)).isEmpty());
        }
    }
    private static Set<BlockPos> walk(Generated g) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=local(0,1,330);
        assertTrue("entrance",g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-395 || n.getX()>SITE.x+395 || n.getZ()<SITE.z-290 || n.getZ()>SITE.z+375
                        || n.getY()<110 || n.getY()>254 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                queue.add(n);reached.add(n);break;
            }
        }
        return reached;
    }
    private static boolean beside(BlockPos p,Set<BlockPos> reached) {
        for(EnumFacing f:EnumFacing.HORIZONTALS)if(reached.contains(p.offset(f)))return true;return false;
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static String localLabel(BlockPos p) {return new BlockPos(p.getX()-SITE.x,p.getY()-SITE.y,p.getZ()-SITE.z).toString();}
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
