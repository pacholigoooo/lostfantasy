package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityLockableLoot;
import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.block.CeilingChestTile;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.TextComponentString;

/** Ordered block volumes are clipped to their own chunk. No World reference or neighbour writes. */
public final class GensokyoBlueprint {
    private final Map<Long,List<Box>> slices=new HashMap<>();
    private final List<Room> rooms=new ArrayList<>();
    private final Map<Long,List<Container>> containers=new HashMap<>();
    private final Map<Long,List<Sign>> signs=new HashMap<>();
    private boolean sealed;
    public void box(int x1,int y1,int z1,int x2,int y2,int z2,IBlockState state) {
        if(sealed)throw new IllegalStateException("Blueprint already sealed");
        if(x1>x2 || y1>y2 || z1>z2 || y1<1 || y2>255)throw new IllegalArgumentException("Invalid volume");
        Box box=new Box(x1,y1,z1,x2,y2,z2,state);
        for(int cx=x1>>4;cx<=x2>>4;cx++)for(int cz=z1>>4;cz<=z2>>4;cz++)
            slices.computeIfAbsent(GensokyoAtlas.key(cx,cz),k->new ArrayList<>()).add(box);
    }
    public void room(String name,int x,int floor,int z) {
        if(sealed)throw new IllegalStateException("Blueprint already sealed");
        rooms.add(new Room(name,x,floor+1,z));
    }
    public void seal() {sealed=true;}
    public void container(int x,int y,int z,ResourceLocation loot) {
        if(sealed)throw new IllegalStateException("Blueprint already sealed");
        containers.computeIfAbsent(GensokyoAtlas.key(x>>4,z>>4),key->new ArrayList<>()).add(new Container(new BlockPos(x,y,z),loot));
    }
    public void sign(BlockPos pos,String first,String second) {
        if(sealed)throw new IllegalStateException("Blueprint already sealed");
        signs.computeIfAbsent(GensokyoAtlas.key(pos.getX()>>4,pos.getZ()>>4),key->new ArrayList<>()).add(new Sign(pos,first,second));
    }
    /** Only called during new-chunk construction. Saved containers are loaded by Minecraft. */
    public void installContainers(Chunk chunk,long seed) {
        long key=GensokyoAtlas.key(chunk.x,chunk.z);
        List<Container> list=containers.getOrDefault(key,Collections.emptyList());
        GensokyoNoise noise=new GensokyoNoise(seed);
        for(Container entry:list) {
            net.minecraft.block.Block block=chunk.getBlockState(entry.pos).getBlock();
            if(block!=Blocks.CHEST && block!=ModBlocks.CEILING_CHEST)continue;
            TileEntityLockableLoot tile=block==ModBlocks.CEILING_CHEST?new CeilingChestTile():new TileEntityChest();
            tile.setLootTable(entry.loot,noise.hash(entry.pos.getX(),entry.pos.getZ(),entry.pos.getY()));
            chunk.addTileEntity(entry.pos,tile);
        }
        for(Sign entry:signs.getOrDefault(key,Collections.emptyList())) {
            if(chunk.getBlockState(entry.pos).getBlock()!=Blocks.WALL_SIGN)continue;
            TileEntitySign tile=new TileEntitySign();
            tile.signText[1]=new TextComponentString(entry.first);tile.signText[2]=new TextComponentString(entry.second);
            chunk.addTileEntity(entry.pos,tile);
        }
    }
    public List<Room> rooms() {return Collections.unmodifiableList(rooms);}
    public Set<Long> chunks() {return Collections.unmodifiableSet(slices.keySet());}
    public void paint(ChunkPrimer primer,int cx,int cz) {
        List<Box> list=slices.get(GensokyoAtlas.key(cx,cz));if(list==null)return;
        int ox=cx<<4,oz=cz<<4;
        for(Box box:list) {
            int x1=Math.max(0,box.x1-ox),x2=Math.min(15,box.x2-ox);
            int z1=Math.max(0,box.z1-oz),z2=Math.min(15,box.z2-oz);
            for(int x=x1;x<=x2;x++)for(int z=z1;z<=z2;z++)for(int y=box.y1;y<=box.y2;y++)
                primer.setBlockState(x,y,z,box.state);
        }
    }
    /** Used for offline spatial inspection; generation uses paint() once per chunk. */
    public IBlockState at(int x,int y,int z) {
        List<Box> list=slices.get(GensokyoAtlas.key(x>>4,z>>4));if(list!=null)
            for(int i=list.size()-1;i>=0;i--) {
                Box b=list.get(i);
                if(x>=b.x1 && x<=b.x2 && y>=b.y1 && y<=b.y2 && z>=b.z1 && z<=b.z2)return b.state;
            }
        return Blocks.AIR.getDefaultState();
    }
    public static final class Room {
        public final String name;public final int x,y,z;
        Room(String name,int x,int y,int z) {this.name=name;this.x=x;this.y=y;this.z=z;}
    }
    private static final class Box {
        final int x1,y1,z1,x2,y2,z2;final IBlockState state;
        Box(int x1,int y1,int z1,int x2,int y2,int z2,IBlockState state) {
            this.x1=x1;this.y1=y1;this.z1=z1;this.x2=x2;this.y2=y2;this.z2=z2;this.state=state;
        }
    }
    private static final class Container {
        final BlockPos pos;final ResourceLocation loot;
        Container(BlockPos pos,ResourceLocation loot) {this.pos=pos;this.loot=loot;}
    }
    private static final class Sign {
        final BlockPos pos;final String first,second;
        Sign(BlockPos pos,String first,String second) {this.pos=pos;this.first=first;this.second=second;}
    }
}
