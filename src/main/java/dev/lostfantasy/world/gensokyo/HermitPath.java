package dev.lostfantasy.world.gensokyo;

import net.minecraft.nbt.NBTTagCompound;

/** Sequential walking route. Progress belongs to the player, never a process-wide UUID cache. */
final class HermitPath {
    static final int[][] POINTS={{0,62},{-20,50},{-20,28},{18,28},{18,-2},{-5,-2},{-5,-23}};
    private HermitPath() {}
    static boolean step(NBTTagCompound state,double x,double y,double z,long tick,boolean walking,boolean grounded) {
        int next=state.getInteger("next");
        if(next<0 || next>POINTS.length) {clear(state);next=0;}
        if(!walking || y<.8 || y>3.5) {clear(state);return false;}
        if(next==POINTS.length) {
            if(!grounded || !near(x,z,POINTS[next-1],2.1) || tick<state.getLong("started")
                    || tick-state.getLong("started")>2400) {clear(state);return false;}
            return true;
        }
        if(next>0) {
            long elapsed=tick-state.getLong("tick");double dx=x-state.getDouble("x"),dz=z-state.getDouble("z");
            if(elapsed<0 || elapsed>1 || tick-state.getLong("started")>2400 || dx*dx+dz*dz>16
                    || distance(x,z,POINTS[next-1],POINTS[next])>3.25) {clear(state);next=0;}
        }
        if(grounded && near(x,z,POINTS[next],2.1)) {
            if(next==0)state.setLong("started",tick);
            ++next;
            state.setInteger("next",next);
        }
        if(next>0) {state.setLong("tick",tick);state.setDouble("x",x);state.setDouble("z",z);}
        return next==POINTS.length;
    }
    static double distance(double x,double z,int[] a,int[] b) {
        double dx=b[0]-a[0],dz=b[1]-a[1],t=Math.max(0,Math.min(1,((x-a[0])*dx+(z-a[1])*dz)/(dx*dx+dz*dz)));
        return Math.hypot(x-a[0]-t*dx,z-a[1]-t*dz);
    }
    private static boolean near(double x,double z,int[] p,double r) {return Math.hypot(x-p[0],z-p[1])<=r;}
    private static void clear(NBTTagCompound state) {state.removeTag("next");state.removeTag("tick");state.removeTag("started");state.removeTag("x");state.removeTag("z");state.removeTag("retry");}
}
