package dev.lostfantasy.world.gensokyo;

/** A rock shoulder behind the mine's small, level loading yard. */
final class RainbowRidge {
    private RainbowRidge() {}
    static double height(int x,int z,double ground,GensokyoNoise noise) {
        int dx=x-GensokyoAtlas.RAINBOW_CAVE.x,dz=z-GensokyoAtlas.RAINBOW_CAVE.z;
        if(dz>=-17 || dz<-318 || Math.abs(dx)>224)return -1;
        double across=dx+noise.value(x,z,77,316)*18;
        double along=dz+133+noise.value(x,z,91,317)*12;
        double distance=Math.hypot(across/156.0,along/127.0);
        double edge=GensokyoNoise.smooth((1-distance)/.48)*GensokyoNoise.smooth((-dz-17)/12.0);
        double peak=GensokyoAtlas.RAINBOW_CAVE.y+10+22*(1-distance)
                +noise.value(x,z,57,318)*8+noise.value(x,z,17,319)*3;
        double front=1-Math.hypot(dx/82.0,(dz+58)/63.0);
        if(distance>=1.32 && front<=0)return -1;
        double shoulder=GensokyoNoise.lerp(GensokyoAtlas.RAINBOW_CAVE.y-27,GensokyoAtlas.RAINBOW_CAVE.y+36,
                GensokyoNoise.smooth(front/.65)*GensokyoNoise.smooth((-dz-17)/12.0));
        double top=Math.max(shoulder,GensokyoNoise.lerp(GensokyoAtlas.RAINBOW_CAVE.y-27,peak,edge));
        // The old loading-yard shoulder ended at Y161 against the newly lowered valley.
        double blend=1-GensokyoNoise.smooth((distance-.78)/.54);
        blend*=GensokyoNoise.smooth((-dz-17)/12.0);
        return GensokyoNoise.lerp(ground,Math.max(ground,top),blend);
    }
}
