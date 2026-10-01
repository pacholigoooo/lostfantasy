package dev.lostfantasy.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiganSurfaceDetailsTest {
    @Test public void farBanksDoNotReadAnyBlocksOrEmitQuads() {
        for(double x:new double[]{-8000,-7000,-1000,0})for(double z:new double[]{-2800,-2000,-1500})
            HiganSurfaceDetails.emit(x,z,420,true,(bx,bz)->{throw new AssertionError("Distant water read");},
                (a,b,c,d,e,f,g,h,y,r,green,blue,alpha)->{throw new AssertionError("Distant geometry");});
    }
    @Test public void cullingPrecedesWorldReadsAndSharesRepeatedBlockSamples() {
        for(double ex:new double[]{-4500,-4000,-3600}) {
            Set<Long> sampled=new HashSet<>();AtomicInteger quads=new AtomicInteger();
            HiganSurfaceDetails.emit(ex,-2000,420,true,(x,z)->{
                double dx=Math.max(0,Math.max(x-ex,ex-x-1)),dz=Math.max(0,Math.max(z+2000,-2000-z-1));
                assertTrue("Query outside visible radius",Math.hypot(dx,dz)<=HiganSurfaceDetails.RADIUS+1);
                assertTrue("Repeated block query",sampled.add(((long)x<<32)^(z&0xffffffffL)));return true;
            },(a,b,c,d,e,f,g,h,y,r,green,blue,alpha)->{
                assertTrue(alpha>0);assertTrue(Math.hypot((a+c+e+g)*.25-ex,(b+d+f+h)*.25+2000)<=HiganSurfaceDetails.RADIUS+1);
                quads.incrementAndGet();
            });
            assertTrue(quads.get()>0);assertTrue("Visible river must remain detailed",quads.get()>1000);
            // The previous all-water loop made 25 strips * 13 rows * (14 * 2 + 1) = 9425 queries here.
            assertTrue("Expected less than half the old block queries, got "+sampled.size(),sampled.size()<9425/2);
            System.out.println("HIGAN_SURFACE_WORK x="+ex+" blockReads="+sampled.size()+" quads="+quads.get());
        }
    }
    @Test public void nonWaterSamplesAreAlsoCachedAndProduceNoGeometry() {
        Set<Long> sampled=new HashSet<>();
        HiganSurfaceDetails.emit(-4000,-2000,420,false,(x,z)->{assertTrue(sampled.add(((long)x<<32)^(z&0xffffffffL)));return false;},
            (a,b,c,d,e,f,g,h,y,r,green,blue,alpha)->{throw new AssertionError("Dry ground quad");});
        assertFalse(sampled.isEmpty());
    }
}
