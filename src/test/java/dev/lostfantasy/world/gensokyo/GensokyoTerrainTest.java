package dev.lostfantasy.world.gensokyo;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GensokyoTerrainTest {
    @Test public void allPlannedGroundPlotsAreDryAndLevelAcrossSeeds() {
        for(long seed:new long[]{0,17,Long.MIN_VALUE,Long.MAX_VALUE}) {
            GensokyoTerrain terrain=new GensokyoTerrain(seed);
            for(GensokyoAtlas s:GensokyoAtlas.values())if(s.grounded())
                for(int dx:new int[]{-s.rx,0,s.rx})for(int dz:new int[]{-s.rz,0,s.rz}) {
                    GensokyoTerrain.Column c=terrain.column(s.x+dx,s.z+dz);
                    assertEquals(s.name()+" ground",s.y,c.ground);assertFalse(s.name()+" flooded",c.wet());
                }
        }
    }
    @Test public void geographyKeepsDistinctLakesAndAltitude() {
        GensokyoTerrain terrain=new GensokyoTerrain(17);
        for(GensokyoAtlas s:new GensokyoAtlas[]{GensokyoAtlas.MIST_LAKE,GensokyoAtlas.WIND_LAKE,GensokyoAtlas.TOAD_POND}) {
            GensokyoTerrain.Column c=terrain.column(s.x,s.z);
            assertTrue(s.name(),c.wet());assertEquals(s.y,c.water);
        }
        assertTrue(terrain.column(GensokyoAtlas.MOUNTAIN_TOP.x,GensokyoAtlas.MOUNTAIN_TOP.z).ground>170);
    }
    @Test public void columnsAreOrderIndependentAtChunkBordersAndNegativeCoordinates() {
        GensokyoTerrain a=new GensokyoTerrain(1986),b=new GensokyoTerrain(1986);
        Random random=new Random(1);
        for(int i=0;i<10000;i++) {
            int x=(random.nextInt(450)-225)*16+(i&1),z=(random.nextInt(450)-225)*16-(i&1);
            GensokyoTerrain.Column ca=a.column(x,z),cb=b.column(x,z);
            assertEquals(ca.ground,cb.ground);assertEquals(ca.water,cb.water);assertEquals(ca.region,cb.region);
            assertTrue(ca.ground>=8 && ca.surface()<=244);
        }
    }
    @Test public void roadJunctionsHaveWalkableGrades() {
        GensokyoTerrain terrain=new GensokyoTerrain(12345);
        for(GensokyoRoads.Segment s:GensokyoRoads.INSTANCE.segments()) {
            double a=s.contour?terrain.roadJunction(s.ax,s.az,s.ay):s.ay;
            double b=s.contour?terrain.roadJunction(s.bx,s.bz,s.by):s.by;
            assertTrue("road too steep",Math.abs(b-a)/Math.hypot(s.bx-s.ax,s.bz-s.az)<=(s.contour?GensokyoRoadProfile.MAX_GRADE:.35));
        }
    }
    @Test public void actualRoadCentrelinesHaveNoCliffs() {
        GensokyoTerrain terrain=new GensokyoTerrain(12345);List<String> gaps=new ArrayList<>();
        for(GensokyoRoads.Segment s:GensokyoRoads.INSTANCE.segments()) {
            int n=(int)Math.ceil(Math.hypot(s.bx-s.ax,s.bz-s.az));int previous=Integer.MIN_VALUE;
            for(int i=0;i<=n;i++) {
                int x=(int)Math.round(s.ax+(s.bx-s.ax)*i/n),z=(int)Math.round(s.az+(s.bz-s.az)*i/n);
                GensokyoTerrain.Column c=terrain.column(x,z);
                int y=c.roadY;
                if(c.wet())assertTrue("submerged bridge at "+x+","+z,c.roadY>c.water);
                if(previous!=Integer.MIN_VALUE && Math.abs(previous-y)>1 && gaps.size()<20)gaps.add(x+","+z+": "+previous+" -> "+y);
                previous=y;
            }
        }
        assertTrue("Unwalkable road transitions "+gaps,gaps.isEmpty());
    }
    @Test public void plotIndexDoesNotReserveTheWholeWorld() {
        assertTrue(GensokyoAtlas.reserved(GensokyoAtlas.HAKUREI.x,GensokyoAtlas.HAKUREI.z,0));
        assertFalse(GensokyoAtlas.reserved(27000,-14000,10));
        assertTrue(GensokyoAtlas.plots(27000,-14000).isEmpty());
    }
}
