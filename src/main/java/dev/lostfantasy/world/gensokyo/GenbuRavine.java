package dev.lostfantasy.world.gensokyo;

/** Basalt benches and hexagonal column walls around the lower mountain tributary. */
final class GenbuRavine {
    private static final double CELL_RADIUS=4.5,ROOT_THREE=Math.sqrt(3);
    private GenbuRavine() {}
    static Sample at(int x,int z,KappaWatercourse.Sample river,GensokyoNoise noise) {
        if(river==null || z<-2030 || z>-1590 || river.distance>89)return null;
        double longitudinal=GensokyoNoise.smooth((z+2030)/60.0)*GensokyoNoise.smooth((-1590-z)/55.0);
        double blend=longitudinal*(1-GensokyoNoise.smooth((river.distance-63)/26));
        if(blend<=0)return null;
        int[] cell=cell(x,z);int cx=(int)Math.round(CELL_RADIUS*1.5*cell[0]);
        int cz=(int)Math.round(CELL_RADIUS*ROOT_THREE*(cell[1]+cell[0]/2.0));
        KappaWatercourse.Sample centre=KappaWatercourse.at(cx,cz);
        double d=centre==null?river.distance:centre.distance;
        double level=centre==null?river.level:centre.level;
        long hash=noise.hash(cell[0],cell[1],208);
        double height;
        if(river.distance<9)height=river.level-5;
        else if(river.distance<12)height=Math.max(Math.floor(river.level),river.level-2+(river.distance-9));
        else if(d<24)height=Math.floor(level)+3+Math.floorMod(hash,4);
        else height=Math.floor(level)+22+Math.floorMod(hash,12);
        // The two levels give each actual hexagonal cell a flat crown and vertical sides.
        return new Sample(height,blend,blend>.70 && river.distance<62);
    }
    static int[] cellCentre(int x,int z) {
        int[] cell=cell(x,z);
        return new int[]{(int)Math.round(CELL_RADIUS*1.5*cell[0]),
                (int)Math.round(CELL_RADIUS*ROOT_THREE*(cell[1]+cell[0]/2.0))};
    }
    private static int[] cell(int x,int z) {
        double q=(2.0/3*x)/CELL_RADIUS,r=(-x/3.0+ROOT_THREE*z/3)/CELL_RADIUS,s=-q-r;
        int qi=(int)Math.round(q),ri=(int)Math.round(r),si=(int)Math.round(s);
        double dq=Math.abs(qi-q),dr=Math.abs(ri-r),ds=Math.abs(si-s);
        if(dq>dr && dq>ds)qi=-ri-si;else if(dr>ds)ri=-qi-si;
        return new int[]{qi,ri};
    }
    static final class Sample {
        final double ground,blend;final boolean basalt;
        Sample(double ground,double blend,boolean basalt) {this.ground=ground;this.blend=blend;this.basalt=basalt;}
    }
}
