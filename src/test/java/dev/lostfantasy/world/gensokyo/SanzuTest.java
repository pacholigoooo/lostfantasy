package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.*;
import dev.lostfantasy.entity.EntityKomachi;
import dev.lostfantasy.world.*;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.*;
import org.junit.*;
import static org.junit.Assert.*;

public class SanzuTest {
    @BeforeClass public static void setup() {GensokyoTestBlocks.register();}
    @Test public void marketAndPierRoomsAreConnectedAndFurnishingsAreUsable() {
        Scene s=new Scene(12345);
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.LIMINAL_ROAD,GensokyoAtlas.SANZU_PIER}) {
            boolean market=site==GensokyoAtlas.LIMINAL_ROAD;GensokyoBlueprint p=new GensokyoBlueprint();if(market)LiminalMarket.build(p);else SanzuLanding.build(p);
            Set<BlockPos> reached=s.walk(site,market);List<String> missing=new ArrayList<>();
            for(GensokyoBlueprint.Room r:p.rooms()) {
                BlockPos room=new BlockPos(r.x,r.y,r.z);
                if(r.name.startsWith("彼岸渡口·"))assertTrue("far shore "+r.name,s.walkable(room));
                else if(!reached.contains(room))missing.add(r.name);
            }
            assertTrue(missing.toString(),missing.isEmpty());int beds=0,chests=0;
            for(int x=market?-75:-15;x<=(market?75:35);x++)for(int z=market?-135:-25;z<=(market?135:35);z++)for(int y=1;y<=3;y++) {
                BlockPos at=new BlockPos(site.x+x,site.y+y,site.z+z);IBlockState b=s.at(at);
                if(b.getBlock()!=Blocks.CHEST && b.getBlock()!=Blocks.BED && b.getBlock()!=Blocks.FURNACE && b.getBlock()!=Blocks.CRAFTING_TABLE)continue;
                assertTrue("support "+at,s.at(at.down()).isFullCube());assertFalse("headroom "+at,s.solid(at.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(at.offset(f));assertTrue("access "+at,access);
                if(b.getBlock()==Blocks.BED)beds++;if(b.getBlock()==Blocks.CHEST)chests++;
            }
            assertEquals(market?8:0,beds);assertEquals(market?25:2,chests);
            System.out.println(site.title+" reachable spaces: "+p.rooms().size());
        }
        assertTrue(s.walkable(HiganWorld.ARRIVAL));
        assertTrue(s.walkable(HiganTerrain.landing(false)));assertTrue(s.walkable(HiganTerrain.landing(true)));
        assertTrue(HiganWorld.inside(Balance.gensokyoDimensionId,HiganWorld.ARRIVAL.getX(),HiganWorld.ARRIVAL.getZ()));
    }
    @Test public void roadRunsContinuouslyFromMarketThroughStoneBankToThePier() {
        for(long seed:new long[]{0,12345,Long.MIN_VALUE}) {
            Scene s=new Scene(seed);int count=0;
            for(GensokyoRoads.Segment road:GensokyoRoads.INSTANCE.segments()) {
                if(road.ax>GensokyoAtlas.LIMINAL_ROAD.x || road.bx>GensokyoAtlas.LIMINAL_ROAD.x || road.az<-3000 || road.bz<-3000 || road.az>-1800 || road.bz>-1800)continue;
                if(road.ax<GensokyoAtlas.SANZU_PIER.x-1 || road.bx<GensokyoAtlas.SANZU_PIER.x-1)continue;
                int steps=(int)Math.ceil(Math.max(Math.abs(road.bx-road.ax),Math.abs(road.bz-road.az))*2);int previous=-1;
                for(int i=0;i<=steps;i++) {
                    double t=i/(double)steps;int x=(int)Math.round(road.ax+(road.bx-road.ax)*t),z=(int)Math.round(road.az+(road.bz-road.az)*t);
                    GensokyoTerrain.Column c=s.terrain.column(x,z);int floor=c.wet()?c.roadY:c.ground;
                    assertTrue("road "+x+","+floor+","+z,s.walkable(new BlockPos(x,floor+1,z)));
                    if(previous>=0)assertTrue("road rise",Math.abs(previous-floor)<=1);previous=floor;count++;
                }
            }
            assertTrue(count>1500);
        }
    }
    @Test public void everyHullPositionHasOpenWaterAndGeneratedRocksCannotClipIt() {
        Scene s=new Scene(12345);
        for(boolean fromFarBank:new boolean[]{false,true})for(int t=0;t<=dev.lostfantasy.core.RiverJourney.DURATION;t+=10) {
            double x=HiganTerrain.boatX(t,fromFarBank),z=HiganTerrain.boatZ(t,fromFarBank);
            double yaw=Math.toRadians(HiganTerrain.boatYaw(t,fromFarBank));
            for(int along=-6;along<=6;along++)for(int across=-2;across<=2;across++) {
                double dx=-Math.sin(yaw)*along*.45+Math.cos(yaw)*across*.5,dz=Math.cos(yaw)*along*.45+Math.sin(yaw)*across*.5;
                BlockPos p=new BlockPos(x+dx,HiganTerrain.WATER,z+dz);
                assertSame("water "+p,ModBlocks.SANZU_WATER,s.at(p).getBlock());
                for(int y=1;y<=4;y++)assertSame("hull clearance "+p,Blocks.AIR,s.at(p.up(y)).getBlock());
            }
        }
        int blooms=0;
        for(int x=-200;x<=-90;x+=5)for(int z=-100;z<=100;z+=5) {
            BlockPos p=new BlockPos(GensokyoAtlas.SANZU_PIER.x+x,72,GensokyoAtlas.SANZU_PIER.z+z);
            if(s.at(p).getBlock()==ModBlocks.FLOATING_LILY)blooms++;
        }
        assertEquals(0,blooms);
    }
    private static final class Scene {
        final GensokyoGenerator generator;final GensokyoTerrain terrain;final Map<Long,ChunkPrimer> cache=new HashMap<>();
        Scene(long seed) {generator=new GensokyoGenerator(null,seed);terrain=new GensokyoTerrain(seed);}
        IBlockState at(BlockPos p) {return cache.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.primer(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState b=at(p);return b.getMaterial().blocksMovement() && b.getBlock()!=Blocks.CARPET && b.getBlock()!=Blocks.WALL_SIGN;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && !at(p).getMaterial().isLiquid();}
        Set<BlockPos> walk(GensokyoAtlas site,boolean market) {
            Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=new BlockPos(site.x,site.y+1,site.z+(market?136:19));
            assertTrue(walkable(start));reached.add(start);queue.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing f:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(f).up(dy);int x=n.getX()-site.x,y=n.getY()-site.y,z=n.getZ()-site.z;
                    if(x<(market?-83:-75) || x>(market?83:40) || Math.abs(z)>(market?143:40) || y<-5 || y>27 || reached.contains(n))continue;
                    if(!walkable(n) || dy>0 && solid(p.up(2)) || dy<0 && solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            return reached;
        }
    }
}
