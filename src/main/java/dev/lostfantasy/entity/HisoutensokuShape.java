package dev.lostfantasy.entity;

import java.util.*;

/** Shared model dimensions allow the actual animated geometry to be inspected offline. Units are blocks. */
public final class HisoutensokuShape {
    public static final List<Part> PARTS;
    public static final int ARM_X=17,ARM_Y=48;
    static {
        List<Part> p=new ArrayList<>();
        // Rounded, stepped fabric panels and darker collars distinguish the inflated sections.
        for(int sign:new int[]{-1,1}) {
            int x=sign*9;
            box(p,0,0,x-5,0,-5,10,5,15);box(p,0,1,x-4,5,-4,8,2,12);
            box(p,0,2,x-3,7,-3,6,2,7);
            box(p,0,0,x-4,9,-4,8,12,9);box(p,0,1,x-3,10,5,6,9,1);
            box(p,0,2,x-3,21,-3,6,3,7);box(p,0,0,x-5,24,-5,10,7,11);
            box(p,0,3,x-3,22,4,6,2,2);
        }
        box(p,0,2,-11,29,-5,22,3,11);box(p,0,0,-13,32,-6,26,5,13);
        box(p,0,0,-15,37,-7,30,11,15);box(p,0,1,-13,48,-6,26,4,13);
        box(p,0,3,-10,41,8,20,5,1);box(p,0,1,-8,39,9,16,2,1);
        box(p,0,2,-5,33,7,10,5,1);box(p,0,1,-3,34,8,6,3,1);
        // Broad collar and a compact, visor-like face.
        box(p,0,2,-5,52,-4,10,3,9);
        box(p,0,0,-7,55,-5,14,9,11);box(p,0,1,-6,64,-4,12,3,9);
        box(p,0,2,-6,58,6,12,4,1);box(p,0,4,-5,60,7,4,1,1);box(p,0,4,1,60,7,4,1,1);
        box(p,0,3,-3,55,6,6,2,1);box(p,0,1,-1,57,7,2,3,1);
        for(int sign:new int[]{-1,1}) {
            box(p,0,0,sign*8-2,61,-2,4,6,5);box(p,0,1,sign*10-1,67,-1,2,5,3);
            int joint=sign<0?1:2;
            box(p,joint,2,-3,-5,-4,6,6,9);
            box(p,joint,0,-6,-5,-6,12,8,13);box(p,joint,1,-5,3,-5,10,2,11);
            box(p,joint,0,-4,-17,-4,8,12,9);box(p,joint,1,-3,-15,5,6,8,1);
            box(p,joint,2,-3,-20,-3,6,3,7);
            box(p,joint,0,-5,-30,-5,10,10,11);box(p,joint,1,-4,-31,-4,8,1,9);
            box(p,joint,3,-4,-27,6,8,2,1);
        }
        PARTS=Collections.unmodifiableList(p);
    }
    private HisoutensokuShape() {}
    private static void box(List<Part> p,int joint,int material,float x,float y,float z,int w,int h,int d) {p.add(new Part(joint,material,x,y,z,w,h,d));}
    public static float armAngle(long ticks,float partial) {
        double phase=(Math.floorMod(ticks,480)+partial)*Math.PI*2/480;
        return .08f+(float)((1-Math.cos(phase))*.5)*1.13f;
    }
    public static final class Part {
        public final int joint,material,w,h,d;public final float x,y,z;
        Part(int joint,int material,float x,float y,float z,int w,int h,int d) {this.joint=joint;this.material=material;this.x=x;this.y=y;this.z=z;this.w=w;this.h=h;this.d=d;}
    }
}
