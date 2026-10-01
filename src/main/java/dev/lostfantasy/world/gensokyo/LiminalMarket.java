package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.entity.EntityHouseSpirit;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Lantern-lit fair on the route to the river, with open counters and working back rooms. */
final class LiminalMarket {
    private LiminalMarket() {}
    static void build(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,GensokyoAtlas.LIMINAL_ROAD);
        a.box(-76,-3,-138,76,0,138,STONE);a.box(-76,1,-138,76,29,138,AIR);
        a.box(-75,0,-137,75,0,137,Blocks.GRAVEL.getDefaultState());
        a.box(-10,0,-138,10,0,138,Blocks.STONE.getStateFromMeta(6));
        for(int side:new int[]{-1,1})for(int i=0;i<5;i++)stall(a,side*42,-57+i*38,i,side);
        service(a,-71,-95,-17,-124,true);service(a,17,-95,71,-124,false);
        for(int z=-76;z<=120;z+=38)for(int x:new int[]{-13,13}) {
            a.box(x,1,z,x,7,z,LOG);a.box(x-2,8,z,x+2,8,z,DARK);
            for(int dx:new int[]{-2,2})a.block(x+dx,7,z,ModBlocks.RED_LANTERN.getDefaultState());
            a.block(x,9,z,SLAB);
        }
        for(int x:new int[]{-9,9}) {a.box(x,1,128,x,11,128,LOG);a.block(x,12,128,SLAB);}
        a.box(-10,10,128,10,11,128,DARK);a.sign(0,10,129,EnumFacing.SOUTH,"中有之道","");
        a.room("缘日街",0,0,0);a.room("南路口",0,0,136);a.room("河岸方向",0,0,-136);
    }
    private static void stall(GensokyoArchitecture a,int x,int z,int type,int side) {
        int edge=x-side*14;
        a.box(x-15,0,z-12,x+15,0,z+12,WOOD);
        for(int px:new int[]{x-14,x+14})for(int pz:new int[]{z-11,z+11})a.box(px,1,pz,px,8,pz,LOG);
        a.box(x+side*14,1,z-11,x+side*14,5,z+11,DARK);
        a.box(x-14,1,z-11,x+14,4,z-11,WOOD);
        a.box(edge,1,z-7,edge,1,z+6,WOOD);a.box(edge,2,z-7,edge,2,z+6,Blocks.WOODEN_SLAB.getDefaultState());
        int[] colors={14,10,5,4,11};
        for(int dx=-16;dx<=16;dx++) {
            int y=9+(16-Math.abs(dx))/5;
            a.box(x+dx,y,z-13,x+dx,y,z+13,Blocks.WOOL.getStateFromMeta(Math.floorMod(dx,5)<2?0:colors[type]));
            if(Math.abs(dx)==16)a.box(x+dx,y-1,z-13,x+dx,y-1,z+13,Blocks.WOOL.getStateFromMeta(colors[type]));
        }
        a.box(x-13,9,z-10,x+13,9,z-10,DARK);a.block(x,8,z-10,ModBlocks.RED_LANTERN.getDefaultState());
        a.chest(x-side*7,0,z-8,type==3?"bookbinding":type==4?"castle_crafts":"liminal_fair");
        a.chest(x+side*8,0,z-8,type==4?"castle_crafts":"liminal_fair");
        if(type==0) {
            // Shallow contained scooping tubs; no ticking ornamental fish entities.
            for(int dz:new int[]{-3,5}) {
                a.box(x-5,0,z+dz-2,x+5,1,z+dz+2,WOOD);a.box(x-4,1,z+dz-1,x+4,1,z+dz+1,Blocks.WATER.getDefaultState());
            }
        } else if(type==1 || type==2) {
            a.table(x-9,0,z+1,18);
            for(int dx:new int[]{-7,-2,3,7})a.block(x+dx,3,z+1,ModBlocks.LACQUER_BOWL.getDefaultState());
            a.block(x+side*9,1,z+8,type==2?Blocks.FURNACE.getDefaultState():Blocks.CRAFTING_TABLE.getDefaultState());
        } else if(type==3) {
            VillageJoinery.lowDesk(a,x-9,0,z+2,16);a.box(x+side*11,1,z-3,x+side*11,3,z+6,Blocks.BOOKSHELF.getDefaultState());
            a.block(x+side*9,1,z+9,Blocks.CRAFTING_TABLE.getDefaultState());
        } else {
            a.table(x-8,0,z+2,14);a.block(x+side*10,1,z+8,Blocks.CRAFTING_TABLE.getDefaultState());
            a.block(x-side*7,1,z+8,Blocks.ANVIL.getDefaultState());
        }
        String[] names={"捞鱼摊","人魂糖果屋","茶食摊","灯笼纸物摊","旅具修补摊"};
        a.sign(edge-side,5,z-7,side<0?EnumFacing.EAST:EnumFacing.WEST,names[type],"");
        a.block(edge,5,z-7,DARK);a.room(names[type],x-side*9,0,z+9);
        // Small displays sit directly on the counter, keeping the fair's main street open.
        a.box(edge,2,z-7,edge,2,z+6,Blocks.WOODEN_SLAB.getStateFromMeta(9));
        for(int dz:new int[]{-4,1,5})a.block(edge,3,z+dz,
                type==3?ModBlocks.RESEARCH_NOTES.getDefaultState():type==4?Blocks.FLOWER_POT.getDefaultState():ModBlocks.LACQUER_BOWL.getDefaultState());
        if(type==1 || type==2)a.box(x-9,2,z+1,x+8,2,z+1,Blocks.WOODEN_SLAB.getStateFromMeta(9));
        for(int dz:new int[]{-11,11})a.box(x-13,5,z+dz,x+13,5,z+dz,DARK);
    }
    private static void service(GensokyoArchitecture a,int x1,int south,int x2,int north,boolean kitchen) {
        VillageJoinery.house(a,x1,north,x2,south,2,1);int mid=(x1+x2)/2;
        a.openZ(mid,south,2,2,3);a.stairsSouth(mid,south+1,0,2,4);
        VillageJoinery.wallX(a,mid,north+1,south-1,2,south-6);
        if(kitchen) {
            for(int x:new int[]{x1+5,x1+9,x1+13})a.block(x,3,north+5,Blocks.FURNACE.getDefaultState());
            a.table(x1+5,2,north+15,13);a.block(x1+17,3,north+5,Blocks.CAULDRON.getDefaultState());
            for(int x:new int[]{mid+5,mid+10,mid+15})a.chest(x,2,north+5,"village_pantry");
        } else {
            for(int x:new int[]{x1+5,x1+10,mid+5,mid+10})a.bed(x,2,north+7);
            for(int x:new int[]{x1+17,mid+17})a.chest(x,2,north+5,"palace_household");
            VillageJoinery.lowDesk(a,x1+5,2,south-9,12);VillageJoinery.lowDesk(a,mid+5,2,south-9,12);
        }
        for(int x:new int[]{x1+9,mid+9})VillageJoinery.lantern(a,x,2,north+15);
        a.room(kitchen?"备食间":"歇宿西间",mid-6,2,south-6);a.room(kitchen?"后仓":"歇宿东间",mid+6,2,south-6);
    }
    static void install(Chunk c) {
        GensokyoAtlas s=GensokyoAtlas.LIMINAL_ROAD;
        if(Math.abs((c.x<<4)-s.x)>90 || Math.abs((c.z<<4)-s.z)>140)return;
        for(int i=0;i<6;i++) {
            BlockPos p=new BlockPos(s.x+(i%2==0?-17:17),s.y+1.8,s.z-53+(i/2)*62);
            if(c.x!=p.getX()>>4 || c.z!=p.getZ()>>4)continue;
            EntityHouseSpirit spirit=new EntityHouseSpirit(c.getWorld());spirit.anchor(p,i*1.7);c.addEntity(spirit);
        }
    }
}
