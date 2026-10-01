package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Winding cliff lanes, three different crossings and individually sited houses. */
final class TenguSettlement {
    private TenguSettlement() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.TENGU);
        for(TenguLayout.Path path:TenguLayout.PATHS)street(a,path);
        for(TenguLayout.Bridge bridge:TenguLayout.BRIDGES)bridge(a,bridge);gate(a);
        TenguHomes.build(plan);TenguInstitutions.build(plan);TenguWatchtower.build(plan);TenguCourtyards.build(plan);
        a.room("入山道",0,0,330);
        for(TenguLayout.Bridge b:TenguLayout.BRIDGES)
            a.room(b.name+"西接路",b.x1,b.y-a.site.y,b.z);
    }
    private static void street(GensokyoArchitecture a,TenguLayout.Path p) {
        int dx=p.bx-p.x,dz=p.bz-p.z,steps=Math.max(Math.abs(dx),Math.abs(dz));
        boolean vertical=Math.abs(dz)>Math.abs(dx),slope=p.y!=p.by;
        for(int n=0;n<=steps;n++) {
            double t=steps==0?0:n/(double)steps;
            int x=p.x+(int)Math.round(dx*t),z=p.z+(int)Math.round(dz*t),y=p.y+(int)Math.round((p.by-p.y)*t)-a.site.y;
            int previous=p.y+(int)Math.round((p.by-p.y)*Math.max(0,n-1)/(double)Math.max(1,steps))-a.site.y;
            boolean boardwalk=Math.abs(x-TenguTerrain.riverX(z))<TenguTerrain.riverWidth(z)+32;
            IBlockState deck=boardwalk?WOOD:Math.floorMod(x*7+z*11,13)<3?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.GRAVEL.getDefaultState();
            int next=p.height(Math.min(1,(n+1)/(double)Math.max(1,steps)))-a.site.y;
            EnumFacing forward=vertical?(dz>0?EnumFacing.SOUTH:EnumFacing.NORTH):(dx>0?EnumFacing.EAST:EnumFacing.WEST);
            if(slope && (y>previous || y>next))deck=(boardwalk?Blocks.SPRUCE_STAIRS:Blocks.STONE_STAIRS).getDefaultState()
                    .withProperty(BlockStairs.FACING,y>previous?forward:forward.getOpposite());
            int x1=x-(vertical?p.width:0),x2=x+(vertical?p.width:0),z1=z-(vertical?0:p.width),z2=z+(vertical?0:p.width);
            a.box(x1,y+1,z1,x2,y+5,z2,AIR);a.box(x1,y-3,z1,x2,y-1,z2,boardwalk?DARK:STONE);a.box(x1,y,z1,x2,y,z2,deck);
            if(!boardwalk && p.width>=3 && !(deck.getBlock() instanceof BlockStairs))for(int side:new int[]{-1,1})
                if(Math.floorMod(x+z,5)!=0)a.block(x+(vertical?side*p.width:0),y,z+(vertical?0:side*p.width),Blocks.GRASS.getDefaultState());
            if(p.width>=3)for(int side:new int[]{-1,1}) {
                int ex=x+(vertical?side*(p.width+1):0),ez=z+(vertical?0:side*(p.width+1));
                if(junction(p,ex,ez,y+a.site.y))continue;
                if(boardwalk) {a.block(ex,y,ez,DARK);a.block(ex,y+1,ez,TenguJoinery.RAIL);}
                if(n%(boardwalk?18:32)==9) {
                    a.box(ex,y+1,ez,ex,y+4,ez,TenguJoinery.POST);
                    a.block(ex,y+5,ez,TenguJoinery.TILE);
                    int lx=ex+(vertical?-side:0),lz=ez+(vertical?0:-side);
                    a.block(lx,y+4,lz,TenguJoinery.RAIL);a.block(lx,y+3,lz,ModBlocks.RED_LANTERN.getDefaultState());
                }
                if(boardwalk && n%12==0)a.box(ex,55-a.site.y,ez,ex,y-1,ez,TenguJoinery.POST);
            }
        }
    }
    private static boolean junction(TenguLayout.Path own,int x,int z,int y) {
        for(TenguLayout.Path p:TenguLayout.PATHS)if(p!=own) {
            double t=p.t(x,z);
            if(Math.abs(p.height(t)-y)<=1 && Math.hypot(x-p.x-t*(p.bx-p.x),z-p.z-t*(p.bz-p.z))<=p.width+2)return true;
        }
        // Bridge landings meet the contour roads and both ascending cliff walks.
        for(TenguLayout.Bridge b:TenguLayout.BRIDGES)if(x>=b.x1-6 && x<=b.x2+6 && Math.abs(z-b.z)<=b.width+2 && Math.abs(y-b.y)<=1)return true;
        return false;
    }
    private static void bridge(GensokyoArchitecture a,TenguLayout.Bridge b) {
        int z=b.z,floor=b.y-a.site.y,width=b.width;
        for(int x=b.x1;x<=b.x2;x++) {
            int y=b.floor(x)-a.site.y,previous=b.floor(x-1)-a.site.y,next=b.floor(x+1)-a.site.y;
            a.box(x,y+1,z-width,x,y+12,z+width,AIR);
            IBlockState deck=WOOD;
            if(y>previous)deck=Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.EAST);
            else if(y>next)deck=Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.WEST);
            a.box(x,y-1,z-width,x,y-1,z+width,DARK);a.box(x,y,z-width,x,y,z+width,deck);
            for(int side:new int[]{-1,1}) {
                int edge=z+side*width;
                // The lower bridge also receives the perpendicular road from the mountain gate.
                if(b.style!=0 || Math.abs(x)>6)a.block(x,y+1,edge,TenguJoinery.RAIL);
                double t=(x-b.x1)/(double)(b.x2-b.x1);
                if(b.style==2) {
                    int cable=floor+3+(int)Math.round(7*Math.pow(2*t-1,2));
                    a.block(x,cable,edge,TenguJoinery.RAIL);
                    if((x-b.x1)%10==0)a.box(x,y+2,edge,x,cable,edge,TenguJoinery.RAIL);
                } else {
                    int arch=floor-17+(int)Math.round(13*4*t*(1-t));
                    a.box(x,arch,edge,x,arch+1,edge,DARK);
                    if((x-b.x1)%10==0)a.box(x,arch+2,edge,x,y-2,edge,TenguJoinery.POST);
                }
            }
        }
        for(int x:new int[]{b.x1+1,b.x2-1})for(int side:new int[]{-1,1}) {
            int edge=z+side*width;
            a.box(x-1,55-a.site.y,edge-1,x+1,floor-1,edge+1,STONE);
            a.box(x,floor+1,edge,x,floor+(b.style==2?11:4),edge,TenguJoinery.POST);
            a.block(x,floor+(b.style==2?12:5),edge,TenguJoinery.TILE);
        }
        if(b.style==0) {
            for(int x=-21;x<=63;x+=14)for(int side:new int[]{-1,1}) {
                a.box(x,floor+1,z+side*width,x,floor+6,z+side*width,TenguJoinery.POST);
                a.block(x,floor+6,z+side*(width-1),TenguJoinery.RAIL);a.block(x,floor+5,z+side*(width-1),ModBlocks.RED_LANTERN.getDefaultState());
            }
            for(int side:new int[]{-1,1})a.box(-24,floor+6,z+side*width,66,floor+6,z+side*width,DARK);
            TenguJoinery.roof(a,-26,z-width-2,68,z+width+2,floor+7,1);
        }
        a.room(b.name,20,b.floor(20)-a.site.y,z);
    }
    private static void gate(GensokyoArchitecture a) {
        for(int x:new int[]{-10,10}) {
            a.box(x-1,55-a.site.y,301,x+1,1,303,STONE);a.box(x,2,302,x,10,302,TenguJoinery.POST);
            a.block(x,7,303,TenguJoinery.RAIL);a.block(x,6,303,ModBlocks.RED_LANTERN.getDefaultState());
        }
        a.box(-14,9,301,14,10,303,DARK);TenguJoinery.roof(a,-19,297,19,308,11,2);
        a.box(-5,7,303,5,8,303,DARK);a.sign(0,7,304,EnumFacing.SOUTH,"天狗聚落","");
    }
}
