package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Post-and-beam cliff houses. Local zero is the occupied ground floor, not the mountain foot. */
final class TenguJoinery {
    static final IBlockState POST=Blocks.LOG2.getStateFromMeta(1),PLASTER=Blocks.CONCRETE.getStateFromMeta(0),
            RAIL=Blocks.DARK_OAK_FENCE.getDefaultState(),TILE=Blocks.STONE_SLAB.getStateFromMeta(5);
    private TenguJoinery() {}
    static void house(GensokyoArchitecture a,TenguLayout.Plot p) {
        int w=p.w,d=p.d,ceiling=p.floors*6;
        a.box(-w-4,1,-d-4,w+4,Math.min(254-p.y,ceiling+22),d+7,AIR);
        a.box(-w-1,-4,-d-1,w+1,-1,-d+3,STONE);
        a.box(-w-1,-1,-d-1,w+1,-1,d+5,DARK);
        // Posts continue into the mountain below the stream-facing ledges.
        for(int x:new int[]{-w,-w/2,w/2,w})for(int z:new int[]{-d,d+4}) {
            a.box(x,55-p.y,z,x,-2,z,POST);
            if(z>0)for(int n=1;n<=5;n++)for(int side:new int[]{-1,1})
                a.block(x+side*n,-7+n,z,Blocks.DARK_OAK_STAIRS.getDefaultState()
                        .withProperty(BlockStairs.HALF,BlockStairs.EnumHalf.TOP).withProperty(BlockStairs.FACING,side<0?EnumFacing.WEST:EnumFacing.EAST));
        }
        a.box(-w,0,-d,w,ceiling,d,PLASTER);
        a.box(-w+1,1,-d+1,w-1,ceiling-1,d-1,AIR);
        for(int floor=0;floor<=ceiling;floor+=6)a.box(-w,floor,-d,w,floor,d,WOOD);
        for(int floor=0;floor<ceiling;floor+=6) {
            for(int x=-w;x<=w;x+=6)for(int z:new int[]{-d,d}) {
                a.box(x,floor+1,z,x,floor+5,z,POST);
                if(x+4<w) {
                    a.box(x+1,floor+2,z,x+4,floor+4,z,PAPER);
                    a.block(x+2,floor+3,z,RAIL);
                    a.box(x+1,floor+1,z,x+4,floor+1,z,DARK);
                }
            }
            for(int z=-d;z<=d;z+=6)for(int x:new int[]{-w,w}) {
                a.box(x,floor+1,z,x,floor+5,z,POST);
                if(z+4<d) {
                    a.box(x,floor+2,z+1,x,floor+4,z+4,PAPER);
                    a.block(x,floor+3,z+2,RAIL);
                }
            }
            for(int z:new int[]{-d,d})a.box(-w,floor+5,z,w,floor+5,z,DARK);
            for(int x:new int[]{-w,w})a.box(x,floor+1,-d,x,floor+5,-d,POST);
            if(floor==0 || p.form==1 || p.form==7 || floor==ceiling-6)a.openZ(0,d,floor,2,4);
            light(a,-w/2,floor,0);light(a,w/2,floor,0);
        }
        // The working porch is shared; upper balconies belong to particular house types.
        veranda(a,w,d,0,true);
        if(p.form==1 || p.form==7)for(int floor=6;floor<ceiling;floor+=6)veranda(a,w,d,floor,false);
        else if(p.floors>1) {
            int left=p.form%2==0?-w+2:-3,right=p.form%2==0?3:w-2,floor=ceiling-6;
            a.box(left,floor,d+1,right,floor,d+3,WOOD);
            a.box(left,floor+1,d+3,right,floor+1,d+3,RAIL);
            a.box(left,floor+1,d+1,left,floor+1,d+3,RAIL);a.box(right,floor+1,d+1,right,floor+1,d+3,RAIL);
        }
        if(p.form==4) {
            crossRoof(a,-w-3,-d-3,1,d+3,ceiling+1);
            crossRoof(a,0,-d-1,w+3,d+3,ceiling+2);
        } else if(p.form==6) {
            roof(a,-w-3,-d-3,2,d+3,ceiling+1,1);
            crossRoof(a,0,-d-3,w+3,d+3,ceiling+3);
        } else if(p.form==3)crossRoof(a,-w-3,-d-3,w+3,d+3,ceiling+1);
        else roof(a,-w-3,-d-3,w+3,d+3,ceiling+1,p.form==2 || p.form==5?1:p.form==1?2:0);
        if(p.form==1)for(int floor=6;floor<ceiling;floor+=6) {
            for(int edge:new int[]{-d-2,d+2})a.box(-w-2,floor-1,edge,w+2,floor-1,edge,TILE);
            for(int edge:new int[]{-w-2,w+2})a.box(edge,floor-1,-d-2,edge,floor-1,d+2,TILE);
        }
        for(int x=-w;x<=w;x+=6)for(int z:new int[]{-d-1,d+1}) {
            a.block(x,ceiling,z,DARK);a.block(x,ceiling,z+(z>0?1:-1),up(z>0?EnumFacing.NORTH:EnumFacing.SOUTH));
        }
        roof(a,-w-2,d,w+2,d+6,p.form==1 || p.form==7?ceiling-1:5,0);
        if(p.annex())wing(a,p);
        if(p.form==2) {
            a.box(-w+2,3,d+6,-4,3,d+6,Blocks.CARPET.getStateFromMeta(11));
            a.box(4,3,d+6,w-2,3,d+6,Blocks.CARPET.getStateFromMeta(11));
        }
        // Each opening includes the upper landing's headroom.
        if(p.floors>1)flight(a,-w+4,d-4,0);
        if(p.floors>2)flight(a,w-4,d-4,6);
    }
    private static void wing(GensokyoArchitecture a,TenguLayout.Plot p) {
        int left=-p.w-13,right=-p.w-1,back=-p.d+1,front=Math.min(3,p.d-5);
        a.box(left-2,1,back-2,right,14,front+2,AIR);
        a.box(left,-2,back,right,0,front,WOOD);a.box(left,1,back,right,5,front,PLASTER);
        a.box(left+1,1,back+1,right-1,4,front-1,AIR);a.box(left,6,back,right,6,front,WOOD);
        for(int x:new int[]{left,right})for(int z:new int[]{back,front})a.box(x,55-p.y,z,x,6,z,POST);
        a.box(left,2,back+3,left,4,front-2,PAPER);
        a.openX(-p.w,back+5,0,1,4);a.openX(right,back+5,0,1,4);
        roof(a,left-2,back-2,right+1,front+2,7,0);
        a.chest(left+3,0,back+2,p.form==5?"tengu_printing":"tengu_residence");
        if(p.form==5)a.block(left+6,1,front-2,Blocks.CRAFTING_TABLE.getDefaultState());
        else VillageJoinery.lowDesk(a,left+4,0,front-3,5);
        light(a,left+6,0,back+6);a.room(p.form==5?"备料偏房":"侧屋",left+7,0,back+5);
    }
    static void crossRoof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base) {
        int middle=(x1+x2)/2,span=(x2-x1)/2;
        for(int x=x1;x<=x2;x++) {
            int rise=Math.max(0,span-Math.abs(x-middle)),y=base+rise/2;
            a.box(x,y,z1,x,y,z2,Blocks.STONE_SLAB.getStateFromMeta(rise%2==0?5:13));
            if(rise>3)for(int z:new int[]{z1+3,z2-3})a.box(x,base-1,z,x,y-1,z,PLASTER);
        }
        a.box(middle,base+span/2+1,z1,middle,base+span/2+1,z2,STONE);
        for(int z:new int[]{z1,z2})a.block(middle,base+span/2+2,z,TILE);
    }
    private static void veranda(GensokyoArchitecture a,int w,int d,int floor,boolean entrance) {
        a.box(-w-1,floor,d+1,w+1,floor,d+5,WOOD);
        for(int x:new int[]{-w-1,w+1})a.box(x,floor+1,d+1,x,floor+1,d+5,RAIL);
        if(entrance) {
            a.box(-w-1,floor+1,d+5,-4,floor+1,d+5,RAIL);a.box(4,floor+1,d+5,w+1,floor+1,d+5,RAIL);
        } else a.box(-w-1,floor+1,d+5,w+1,floor+1,d+5,RAIL);
        for(int x=-w;x<=w;x+=6)if(Math.abs(x)>3)a.box(x,floor+1,d+4,x,floor+5,d+4,POST);
        a.box(-w-1,floor+5,d+4,w+1,floor+5,d+4,DARK);
        light(a,-w/2,floor,d+3);light(a,w/2,floor,d+3);
    }
    static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,int form) {
        int depth=(z2-z1)/2,middle=(z1+z2)/2,hip=Math.min(depth,(x2-x1)/2);
        for(int n=0;n<=depth;n++) {
            int inset=form==1?0:form==2?Math.min(n,4):Math.min(n,hip);
            int left=x1+inset,right=x2-inset;
            if(left>right)break;
            int y=base+n/2;
            IBlockState course=Blocks.STONE_SLAB.getStateFromMeta(n%2==0?5:13);
            a.box(left,y,z1+n,right,y,z1+n,course);a.box(left,y,z2-n,right,y,z2-n,course);
            if(inset>0)for(int x:new int[]{left,right})a.box(x,y,z1+n,x,y,z2-n,course);
            if(form!=0 && n>3)for(int x:new int[]{x1+3+inset,x2-3-inset})
                if(x>=left && x<=right)a.box(x,base-1,z1+n,x,y-1,z2-n,PLASTER);
        }
        int trim=form==1?0:form==2?4:hip;
        int left=x1+trim,right=x2-trim;
        if(left<=right) {
            a.box(left,base+depth/2+1,middle,right,base+depth/2+1,middle,STONE);
            a.block(left,base+depth/2+2,middle,TILE);a.block(right,base+depth/2+2,middle,TILE);
        }
        for(int z:new int[]{z1,z2}) {
            a.box(x1-1,base,z,x2+1,base,z,Blocks.STONE_SLAB.getStateFromMeta(13));
            for(int x:new int[]{x1-1,x2+1})a.block(x,base+1,z,TILE);
        }
    }
    static void flight(GensokyoArchitecture a,int x,int south,int floor) {
        for(int n=1;n<=6;n++) {
            int z=south-n+1;
            a.box(x-1,floor+1,z,x+1,floor+n+3,z,AIR);
            if(n>1)a.box(x-1,floor+1,z,x+1,floor+n-1,z,DARK);
            a.box(x-1,floor+n,z,x+1,floor+n,z,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            a.block(x-2,floor+n+1,z,RAIL);a.block(x+2,floor+n+1,z,RAIL);
        }
        a.box(x-1,floor+7,south-8,x+1,floor+9,south-6,AIR);
        a.box(x-1,floor+6,south-8,x+1,floor+6,south-6,WOOD);
    }
    static void light(GensokyoArchitecture a,int x,int floor,int z) {
        a.block(x,floor+5,z,RAIL);a.block(x,floor+4,z,ModBlocks.RED_LANTERN.getDefaultState());
    }
    static IBlockState up(EnumFacing facing) {return Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.HALF,BlockStairs.EnumHalf.TOP).withProperty(BlockStairs.FACING,facing);}
    static void pine(GensokyoArchitecture a,int x,int floor,int z,int height) {
        a.box(x,floor-5,z,x,floor+height,z,Blocks.LOG.getStateFromMeta(1));
        for(int y=height-9;y<=height+2;y+=2) {
            int radius=Math.max(1,(height+3-y)/3);
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
                if(dx*dx+dz*dz<=radius*radius && (dx!=0 || dz!=0 || y>height))
                    a.block(x+dx,floor+y,z+dz,Blocks.LEAVES.getStateFromMeta(5));
        }
    }
}
