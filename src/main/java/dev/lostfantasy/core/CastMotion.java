package dev.lostfantasy.core;

/** Shared clocks/geometry: the server's release and the client's pose use one timeline. */
public final class CastMotion {
    public static final int TRAIN_CHARGE=25, TRAIN_DURATION=90, SPEAR_RELEASE=12, SPEAR_DURATION=27;
    public static final double TRAIN_SPEED=1.2, TRAIN_LENGTH=29.2, TRAIN_HALF_WIDTH=1.55, TRAIN_HEIGHT=3.85;
    private CastMotion() {}
    public static double smooth(double t) {t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    public static double charge(double age,int ticks) {return smooth(age/Math.max(1,ticks));}
    public static double flareLift(double age,int charge) {return 2*charge(age,charge);}
    public static double flareSpread(double age,int charge) {return Math.PI*.80*charge(age,charge);}
    // Lift both arms above the head in an outward V. The sun has its own clear overhead space.
    public static double flareHandHeight(double age,int charge) {return 1.40-.625*Math.cos(flareSpread(age,charge));}
    public static double flareSunHeight(double age,int charge) {return 2.50+.32*charge(age,charge);}
    public static double flareRadius(double age,int charge) {return .10+.42*charge(age,charge);}
    public static double trainTravel(double age) {return Math.max(0,age-TRAIN_CHARGE)*TRAIN_SPEED;}
    public static double spearTravel(double age) {return Math.max(0,age-SPEAR_RELEASE)*4;}
    public static double trainRecovery(double age) {return 1-smooth((age-(TRAIN_DURATION-10))/10);}
    public static double trainPoint(double age) {return smooth((age-5)/15)*trainRecovery(age);}
    public static float trainBodyYaw(float startYaw,float trackYaw,double age) {
        // The track stays on the caster's left, so the left arm extends outwards.
        return Facing.interpolate(startYaw,trackYaw+90,charge(age,18));
    }
    public static float trainHeadYaw(float restingYaw,float trackYaw,double age) {
        return Facing.interpolate(restingYaw,trackYaw+15,charge(age,12)*trainRecovery(age));
    }
    public static double spearArm(double age) {
        if(age<8)return -Math.PI*.92*smooth(age/8);
        if(age<SPEAR_RELEASE)return -Math.PI*.92+(Math.PI*.47)*smooth((age-8)/4);
        return -Math.PI*.45*(1-smooth((age-SPEAR_RELEASE)/10));
    }
    /** SAT test of an axis-aligned box against a horizontal swept train rectangle. */
    public static boolean trainIntersects(double cx,double cz,double hx,double hz,double dx,double dz,double back,double front) {
        double middle=(back+front)*.5, half=(front-back)*.5;
        cx-=dx*middle;cz-=dz*middle;
        double w=TRAIN_HALF_WIDTH;
        return Math.abs(cx*dx+cz*dz)<=half+hx*Math.abs(dx)+hz*Math.abs(dz)
            && Math.abs(cx*dz-cz*dx)<=w+hx*Math.abs(dz)+hz*Math.abs(dx)
            && Math.abs(cx)<=half*Math.abs(dx)+w*Math.abs(dz)+hx
            && Math.abs(cz)<=half*Math.abs(dz)+w*Math.abs(dx)+hz;
    }
}
