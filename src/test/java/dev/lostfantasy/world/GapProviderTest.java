package dev.lostfantasy.world;

import org.junit.Test;
import static org.junit.Assert.*;

public class GapProviderTest {
    @Test public void unlitGapRemainsVisibleAndHigherLightLevelsNeverDarkenIt() {
        GapProvider provider=new GapProvider();provider.generateLightBrightnessTable();
        float[] levels=provider.getLightBrightnessTable();
        assertTrue(levels[0]>=.15f);
        for(int i=1;i<levels.length;i++)assertTrue(levels[i]>=levels[i-1] && levels[i]<=1);
        assertEquals(1,levels[15],.00001);
    }
    @Test public void lightmapHasAnAmbientFloorWithoutDimmingNearbyLightSources() {
        GapProvider provider=new GapProvider();float[] dark={0,0,0};
        provider.getLightmapColors(0,0,0,0,dark);
        for(float color:dark)assertTrue(color>=.15f && color<=.4f);
        assertTrue(dark[2]>dark[0] && dark[0]>dark[1]);
        float[] lit={.8f,.9f,1};provider.getLightmapColors(0,1,1,1,lit);
        assertArrayEquals(new float[]{.8f,.9f,1},lit,0);
    }
    @Test public void voidFadeBeginsWellBelowTheWalkingSurface() {
        assertTrue(new GapProvider().getHorizon()<GapSupport.HEIGHT-64);
    }
}
