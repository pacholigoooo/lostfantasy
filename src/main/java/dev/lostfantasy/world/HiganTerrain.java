package dev.lostfantasy.world;

import dev.lostfantasy.core.RiverJourney;
import dev.lostfantasy.world.gensokyo.GensokyoAtlas;
import dev.lostfantasy.world.gensokyo.SanzuCoast;
import net.minecraft.util.math.BlockPos;

/** One continuous water route between two physical piers in Gensokyo. */
public final class HiganTerrain {
    public static final int WATER=71;
    private HiganTerrain() {}
    public static double center(double z) {return SanzuCoast.bank((int)Math.floor(z))-640;}
    public static double halfWidth(double z) {return 640;}
    public static double bankDistance(double x,double z) {return Math.abs(x-center(z))-halfWidth(z);}
    public static boolean farSide(double x,double z) {return x<center(z);}
    public static int farBankX() {return (int)Math.round(SanzuCoast.bank(GensokyoAtlas.SANZU_PIER.z))-1280;}
    public static BlockPos landing(boolean farBank) {
        return new BlockPos(farBank?farBankX()+50:GensokyoAtlas.SANZU_PIER.x-70,WATER+2,GensokyoAtlas.SANZU_PIER.z);
    }
    public static int hash(int x,int z) {int h=x*0x1f123bb5 ^ z*0x5f356495;h^=h>>>16;h*=0x45d9f3b;return h^(h>>>16);}
    private static double progress(int ticks,boolean returning) {
        double t=Math.max(0,Math.min(1,ticks/(double)RiverJourney.DURATION));
        double eased=t*t*(3-2*t);return returning?1-eased:eased;
    }
    public static double boatX(int ticks) {return boatX(ticks,false);}
    public static double boatZ(int ticks) {return boatZ(ticks,false);}
    public static float boatYaw(int ticks) {return boatYaw(ticks,false);}
    public static double boatX(int ticks,boolean returning) {
        double start=GensokyoAtlas.SANZU_PIER.x-74;
        return start+(farBankX()+55-start)*progress(ticks,returning);
    }
    private static double routeZ(double u) {double s=Math.sin(Math.PI*u);return GensokyoAtlas.SANZU_PIER.z+40*s*s;}
    public static double boatZ(int ticks,boolean returning) {return routeZ(progress(ticks,returning));}
    public static float boatYaw(int ticks,boolean returning) {
        double u=progress(ticks,returning),dx=farBankX()+55-(GensokyoAtlas.SANZU_PIER.x-74);
        double dz=40*Math.PI*Math.sin(2*Math.PI*u);
        return (float)Math.toDegrees(Math.atan2(returning?dx:-dx,returning?-dz:dz));
    }
    /** Includes hull, oar and the maximum radius of a generated river rock. */
    public static boolean ferryCorridor(int x,int z) {
        double start=GensokyoAtlas.SANZU_PIER.x-74,end=farBankX()+55;
        if(x<end-12 || x>start+12)return false;
        double u=Math.max(0,Math.min(1,(x-start)/(end-start)));
        return Math.abs(z-routeZ(u))<16;
    }
}
