package dev.lostfantasy.core;

import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public final class EchoMovement {
    private EchoMovement() {}

    public static Vec3d slide(AxisAlignedBB box, Vec3d desired, List<AxisAlignedBB> obstacles) {
        if (obstacles.isEmpty()) return desired;
        // Match Entity.move: descend first, then resolve horizontal movement against the new height.
        double y = desired.y;
        for (AxisAlignedBB obstacle : obstacles) y = obstacle.calculateYOffset(box, y);
        box = box.offset(0, y, 0);
        double x = desired.x;
        for (AxisAlignedBB obstacle : obstacles) x = obstacle.calculateXOffset(box, x);
        box = box.offset(x, 0, 0);
        double z = desired.z;
        for (AxisAlignedBB obstacle : obstacles) z = obstacle.calculateZOffset(box, z);
        return new Vec3d(x, y, z);
    }
}
