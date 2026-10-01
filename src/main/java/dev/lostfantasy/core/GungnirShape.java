package dev.lostfantasy.core;

/** Profile measured back from the tip: long sharp point, three jagged barbs, thin rear shaft. */
public final class GungnirShape {
    public static final double LENGTH=8;
    private static final double[] BACK={0,1.8,1.32,3.05,2.5,4.35,3.8,8};
    private static final double[] WIDTH={0,.62,.15,1.02,.17,.66,.085,0};
    private GungnirShape() {}
    public static int points() {return BACK.length;}
    public static double back(int index) {return BACK[index];}
    public static double width(int index) {return WIDTH[index];}
}
