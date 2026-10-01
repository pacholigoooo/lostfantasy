package dev.lostfantasy.core;

/** Opening dimensions are derived to clear the train body, not just its centre line. */
public final class TrainRiftShape {
    public static final double CENTER=2,HALF_HEIGHT=3.1,HALF_WIDTH=2.6;
    private TrainRiftShape() {}
    public static double widthAt(double y) {
        if(y<=CENTER-HALF_HEIGHT || y>=CENTER+HALF_HEIGHT)return 0;
        double t=(y-CENTER)/HALF_HEIGHT;
        return HALF_WIDTH*Math.pow(Math.max(0,1-t*t),.75);
    }
}
