package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;
import static dev.lostfantasy.world.gensokyo.VillageHouseParts.*;

/** Street-facing shops, backstreet households and enclosed homes in the Human Village. */
final class VillageHomes {
    static final int SHOP=0,HOME=1,WORKSHOP=2,ROW_HOME=3,COURTYARD=4,INN=5;
    private static final String[] SHOPS={"米铺","布店","木工铺","茶食铺","纸墨铺","陶器铺"};
    private static final int[] DYES={12,11,13,14,9,4};
    static final List<Lot> LOTS=lots();
    private VillageHomes() {}

    static void build(GensokyoBlueprint plan) {
        for(Lot lot:LOTS) {
            GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.VILLAGE,lot.x,lot.z,lot.title(),lot.rotation);
            yard(a,lot);
            switch(lot.type) {
                case SHOP:shop(a,lot);break;
                case HOME:home(a,lot);break;
                case WORKSHOP:workshop(a,lot);break;
                case ROW_HOME:rowHome(a,lot);break;
                case COURTYARD:courtyard(a,lot);break;
                case INN:inn(a,lot);break;
                default:throw new IllegalArgumentException("Unknown village house");
            }
            // The entire frontage meets its own street, including off-centre and paired doors.
            a.box(-lot.half-1,0,lot.depth+3,lot.half+1,0,lot.streetDistance,Blocks.GRAVEL.getDefaultState());
        }
        courts(plan);
    }

    private static void shop(GensokyoArchitecture a,Lot l) {
        int h=l.half,d=l.depth,t=l.variant%SHOPS.length;
        shell(a,-h,-d,h,d,2,l.variant);entrance(a,0,d,1);canopy(a,-h,h,d,DYES[t],SHOPS[t]);
        a.box(-h+2,2,d-4,-3,2,d-3,WOOD);
        if(t==1)a.box(-h+2,3,d-4,-4,3,d-4,Blocks.WOOL.getStateFromMeta(11));
        else if(t==2)a.block(-h+2,3,d-4,Blocks.CRAFTING_TABLE.getDefaultState());
        else if(t==4)a.block(-h+2,3,d-4,ModBlocks.RESEARCH_NOTES.getDefaultState());
        else for(int x=-h+2;x<=-4;x+=3)a.block(x,3,d-4,Blocks.FLOWER_POT.getDefaultState());
        String loot=t==2?"kourindou_tools":t==4?"bookbinding":t==1?"doll_materials":"village_pantry";
        a.chest(-h+2,1,d-8,loot);a.chest(h-1,1,1,loot);
        if(t==4)a.box(h-1,2,4,h-1,4,d-2,Blocks.BOOKSHELF.getDefaultState());
        else if(t==0)a.box(h-2,2,5,h-1,3,7,Blocks.HAY_BLOCK.getDefaultState());
        else a.box(h-1,2,5,h-1,3,d-2,t==1?Blocks.WOOL.getStateFromMeta(14):WOOD);
        VillageJoinery.wallZ(a,-h+1,h-5,-2,1,-2);
        kitchen(a,-h+2,-d+2,1);VillageJoinery.lowDesk(a,-h+2,1,-6,3);
        stair(a,h,-d);
        a.bed(-h+3,6,-d+4);a.bed(-h+6,6,-d+4);a.chest(-h+2,6,d-2,"village_pantry");
        VillageJoinery.wallZ(a,-h+1,h-5,-2,6,-2);
        VillageJoinery.lowDesk(a,-3,6,4,4);
        a.room("店面",0,1,d-3);a.room("后灶",0,1,-6);
        a.room("寝间",0,6,-6);a.room("楼上起居",0,6,1);
        roomLight(a,0,4,1);roomLight(a,0,-6,1);roomLight(a,-2,-6,6);roomLight(a,0,3,6);
        merchandise(a,l,t);
    }

    private static void merchandise(GensokyoArchitecture a,Lot l,int trade) {
        int left=-l.half+2,z=l.depth-4;
        // Each trade uses its own counter display; the centre aisle and stair strip remain open.
        if(trade==0) {
            a.box(left,3,z,left+2,3,z,Blocks.HAY_BLOCK.getDefaultState());
            a.block(left+4,3,z,ModBlocks.LACQUER_BOWL.getDefaultState());
        } else if(trade==1) {
            for(int i=0;i<4;i++)a.block(left+i,3,z,Blocks.WOOL.getStateFromMeta(new int[]{0,11,12,14}[i]));
            a.box(left,4,z,left+3,4,z,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        } else if(trade==2) {
            a.block(left+3,3,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
            a.box(left,2,z-2,left+3,2,z-2,Blocks.LOG.getStateFromMeta(4));
        } else if(trade==3) {
            for(int x=left;x<=-4;x+=2)a.block(x,3,z,ModBlocks.LACQUER_BOWL.getDefaultState());
            a.block(l.half-2,2,4,Blocks.CAULDRON.getStateFromMeta(3));
        } else if(trade==4) {
            a.box(left,2,z-2,left+3,3,z-2,Blocks.BOOKSHELF.getDefaultState());
            a.block(left+3,3,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
        } else {
            a.box(l.half-2,4,5,l.half-1,4,l.depth-2,Blocks.WOODEN_SLAB.getStateFromMeta(9));
            for(int zz=5;zz<=l.depth-2;zz+=3)a.block(l.half-1,5,zz,ModBlocks.LACQUER_BOWL.getDefaultState());
        }
        InteriorFinishes.tatami(a,-l.half+2,-l.depth+2,2,-4,1);
    }

    private static void home(GensokyoArchitecture a,Lot l) {
        int h=l.half,d=l.depth;
        shell(a,-h,-d,h,d,1,l.variant);entrance(a,0,d,1);
        VillageJoinery.wallZ(a,-h+1,h-1,-1,1,0);
        kitchen(a,-h+1,3,1);VillageJoinery.lowDesk(a,1,1,3,3);
        a.bed(-h+2,1,-d+4);a.bed(h-3,1,-d+4);
        a.chest(-h+2,1,-d+2,"village_pantry");a.chest(h-2,1,-d+2);
        a.box(-3,5,d+1,3,5,d+2,SLAB);
        a.room("起居",0,1,d-2);a.room("灶间",-2,1,2);a.room("寝间",0,1,-5);
        roomLight(a,0,4,1);roomLight(a,0,-5,1);
    }

    private static void workshop(GensokyoArchitecture a,Lot l) {
        int h=l.half,d=l.depth;
        shell(a,-h,-d,2,d,1,l.variant);shell(a,3,-d,h,3,1,l.variant+1);
        a.box(2,2,-8,3,4,-4,AIR);entrance(a,-5,d,1);
        VillageJoinery.wallZ(a,-h+1,1,-2,1,-5);
        a.bed(-h+3,1,-d+4);a.bed(-h+6,1,-d+4);
        a.chest(-h+2,1,-d+2,"village_pantry");kitchen(a,-h+1,4,1);
        VillageJoinery.lowDesk(a,-7,1,d-4,3);
        a.box(5,2,-d+3,h-2,2,-d+4,WOOD);
        a.block(6,3,-d+3,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(h-2,1,0,"kourindou_tools");a.block(5,2,-3,Blocks.ANVIL.getDefaultState());
        a.box(3,0,5,h,0,d,Blocks.GRAVEL.getDefaultState());a.openX(2,7,1,1,3);
        a.box(h-4,1,7,h-1,2,8,Blocks.LOG.getStateFromMeta(8));
        a.box(4,5,4,h,5,6,SLAB);a.box(h,1,6,h,4,6,Blocks.SPRUCE_FENCE.getDefaultState());
        a.sign(-5,5,d+1,EnumFacing.SOUTH,l.variant%2==0?"木工":"修配","");
        a.room("起居",-5,1,3);a.room("寝间",-5,1,-7);a.room("工房",8,1,-5);a.room("作业庭",6,0,d-2);
        roomLight(a,-5,4,1);roomLight(a,-5,-7,1);roomLight(a,8,-5,1);
    }

    private static void rowHome(GensokyoArchitecture a,Lot l) {
        int h=l.half,d=l.depth;
        shell(a,-h,-d,h,d,1,l.variant);a.box(0,2,-d+1,0,5,d-1,WHITE);
        for(int side:new int[]{-1,1}) {
            int x1=side<0?-h:0,x2=side<0?0:h,door=(x1+x2)/2;
            entrance(a,door,d,1);VillageJoinery.wallZ(a,x1+1,x2-1,-1,1,door);
            kitchen(a,x1+1,4,1);VillageJoinery.lowDesk(a,x2-5,1,4,3);
            a.bed(x1+3,1,-d+4);a.chest(x2-2,1,-d+2,"village_pantry");
            a.room(side<0?"左户起居":"右户起居",door,1,d-2);
            a.room(side<0?"左户寝间":"右户寝间",door,1,-5);
            roomLight(a,door,4,1);roomLight(a,door,-5,1);
        }
    }

    private static void courtyard(GensokyoArchitecture a,Lot l) {
        int h=l.half,d=l.depth;
        shell(a,-h,-d,h,-3,1,l.variant);shell(a,-h,-2,-h+8,d,1,l.variant+1);
        a.box(-h+2,2,-3,-h+6,4,-2,AIR);a.openX(-h+8,6,1,1,3);entrance(a,0,-3,1);
        for(int x:new int[]{-3,3})VillageJoinery.wallX(a,x,-d+1,-4,1,-8);
        a.bed(-h+3,1,-d+4);a.bed(h-5,1,-d+4);
        a.chest(-h+5,1,-d+2,"village_pantry");a.chest(h-3,1,-d+2,"village_pantry");
        kitchen(a,-h+1,4,1);a.chest(-h+2,1,1,"village_pantry");
        a.box(-h+9,0,-1,h,0,d,Blocks.GRAVEL.getDefaultState());
        a.box(-h+9,1,d,h,2,d,WHITE);a.openZ(0,d,0,2,3);
        for(int x:new int[]{-3,3})a.box(x,1,d,x,4,d,LOG);
        roof(a,-3,d-1,3,d,5,false);
        a.box(h-7,0,3,h-2,0,d-3,Blocks.GRASS.getDefaultState());
        a.box(h-5,1,5,h-5,3,5,LOG);
        a.box(h-7,4,3,h-3,5,7,Blocks.LEAVES.getStateFromMeta(4));
        bench(a,-h+10,5,4);
        a.room("正屋",0,1,-8);a.room("西寝间",-6,1,-6);a.room("东寝间",6,1,-6);
        a.room("侧屋厨房",-h+5,1,8);a.room("小庭",0,0,5);
        roomLight(a,0,-8,1);roomLight(a,-6,-7,1);roomLight(a,6,-7,1);roomLight(a,-h+4,7,1);
    }

    private static void inn(GensokyoArchitecture a,Lot l) {
        int h=l.half,d=l.depth;
        shell(a,-h,-d,h,d,2,l.variant);entrance(a,0,d,2);canopy(a,-h,h,d,12,"旅笼");
        for(int z:new int[]{4,9})VillageJoinery.lowDesk(a,-h+3,1,z,5);
        a.box(5,2,5,10,2,6,WOOD);a.block(6,3,5,ModBlocks.RESEARCH_NOTES.getDefaultState());
        VillageJoinery.wallZ(a,-h+1,h-5,-3,1,0);kitchen(a,-h+2,-d+2,1);
        a.chest(-h+2,1,-6,"village_pantry");a.chest(5,1,-d+2,"village_pantry");
        stair(a,h,-d);
        for(int x:new int[]{-3,3}) {
            VillageJoinery.wallX(a,x,-d+1,d-1,6,-6);a.openX(x,6,6,1,3);
        }
        a.box(-h+1,7,0,-4,10,0,WHITE);a.box(4,7,0,h-1,10,0,WHITE);
        for(int side:new int[]{-1,1})for(int end:new int[]{-1,1}) {
            int x=side<0?-h+3:5,z=end<0?-d+4:d-3;
            a.bed(x,6,z);a.chest(x+2,6,end<0?-d+2:d-5);
            a.room("客房",side<0?-6:8,6,end<0?-4:4);
            roomLight(a,side*6,end*6,6);
        }
        a.room("食堂",0,1,6);a.room("厨房",0,1,-7);a.room("楼上走廊",0,6,0);
        roomLight(a,0,6,1);roomLight(a,0,-7,1);
    }

    private static void yard(GensokyoArchitecture a,Lot l) {
        int h=l.half,d=l.depth,back=-d-l.yard;
        a.box(-h-1,0,back,h+1,0,-d-2,Blocks.GRASS.getDefaultState());
        a.box(-h-2,1,back,h+2,1,back,Blocks.SPRUCE_FENCE.getDefaultState());
        for(int x:new int[]{-h-2,h+2})a.box(x,1,back,x,1,-d-2,Blocks.SPRUCE_FENCE.getDefaultState());
        if(l.yard<6)return;
        a.box(-1,0,back+1,1,0,-d-2,Blocks.GRAVEL.getDefaultState());
        if(l.type==WORKSHOP) {
            a.box(-h,1,back+2,-h+5,2,back+3,Blocks.LOG.getStateFromMeta(8));
        } else for(int x=-h;x<=-3;x++)for(int z=back+2;z<=-d-3;z++) {
            boolean water=x==-h+3;
            a.block(x,0,z,water?Blocks.WATER.getDefaultState():Blocks.FARMLAND.getStateFromMeta(7));
            if(!water)a.block(x,1,z,l.variant%2==0?Blocks.CARROTS.getStateFromMeta(7):Blocks.WHEAT.getStateFromMeta(7));
        }
        bench(a,3,back+2,4);
        a.block(h,1,back+2,Blocks.CAULDRON.getStateFromMeta(3));
        if(l.type!=WORKSHOP && l.variant%3==0) {
            // A rear drying rack has two anchored posts and stops before the house's back eave.
            for(int x:new int[]{4,h-1})a.box(x,1,back+4,x,4,back+4,Blocks.SPRUCE_FENCE.getDefaultState());
            a.box(4,4,back+4,h-1,4,back+4,Blocks.WOODEN_SLAB.getStateFromMeta(1));
            for(int x=5;x<h-1;x+=2)a.block(x,3,back+4,Blocks.WOOL.getStateFromMeta(l.variant%2==0?0:11));
        }
    }

    private static void courts(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.VILLAGE);
        for(int[] c:VillageLayout.COURTS)a.box(c[0],0,c[1],c[2],0,c[3],Blocks.GRAVEL.getDefaultState());
        well(a,101,-58);VillageJoinery.maple(a,81,-56,8,4);bench(a,83,-48,7);
        well(a,-155,72);well(a,388,0);bench(a,373,8,8);
        VillageJoinery.maple(a,222,-190,8,4);bench(a,209,-183,6);
        a.room("商街井边",94,0,-57);a.room("后巷井庭",-151,0,76);
    }

    private static void well(GensokyoArchitecture a,int x,int z) {
        a.box(x-2,0,z-2,x+2,1,z+2,STONE);a.box(x-1,1,z-1,x+1,1,z+1,Blocks.WATER.getDefaultState());
        for(int dx:new int[]{-2,2})a.box(x+dx,2,z,x+dx,5,z,Blocks.SPRUCE_FENCE.getDefaultState());
        roof(a,x-2,z-2,x+2,z+2,6,false);
        a.block(x,5,z,ModBlocks.RED_LANTERN.getDefaultState());
    }

    private static List<Lot> lots() {
        List<Lot> result=new ArrayList<>();
        // A few larger properties establish the blocks before narrower frontages fill in.
        add(result,276,-78,4,Rotation.NONE,INN,16,14,8,0);
        add(result,-32,176,3,Rotation.NONE,INN,16,13,8,3);
        add(result,366,24,3,Rotation.CLOCKWISE_180,COURTYARD,17,14,3,1);
        add(result,268,24,3,Rotation.CLOCKWISE_180,COURTYARD,17,14,3,2);
        // Main shop street, living street, craft street, school lane and canal-side shops.
        int[] order={0,1,2,3,4,6,7,9,10,11,12,13,14,15,16,17,18};
        for(int index:order) {
            int[] s=VillageLayout.STREETS[index];boolean eastWest=s[1]==s[3];
            int start=eastWest?s[0]:s[1],end=eastWest?s[2]:s[3];
            for(int side:new int[]{-1,1}) {
                if((index==4 || index==6) && side>0)continue;
                Rotation facing=eastWest?(side<0?Rotation.NONE:Rotation.CLOCKWISE_180):
                        (side<0?Rotation.COUNTERCLOCKWISE_90:Rotation.CLOCKWISE_90);
                int cursor=start+5,sequence=0;
                while(cursor<end-14) {
                    int variant=Math.floorMod(index*17+sequence*7+(side+1)*3,30);
                    int type=type(index,sequence,variant);
                    int h=type==ROW_HOME?17+variant%3:type==WORKSHOP?14+variant%3:
                            type==SHOP?9+variant%5:8+variant%4;
                    int d=index==3?8+variant%2:type==WORKSHOP?13+variant%3:type==SHOP?11+variant%4:10+variant%3;
                    int yard=index==3?4:type==SHOP?6:8;
                    int centre=cursor+h+3;
                    if(centre+h+3>end-3)break;
                    int sx=eastWest?centre:s[0],sz=eastWest?s[1]:centre;
                    if(add(result,sx,sz,s[4],facing,type,h,d,yard,variant))cursor=centre+h+6+variant%3;
                    else cursor+=3;
                    sequence++;
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static int type(int street,int sequence,int variant) {
        if(street==0 || street==4 || street==6)return SHOP;
        if(street==2)return sequence%3==0?WORKSHOP:sequence%3==1?HOME:ROW_HOME;
        if(street==1)return sequence%4==0?SHOP:sequence%4==1?ROW_HOME:HOME;
        if(street==3)return sequence%3==0?ROW_HOME:HOME;
        return variant%5==0?WORKSHOP:HOME;
    }

    private static boolean add(List<Lot> lots,int sx,int sz,int roadWidth,Rotation rotation,int type,int h,int d,int yard,int variant) {
        Lot lot=new Lot(lots.size()+1,sx,sz,roadWidth,rotation,type,h,d,yard,variant);
        if(!clear(lot,lots))return false;
        lots.add(lot);return true;
    }

    private static boolean clear(Lot lot,List<Lot> previous) {
        int x1=lot.minX,z1=lot.minZ,x2=lot.maxX,z2=lot.maxZ;
        if(x1<-466 || x2>466 || z1<-230 || z2>224 || VillageLayout.courtOverlaps(x1,z1,x2,z2))return false;
        for(Lot p:previous)if(x1<=p.maxX+1 && x2>=p.minX-1 && z1<=p.maxZ+1 && z2>=p.minZ-1)return false;
        GensokyoAtlas v=GensokyoAtlas.VILLAGE;
        for(GensokyoAtlas p:GensokyoAtlas.values())if(p!=v && p.grounded() &&
                x1<=p.x-v.x+p.rx+4 && x2>=p.x-v.x-p.rx-4 && z1<=p.z-v.z+p.rz+4 && z2>=p.z-v.z-p.rz-4)return false;
        if(VillageCanal.overlaps(x1,z1,x2,z2,9))return false;
        for(GensokyoRoads.Segment s:GensokyoRoads.INSTANCE.segments())
            if(VillageLayout.crosses(s.ax-v.x,s.az-v.z,s.bx-v.x,s.bz-v.z,x1,z1,x2,z2,s.width+1))return false;
        return true;
    }

    static final class Lot {
        final int id,x,z,streetX,streetZ,streetDistance,type,half,depth,yard,variant,minX,minZ,maxX,maxZ;
        final Rotation rotation;
        Lot(int id,int sx,int sz,int roadWidth,Rotation rotation,int type,int half,int depth,int yard,int variant) {
            this.id=id;streetX=sx;streetZ=sz;streetDistance=depth+roadWidth+5;
            this.rotation=rotation;this.type=type;this.half=half;this.depth=depth;this.yard=yard;this.variant=variant;
            BlockPos front=new BlockPos(0,0,streetDistance).rotate(rotation);x=sx-front.getX();z=sz-front.getZ();
            BlockPos p=relative(-half-3,-depth-yard-1),q=relative(half+3,depth+3);
            minX=Math.min(p.getX(),q.getX());maxX=Math.max(p.getX(),q.getX());
            minZ=Math.min(p.getZ(),q.getZ());maxZ=Math.max(p.getZ(),q.getZ());
        }
        private BlockPos relative(int lx,int lz) {return new BlockPos(lx,0,lz).rotate(rotation).add(x,0,z);}
        BlockPos world(int lx,int y,int lz) {
            return relative(lx,lz).add(GensokyoAtlas.VILLAGE.x,GensokyoAtlas.VILLAGE.y+y,GensokyoAtlas.VILLAGE.z);
        }
        int bedCount() {return type==INN?4:2;}
        String title() {return "人里街坊"+id;}
    }
}
