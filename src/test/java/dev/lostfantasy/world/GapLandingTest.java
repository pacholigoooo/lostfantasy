package dev.lostfantasy.world;

import dev.lostfantasy.TestWorld;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GapLandingTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void missingReturnDimensionUsesSpawnInsteadOfForeignCoordinates() {
        PlayerData data = new PlayerData();
        data.hasReturn = true; data.returnDimension = 735;
        data.returnX = 18000; data.returnY = 65; data.returnZ = -500;
        BlockPos spawn = new BlockPos(20, 72, 30);
        assertEquals(new Vec3d(18000, 65, -500), GapWorld.returnPosition(data, 735, spawn));
        assertEquals(new Vec3d(spawn), GapWorld.returnPosition(data, 0, spawn));
        data.hasReturn = false;
        assertEquals(new Vec3d(spawn), GapWorld.returnPosition(data, 735, spawn));
    }

    @Test public void landingRequiresClearFeetAndHeadAndHarmlessSolidGround() {
        TestWorld world = new TestWorld();
        BlockPos feet = new BlockPos(0, 65, 0);
        assertFalse(GapWorld.safeLanding(world, feet));
        for (Block hazard : new Block[]{Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.WATER, Blocks.MAGMA, Blocks.CACTUS}) {
            world.blocks.put(feet.down(), hazard.getDefaultState());
            assertFalse(GapWorld.safeLanding(world, feet));
        }
        world.blocks.put(feet.down(), Blocks.STONE.getDefaultState());
        assertTrue(GapWorld.safeLanding(world, feet));
        for (Block obstruction : new Block[]{Blocks.STONE, Blocks.LAVA, Blocks.WATER, Blocks.FIRE}) {
            for (BlockPos pos : new BlockPos[]{feet, feet.up()}) {
                world.blocks.put(pos, obstruction.getDefaultState());
                assertFalse(GapWorld.safeLanding(world, feet));
                world.blocks.remove(pos);
            }
        }
    }

    @Test public void landingRejectsUnloadedChunksWorldBorderAndBuildLimits() {
        TestWorld world = new TestWorld();
        BlockPos feet = new BlockPos(0, 65, 0);
        world.blocks.put(feet.down(), Blocks.STONE.getDefaultState());
        world.loaded = false;
        assertFalse(GapWorld.safeLanding(world, feet));
        world.loaded = true;
        world.getWorldBorder().setCenter(100, 100);
        world.getWorldBorder().setTransition(10);
        assertFalse(GapWorld.safeLanding(world, feet));
        assertFalse(GapWorld.safeLanding(world, new BlockPos(100, 1, 100)));
        assertFalse(GapWorld.safeLanding(world, new BlockPos(100, 254, 100)));
    }
}
