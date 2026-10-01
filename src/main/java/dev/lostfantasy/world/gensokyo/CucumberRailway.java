package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A sealed, gently graded rail adit, with a two-block footway on either side. */
final class CucumberRailway {
    private CucumberRailway() {}
    static List<BlockPos> route() {
        List<BlockPos> points=new ArrayList<>();
        int dz=GensokyoAtlas.WATERFALL.z-GensokyoAtlas.CUCUMBER_FACTORY.z-56;
        points.add(new BlockPos(25,0,-35));line(points,25,0,-45);line(points,168,0,-45);
        // Follow the east bank inside the rock; the former X=68 bore crossed the open river.
        line(points,168,13,dz);line(points,0,13,dz);
        return points;
    }
    private static void line(List<BlockPos> points,int x,int y,int z) {
        BlockPos start=points.get(points.size()-1);int length=Math.abs(x-start.getX())+Math.abs(z-start.getZ());
        for(int i=1;i<=length;i++) {
            double fraction=y==start.getY()?0:Math.max(0,Math.min(1,(i-5.0)/(length-10)));
            points.add(new BlockPos(start.getX()+Integer.signum(x-start.getX())*i,
                    start.getY()+(int)Math.round((y-start.getY())*fraction),start.getZ()+Integer.signum(z-start.getZ())*i));
        }
    }
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.CUCUMBER_FACTORY);
        List<BlockPos> path=route();
        // Shells first, then bores: intersecting sections cannot wall off a bend.
        for(int i=5;i<path.size();i++) {
            BlockPos p=path.get(i);a.box(p.getX()-4,p.getY()-1,p.getZ()-4,p.getX()+4,p.getY()+6,p.getZ()+4,STONE);
        }
        for(int i=0;i<path.size();i++) {
            BlockPos p=path.get(i);
            a.box(p.getX()-3,p.getY()+1,p.getZ()-3,p.getX()+3,p.getY()+5,p.getZ()+3,AIR);
            a.box(p.getX()-3,p.getY(),p.getZ()-3,p.getX()+3,p.getY(),p.getZ()+3,STONE);
        }
        observation(plan);
        footEntrance(plan);
        // Reassert each precise rail support after neighbouring slope sections have overlapped.
        for(int i=0;i<path.size();i++) {
            BlockPos p=path.get(i);BlockRailBase.EnumRailDirection shape=shape(path,i);
            boolean powered=i>3 && i<path.size()-4 && i%12==0 && shape.getMetadata()<6;
            IBlockState rail=powered?Blocks.GOLDEN_RAIL.getDefaultState().withProperty(BlockRailPowered.SHAPE,shape)
                    .withProperty(BlockRailPowered.POWERED,true):Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE,shape);
            a.block(p.getX(),p.getY(),p.getZ(),powered?Blocks.REDSTONE_BLOCK.getDefaultState():STONE);
            a.block(p.getX(),p.getY()+1,p.getZ(),rail);a.box(p.getX(),p.getY()+2,p.getZ(),p.getX(),p.getY()+4,p.getZ(),AIR);
            if(i%9==4)a.block(p.getX(),p.getY()+5,p.getZ(),Blocks.SEA_LANTERN.getDefaultState());
        }
        BlockPos first=path.get(0),last=path.get(path.size()-1);
        a.block(first.getX(),first.getY()+1,first.getZ()+1,LOG);
        a.block(last.getX()-1,last.getY()+1,last.getZ(),LOG);
        BlockPos p=path.get(path.size()/2);a.room("轨道中段",p.getX()+2,p.getY(),p.getZ());
    }
    private static void footEntrance(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.WATERFALL);
        List<BlockPos> path=new ArrayList<>();path.add(new BlockPos(-135,14,17));line(path,-135,8,-56);line(path,-7,8,-56);
        for(int i=5;i<path.size();i++) {
            BlockPos p=path.get(i);a.box(p.getX()-3,p.getY()-1,p.getZ()-3,p.getX()+3,p.getY()+5,p.getZ()+3,STONE);
        }
        for(BlockPos p:path) {
            a.box(p.getX()-2,p.getY()+1,p.getZ()-2,p.getX()+2,p.getY()+4,p.getZ()+2,AIR);
            a.box(p.getX()-2,p.getY(),p.getZ()-2,p.getX()+2,p.getY(),p.getZ()+2,STONE);
        }
        for(int i=5;i<path.size();i+=9) {
            BlockPos p=path.get(i);a.block(p.getX(),p.getY()+4,p.getZ(),Blocks.SEA_LANTERN.getDefaultState());
        }
        a.room("瀑后步道入口",-135,14,18);
    }
    private static BlockRailBase.EnumRailDirection shape(List<BlockPos> path,int index) {
        BlockPos p=path.get(index),before=path.get(Math.max(0,index-1)),after=path.get(Math.min(path.size()-1,index+1));
        BlockPos up=before.getY()>p.getY()?before:after.getY()>p.getY()?after:null;
        if(up!=null) {
            if(up.getX()>p.getX())return BlockRailBase.EnumRailDirection.ASCENDING_EAST;
            if(up.getX()<p.getX())return BlockRailBase.EnumRailDirection.ASCENDING_WEST;
            return up.getZ()>p.getZ()?BlockRailBase.EnumRailDirection.ASCENDING_SOUTH:BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
        }
        boolean east=before.getX()>p.getX() || after.getX()>p.getX(),west=before.getX()<p.getX() || after.getX()<p.getX();
        boolean north=before.getZ()<p.getZ() || after.getZ()<p.getZ(),south=before.getZ()>p.getZ() || after.getZ()>p.getZ();
        if(east && south)return BlockRailBase.EnumRailDirection.SOUTH_EAST;
        if(west && south)return BlockRailBase.EnumRailDirection.SOUTH_WEST;
        if(east && north)return BlockRailBase.EnumRailDirection.NORTH_EAST;
        if(west && north)return BlockRailBase.EnumRailDirection.NORTH_WEST;
        return east || west?BlockRailBase.EnumRailDirection.EAST_WEST:BlockRailBase.EnumRailDirection.NORTH_SOUTH;
    }
    private static void observation(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.WATERFALL);
        for(int x=-9;x<=9;x++) {
            int front=MountainCascade.lip(x)-1;
            a.box(x,8,-60,x,8,front,STONE);a.box(x,15,-60,x,15,front,STONE);
            if(Math.abs(x)==9)a.box(x,9,-60,x,14,front,STONE);
            else {
                a.box(x,9,-60,x,14,front-1,AIR);
                a.box(x,9,front,x,14,front,Blocks.GLASS.getDefaultState());
            }
        }
        // The rail enters from the east; its support and track remain undisturbed at y=145/146.
        a.box(8,9,-59,9,13,-53,AIR);
        for(int x:new int[]{-5,5})a.block(x,14,-49,Blocks.SEA_LANTERN.getDefaultState());
        a.box(-7,9,-46,-7,9,-41,WOOD);a.room("瀑后停靠台",4,8,-48);
    }
}
