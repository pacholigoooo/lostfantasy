package dev.lostfantasy.world.gensokyo;

/** Regional cave geometry. Computed once per generated column, never during rendering. */
final class BlazingHellTerrain {
    static final int CENTER_Z=1460, RADIUS_X=720, RADIUS_Z=1140, LAVA_Y=32;
    private static final int[][] PIERS={{-285,-270,35,123},{295,-160,43,149},{-375,135,55,172},
            {330,315,46,139},{-100,480,40,163},{120,-405,32,104}};
    private final GensokyoNoise noise;
    BlazingHellTerrain(GensokyoNoise noise) {this.noise=noise;}
    Column column(int x,int z) {
        // The palace's barred observation crack opens into the northern end of the region.
        if(Math.abs(x)<=24 && z>=Chireiden.Z+100 && z<710) {
            int width=16+(z-Chireiden.Z-100)/5;
            if(Math.abs(x)<=width)return new Column(46,83+(z-620)/3,49);
        }
        if(Math.abs(x)>RADIUS_X+30 || Math.abs(z-CENTER_Z)>RADIUS_Z+30)return null;
        double dz=z-CENTER_Z;
        double d=x*(double)x/(RADIUS_X*(double)RADIUS_X)+dz*dz/(RADIUS_Z*(double)RADIUS_Z);
        d+=noise.value(x,z,127,941)*.035;
        if(d>=1)return null;
        double broad=noise.value(x,z,153,942),cut=noise.value(x,z,57,943);
        int floor=15+(int)Math.round((broad+.25)*32+cut*9+Math.pow(d,3)*38);
        floor=Math.max(9,Math.min(77,floor));
        int roof=Math.min(246,52+(int)Math.round(189*Math.sqrt(1-d)+noise.value(x,z,43,944)*6));
        // Low approach first; the full-height cave reveals itself after the descent.
        roof=Math.min(roof,68+Math.max(0,z-630)/2);
        for(int[] pier:PIERS) {
            double distance=Math.hypot(x-pier[0],dz-pier[1]),radius=pier[2]*(1+.24*noise.value(x,z,19,945));
            if(distance<radius)floor=Math.max(floor,LAVA_Y+(int)Math.round(pier[3]*Math.pow(1-distance/radius,.6)));
            if(distance<radius*.65)roof-=Math.max(0,(int)Math.round(27*(1-distance/(radius*.65))));
        }
        // A bedrock roof and a continuous rock floor always enclose the cavity.
        if(roof<=floor+3)return null;
        return new Column(floor,roof,LAVA_Y);
    }
    static final class Column {
        final int floor,roof,lava;
        Column(int floor,int roof,int lava) {this.floor=floor;this.roof=roof;this.lava=lava;}
    }
}
