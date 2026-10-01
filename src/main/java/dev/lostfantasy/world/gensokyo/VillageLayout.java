package dev.lostfantasy.world.gensokyo;

/** Human Village only. Streets, frontage reservations and courts use the same local plan. */
final class VillageLayout {
    // x1, z1, x2, z2, half width. Main streets are connected by shorter, unequal side lanes.
    static final int[][] STREETS={
            {-458,-78,448,-78,4}, {-450,24,448,24,3},
            {-458,-162,434,-162,2}, {-450,96,208,96,2},
            {-458,176,170,176,3}, {170,176,245,215,3}, {245,215,448,215,3},
            {24,-210,24,220,4}, {24,220,0,240,4},
            {-424,-208,-424,176,2}, {-332,-162,-332,24,2},
            {-198,-210,-198,176,2}, {-72,-162,-72,96,1},
            {178,-162,178,180,2}, {314,-206,314,24,2},
            {434,-162,434,215,2}, {92,24,92,176,1},
            {-264,24,-264,96,1}, {234,-78,234,56,2}
    };
    // Clear shared spaces, not vacant gaps repeated behind every house.
    static final int[][] COURTS={{74,-97,114,-47},{-164,59,-146,83},
            {206,-204,231,-181},{370,-15,401,12}};
    private VillageLayout() {}
    static boolean courtOverlaps(int x1,int z1,int x2,int z2) {
        for(int[] c:COURTS)if(x1<=c[2]+1 && x2>=c[0]-1 && z1<=c[3]+1 && z2>=c[1]-1)return true;
        return false;
    }
    static boolean crosses(double ax,double az,double bx,double bz,int x1,int z1,int x2,int z2,int margin) {
        if(Math.max(ax,bx)<x1-margin || Math.min(ax,bx)>x2+margin ||
                Math.max(az,bz)<z1-margin || Math.min(az,bz)>z2+margin)return false;
        double lo=0,hi=1;
        double[] starts={ax,az},steps={bx-ax,bz-az},mins={x1-margin,z1-margin},maxs={x2+margin,z2+margin};
        for(int axis=0;axis<2;axis++) {
            if(Math.abs(steps[axis])<.0001) {
                if(starts[axis]<mins[axis] || starts[axis]>maxs[axis])return false;
            } else {
                double t1=(mins[axis]-starts[axis])/steps[axis],t2=(maxs[axis]-starts[axis])/steps[axis];
                lo=Math.max(lo,Math.min(t1,t2));hi=Math.min(hi,Math.max(t1,t2));
                if(lo>hi)return false;
            }
        }
        return true;
    }
}
