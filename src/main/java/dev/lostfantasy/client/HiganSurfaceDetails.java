package dev.lostfantasy.client;

import dev.lostfantasy.world.HiganTerrain;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

/** River detail geometry with distance rejection before any world reads. */
final class HiganSurfaceDetails {
    static final double RADIUS=96;
    interface Surface {boolean at(int x,int z);}
    interface Quad {void emit(double x0,double z0,double x1,double z1,double x2,double z2,double x3,double z3,double y,int r,int g,int b,int a);}
    private HiganSurfaceDetails() {}
    static boolean nearRiver(double x,double z,double radius) {
        return z>-4300-radius && z<-1060+radius && HiganTerrain.bankDistance(x,z)<1.7*radius;
    }
    static void emit(double ex,double ez,double time,boolean shaded,Surface surface,Quad output) {
        if(!nearRiver(ex,ez,RADIUS))return;
        Long2ByteOpenHashMap samples=new Long2ByteOpenHashMap(2048);
        int start=(int)Math.floor((ez-RADIUS-24)/8),end=(int)Math.ceil((ez+RADIUS)/8);
        int firstRow=(int)Math.floor((ex-RADIUS-5)/12),lastRow=(int)Math.ceil((ex+RADIUS+5)/12);
        for(int strip=start;strip<=end;strip++)for(int row=firstRow;row<=lastRow;row++) {
            int hash=HiganTerrain.hash(strip,row)&0x7fffffff;
            double phase=(time*.014+hash%80)/8.0,progress=phase-Math.floor(phase);
            double baseZ=strip*8+(hash%71)/10.0+progress*8,offset=row*12+(hash%27)*.1;
            double len=3+hash%60*.10,width=.012+(hash%9)*.007;
            double drift=Math.sin(time*.003+(hash%100))*.6;
            double span=Math.max(len,8),cx=offset;
            double margin=.406*span+1.5;
            if(!nearBox(ex,ez,cx-margin,baseZ-.2,cx+margin,baseZ+span+.2))continue;
            for(int k=0;k<14;k++) {
                double z0=baseZ+len*k/14,z1=baseZ+len*(k+1)/14;
                double x0=offset+drift+.7*Math.sin(z0*.065+row);
                double x1=offset+drift+.7*Math.sin(z1*.065+row);
                if(!nearBox(ex,ez,Math.min(x0,x1)-width,z0,Math.max(x0,x1)+width,z1))continue;
                double taper=Math.sin(Math.PI*(k+.5)/14),y=HiganTerrain.WATER+.902+Math.floorMod(row,13)*.0003;
                int alpha=(int)((shaded?22:42)*taper*Math.sin(Math.PI*progress)*fade(ex,ez,(x0+x1)*.5,(z0+z1)*.5));
                if(alpha==0 || !sample(samples,surface,x0,z0) || !sample(samples,surface,x1,z1))continue;
                output.emit(x0-width*taper,z0,x1-width*taper,z1,x1+width*taper,z1,x0+width*taper,z0,y,166,197,178,alpha);
            }
            double pz=baseZ+(time*.012+hash%60)%8,px=offset+Math.sin(pz*.12+row)*.5;
            if(!nearBox(ex,ez,px-.1,pz-.16,px+.1,pz+.18))continue;
            int alpha=(int)(85*fade(ex,ez,px,pz));
            if(alpha>0 && sample(samples,surface,px,pz))output.emit(px-.09,pz-.16,px+.035,pz-.06,px+.10,pz+.18,px-.03,pz+.09,HiganTerrain.WATER+.918,174,214,215,alpha);
        }
    }
    private static boolean sample(Long2ByteOpenHashMap cache,Surface surface,double x,double z) {
        int bx=(int)Math.floor(x),bz=(int)Math.floor(z);long key=((long)bx<<32)^(bz&0xffffffffL);
        byte known=cache.get(key);if(known!=0)return known==1;
        boolean water=surface.at(bx,bz);cache.put(key,(byte)(water?1:2));return water;
    }
    private static boolean nearBox(double ex,double ez,double x0,double z0,double x1,double z1) {
        double dx=Math.max(0,Math.max(x0-ex,ex-x1)),dz=Math.max(0,Math.max(z0-ez,ez-z1));
        return dx*dx+dz*dz<=RADIUS*RADIUS;
    }
    private static double fade(double ex,double ez,double x,double z) {
        double distance=Math.hypot(x-ex,z-ez);return Math.max(0,Math.min(1,(RADIUS-distance)/16));
    }
}
