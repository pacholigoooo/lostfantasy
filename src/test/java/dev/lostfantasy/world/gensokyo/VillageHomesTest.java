package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class VillageHomesTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void allHouseholdsHaveAccessibleRoomsAndDifferentPlans() {
        Generated generated=new Generated();GensokyoAtlas v=GensokyoAtlas.VILLAGE;
        GensokyoBlueprint plan=GensokyoStructures.create();Set<Integer> styles=new HashSet<>();int rooms=0;
        assertTrue("Insufficient populated lots: "+VillageHomes.LOTS.size(),VillageHomes.LOTS.size()>=50);
        for(VillageHomes.Lot lot:VillageHomes.LOTS) {
            styles.add(lot.type);Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=new BlockPos(v.x+lot.streetX,v.y+1,v.z+lot.streetZ);queue.add(start);reached.add(start);
            int minX=v.x+Math.min(lot.minX,lot.streetX)-1,maxX=v.x+Math.max(lot.maxX,lot.streetX)+1;
            int minZ=v.z+Math.min(lot.minZ,lot.streetZ)-1,maxZ=v.z+Math.max(lot.maxZ,lot.streetZ)+1;
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(side).up(dy);
                    if(n.getX()<minX || n.getX()>maxX || n.getZ()<minZ || n.getZ()>maxZ
                            || n.getY()<v.y+1 || n.getY()>v.y+13 || reached.contains(n))continue;
                    if(!generated.solid(n.down()) || generated.solid(n) || generated.solid(n.up()))continue;
                    if(dy>0 && generated.solid(p.up(2)) || dy<0 && generated.solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            List<String> missing=new ArrayList<>();
            for(GensokyoBlueprint.Room room:plan.rooms())if(room.name.startsWith(lot.title()+"·")) {
                rooms++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-v.x-lot.x,room.y-v.y,room.z-v.z-lot.z));
            }
            assertTrue(missing.toString(),missing.isEmpty());
        }
        assertEquals(6,styles.size());assertTrue(rooms>=VillageHomes.LOTS.size()*3);
    }
    @Test public void eachHouseHasBedsStorageAndFurnitureOnRealFloors() {
        Generated generated=new Generated();GensokyoAtlas v=GensokyoAtlas.VILLAGE;
        for(VillageHomes.Lot lot:VillageHomes.LOTS) {
            int beds=0,chests=0;
            for(int dx=-lot.half;dx<=lot.half;dx++)for(int dz=-lot.depth;dz<=lot.depth;dz++)for(int y=1;y<=10;y++) {
                BlockPos p=lot.world(dx,y,dz);IBlockState state=generated.at(p);
                if(state.getBlock()==Blocks.BED) {
                    beds++;assertTrue(lot.title()+" bed support "+p,generated.at(p.down()).isFullCube());
                    EnumFacing facing=state.getValue(BlockBed.FACING);
                    assertSame(Blocks.BED,generated.at(p.offset(state.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?facing.getOpposite():facing)).getBlock());
                    assertFalse(generated.solid(p.up()));
                } else if(state.getBlock()==Blocks.CHEST) {
                    chests++;assertTrue("Chest support "+p,generated.at(p.down()).isFullCube());assertFalse("Chest lid "+p,generated.at(p.up()).isFullCube());
                }
            }
            assertEquals(lot.title()+" beds",lot.bedCount()*2,beds);assertTrue(lot.title()+" storage",chests>=2);
        }
    }
    @Test public void gardensHaveSupportedCropsAndLeaveTheCanalOpen() {
        Generated generated=new Generated();int crops=0;
        for(VillageHomes.Lot lot:VillageHomes.LOTS)
        for(int x=-lot.half;x<=-3;x++)for(int z=-lot.depth-lot.yard+2;z<=-lot.depth-3;z++) {
            BlockPos p=lot.world(x,1,z);IBlockState s=generated.at(p);
            if(s.getBlock()==Blocks.WHEAT || s.getBlock()==Blocks.CARROTS) {
                crops++;assertSame(Blocks.FARMLAND,generated.at(p.down()).getBlock());assertTrue(generated.at(p.down(2)).isFullCube());
                VillageCanal.Sample c=VillageCanal.at(p.getX(),p.getZ());assertTrue(c==null || c.distance>=9);
            }
        }
        assertTrue("Rear household gardens should contain crops",crops>0);
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        IBlockState at(BlockPos p) {
            int cx=p.getX()>>4,cz=p.getZ()>>4;
            return chunks.computeIfAbsent(GensokyoAtlas.key(cx,cz),key->generator.primer(cx,cz)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);
        }
        boolean solid(BlockPos p) {
            IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.WALL_SIGN;
        }
    }
}
