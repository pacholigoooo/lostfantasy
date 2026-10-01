package dev.lostfantasy.world.gensokyo;

/** Bounded value noise, including negative coordinates and the world border. */
final class GensokyoNoise {
    private final long seed;
    GensokyoNoise(long seed) {this.seed=seed;}
    long hash(int x,int z,int salt) {
        long v=seed ^ x*0x632be59bd9b4e019L ^ z*0x94d049bb133111ebL ^ salt*0x9e3779b97f4a7c15L;
        v=(v^(v>>>30))*0xbf58476d1ce4e5b9L; v=(v^(v>>>27))*0x94d049bb133111ebL;
        return v^(v>>>31);
    }
    double value(double x,double z,int scale,int salt) {
        double gx=x/scale,gz=z/scale; int ix=(int)Math.floor(gx),iz=(int)Math.floor(gz);
        double u=smooth(gx-ix),v=smooth(gz-iz);
        return lerp(lerp(gradient(ix,iz,salt,gx-ix,gz-iz),gradient(ix+1,iz,salt,gx-ix-1,gz-iz),u),
                lerp(gradient(ix,iz+1,salt,gx-ix,gz-iz-1),gradient(ix+1,iz+1,salt,gx-ix-1,gz-iz-1),u),v)*1.4;
    }
    private double gradient(int x,int z,int salt,double dx,double dz) {
        int direction=(int)hash(x,z,salt)&7;
        switch(direction) {
            case 0:return dx;case 1:return -dx;case 2:return dz;case 3:return -dz;
            case 4:return (dx+dz)*.7071;case 5:return (dx-dz)*.7071;
            case 6:return (-dx+dz)*.7071;default:return (-dx-dz)*.7071;
        }
    }
    static double smooth(double t) {t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    static double lerp(double a,double b,double t) {return a+(b-a)*t;}
}
