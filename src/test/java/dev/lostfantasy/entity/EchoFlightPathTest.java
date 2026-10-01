package dev.lostfantasy.entity;

import dev.lostfantasy.core.EchoMovement;
import dev.lostfantasy.core.EchoNavigation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class EchoFlightPathTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void fliesAroundACanopyToReachTheEnemyBelow() {
        reachBelow(new Scene(false));
    }

    @Test public void findsAOneBlockOpeningInsteadOfFlyingAboveTheCeiling() {
        reachBelow(new Scene(true));
    }

    private void reachBelow(Scene scene) {
        EntityCompanion echo = new EntityCompanion(null);
        echo.setPosition(.5, 66, .5);
        EchoNavigation navigation = new EchoNavigation();
        AtomicInteger searches = new AtomicInteger();
        Vec3d goal = new Vec3d(.5, 63, .5);
        double furthest = .5;
        for (int tick = 0; tick < 160 && echo.getPositionVector().squareDistanceTo(goal) > .04; tick++) {
            AxisAlignedBB body = echo.getEntityBoundingBox();
            Vec3d step = navigation.step(echo.getPositionVector(), goal, .32, 0, tick,
                    desired -> EchoMovement.slide(body, desired, scene.obstacles(body.expand(desired.x, desired.y, desired.z))),
                    () -> {
                        searches.incrementAndGet();
                        FlyingNodeProcessor nodes = new FlyingNodeProcessor();
                        nodes.setCanEnterDoors(true);
                        Path path = new PathFinder(nodes).findPath(scene, echo, new BlockPos(goal), 16);
                        if (path == null) return Collections.emptyList();
                        List<Vec3d> route = new ArrayList<>();
                        for (int i = 0; i < path.getCurrentPathLength(); i++) route.add(path.getVectorFromIndex(echo, i));
                        return route;
                    });
            assertTrue(step.lengthSquared() <= .32 * .32 + 1e-10);
            echo.setPosition(echo.posX + step.x, echo.posY + step.y, echo.posZ + step.z);
            assertTrue("The entire clone must clear the blocks", scene.obstacles(echo.getEntityBoundingBox()).isEmpty());
            furthest = Math.max(furthest, echo.posX);
            assertTrue("A target below must not cause endless climbing", echo.posY <= 67);
        }
        assertTrue("Must reach the target through an available route", echo.getPositionVector().squareDistanceTo(goal) < .04);
        assertTrue("Cached path should avoid repeated searches", searches.get() <= 3);
        if (scene.opening) assertTrue(furthest > 2.2);
    }

    private static final class Scene implements IBlockAccess {
        final boolean opening;
        Scene(boolean opening) { this.opening = opening; }

        @Override public IBlockState getBlockState(BlockPos pos) {
            boolean roof = pos.getY() == 65 && (opening
                    ? !(pos.getX() == 2 && pos.getZ() == 0)
                    : Math.abs(pos.getX()) <= 2 && Math.abs(pos.getZ()) <= 2);
            return (roof || pos.getY() == 62 ? Blocks.STONE : Blocks.AIR).getDefaultState();
        }

        List<AxisAlignedBB> obstacles(AxisAlignedBB box) {
            List<AxisAlignedBB> result = new ArrayList<>();
            for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(box.minX, box.minY, box.minZ),
                    new BlockPos(box.maxX, box.maxY, box.maxZ))) {
                if (!isAirBlock(pos)) {
                    AxisAlignedBB block = new AxisAlignedBB(pos);
                    if (box.intersects(block)) result.add(block);
                }
            }
            return result;
        }

        @Override public TileEntity getTileEntity(BlockPos pos) { return null; }
        @Override public int getCombinedLight(BlockPos pos, int light) { return 0; }
        @Override public boolean isAirBlock(BlockPos pos) { return getBlockState(pos).getBlock() == Blocks.AIR; }
        @Override public Biome getBiome(BlockPos pos) { return Biomes.PLAINS; }
        @Override public int getStrongPower(BlockPos pos, EnumFacing side) { return 0; }
        @Override public WorldType getWorldType() { return WorldType.DEFAULT; }
        @Override public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean fallback) { return !isAirBlock(pos); }
    }
}
