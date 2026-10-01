package dev.lostfantasy.combat;

import dev.lostfantasy.core.EmeraldCity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Plans only against loaded block collision shapes; never replaces terrain or requests chunks. */
public final class EmeraldGround {
    private static final double EPSILON=1e-6;
    private EmeraldGround() {}
    public static List<EmeraldCity.Column> plan(World world,Vec3d origin,Vec3d forward) {
        double ground=floor(world,origin.x,origin.z,origin.y,.15);
        if(!Double.isFinite(ground) || Math.abs(ground-origin.y)>.15) return Collections.emptyList();
        List<EmeraldCity.Column> columns=new ArrayList<>(EmeraldCity.MAX_COLUMNS);
        double heading=Math.atan2(forward.x,forward.z);
        for(int round=0;round<EmeraldCity.RINGS;round++) for(int spoke=0;spoke<EmeraldCity.columnsInRing(round);spoke++) {
            Vec3d direction=EmeraldCity.direction(heading,round,spoke);
            double previous=ground, lastPillar=ground;
            // Every outer position traces its own radial route; barriers cannot be jumped by a later ring.
            for(int step=1;step<=(round+1)*4;step++) {
                double distance=step*.5, x=origin.x+direction.x*distance, z=origin.z+direction.z*distance;
                double y=floor(world,x,z,previous,1.001);
                if(!Double.isFinite(y) || clearance(world,x,y,z,.2)<.35) break;
                previous=y;
                if(step%4!=0) continue;
                double base=y;
                if(Math.abs(base-lastPillar)>1.001 || !supportedBase(world,x,base,z)) break;
                double height=clearance(world,x,base,z,EmeraldCity.RADIUS);
                if(height<.35) break;
                if(step==(round+1)*4)columns.add(new EmeraldCity.Column(x,base,z,height,round));
                lastPillar=previous=base;
            }
        }
        // Inner rings are resolved before outer rings; every ring covers the whole circle.
        return Collections.unmodifiableList(columns);
    }
    static double floor(World world,double x,double z,double expected,double tolerance) {
        AxisAlignedBB probe=new AxisAlignedBB(x-.025,expected-tolerance-.05,z-.025,x+.025,expected+tolerance+.05,z+.025);
        if(!loaded(world,probe)) return Double.NaN;
        double best=Double.NaN;
        for(AxisAlignedBB box:world.getCollisionBoxes(null,probe)) {
            if(box.minX<=x && box.maxX>=x && box.minZ<=z && box.maxZ>=z
                    && Math.abs(box.maxY-expected)<=tolerance && (!Double.isFinite(best) || box.maxY>best)) best=box.maxY;
        }
        return best;
    }
    /** Cover the whole base with collision tops at its height, including holes between sample points. */
    static boolean supportedBase(World world,double x,double y,double z) {
        double radius=EmeraldCity.RADIUS;
        AxisAlignedBB base=new AxisAlignedBB(x-radius,y-.01,z-radius,x+radius,y+.01,z+radius);
        if(!loaded(world,base)) return false;
        List<AxisAlignedBB> tops=new ArrayList<>();
        List<Double> cuts=new ArrayList<>();
        cuts.add(base.minX);cuts.add(base.maxX);
        for(AxisAlignedBB box:world.getCollisionBoxes(null,base)) {
            if(Math.abs(box.maxY-y)>EPSILON) continue;
            double minX=Math.max(base.minX,box.minX),maxX=Math.min(base.maxX,box.maxX);
            double minZ=Math.max(base.minZ,box.minZ),maxZ=Math.min(base.maxZ,box.maxZ);
            if(maxX-minX<=EPSILON || maxZ-minZ<=EPSILON) continue;
            tops.add(new AxisAlignedBB(minX,y,minZ,maxX,y,maxZ));
            cuts.add(minX);cuts.add(maxX);
        }
        Collections.sort(cuts);
        tops.sort(Comparator.comparingDouble(box->box.minZ));
        // Each strip has constant rectangle coverage. Merge its Z intervals to detect every gap.
        for(int i=1;i<cuts.size();i++) {
            if(cuts.get(i)-cuts.get(i-1)<=EPSILON) continue;
            double mid=(cuts.get(i)+cuts.get(i-1))*.5,covered=base.minZ;
            for(AxisAlignedBB top:tops) {
                if(mid<top.minX || mid>top.maxX) continue;
                if(top.minZ>covered+EPSILON) return false;
                covered=Math.max(covered,top.maxZ);
            }
            if(covered<base.maxZ-EPSILON) return false;
        }
        return true;
    }
    static double clearance(World world,double x,double y,double z,double radius) {
        AxisAlignedBB space=new AxisAlignedBB(x-radius,y+.01,z-radius,x+radius,y+EmeraldCity.HEIGHT,z+radius);
        if(!loaded(world,space)) return 0;
        double height=EmeraldCity.HEIGHT;
        for(AxisAlignedBB box:world.getCollisionBoxes(null,space)) height=Math.min(height,Math.max(0,box.minY-y));
        return height;
    }
    static boolean loaded(World world,AxisAlignedBB box) {
        // Vanilla collision queries inspect a one-block rim around their requested box.
        return world.isAreaLoaded(new BlockPos(box.minX-1,box.minY-1,box.minZ-1),new BlockPos(box.maxX+1,box.maxY+1,box.maxZ+1));
    }
}
