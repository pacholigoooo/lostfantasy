package dev.lostfantasy.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

final class EchoPathfinder extends PathNavigateFlying {
    EchoPathfinder(EntityLiving entity) {
        super(entity, entity.world);
    }

    @Override
    public float getPathSearchRange() {
        return 16;
    }

    List<Vec3d> routeTo(Vec3d goal) {
        // PathNavigate builds a ChunkCache with searchRange + 8 padding. Never load it on demand.
        int margin = (int) getPathSearchRange() + 8;
        BlockPos at = new BlockPos(entity);
        if (!world.isAreaLoaded(new BlockPos(at.getX() - margin, 0, at.getZ() - margin),
                new BlockPos(at.getX() + margin, world.getHeight() - 1, at.getZ() + margin), false)) {
            return Collections.emptyList();
        }
        Path path = getPathToXYZ(goal.x, goal.y, goal.z);
        if (path == null) return Collections.emptyList();
        List<Vec3d> route = new ArrayList<>(path.getCurrentPathLength());
        for (int i = 0; i < path.getCurrentPathLength(); i++) route.add(path.getVectorFromIndex(entity, i));
        return route;
    }
}
