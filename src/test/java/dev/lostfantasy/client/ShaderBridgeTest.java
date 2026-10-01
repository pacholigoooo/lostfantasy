package dev.lostfantasy.client;
import dev.lostfantasy.Balance;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ShaderBridgeTest {
    @Test public void waterFallbackHonoursPackOverridesAndLeavesFurnitureMaterialsAlone() throws Exception {
        String[] names={"riverBlock","waterBlock"};
        int[] ids={3000,9},old=new int[ids.length];
        java.lang.reflect.Field[] fields=new java.lang.reflect.Field[ids.length];
        for(int i=0;i<ids.length;i++) {
            fields[i]=ShaderBridge.class.getDeclaredField(names[i]);fields[i].setAccessible(true);
            old[i]=fields[i].getInt(null);fields[i].setInt(null,ids[i]);
        }
        try {
            for(Object[] aliases:new Object[][]{null,new Object[0],new Object[3010]}) {
                assertEquals(9,ShaderBridge.material(3000,aliases));
                assertEquals(3001,ShaderBridge.material(3001,aliases));assertEquals(3002,ShaderBridge.material(3002,aliases));
                assertEquals(3003,ShaderBridge.material(3003,aliases));assertEquals(3004,ShaderBridge.material(3004,aliases));
                assertEquals(3005,ShaderBridge.material(3005,aliases));assertEquals(1,ShaderBridge.material(1,aliases));
                assertEquals(-1,ShaderBridge.material(-1,aliases));
            }
            Object[] configured=new Object[3010];
            for(int id=3000;id<=3002;id++) {
                configured[id]=new Object();assertEquals(id,ShaderBridge.material(id,configured));
            }
        } finally {for(int i=0;i<ids.length;i++)fields[i].setInt(null,old[i]);}
    }
    @Test public void blockLightsKeepTheirGameplayBrightness() {
        net.minecraft.init.Bootstrap.register();
        assertEquals(12,dev.lostfantasy.ModBlocks.RIVER_LANTERN.getDefaultState().getLightValue());
        assertEquals(12,dev.lostfantasy.ModBlocks.LIBRARY_LAMP.getDefaultState().getLightValue());
        assertEquals(3,dev.lostfantasy.ModBlocks.SPIDER_LILY.getDefaultState().getLightValue());
        assertEquals(3,dev.lostfantasy.ModBlocks.FLOATING_LILY.getDefaultState().getLightValue());
    }
    public static final class Pack {
        private final String path;
        public Pack(int dim){path="/shaders/world"+dim;}
        public boolean hasDirectory(String candidate){return path.equals(candidate);}
    }
    @Test public void customDimensionsUseAvailableProgramsWithoutChangingOthers() {
        List<Integer> dirs=new ArrayList<>(Arrays.asList(-1,0,1));
        assertEquals(0,ShaderBridge.dimension(Balance.gensokyoDimensionId,dirs,null));
        assertEquals(0,ShaderBridge.dimension(Balance.gensokyoDimensionId,dirs,null));
        assertEquals(0,ShaderBridge.dimension(Balance.kasenDimensionId,dirs,null));
        assertEquals(0,ShaderBridge.dimension(Balance.boundaryDimensionId,dirs,null));
        assertEquals(0,ShaderBridge.dimension(Balance.oldHellDimensionId,dirs,null));
        assertEquals(0,ShaderBridge.dimension(Balance.senkaiDimensionId,dirs,null));
        assertEquals(0,ShaderBridge.dimension(Balance.netherworldDimensionId,dirs,null));
        assertEquals(0,ShaderBridge.dimension(Balance.bloodPoolDimensionId,dirs,null));
        assertEquals(Balance.bloodPoolDimensionId,ShaderBridge.dimension(Balance.bloodPoolDimensionId,dirs,new Pack(Balance.bloodPoolDimensionId)));
        assertEquals(Balance.netherworldDimensionId,ShaderBridge.dimension(Balance.netherworldDimensionId,dirs,new Pack(Balance.netherworldDimensionId)));
        assertEquals(Balance.senkaiDimensionId,ShaderBridge.dimension(Balance.senkaiDimensionId,dirs,new Pack(Balance.senkaiDimensionId)));
        assertEquals(1,ShaderBridge.dimension(Balance.dimensionId,dirs,null));
        assertEquals(31,ShaderBridge.dimension(31,dirs,null));
        assertEquals(-1,ShaderBridge.dimension(-1,dirs,null));
        assertEquals(Balance.gensokyoDimensionId,ShaderBridge.dimension(Balance.gensokyoDimensionId,new ArrayList<>(),null));
    }
    @Test public void explicitDimensionOutsideOptifineScanRangeWins() {
        List<Integer> dirs=new ArrayList<>(Arrays.asList(-1,0,1));int dim=Balance.gensokyoDimensionId;
        assertEquals(dim,ShaderBridge.dimension(dim,dirs,new Pack(dim)));
        assertTrue(dirs.contains(dim));
        assertEquals(dim,ShaderBridge.dimension(dim,dirs,null));
        int gensokyo=Balance.gensokyoDimensionId;
        assertEquals(gensokyo,ShaderBridge.dimension(gensokyo,dirs,new Pack(gensokyo)));
        int kasen=Balance.kasenDimensionId;
        assertEquals(kasen,ShaderBridge.dimension(kasen,dirs,new Pack(kasen)));
        int boundary=Balance.boundaryDimensionId;
        assertEquals(boundary,ShaderBridge.dimension(boundary,dirs,new Pack(boundary)));
        int oldHell=Balance.oldHellDimensionId;
        assertEquals(oldHell,ShaderBridge.dimension(oldHell,dirs,new Pack(oldHell)));
    }
}
