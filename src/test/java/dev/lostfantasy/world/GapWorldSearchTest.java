package dev.lostfantasy.world;

import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import java.util.*;
import java.util.function.Predicate;
import static org.junit.Assert.*;

public class GapWorldSearchTest {
    private static BlockPos exhaustive(BlockPos base,Predicate<BlockPos> safe) {
        for(int radius=0;radius<=8;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)for(int dy=-4;dy<=12;dy++) {
            BlockPos pos=base.add(dx,dy,dz);
            if(pos.getY()>=2 && pos.getY()<=253 && safe.test(pos))return pos;
        }
        return null;
    }
    @Test public void visitsEveryCandidateOnlyOnceWhenNoSafeSpotExists() {
        BlockPos base=new BlockPos(100,65,-100);Set<BlockPos> visited=new HashSet<>();
        assertNull(GapWorld.findSafePosition(base,p->{assertTrue("Duplicate world query",visited.add(p.toImmutable()));return false;}));
        assertEquals(17*17*17,visited.size());
        int[] originalQueries={0};assertNull(exhaustive(base,p->{originalQueries[0]++;return false;}));
        assertEquals(16473,originalQueries[0]);
    }
    @Test public void preservesFirstResultAcrossRandomTerrainAndHeightLimits() {
        Random random=new Random(1707);
        for(int trial=0;trial<240;trial++) {
            BlockPos base=new BlockPos(random.nextInt(60000000)-30000000,random.nextInt(270)-8,random.nextInt(60000000)-30000000);
            Set<BlockPos> safe=new HashSet<>();
            for(int i=0;i<trial%12;i++)safe.add(base.add(random.nextInt(19)-9,random.nextInt(21)-6,random.nextInt(19)-9));
            assertEquals(exhaustive(base,safe::contains),GapWorld.findSafePosition(base,safe::contains));
        }
    }
    @Test public void returnsImmutablePositionAndNeverQueriesOutsideBuildHeight() {
        for(int y:new int[]{-20,0,2,65,253,255,280}) {
            BlockPos result=GapWorld.findSafePosition(new BlockPos(-7,y,9),p->{
                assertTrue(p.getY()>=2 && p.getY()<=253);return true;
            });
            if(result!=null) {
                assertFalse(result instanceof BlockPos.MutableBlockPos);
                assertEquals(-7,result.getX());assertEquals(9,result.getZ());
            }
        }
    }
}
