package dev.lostfantasy.entity;

import net.minecraft.util.math.MathHelper;

/** Tick-based steering, shared by the server route and the client's packet interpolation. */
final class FerryHeading {
    private float speed;
    float turn(float current,float target) {
        float error=MathHelper.wrapDegrees(target-current);
        float wanted=MathHelper.clamp(error*.18f,-1.8f,1.8f);
        speed+=MathHelper.clamp(wanted-speed,-.18f,.18f);
        if(Math.abs(error)<Math.abs(speed) && error*speed>=0) {speed=0;return current+error;}
        return current+speed;
    }
}
