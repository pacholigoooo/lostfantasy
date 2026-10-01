package dev.lostfantasy.client;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.math.Vec3d;

/** Two fixed-length Alex arms holding a rigid shaft. Coordinates are model pixels, Y down. */
final class KomachiRowingPose {
    static final double PALM_Y = 8.5, GRIP_SPACING = 7, OAR_SCALE = 1.02;
    static final Vec3d LEFT_SHOULDER = new Vec3d(5, 2.5, 0);
    static final Vec3d RIGHT_SHOULDER = new Vec3d(-5, 2.5, 0);
    final Vec3d upper, lower, direction;
    final float feather;

    private KomachiRowingPose(Vec3d upper, Vec3d direction, float feather) {
        this.upper = upper;
        this.direction = direction;
        this.lower = upper.add(direction.scale(GRIP_SPACING));
        this.feather = feather;
    }

    static KomachiRowingPose at(float age) {
        double phase = age * .09, sweep = .42 * Math.sin(phase);
        double vertical = Math.sqrt(1 - .74 * .74);
        Vec3d direction = new Vec3d(-.74, vertical * Math.cos(sweep), vertical * Math.sin(sweep));
        // Intersect the two arm-reach spheres after shifting the right one by the grip spacing.
        Vec3d shiftedRight = RIGHT_SHOULDER.subtract(direction.scale(GRIP_SPACING));
        Vec3d separation = shiftedRight.subtract(LEFT_SHOULDER);
        Vec3d axis = separation.normalize();
        Vec3d down = new Vec3d(0, 1, 0).subtract(axis.scale(axis.y)).normalize();
        Vec3d forward = axis.crossProduct(down);
        double radius = Math.sqrt(PALM_Y * PALM_Y + .25 - separation.lengthSquared() * .25);
        double lift = 1.12 + .16 * Math.cos(phase);
        Vec3d center = LEFT_SHOULDER.add(shiftedRight).scale(.5);
        Vec3d upper = center.add(down.scale(radius * Math.cos(lift)))
                .add(forward.scale(radius * Math.sin(lift)));
        return new KomachiRowingPose(upper, direction, (float)(18 * Math.cos(phase)));
    }

    void apply(ModelRenderer left, ModelRenderer right) {
        aim(left, LEFT_SHOULDER, upper, .5);
        aim(right, RIGHT_SHOULDER, lower, -.5);
    }

    private static void aim(ModelRenderer arm, Vec3d shoulder, Vec3d hand, double palmX) {
        Vec3d reach = hand.subtract(shoulder);
        arm.setRotationPoint((float)shoulder.x, (float)shoulder.y, (float)shoulder.z);
        arm.rotateAngleX = (float)Math.asin(reach.z / PALM_Y);
        arm.rotateAngleY = 0;
        arm.rotateAngleZ = (float)(Math.atan2(-reach.x, reach.y)
                + Math.atan2(palmX, PALM_Y * Math.cos(arm.rotateAngleX)));
    }
}
