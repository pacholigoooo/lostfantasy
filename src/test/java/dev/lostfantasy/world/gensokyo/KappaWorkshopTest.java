package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class KappaWorkshopTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.KAPPA;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void workshopHomesAndBothBanksHaveDryWalkingRoutes() {
        Generated g=new Generated();Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> q=new ArrayDeque<>();
        BlockPos start=local(0,1,119);q.add(start);reached.add(start);
        while(!q.isEmpty()) {
            BlockPos p=q.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-151 || n.getX()>SITE.x+150 || n.getZ()<SITE.z-95 || n.getZ()>SITE.z+120 || n.getY()<SITE.y+1 || n.getY()>SITE.y+25 || reached.contains(n))continue;
                if(!g.solid(n.down()) || g.solid(n) || g.solid(n.up()))continue;
                if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                if(g.at(n).getMaterial()==net.minecraft.block.material.Material.WATER)continue;
                q.add(n);reached.add(n);break;
            }
        }
        List<String> missing=new ArrayList<>();int rooms=0;
        for(GensokyoBlueprint.Room r:GensokyoStructures.create().rooms())if(r.name.startsWith(SITE.title+"·")) {
            rooms++;if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name+" "+new BlockPos(r.x-SITE.x,r.y-SITE.y,r.z-SITE.z));
        }
        assertTrue(missing.toString(),missing.isEmpty());assertEquals(56,rooms);
    }
    @Test public void bedsStorageLeversAndCeilingsHaveCorrectSupportAndClearance() {
        Generated g=new Generated();int beds=0,chests=0,levers=0,anvils=0;
        for(int x=-133;x<=80;x++)for(int z=-91;z<=108;z++)for(int y=1;y<=25;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED) {
                beds++;assertTrue("bed support "+p,g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));
                EnumFacing facing=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?facing.getOpposite():facing));
                assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));
            } else if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("chest support "+p,g.at(p.down()).isFullCube());assertFalse("chest lid "+p,g.at(p.up()).isFullCube());
            } else if(s.getBlock()==Blocks.LEVER) {
                levers++;assertEquals(BlockLever.EnumOrientation.UP_X,s.getValue(BlockLever.FACING));assertTrue("lever support "+p,g.at(p.down()).isFullCube());
            } else if(s.getBlock()==Blocks.ANVIL) {anvils++;assertTrue(g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));}
        }
        assertEquals(24,beds);assertEquals(39,chests);assertEquals("control levers",4,levers);assertEquals(3,anvils);
        for(int x=-58;x<=12;x++)for(int z=-51;z<=-8;z++)assertTrue("upper roof "+x+","+z,g.at(local(x,19,z)).isFullCube());
    }
    @Test public void tributaryAndTestingPoolConnectWithoutDrowningTheDecks() {
        Generated g=new Generated();int[][] points={{264,-2386},{264,-2240},{264,-2110},{207,-2010},
                {166,-1950},{177,-1910},{85,-1860},{86,-1828},{-36,-1780},{-57,-1740},
                {-164,-1700},{-185,-1660},{-280,-1620},{-360,-1568}};
        for(long seed:new long[]{0,17,12345}) {
            GensokyoTerrain terrain=new GensokyoTerrain(seed);int previous=139;
            for(int segment=1;segment<points.length;segment++) {
                int[] a=points[segment-1],b=points[segment];int count=(int)Math.ceil(Math.hypot(b[0]-a[0],b[1]-a[1]));
                for(int i=0;i<=count;i++) {
                    int x=(int)Math.round(a[0]+(b[0]-a[0])*i/(double)count),z=(int)Math.round(a[1]+(b[1]-a[1])*i/(double)count);
                    GensokyoTerrain.Column c=terrain.column(x,z);assertTrue("dry tributary "+x+","+z,c.wet());
                    assertTrue("uphill flow "+previous+" -> "+c.water,c.water<=previous);previous=c.water;
                }
            }
            assertEquals(108,previous);
        }
        // The road bridge has pilings. Check a real two-block-wide route around them,
        // rather than requiring the arbitrary centre column to contain only water.
        GensokyoTerrain terrain=new GensokyoTerrain(12345);
        for(int z=-62;z<=61;z++) {
            BlockPos p=local(32,-3,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.WATER)continue;
            assertSame("only supported bridge pilings in the channel "+p,Blocks.LOG,s.getBlock());
            GensokyoTerrain.Column c=terrain.column(p.getX(),p.getZ());assertTrue(c.path());
            assertSame("bridge above its piling",Blocks.PLANKS,g.at(new BlockPos(p.getX(),c.roadY,p.getZ())).getBlock());
        }
        Set<BlockPos> water=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=local(32,-3,-62),end=local(32,-3,60);assertTrue(boatSpace(g,start));water.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();for(EnumFacing f:EnumFacing.HORIZONTALS) {
                BlockPos q=p.offset(f);
                if(q.getX()<SITE.x+23 || q.getX()>SITE.x+40 || q.getZ()<SITE.z-62 || q.getZ()>SITE.z+60 || water.contains(q) || !boatSpace(g,q))continue;
                water.add(q);queue.add(q);
            }
        }
        assertTrue("continuous two-block-wide water passage beneath the bridges",water.contains(end));
        for(int x=-55;x<=32;x++)assertSame("pool outlet "+x,Blocks.WATER,g.at(local(x,-3,22)).getBlock());
        for(int x=24;x<=40;x++) {
            assertTrue(g.at(local(x,0,-1)).isFullCube());assertFalse(g.solid(local(x,1,-1)));assertFalse(g.solid(local(x,2,-1)));
        }
    }
    @Test public void pipeAxesRotateAndChunksKeepTheSameStatesInDifferentOrders() {
        IBlockState base=ModBlocks.KAPPA_PIPE.getDefaultState();
        for(EnumFacing.Axis axis:EnumFacing.Axis.values()) {
            IBlockState state=base.withProperty(BlockRotatedPillar.AXIS,axis);
            assertEquals(state,ModBlocks.KAPPA_PIPE.getStateFromMeta(ModBlocks.KAPPA_PIPE.getMetaFromState(state)));
            net.minecraft.util.math.AxisAlignedBB bounds=state.getBoundingBox(null,BlockPos.ORIGIN);
            assertEquals(1,axis==EnumFacing.Axis.X?bounds.maxX-bounds.minX:axis==EnumFacing.Axis.Y?bounds.maxY-bounds.minY:bounds.maxZ-bounds.minZ,0);
            assertFalse(state.isOpaqueCube());
        }
        assertEquals(EnumFacing.Axis.Z,base.withProperty(BlockRotatedPillar.AXIS,EnumFacing.Axis.X).withRotation(Rotation.CLOCKWISE_90).getValue(BlockRotatedPillar.AXIS));
        Generated one=new Generated(),two=new Generated();int pipes=0;
        for(int cx=(SITE.x-66)>>4;cx<=(SITE.x+68)>>4;cx++)for(int cz=(SITE.z-29)>>4;cz<=(SITE.z+8)>>4;cz++) {
            ChunkPrimer first=one.chunk(cx,cz);two.chunk(cx+1,cz-1);ChunkPrimer second=two.chunk(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=SITE.y-3;y<SITE.y+22;y++) {
                IBlockState s=first.getBlockState(x,y,z);assertEquals(s,second.getBlockState(x,y,z));if(s.getBlock()==ModBlocks.KAPPA_PIPE)pipes++;
            }
        }
        assertTrue(pipes>180);
    }
    @Test public void workshopMaterialsAndToolsUseSavedLootContainers() {
        TestWorld world=new TestWorld();GensokyoGenerator generator=new GensokyoGenerator(world,12345);LootTableManager manager=new LootTableManager(null);
        int[][] boxes={{-55,1,-32},{-54,1,-26}};String[] names={"kappa_tools","kappa_parts"};
        for(int i=0;i<boxes.length;i++) {
            int[] c=boxes[i];BlockPos p=local(c[0],c[1],c[2]);Chunk chunk=generator.generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest tile=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(tile);
            NBTTagCompound nbt=tile.writeToNBT(new NBTTagCompound());ResourceLocation key=new ResourceLocation("lostfantasy","chests/"+names[i]);assertEquals(key.toString(),nbt.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
            LootTable loot=manager.getLootTableFromLocation(key);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            assertFalse(loot.generateLootForPools(new Random(17),new LootContext(0,null,manager,null,null,null)).isEmpty());
        }
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static boolean boatSpace(Generated g,BlockPos p) {
        for(int dx=0;dx<=1;dx++)for(int dz=0;dz<=1;dz++) {
            BlockPos q=p.add(dx,0,dz);
            if(g.at(q).getBlock()!=Blocks.WATER || g.solid(q.up()) || g.solid(q.up(2)))return false;
        }
        return true;
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.LEVER;}
    }
}
