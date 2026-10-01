package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import java.nio.file.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.*;
import org.junit.*;
import static org.junit.Assert.*;

public class NetherworldTest {
    @BeforeClass public static void setup() {GensokyoTestBlocks.register();}
    @Test public void wholeApproachGardensAndEveryRoomAreWalkable() {
        Scene s=new Scene(12345);Set<BlockPos> reached=s.walk();List<String> missing=new ArrayList<>();
        for(GensokyoBlueprint.Room r:Hakugyokurou.create().rooms())if(!reached.contains(new BlockPos(r.x,r.y,r.z)))missing.add(r.name+" "+r.x+","+r.y+","+r.z);
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(reached.contains(NetherworldWorld.gate(true)));
        System.out.println("Netherworld reachable spaces: "+Hakugyokurou.create().rooms().size());
        int chests=0,beds=0,work=0;
        for(int x=-81;x<=81;x++)for(int z=-76;z<=58;z++) {
            BlockPos p=NetherworldWorld.local(x,5,z);IBlockState b=s.at(p);
            if(b.getBlock()!=Blocks.CHEST && b.getBlock()!=Blocks.BED && b.getBlock()!=Blocks.FURNACE && b.getBlock()!=Blocks.CRAFTING_TABLE)continue;
            assertTrue("support "+p,s.at(p.down()).isFullCube());assertFalse("headroom "+p,s.solid(p.up()));
            boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
            if(b.getBlock()==Blocks.CHEST)chests++;else if(b.getBlock()==Blocks.BED) {
                beds++;EnumFacing f=b.getValue(BlockBed.FACING);assertSame(Blocks.BED,s.at(p.offset(b.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?f.getOpposite():f)).getBlock());
            } else work++;
        }
        assertEquals(30,chests);assertEquals(12,beds);assertEquals(6,work);
    }
    @Test public void boundaryLandingsAndLongStairAreStableAcrossSeeds() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Scene s=new Scene(seed);
            for(int z=140;z<=925;z++)assertTrue("stair "+z,s.walkable(NetherworldWorld.local(0,NetherworldEntrance.stairFloor(z)+1,z)));
            assertTrue(s.walkable(NetherworldWorld.arrival()));assertTrue(s.walkable(NetherworldWorld.gate(true)));
            GensokyoGenerator outside=new GensokyoGenerator(null,seed);
            for(BlockPos p:new BlockPos[]{NetherworldWorld.gate(false),NetherworldWorld.returnPoint()}) {
                ChunkPrimer c=outside.primer(p.getX()>>4,p.getZ()>>4);
                assertTrue(c.getBlockState(p.getX()&15,p.getY()-1,p.getZ()&15).isFullCube());
                assertSame(Blocks.AIR,c.getBlockState(p.getX()&15,p.getY(),p.getZ()&15).getBlock());
                assertSame(Blocks.AIR,c.getBlockState(p.getX()&15,p.getY()+1,p.getZ()&15).getBlock());
            }
            BlockPos p=NetherworldWorld.local(90,0,86);int cx=p.getX()>>4,cz=p.getZ()>>4;
            ChunkPrimer first=s.generator.primer(cx,cz);s.generator.primer(cx+1,cz+1);ChunkPrimer second=s.generator.primer(cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=135;y<180;y++)assertEquals(first.getBlockState(x,y,z),second.getBlockState(x,y,z));
        }
    }
    @Test public void ancientCherryRemainsBareWithinItsSealedClearing() {
        Scene s=new Scene(12345);int wood=0;
        for(int x=-68;x<=68;x+=2)for(int z=-279;z<=-143;z+=2)for(int y=1;y<=88;y+=2) {
            IBlockState b=s.at(NetherworldWorld.local(x,y,z));assertNotSame(ModBlocks.CHERRY_LEAVES,b.getBlock());
            if(b.getBlock()==Blocks.LOG)wood++;
        }
        assertTrue(wood>500);assertSame(ModBlocks.SHRINE_ROPE,s.at(NetherworldWorld.local(0,7,-199)).getBlock());
    }
    @Test public void gatesCannotBounceAndNewLootUsesExistingTables() {
        for(boolean inside:new boolean[]{false,true}) {
            BlockPos g=NetherworldWorld.gate(inside),a=inside?NetherworldWorld.arrival():NetherworldWorld.returnPoint();
            assertTrue(NetherworldWorld.doorway(inside,g.getX()+.5,g.getY(),g.getZ()+.5));
            assertFalse(NetherworldWorld.doorway(inside,a.getX()+.5,a.getY(),a.getZ()+.5));
            assertFalse(NetherworldWorld.doorway(inside,g.getX()+.5,g.getY()+3,g.getZ()+.5));
        }
        NetherworldGenerator g=new NetherworldGenerator(new TestWorld(),12345);int count=0;
        for(long key:Hakugyokurou.create().chunks()) {
            int cx=(int)(key>>32),cz=(int)key;
            // Only the residence contains containers; avoid generating the 800-block approach here.
            if(Math.abs((cx<<4)-GensokyoAtlas.NETHER_GATE.x)>110 || Math.abs((cz<<4)-GensokyoAtlas.NETHER_GATE.z)>100)continue;
            Chunk c=g.generateChunk(cx,cz);
            for(net.minecraft.tileentity.TileEntity t:c.getTileEntityMap().values())if(t instanceof TileEntityChest) {
                count++;NBTTagCompound n=t.writeToNBT(new NBTTagCompound());String table=n.getString("LootTable");
                assertTrue(table,Files.isRegularFile(Paths.get("src/main/resources/assets/lostfantasy/loot_tables/"+table.substring(table.indexOf(':')+1)+".json")));
                TileEntityChest copy=new TileEntityChest();copy.readFromNBT(n);assertEquals(n,copy.writeToNBT(new NBTTagCompound()));
            }
        }
        assertEquals(30,count);
    }
    private static final class Scene {
        final NetherworldGenerator generator;final Map<Long,ChunkPrimer> cache=new HashMap<>();
        Scene(long seed) {generator=new NetherworldGenerator(null,seed);}
        IBlockState at(BlockPos p) {return cache.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState b=at(p);return b.getMaterial().blocksMovement() && b.getBlock()!=Blocks.CARPET && b.getBlock()!=Blocks.WALL_SIGN;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && !at(p).getMaterial().isLiquid();}
        Set<BlockPos> walk() {
            Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=NetherworldWorld.arrival();
            assertTrue(walkable(start));reached.add(start);queue.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(f).up(dy);int x=n.getX()-GensokyoAtlas.NETHER_GATE.x,y=n.getY()-GensokyoAtlas.NETHER_GATE.y,z=n.getZ()-GensokyoAtlas.NETHER_GATE.z;
                    if(Math.abs(x)>(z>155?36:125) || z<-286 || z>952 || y<-98 || y>90 || reached.contains(n))continue;
                    if(!walkable(n) || dy>0 && solid(p.up(2)) || dy<0 && solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            return reached;
        }
    }
}
