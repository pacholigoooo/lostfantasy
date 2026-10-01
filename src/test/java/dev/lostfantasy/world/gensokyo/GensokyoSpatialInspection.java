package dev.lostfantasy.world.gensokyo;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiFunction;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;

/** Offline generated-block inspection. Does not create a World, client, server or GL context. */
public final class GensokyoSpatialInspection {
    private static BufferedWriter out;
    private static int checked, flagged;
    public static void main(String[] args) throws Exception {
        GensokyoTestBlocks.register();
        if(args.length>0 && "inspect-roads".equals(args[0])) {roads();return;}
        Path path=Paths.get("build-logs/audit-2026-10-01/spatial.tsv");Files.createDirectories(path.getParent());
        try(BufferedWriter writer=Files.newBufferedWriter(path,StandardCharsets.UTF_8)) {
            out=writer;out.write("dimension\troom\tx\ty\tz\tfeet\thead\tfloor\tnearbyStanding\n");
            GensokyoGenerator overworld=new GensokyoGenerator(null,12345);inspect("gensokyo",overworld,overworld::primer);
            KasenGenerator kasen=new KasenGenerator(null,12345);inspect("kasen",kasen,kasen::primer);
            BoundaryGenerator boundary=new BoundaryGenerator(null,12345);inspect("boundary",boundary,boundary::primer);
            OldHellGenerator hell=new OldHellGenerator(null,12345);inspect("old_hell",hell,hell::primer);
            SenkaiGenerator senkai=new SenkaiGenerator(null,12345);inspect("senkai",senkai,senkai::primer);
            NetherworldGenerator netherworld=new NetherworldGenerator(null,12345);inspect("netherworld",netherworld,netherworld::primer);
            BloodPoolGenerator blood=new BloodPoolGenerator(null,12345);inspect("blood_pool",blood,blood::primer);
        }
        System.out.println("SPATIAL_INSPECTION rooms="+checked+" markersRequiringReview="+flagged+" file="+path);
    }
    private static void inspect(String name,Object generator,BiFunction<Integer,Integer,ChunkPrimer> factory) throws Exception {
        Map<Long,ChunkPrimer> chunks=new LinkedHashMap<Long,ChunkPrimer>(64,.75f,true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Long,ChunkPrimer> entry) {return size()>64;}
        };
        for(Field field:generator.getClass().getDeclaredFields())if(field.getType()==GensokyoBlueprint.class) {
            field.setAccessible(true);GensokyoBlueprint plan=(GensokyoBlueprint)field.get(generator);
            for(GensokyoBlueprint.Room room:plan.rooms()) {
                checked++;BlockPos pos=new BlockPos(room.x,room.y,room.z);int nearby=0;
                for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int dy=-1;dy<=1;dy++) {
                    BlockPos p=pos.add(dx,dy,dz);
                    if(empty(at(chunks,factory,p)) && empty(at(chunks,factory,p.up())) && support(at(chunks,factory,p.down())))nearby++;
                }
                IBlockState feet=at(chunks,factory,pos),head=at(chunks,factory,pos.up()),floor=at(chunks,factory,pos.down());
                if(nearby==0 || !empty(feet) || !empty(head) || !support(floor)) {
                    flagged++;out.write(name+"\t"+room.name+"\t"+room.x+"\t"+room.y+"\t"+room.z+"\t"+feet+"\t"+head+"\t"+floor+"\t"+nearby+"\n");
                }
            }
        }
        out.flush();System.out.println("Inspected "+name+"; total rooms="+checked);
    }
    private static IBlockState at(Map<Long,ChunkPrimer> chunks,BiFunction<Integer,Integer,ChunkPrimer> factory,BlockPos pos) {
        if(pos.getY()<0 || pos.getY()>255)return Blocks.BEDROCK.getDefaultState();
        long key=GensokyoAtlas.key(pos.getX()>>4,pos.getZ()>>4);
        ChunkPrimer chunk=chunks.computeIfAbsent(key,k->factory.apply(pos.getX()>>4,pos.getZ()>>4));
        return chunk.getBlockState(pos.getX()&15,pos.getY(),pos.getZ()&15);
    }
    private static boolean empty(IBlockState state) {
        return !state.getMaterial().isLiquid() && (state.getBlock() instanceof BlockCarpet
                || !state.getMaterial().blocksMovement() && state.getBlock()!=Blocks.FIRE);
    }
    private static boolean support(IBlockState state) {
        return state.getMaterial().blocksMovement() && !state.getMaterial().isLiquid()
                && !(state.getBlock() instanceof BlockFence) && !(state.getBlock() instanceof BlockPane);
    }
    private static void roads() throws Exception {
        GensokyoGenerator generator=new GensokyoGenerator(null,12345);
        GensokyoTerrain terrain=new GensokyoTerrain(12345);
        Map<Long,ChunkPrimer> chunks=new LinkedHashMap<Long,ChunkPrimer>(64,.75f,true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Long,ChunkPrimer> e) {return size()>64;}
        };
        Path file=Paths.get("build-logs/audit-2026-10-01/roads.tsv");int index=0,samples=0,issues=0;
        try(BufferedWriter writer=Files.newBufferedWriter(file,StandardCharsets.UTF_8)) {
            writer.write("segment\tx\ty\tz\tproblem\tfeet\thead\tfloor\n");
            for(GensokyoRoads.Segment segment:GensokyoRoads.INSTANCE.segments()) {
                int steps=(int)Math.ceil(Math.hypot(segment.bx-segment.ax,segment.bz-segment.az)*2);BlockPos last=null;int segmentIssues=0;
                for(int i=0;i<=steps;i++) {
                    double t=i/(double)steps;int x=(int)Math.round(segment.ax+(segment.bx-segment.ax)*t),z=(int)Math.round(segment.az+(segment.bz-segment.az)*t);
                    GensokyoTerrain.Column column=terrain.column(x,z);BlockPos p=new BlockPos(x,(column.wet()&&column.path()?column.roadY:column.ground)+1,z);
                    if(last!=null && p.getX()==last.getX() && p.getZ()==last.getZ())continue;
                    if(GensokyoAtlas.reserved(x,z,3)) {last=null;continue;}
                    // Authored aprons can sit one block above or below the terrain road.
                    if(!standing(chunks,generator::primer,p)) {
                        if(standing(chunks,generator::primer,p.up()))p=p.up();
                        else if(standing(chunks,generator::primer,p.down()))p=p.down();
                    }
                    samples++;String problem="";
                    if(last!=null && Math.abs(p.getY()-last.getY())>1) {
                        boolean diagonal=p.getX()!=last.getX() && p.getZ()!=last.getZ();
                        int mid=(p.getY()+last.getY())/2;
                        if(!diagonal || !(standing(chunks,generator::primer,new BlockPos(p.getX(),mid,last.getZ()))
                                || standing(chunks,generator::primer,new BlockPos(last.getX(),mid,p.getZ()))))
                            problem="grade:"+last.getY()+"->"+p.getY();
                    }
                    IBlockState feet=at(chunks,generator::primer,p),head=at(chunks,generator::primer,p.up()),floor=at(chunks,generator::primer,p.down());
                    if(!empty(feet)||!empty(head)||!support(floor))problem+=(problem.isEmpty()?"":";")+"obstruction";
                    if(!problem.isEmpty()) {issues++;if(segmentIssues++<8)writer.write(index+"\t"+x+"\t"+p.getY()+"\t"+z+"\t"+problem+"\t"+feet+"\t"+head+"\t"+floor+"\n");}
                    last=p;
                }
                index++;
            }
        }
        System.out.println("ROAD_INSPECTION segments="+index+" samples="+samples+" reviewPoints="+issues+" file="+file);
    }
    private static boolean standing(Map<Long,ChunkPrimer> chunks,BiFunction<Integer,Integer,ChunkPrimer> factory,BlockPos p) {
        return empty(at(chunks,factory,p)) && empty(at(chunks,factory,p.up())) && support(at(chunks,factory,p.down()));
    }
}
