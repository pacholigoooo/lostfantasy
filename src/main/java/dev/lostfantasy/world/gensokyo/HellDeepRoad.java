package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A descending foot route through the deep cave; its southern end joins the existing city approach. */
final class HellDeepRoad {
    private static final IBlockState ROCK=ModBlocks.COLUMNAR_BASALT.getDefaultState(),
            RAIL=Blocks.COBBLESTONE_WALL.getDefaultState();
    // x, z, floor above the city's datum. Flat landings separate all changes of direction.
    private static final int[][] ROUTE={{-52,-904,120},{-52,-846,120},{-156,-846,120},
            {-156,-656,84},{-156,-616,84},{156,-616,84},{156,-298,24},
            {-52,-298,24},{-52,-190,0},{-52,-164,0}};
    private HellDeepRoad() {}
    static BlockPos gate() {return OldHellWorld.local(-52,121,-900);}
    static BlockPos arrival() {return OldHellWorld.local(-52,121,-887);}

    static GensokyoBlueprint create() {
        GensokyoBlueprint p=new GensokyoBlueprint();
        GensokyoArchitecture a=new GensokyoArchitecture(p,OldHellWorld.ORIGIN,0,0,"地狱的深道");
        // The upper portal has a closed stone back and enough headroom for both travel directions.
        a.box(-60,116,-909,-44,134,-850,ROCK);
        a.box(-56,121,-905,-48,130,-850,AIR);
        for(int i=1;i<ROUTE.length;i++)walkway(a,ROUTE[i-1],ROUTE[i]);
        bridgeArches(a);
        for(int[] point:ROUTE)junction(a,point[0],point[2],point[1]);
        rest(a,-156,84,-640,-1,"岩壁歇脚台");
        rest(a,156,24,-298,1,"临谷歇脚台");
        a.box(-47,121,-878,-47,125,-878,ROCK);
        a.sign(-48,124,-878,EnumFacing.WEST,"地狱的深道","旧都 ↓");
        a.room("上层来路",-52,120,-884);
        a.room("跨谷长桥",0,84,-616);
        a.room("下层转折",-52,24,-298);
        a.room("旧都接续",-52,0,-167);
        p.seal();return p;
    }

    static boolean contains(BlockPos position) {
        int x=position.getX()-OldHellWorld.ORIGIN.x,y=position.getY()-OldHellWorld.ORIGIN.y,
                z=position.getZ()-OldHellWorld.ORIGIN.z;
        for(int i=1;i<ROUTE.length;i++) {
            int[] start=ROUTE[i-1],end=ROUTE[i];boolean alongZ=start[0]==end[0];
            int length=Math.abs(end[0]-start[0])+Math.abs(end[1]-start[1]);
            int at=alongZ?(z-start[1])*Integer.signum(end[1]-start[1])
                    :(x-start[0])*Integer.signum(end[0]-start[0]);
            int side=alongZ?Math.abs(x-start[0]):Math.abs(z-start[1]);
            if(at<0 || at>length || side>4)continue;
            int f=floor(start,end,at,length);
            if(y>=f && y<=f+9)return true;
        }
        return false;
    }

    private static int floor(int[] start,int[] end,int at,int length) {
        double t=Math.max(0,Math.min(1,(at-6)/(double)Math.max(1,length-12)));
        return (int)Math.round(GensokyoNoise.lerp(start[2],end[2],t));
    }

    private static void walkway(GensokyoArchitecture a,int[] start,int[] end) {
        int dx=Integer.signum(end[0]-start[0]),dz=Integer.signum(end[1]-start[1]);
        boolean alongZ=dx==0;int length=Math.abs(end[0]-start[0])+Math.abs(end[1]-start[1]);
        EnumFacing uphill=alongZ?(dz>0?EnumFacing.NORTH:EnumFacing.SOUTH)
                :(dx>0?EnumFacing.WEST:EnumFacing.EAST);
        int previous=start[2];
        for(int n=0;n<=length;n++) {
            int x=start[0]+dx*n,z=start[1]+dz*n,f=floor(start,end,n,length);
            int hx=alongZ?4:0,hz=alongZ?0:4;
            a.box(x-hx,f-3,z-hz,x+hx,f,z+hz,ROCK);
            a.box(x-hx,f+1,z-hz,x+hx,f+9,z+hz,AIR);
            a.box(x-hx,f,z-hz,x+hx,f,z+hz,STONE);
            if(f<previous)a.box(x-(alongZ?3:0),f+1,z-(alongZ?0:3),
                    x+(alongZ?3:0),f+1,z+(alongZ?0:3),
                    Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,uphill));
            for(int side:new int[]{-1,1}) {
                int sx=x+(alongZ?side*4:0),sz=z+(alongZ?0:side*4);
                a.block(sx,f+1,sz,RAIL);
                if(n%24==0 && z!=-616) {
                    a.box(sx,-60,sz,sx,f-4,sz,ROCK);
                    a.box(sx,f+2,sz,sx,f+3,sz,RAIL);
                    a.block(sx,f+4,sz,ModBlocks.RED_LANTERN.getDefaultState());
                    a.block(sx,f+5,sz,SLAB);
                }
            }
            previous=f;
        }
    }

    private static void bridgeArches(GensokyoArchitecture a) {
        int f=84,z=-616;
        for(int x=-156;x<=156;x+=52)for(int side:new int[]{-1,1}) {
            a.box(x-2,-60,z+side*3-1,x+2,f-4,z+side*3+1,ROCK);
            a.box(x,f+1,z+side*4,x,f+3,z+side*4,STONE);
            a.block(x,f+4,z+side*4,ModBlocks.RED_LANTERN.getDefaultState());
            a.block(x,f+5,z+side*4,SLAB);
        }
        for(int x=-155;x<156;x++) {
            double t=(Math.floorMod(x+156,52)-26)/26.0;
            int bottom=f-4-(int)Math.round(17*t*t);
            for(int side:new int[]{-1,1})a.box(x,bottom,z+side*3,x,f-4,z+side*3,STONE);
        }
    }

    private static void junction(GensokyoArchitecture a,int x,int f,int z) {
        a.box(x-4,f-3,z-4,x+4,f,z+4,STONE);
        a.box(x-4,f+1,z-4,x+4,f+7,z+4,AIR);
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++) {
            if(Math.abs(dx)!=4 && Math.abs(dz)!=4)continue;
            BlockPos pos=OldHellWorld.local(x+dx,f,z+dz);
            for(EnumFacing side:EnumFacing.HORIZONTALS) {
                BlockPos outside=pos.offset(side);
                if(!a.plan.at(outside.getX(),outside.getY(),outside.getZ()).isFullCube()) {
                    a.block(x+dx,f+1,z+dz,RAIL);break;
                }
            }
        }
    }

    private static void rest(GensokyoArchitecture a,int x,int f,int z,int side,String name) {
        int mid=x+side*17;
        a.box(mid-11,f-3,z-10,mid+11,f,z+10,ROCK);
        a.box(mid-11,f+1,z-10,mid+11,f+9,z+10,AIR);
        for(int sx:new int[]{mid-11,mid+11}) {
            a.box(sx,f+1,z-10,sx,f+1,z+10,RAIL);
            for(int sz:new int[]{z-10,z+10})a.box(sx,-60,sz,sx,f-4,sz,ROCK);
        }
        for(int sz:new int[]{z-10,z+10})a.box(mid-11,f+1,sz,mid+11,f+1,sz,RAIL);
        // A seven-block opening meets the stair lane; the short throat keeps a guarded edge.
        a.box(Math.min(x,mid),f-2,z-3,Math.max(x,mid),f,z+3,STONE);
        a.box(Math.min(x,mid),f+1,z-3,Math.max(x,mid),f+5,z+3,AIR);
        for(int sz:new int[]{z-3,z+3})a.box(Math.min(x+side*4,mid-side*11),f+1,sz,
                Math.max(x+side*4,mid-side*11),f+1,sz,RAIL);
        a.box(mid-7,f+1,z+6,mid+1,f+1,z+6,Blocks.STONE_BRICK_STAIRS.getDefaultState());
        a.chest(mid+7,f,z+6,"old_hell_household");
        a.block(mid+7,f+1,z-6,Blocks.CAULDRON.getDefaultState()
                .withProperty(net.minecraft.block.BlockCauldron.LEVEL,3));
        a.lamp(mid-8,f,z-7);a.room(name,mid,f,z);
    }
}
