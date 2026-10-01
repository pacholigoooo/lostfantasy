package dev.lostfantasy.world.gensokyo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.world.chunk.ChunkPrimer;

/** Exports actual Java terrain and chunk geometry, without a game client or graphics context. */
public final class GensokyoExport {
    public static void main(String[] args) throws Exception {
        if(args.length>0 && args[0].startsWith("inspect")) {GensokyoSpatialInspection.main(args);return;}
        GensokyoTestBlocks.register();Path root=Paths.get("build/gensokyo");Files.createDirectories(root);
        if(args.length>0 && "layout".equals(args[0])) {
            try(BufferedWriter out=Files.newBufferedWriter(root.resolve("sites.tsv"),StandardCharsets.UTF_8)) {
                for(GensokyoAtlas s:GensokyoAtlas.values())out.write(s.name()+"\t"+s.title+"\t"+s.x+"\t"+s.y+"\t"+s.z+"\t"+s.kind+"\n");
            }
            try(BufferedWriter out=Files.newBufferedWriter(root.resolve("roads.tsv"),StandardCharsets.UTF_8)) {
                for(GensokyoRoads.Segment s:GensokyoRoads.INSTANCE.segments())out.write(s.ax+"\t"+s.az+"\t"+s.ay+"\t"+s.bx+"\t"+s.bz+"\t"+s.by+"\t"+s.width+"\n");
            }
            return;
        }
        if(args.length>0 && "tengu".equals(args[0])) {
            try(BufferedWriter out=Files.newBufferedWriter(root.resolve("tengu-layout.tsv"),StandardCharsets.UTF_8)) {
                for(TenguLayout.Plot p:TenguLayout.all())out.write("house\t"+p.name+"\t"+p.x+"\t"+p.z+"\t"+p.y+"\t"+p.w+"\t"+p.d+"\t"+p.floors+"\t"+p.form+"\t"+p.turn+"\n");
                for(TenguLayout.Path p:TenguLayout.PATHS)out.write("path\t"+p.x+"\t"+p.z+"\t"+p.y+"\t"+p.bx+"\t"+p.bz+"\t"+p.by+"\t"+p.width+"\n");
            }
            siteSlice(new GensokyoGenerator(null,12345),root,GensokyoAtlas.TENGU,"tengu",-395,395,-300,380,-28,133,true);
            return;
        }
        if(args.length>0 && "waterfall".equals(args[0])) {
            siteSlice(new GensokyoGenerator(null,12345),root,GensokyoAtlas.WATERFALL,"waterfall",-218,235,-175,245,-40,108,true);
            if(args.length<2 || !"mountain".equals(args[1]))return;
        }
        if(args.length>0 && ("mountain".equals(args[0]) || args.length>1 && "mountain".equals(args[1]))) {
            GensokyoTerrain terrain=new GensokyoTerrain(12345);
            try(BufferedWriter out=Files.newBufferedWriter(root.resolve("mountain.tsv"),StandardCharsets.UTF_8)) {
                for(int z=-4136;z<=-1124;z+=12)for(int x=-2456;x<=2536;x+=12) {
                    GensokyoTerrain.Column c=terrain.column(x,z);
                    out.write(x+"\t"+z+"\t"+c.ground+"\t"+c.water+"\t"+(c.rockSurface?1:0)+"\n");
                }
            }
            try(BufferedWriter out=Files.newBufferedWriter(root.resolve("mountain-roads.tsv"),StandardCharsets.UTF_8)) {
                int id=0;
                for(GensokyoRoads.Segment s:GensokyoRoads.INSTANCE.segments()) {
                    if(s.contour) {
                        double a=terrain.roadJunction(s.ax,s.az,s.ay),b=terrain.roadJunction(s.bx,s.bz,s.by);
                        if(Math.abs(a-b)>GensokyoRoadProfile.MAX_GRADE*Math.hypot(s.bx-s.ax,s.bz-s.az))System.out.println("ROAD APPROACH "+id+": "+s.ax+","+s.az+","+a+" -> "+s.bx+","+s.bz+","+b);
                        int n=Math.max(1,(int)Math.ceil(Math.hypot(s.bx-s.ax,s.bz-s.az)/6));
                        for(int i=0;i<=n;i++) {
                            double t=i/(double)n;int x=(int)Math.round(GensokyoNoise.lerp(s.ax,s.bx,t)),z=(int)Math.round(GensokyoNoise.lerp(s.az,s.bz,t));
                            GensokyoTerrain.Column c=terrain.column(x,z),natural=terrain.naturalColumn(x,z);
                            out.write(id+"\t"+x+"\t"+z+"\t"+c.roadY+"\t"+natural.ground+"\t"+natural.water+"\t"+GensokyoNoise.lerp(s.ay,s.by,t)+"\n");
                        }
                    }
                    id++;
                }
            }
            return;
        }
        if(args.length>0 && "old_hell".equals(args[0])) {oldHell(new GensokyoGenerator(null,12345),root);return;}
        if(args.length>0 && "chireiden".equals(args[0])) {chireiden(root);return;}
        if(args.length>0 && "blazing_hell".equals(args[0])) {blazingHell(root);return;}
        if(args.length>0 && "geyser".equals(args[0])) {geyser(new GensokyoGenerator(null,12345),root);return;}
        if(args.length>0 && "aquatic_market".equals(args[0])) {market(new GensokyoGenerator(null,12345),root);return;}
        if(args.length>0 && "ropeway".equals(args[0])) {ropeway(new GensokyoGenerator(null,12345),root);return;}
        if(args.length>0 && "senkai".equals(args[0])) {senkai(root);return;}
        if(args.length>0 && "netherworld".equals(args[0])) {netherworld(root);return;}
        if(args.length>0 && "sanzu".equals(args[0])) {sanzu(root);return;}
        if(args.length>0 && "blood_pool".equals(args[0])) {bloodPool(root);return;}
        if(args.length>0 && "forest_finish".equals(args[0])) {forestFinish(root);return;}
        if(args.length>0 && "rice_fields".equals(args[0])) {
            GensokyoGenerator generator=new GensokyoGenerator(null,12345);
            siteSlice(generator,root,GensokyoAtlas.RICE_FIELDS,"rice_fields",-353,408,-235,175,-10,30,true);
            siteSlice(generator,root,GensokyoAtlas.RICE_FIELDS,"rice_farmyard",12,110,-228,-174,-4,24,false);
            siteSlice(generator,root,GensokyoAtlas.RICE_FIELDS,"rice_detail",-170,-110,-45,5,-3,5,false);
            return;
        }
        GensokyoTerrain terrain=new GensokyoTerrain(12345);int size=901,step=8,min=-3600;
        try(DataOutputStream out=new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(root.resolve("terrain.bin"))))) {
            out.writeInt(size);out.writeInt(step);out.writeInt(min);out.writeInt(min);
            for(int z=0;z<size;z++)for(int x=0;x<size;x++) {
                GensokyoTerrain.Column c=terrain.column(min+x*step,min+z*step);
                out.writeShort(c.ground);out.writeShort(c.water);out.writeByte(c.region.ordinal());out.writeByte(c.path()?1:0);
            }
        }
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("sites.tsv"),StandardCharsets.UTF_8)) {
            for(GensokyoAtlas s:GensokyoAtlas.values())out.write(s.name()+"\t"+s.title+"\t"+s.x+"\t"+s.y+"\t"+s.z+"\t"+s.kind+"\n");
        }
        GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.HAKUREI,GensokyoAtlas.FAIRY_OLD_TREE,GensokyoAtlas.FAIRY_TREE,GensokyoAtlas.SCARLET,GensokyoAtlas.MARISA,GensokyoAtlas.ALICE,GensokyoAtlas.KOURINDOU,GensokyoAtlas.MYSTIA,GensokyoAtlas.SUZUNAAN,GensokyoAtlas.TERAKOYA,GensokyoAtlas.HIEDA,GensokyoAtlas.MYOUREN,GensokyoAtlas.EIENTEI,GensokyoAtlas.MOKOU,GensokyoAtlas.MORIYA,GensokyoAtlas.CUCUMBER_FACTORY,GensokyoAtlas.KAPPA,GensokyoAtlas.NEMUNO}) {
        int radius=site==GensokyoAtlas.SCARLET?118:site==GensokyoAtlas.HAKUREI?96:site==GensokyoAtlas.HIEDA?85:
                site==GensokyoAtlas.MYOUREN?110:site==GensokyoAtlas.MORIYA?134:site==GensokyoAtlas.EIENTEI?123:site==GensokyoAtlas.KAPPA?152:site==GensokyoAtlas.CUCUMBER_FACTORY?80:site==GensokyoAtlas.MOKOU || site==GensokyoAtlas.NEMUNO?48:site==GensokyoAtlas.MARISA || site==GensokyoAtlas.ALICE?52:site==GensokyoAtlas.SUZUNAAN || site==GensokyoAtlas.TERAKOYA?48:35;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve(site.name().toLowerCase(java.util.Locale.ROOT)+".tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(site.x-radius)>>4;cx<=(site.x+radius)>>4;cx++)for(int cz=(site.z-radius)>>4;cz<=(site.z+radius)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y+(site==GensokyoAtlas.SCARLET?-52:site==GensokyoAtlas.KAPPA?-8:-4);y<site.y+(site==GensokyoAtlas.SCARLET?84:53);y++) {
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    out.write(((cx<<4)+x-site.x)+"\t"+(y-site.y)+"\t"+((cz<<4)+z-site.z)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
        }
        village(generator,root);
        grove(generator,terrain,root);
        shrineShore(generator,root);
        waterfall(generator,root);
        genbu(generator,root);
        tengu(generator,root);
        rainbow(generator,root);
        siteSlice(generator,root,GensokyoAtlas.SECRET_CLIFF,"secret_cliff",-154,154,-45,47,-4,27,false);
        siteSlice(generator,root,GensokyoAtlas.FALSE_HEAVEN,"false_heaven",-57,57,-49,52,-4,32,false);
        siteSlice(generator,root,GensokyoAtlas.SECRET_CLIFF,"secret_highland",-370,230,-335,90,-8,110,true);
        siteSlice(generator,root,GensokyoAtlas.KASEN,"kasen_entrance",-42,42,-38,68,-3,38,false);
        KasenGenerator hermit=new KasenGenerator(null,12345);
        siteSlice(generator,root,GensokyoAtlas.PEONY_FIELD,"peony_field",-43,43,-41,42,-3,20,false);
        siteSlice(generator,root,GensokyoAtlas.MAYOHIGA,"mayohiga",-132,132,-116,123,-3,30,false);
        siteSlice(generator,root,GensokyoAtlas.TOAD_POND,"toad_pond",-135,132,-101,102,-8,53,true);
        siteSlice(generator,root,GensokyoAtlas.TOAD_POND,"pond_shrine",58,110,-17,32,1,22,false);
        siteSlice(hermit::primer,root,GensokyoAtlas.KASEN,"kasen",-48,48,-37,67,-3,38,false);
        siteSlice(hermit::primer,root,GensokyoAtlas.KASEN,"hermit_world",-200,200,-220,220,-65,68,true);
        siteSlice(generator,root,GensokyoAtlas.YAKUMO,"boundary_entrance",-46,46,0,86,-3,35,false);
        BoundaryGenerator boundary=new BoundaryGenerator(null,12345);
        siteSlice(boundary::primer,root,GensokyoAtlas.YAKUMO,"boundary_house",-63,63,-48,82,-3,35,false);
        siteSlice(generator,root,GensokyoAtlas.GHOST_HOUSE,"ghost_house",-40,40,-34,42,-3,39,false);
        siteSlice(generator,root,GensokyoAtlas.FAIRY_SHRINE,"fairy_shrine",-34,36,-31,40,-3,64,false);
        siteSlice(generator,root,GensokyoAtlas.LARVA,"larva_hollow",-23,23,-25,29,-18,30,false);
        siteSlice(generator,root,GensokyoAtlas.MUENZUKA,"muenzuka",-45,45,-42,47,-3,37,false);
        siteSlice(generator,root,GensokyoAtlas.RECONSIDERATION,"reconsideration",-72,72,-72,72,-32,58,true);
        siteSlice(generator,root,GensokyoAtlas.SUN_GARDEN,"concert_stage",82,138,55,134,-3,26,false);
        siteSlice(generator,root,GensokyoAtlas.SUN_GARDEN,"sun_garden",-445,445,-350,355,-21,56,true);
        siteSlice(generator,root,GensokyoAtlas.SUZURAN,"suzuran",-110,130,-105,110,-26,46,true);
        siteSlice(generator,root,GensokyoAtlas.CIRNO,"cirno",-48,48,-44,65,-12,22,false);
        siteSlice(generator,root,GensokyoAtlas.CIRNO,"cirno_shore",-250,165,-110,145,-25,36,true);
        siteSlice(generator,root,GensokyoAtlas.RUINED_MANSION,"prismriver",-53,53,-39,61,-4,39,false);
        siteSlice(generator,root,GensokyoAtlas.PALANQUIN,"palanquin",-27,27,-56,49,-19,50,false);
        siteSlice(generator,root,GensokyoAtlas.SHINING_NEEDLE,"shining_needle",-82,82,-42,78,-44,49,false);
        oldHell(generator,root);
        chireiden(root);
        blazingHell(root);
        geyser(generator,root);
        market(generator,root);
        ropeway(generator,root);
        senkai(root);
        System.out.println("Exported actual terrain, atlas and completed building slices to "+root.toAbsolutePath());
    }
    private static void forestFinish(Path root) throws IOException {
        GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        siteSlice(generator,root,GensokyoAtlas.HAKUREI,"hakurei",-85,85,-83,86,-3,36,false);
        siteSlice(generator,root,GensokyoAtlas.HAKUREI,"hakurei_branch",-45,-23,-71,-45,-1,13,false);
        // The western hollow and its complete canopy, in old tree-house coordinates.
        siteSlice(generator,root,GensokyoAtlas.FAIRY_OLD_TREE,"forest_wetland",-322,-141,76,202,-35,39,false);
    }
    private static void bloodPool(Path root) throws IOException {
        BloodPoolGenerator g=new BloodPoolGenerator(null,12345);
        siteSlice(g::primer,root,OldHellWorld.ORIGIN,"blood_pool",-290,290,-40,640,-36,36,true);
        siteSlice(g::primer,root,OldHellWorld.ORIGIN,"blood_pool_archive",-252,-195,210,270,-22,6,false);
        siteSlice(g::primer,root,OldHellWorld.ORIGIN,"blood_pool_stair",-10,10,-301,50,-24,130,true);
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("blood_pool_region.tsv"),StandardCharsets.UTF_8)) {
            for(int z=-480;z<=2580;z+=12)for(int x=-1056;x<=1056;x+=12) {
                BloodPoolGenerator.Column c=g.column(x,z);out.write(x+"\t"+z+"\t"+c.floor+"\t"+c.roof+"\n");
            }
        }
    }
    private static void oldHell(GensokyoGenerator generator,Path root) throws IOException {
        siteSlice(generator,root,GensokyoAtlas.BLOWHOLE,"blowhole",-18,20,-34,160,-52,22,true);
        siteSlice(generator,root,GensokyoAtlas.EARTH_RAINBOW,"earth_rainbow",-25,36,-34,146,-44,29,true);
        OldHellGenerator oldHell=new OldHellGenerator(null,12345);
        siteSlice(oldHell::primer,root,OldHellWorld.ORIGIN,"old_capital",-390,495,-77,OldHellCity.EXIT_Z,-25,40,true);
        siteSlice(oldHell::primer,root,OldHellWorld.ORIGIN,"old_hell_cavern",-496,496,-235,OldHellCity.EXIT_Z,-26,146,true);
    }
    private static void chireiden(Path root) throws IOException {
        OldHellGenerator g=new OldHellGenerator(null,12345);
        siteSlice((x,z)->g.primer(x,z),root,OldHellWorld.ORIGIN,"chireiden",-101,101,Chireiden.Z-88,Chireiden.Z+76,-14,78,true);
        siteSlice((x,z)->g.primer(x,z),root,OldHellWorld.ORIGIN,"chireiden_underground",-35,35,Chireiden.Z-54,Chireiden.Z+109,-41,3,false);
    }
    private static void blazingHell(Path root) throws IOException {
        OldHellGenerator g=new OldHellGenerator(null,12345);
        siteSlice(g::primer,root,OldHellWorld.ORIGIN,"fusion_furnace",-116,80,BlazingHell.CORE_Z-80,BlazingHell.CORE_Z+80,-58,108,false);
        siteSlice(g::primer,root,OldHellWorld.ORIGIN,"hell_workshop",212,268,1209,1271,-40,-14,false);
        siteSlice(g::primer,root,OldHellWorld.ORIGIN,"hell_descent",20,64,545,860,-42,4,true);
        // An eight-block sample of actual chunks, including the palace and the entire hell region.
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("blazing_region.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(OldHellWorld.ORIGIN.x-752)>>4;cx<=(OldHellWorld.ORIGIN.x+752)>>4;cx++)
                for(int cz=(OldHellWorld.ORIGIN.z+300)>>4;cz<=(OldHellWorld.ORIGIN.z+2640)>>4;cz++) {
                    ChunkPrimer p=g.primer(cx,cz);
                    for(int x=0;x<16;x+=8)for(int z=0;z<16;z+=8) {
                        int roof=-1,floor=-1,level=0;
                        for(int y=248;y>=1;y--) {
                            IBlockState state=p.getBlockState(x,y,z);
                            if(roof<0 && state.getBlock()==Blocks.AIR)roof=y+1;
                            else if(roof>=0 && state.getMaterial().blocksMovement()) {floor=y;break;}
                            if(state.getBlock()==Blocks.LAVA)level=y;
                        }
                        if(roof>floor && floor>=0)out.write(((cx<<4)+x-OldHellWorld.ORIGIN.x)+"\t"+((cz<<4)+z-OldHellWorld.ORIGIN.z)+"\t"+floor+"\t"+roof+"\t"+level+"\n");
                    }
                }
        }
        System.out.println("Exported blazing hell, reactor, workshop and descent");
    }
    private static void sanzu(Path root) throws IOException {
        GensokyoGenerator g=new GensokyoGenerator(null,12345);
        siteSlice(g::primer,root,GensokyoAtlas.LIMINAL_ROAD,"liminal_market",-83,83,-143,143,-3,26,false);
        siteSlice(g::primer,root,GensokyoAtlas.SANZU_PIER,"sanzu_pier",-265,95,-125,125,-14,36,true);
        siteSlice(g::primer,root,GensokyoAtlas.SAI_BANK,"sai_bank",-245,95,-175,175,-32,28,true);
    }
    private static void netherworld(Path root) throws IOException {
        NetherworldGenerator g=new NetherworldGenerator(null,12345);
        siteSlice(g::primer,root,GensokyoAtlas.NETHER_GATE,"hakugyokurou",-137,137,-305,155,-2,89,false);
        siteSlice(g::primer,root,GensokyoAtlas.NETHER_GATE,"nether_stair",-53,53,140,955,-100,35,true);
    }
    private static void senkai(Path root) throws IOException {
        SenkaiGenerator g=new SenkaiGenerator(null,12345);
        siteSlice(g::primer,root,GensokyoAtlas.MYOUREN,"senkai",-118,118,-118,108,-3,65,false);
        siteSlice(new GensokyoGenerator(null,12345),root,GensokyoAtlas.MYOUREN,"mausoleum_cave",-47,47,-119,25,-60,9,false);
    }
    private static void ropeway(GensokyoGenerator g,Path root) throws IOException {
        for(int end=0;end<2;end++)siteSlice(g,root,RopewayPath.station(end),"ropeway_"+end,-37,37,-40,34,-3,35,false);
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("ropeway_profile.tsv"),StandardCharsets.UTF_8)) {
            GensokyoTerrain t=new GensokyoTerrain(12345);
            for(int z=0;z<=2600;z+=4) {
                net.minecraft.util.math.Vec3d at=RopewayPath.point(z/2600.0,0);
                out.write(z+"\t"+at.x+"\t"+at.y+"\t"+at.z+"\t"+t.column((int)Math.floor(at.x),(int)Math.floor(at.z)).surface()+"\n");
            }
        }
    }
    private static void market(GensokyoGenerator g,Path root) throws IOException {
        siteSlice(g,root,GensokyoAtlas.AQUATIC_MARKET,"aquatic_market",-61,61,-45,49,-3,90,false);
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("hisoutensoku.tsv"),StandardCharsets.UTF_8)) {
            for(dev.lostfantasy.entity.HisoutensokuShape.Part p:dev.lostfantasy.entity.HisoutensokuShape.PARTS)
                out.write(p.joint+"\t"+p.material+"\t"+p.x+"\t"+p.y+"\t"+p.z+"\t"+p.w+"\t"+p.h+"\t"+p.d+"\n");
        }
    }
    private static void geyser(GensokyoGenerator g,Path root) throws IOException {
        siteSlice(g,root,GensokyoAtlas.GEYSER,"geyser",-59,59,-48,54,-4,43,false);
        siteSlice(g,root,GensokyoAtlas.GEYSER,"geyser_shaft",-31,31,-39,31,-96,43,false);
        OldHellGenerator deep=new OldHellGenerator(null,12345);
        siteSlice(deep::primer,root,OldHellWorld.ORIGIN,"geyser_lower",-9,9,BlazingHell.CORE_Z-105,BlazingHell.CORE_Z-65,58,74,false);
    }
    private static void rainbow(GensokyoGenerator generator,Path root) throws IOException {
        siteSlice(generator,root,GensokyoAtlas.RAINBOW_CAVE,"rainbow",-110,110,-195,31,-36,49,true);
        siteSlice(generator,root,GensokyoAtlas.RAINBOW_CAVE,"rainbow_entry",-36,36,-42,28,-4,28,false);
        siteSlice(generator,root,GensokyoAtlas.RAINBOW_CAVE,"rainbow_upper",-63,27,-88,-47,-12,6,false);
        siteSlice(generator,root,GensokyoAtlas.RAINBOW_CAVE,"rainbow_west",-81,-24,-144,-97,-30,-2,false);
        siteSlice(generator,root,GensokyoAtlas.RAINBOW_CAVE,"rainbow_deep",15,79,-176,-122,-36,-8,false);
    }
    private static void siteSlice(GensokyoGenerator generator,Path root,GensokyoAtlas site,String name,int x1,int x2,int z1,int z2,int bottom,int top,boolean surfaceOnly) throws IOException {
        siteSlice(generator::primer,root,site,name,x1,x2,z1,z2,bottom,top,surfaceOnly);
    }
    private static void siteSlice(java.util.function.BiFunction<Integer,Integer,ChunkPrimer> generator,Path root,GensokyoAtlas site,String name,int x1,int x2,int z1,int z2,int bottom,int top,boolean surfaceOnly) throws IOException {
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve(name+".tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(site.x+x1)>>4;cx<=(site.x+x2)>>4;cx++)for(int cz=(site.z+z1)>>4;cz<=(site.z+z2)>>4;cz++) {
                ChunkPrimer p=generator.apply(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y+bottom;y<=site.y+top;y++) {
                    int dx=(cx<<4)+x-site.x,dz=(cz<<4)+z-site.z;if(dx<x1 || dx>x2 || dz<z1 || dz>z2)continue;
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    if(surfaceOnly && y>site.y+bottom && dx>x1 && dx<x2 && dz>z1 && dz<z2 && x>0 && x<15 && z>0 && z<15 && s.isFullCube()
                            && p.getBlockState(x-1,y,z).isFullCube() && p.getBlockState(x+1,y,z).isFullCube()
                            && p.getBlockState(x,y-1,z).isFullCube() && p.getBlockState(x,y+1,z).isFullCube()
                            && p.getBlockState(x,y,z-1).isFullCube() && p.getBlockState(x,y,z+1).isFullCube())continue;
                    out.write(dx+"\t"+(y-site.y)+"\t"+dz+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
    }
    private static void tengu(GensokyoGenerator generator,Path root) throws IOException {
        GensokyoAtlas site=GensokyoAtlas.TENGU;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("tengu.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(site.x-370)>>4;cx<=(site.x+370)>>4;cx++)for(int cz=(site.z-320)>>4;cz<=(site.z+398)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=96;y<249;y++) {
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    out.write(((cx<<4)+x-site.x)+"\t"+(y-site.y)+"\t"+((cz<<4)+z-site.z)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
    }
    private static void grove(GensokyoGenerator generator,GensokyoTerrain terrain,Path root) throws IOException {
        int ox=1560,oz=1494,oy=terrain.column(ox,oz).ground;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("bamboo_grove.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(ox-48)>>4;cx<=(ox+48)>>4;cx++)for(int cz=(oz-48)>>4;cz<=(oz+48)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=oy-16;y<oy+45;y++) {
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    out.write(((cx<<4)+x-ox)+"\t"+(y-oy)+"\t"+((cz<<4)+z-oz)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
    }
    private static void shrineShore(GensokyoGenerator generator,Path root) throws IOException {
        GensokyoAtlas site=GensokyoAtlas.MORIYA;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("moriya_lake.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(site.x+60)>>4;cx<=(site.x+290)>>4;cx++)for(int cz=(site.z-85)>>4;cz<=(site.z+85)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y-18;y<site.y+31;y++) {
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    out.write(((cx<<4)+x-site.x)+"\t"+(y-site.y)+"\t"+((cz<<4)+z-site.z)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
    }
    private static void waterfall(GensokyoGenerator generator,Path root) throws IOException {
        GensokyoAtlas site=GensokyoAtlas.WATERFALL;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("waterfall.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(site.x-144)>>4;cx<=(site.x+128)>>4;cx++)for(int cz=(site.z-144)>>4;cz<=(site.z+144)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y-19;y<site.y+76;y++) {
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    out.write(((cx<<4)+x-site.x)+"\t"+(y-site.y)+"\t"+((cz<<4)+z-site.z)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
    }
    private static void genbu(GensokyoGenerator generator,Path root) throws IOException {
        GensokyoAtlas site=GensokyoAtlas.GENBU;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("genbu.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=-23;cx<=18;cx++)for(int cz=(site.z-240)>>4;cz<=(site.z+205)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y-10;y<site.y+72;y++) {
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    int wx=(cx<<4)+x,wz=(cz<<4)+z-site.z;
                    boolean cave=wx>=-190 && wx<=70 && wz>=-58 && wz<=24;
                    // Keep complete cave volumes for cutaways, and exposed terrain faces elsewhere.
                    if(!cave && x>0 && x<15 && z>0 && z<15 && s.isFullCube()
                            && p.getBlockState(x-1,y,z).isFullCube() && p.getBlockState(x+1,y,z).isFullCube()
                            && p.getBlockState(x,y-1,z).isFullCube() && p.getBlockState(x,y+1,z).isFullCube()
                            && p.getBlockState(x,y,z-1).isFullCube() && p.getBlockState(x,y,z+1).isFullCube())continue;
                    out.write(((cx<<4)+x-site.x)+"\t"+(y-site.y)+"\t"+((cz<<4)+z-site.z)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
    }
    private static void village(GensokyoGenerator generator,Path root) throws IOException {
        GensokyoAtlas v=GensokyoAtlas.VILLAGE;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("village-lots.tsv"),StandardCharsets.UTF_8)) {
            for(VillageHomes.Lot lot:VillageHomes.LOTS)out.write(lot.id+"\t"+lot.x+"\t"+lot.z+"\t"+lot.type+"\t"+lot.title()
                    +"\t"+lot.rotation+"\t"+(lot.half*2+1)+"\t"+(lot.depth*2+1)+"\n");
        }
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("village-roofs.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(v.x-470)>>4;cx<=(v.x+470)>>4;cx++)for(int cz=(v.z-235)>>4;cz<=(v.z+235)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
                    int dx=(cx<<4)+x-v.x,dz=(cz<<4)+z-v.z;if(Math.abs(dx)>470 || Math.abs(dz)>235)continue;
                    for(int y=v.y+34;y>v.y-8;y--) {
                        IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                        out.write(dx+"\t"+dz+"\t"+(y-v.y)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");break;
                    }
                }
            }
        }
        int ox=v.x-300,oz=v.z-80;
        try(BufferedWriter out=Files.newBufferedWriter(root.resolve("village_quarter.tsv"),StandardCharsets.UTF_8)) {
            for(int cx=(ox-60)>>4;cx<=(ox+60)>>4;cx++)for(int cz=(oz-126)>>4;cz<=(oz+116)>>4;cz++) {
                ChunkPrimer p=generator.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=v.y-7;y<v.y+27;y++) {
                    IBlockState s=p.getBlockState(x,y,z);if(s.getBlock()==Blocks.AIR)continue;
                    out.write(((cx<<4)+x-ox)+"\t"+(y-v.y)+"\t"+((cz<<4)+z-oz)+"\t"+s.getBlock().getRegistryName()+"\t"+s.getBlock().getMetaFromState(s)+"\n");
                }
            }
        }
        System.out.println("Village households: "+VillageHomes.LOTS.size());
    }
}
