package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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

public class AquaticMarketTest {
    private static Generated scene;private static Set<BlockPos> reached;
    @BeforeClass public static void bootstrap() {
        GensokyoTestBlocks.register();
        for(net.minecraft.item.Item item:new net.minecraft.item.Item[]{dev.lostfantasy.ModItems.CUCUMBER,dev.lostfantasy.ModItems.CUCUMBER_SEEDS})
            if(!net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.containsKey(item.getRegistryName()))net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);
    }
    private static void walk() {
        if(scene!=null)return;scene=new Generated(12345);reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        BlockPos start=AquaticMarket.local(0,1,47);assertTrue(scene.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);int x=n.getX()-AquaticMarket.SITE.x,z=n.getZ()-AquaticMarket.SITE.z,y=n.getY()-AquaticMarket.SITE.y;
                if(Math.abs(x)>59 || z<-43 || z>49 || y<0 || y>18 || reached.contains(n))continue;
                if(!scene.walkable(n) || dy>0 && scene.solid(p.up(2)) || dy<0 && scene.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
    }
    @Test public void allStallsStoresAndBedsConnectWithoutSwimmingOrFlying() {
        walk();GensokyoBlueprint p=new GensokyoBlueprint();AquaticMarket.build(p);p.seal();List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:p.rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name);
        assertTrue(missing.toString(),missing.isEmpty());int beds=0,chests=0,work=0;
        for(int x=-56;x<=56;x++)for(int z=-41;z<=40;z++)for(int y=3;y<=16;y++) {
            BlockPos pos=AquaticMarket.local(x,y,z);IBlockState s=scene.at(pos);
            if(s.getBlock()!=Blocks.BED && s.getBlock()!=Blocks.CHEST && s.getBlock()!=Blocks.FURNACE && s.getBlock()!=Blocks.CRAFTING_TABLE)continue;
            assertTrue("support "+pos,scene.at(pos.down()).isFullCube());assertFalse("lid or bed headroom "+pos,scene.solid(pos.up()));
            boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(pos.offset(f));assertTrue("access "+pos,access);
            if(s.getBlock()==Blocks.BED)beds++;else if(s.getBlock()==Blocks.CHEST)chests++;else work++;
        }
        assertEquals(8,beds);assertEquals(36,chests);assertEquals(14,work);
        System.out.println("Aquatic market: rooms="+p.rooms().size()+", chests="+chests+", beds="+beds/2+", work="+work);
    }
    @Test public void demonstrationPoolsAreContainedAndChunkOrderIsStable() {
        walk();int water=0;
        for(int x=-25;x<=25;x++)for(int z=-7;z<=20;z++) {
            BlockPos p=AquaticMarket.local(x,2,z);if(scene.at(p).getMaterial()!=Material.WATER)continue;water++;
            assertTrue(scene.at(p.down()).isFullCube());assertFalse(scene.solid(p.up()));
            for(EnumFacing f:EnumFacing.HORIZONTALS)assertTrue("water boundary "+p,scene.at(p.offset(f)).getMaterial()==Material.WATER || scene.at(p.offset(f)).isFullCube());
        }
        assertEquals(672,water);
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Generated g=new Generated(seed);assertTrue(g.walkable(AquaticMarket.local(0,1,47)));
            for(int[] xz:new int[][]{{0,40},{-43,-32},{43,6},{0,-27}}) {
                BlockPos p=AquaticMarket.local(xz[0],0,xz[1]);int cx=p.getX()>>4,cz=p.getZ()>>4;
                ChunkPrimer before=g.generator.primer(cx,cz);g.generator.primer(cx-1,cz+1);ChunkPrimer after=g.generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=103;y<200;y++)assertEquals(before.getBlockState(x,y,z),after.getBlockState(x,y,z));
            }
        }
    }
    @Test public void stallSuppliesUseRealLootAndPersistAsNormalContainers() {
        TestWorld world=new TestWorld();GensokyoGenerator g=new GensokyoGenerator(world,12345);LootTableManager manager=new LootTableManager(null);
        int[][] positions={{-47,3,-14},{-39,3,-14},{-47,3,18},{-50,3,32}};
        String[] names={"kappa_parts","kappa_tools","cucumber_supplies","night_stall"};
        for(int i=0;i<names.length;i++) {
            BlockPos p=AquaticMarket.local(positions[i][0],positions[i][1],positions[i][2]);Chunk c=g.generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)c.getTileEntityMap().get(p);assertNotNull(chest);NBTTagCompound saved=chest.writeToNBT(new NBTTagCompound());
            ResourceLocation name=new ResourceLocation("lostfantasy","chests/"+names[i]);assertEquals(name.toString(),saved.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(saved);assertEquals(saved,restored.writeToNBT(new NBTTagCompound()));
            LootTable loot=manager.getLootTableFromLocation(name);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            assertFalse(loot.generateLootForPools(new Random(3),new LootContext(0,null,manager,null,null,null)).isEmpty());
        }
    }
    @Test public void oneSavedBalloonHasBoundedMotionAndNeverIntersectsTheBuildings() {
        TestWorld world=new TestWorld();GensokyoGenerator g=new GensokyoGenerator(world,12345);
        BlockPos anchor=AquaticMarket.local(0,19,-27);int count=0;
        for(int cx=(anchor.getX()>>4)-1;cx<=(anchor.getX()>>4)+1;cx++)for(int cz=(anchor.getZ()>>4)-1;cz<=(anchor.getZ()>>4)+1;cz++) {
            Chunk chunk=g.generateChunk(cx,cz);
            for(net.minecraft.util.ClassInheritanceMultiMap<net.minecraft.entity.Entity> list:chunk.getEntityLists())for(net.minecraft.entity.Entity e:list) {
                assertTrue(e instanceof dev.lostfantasy.entity.EntityHisoutensoku);count++;
                NBTTagCompound saved=e.writeToNBT(new NBTTagCompound());dev.lostfantasy.entity.EntityHisoutensoku restored=new dev.lostfantasy.entity.EntityHisoutensoku(world);restored.readFromNBT(saved);
                assertEquals(e.posX,restored.posX,0);assertEquals(e.posY,restored.posY,0);assertEquals(e.posZ,restored.posZ,0);
                assertFalse(e.canBePushed());assertFalse(e.canBeCollidedWith());assertTrue(e.hasNoGravity());assertEquals(1,e.width,0);
            }
        }
        assertEquals(1,count);walk();
        float previous=dev.lostfantasy.entity.HisoutensokuShape.armAngle(0,0);
        for(int tick=0;tick<=480;tick++) {
            float angle=dev.lostfantasy.entity.HisoutensokuShape.armAngle(tick,0);assertTrue(Math.abs(angle-previous)<.008);previous=angle;
            if(tick%12!=0)continue;
            for(dev.lostfantasy.entity.HisoutensokuShape.Part p:dev.lostfantasy.entity.HisoutensokuShape.PARTS) {
                assertTrue(4+2*(p.w+p.d)<=128);assertTrue(4+p.h+p.d<=64);
                double turn=p.joint==1?-angle:p.joint==2?angle:0;
                for(int i=0;i<8;i++) {
                    double px=p.x+((i&1)==0?0:p.w),py=p.y+((i&2)==0?0:p.h),pz=p.z+((i&4)==0?0:p.d);
                    double x=px*Math.cos(turn)-py*Math.sin(turn)+(p.joint==0?0:p.joint==1?-17:17),y=px*Math.sin(turn)+py*Math.cos(turn)+(p.joint==0?0:48);
                    assertTrue("render bounds",Math.abs(x)<=52 && y>=-1 && y<=74 && pz>=-10 && pz<=12);
                    assertSame("balloon clearance",Blocks.AIR,scene.at(new BlockPos(anchor.getX()+.5+x,anchor.getY()+y,anchor.getZ()+.5+pz)).getBlock());
                }
            }
        }
        assertEquals(dev.lostfantasy.entity.HisoutensokuShape.armAngle(479,.99f),dev.lostfantasy.entity.HisoutensokuShape.armAngle(480,0),.0001f);
    }
    private static final class Generated {
        final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(long seed) {generator=new GensokyoGenerator(null,seed);}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
