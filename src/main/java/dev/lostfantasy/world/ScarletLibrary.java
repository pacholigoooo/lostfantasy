package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.block.RuinedBookcase;
import dev.lostfantasy.world.gensokyo.GensokyoAtlas;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

/** The existing library at a fixed mansion site, with an intact shell and the same study furniture. */
public final class ScarletLibrary {
    private static final int[][] CHESTS={{-38,-36},{38,-36},{-38,36},{38,36},{-9,13},{9,13},{14,-10},{18,63}};
    private static final EnumFacing[] FACING={EnumFacing.SOUTH,EnumFacing.SOUTH,EnumFacing.NORTH,EnumFacing.NORTH,
            EnumFacing.WEST,EnumFacing.EAST,EnumFacing.NORTH,EnumFacing.SOUTH};
    private ScarletLibrary() {}
    public static LibraryRuinLayout.Site site(long seed) {
        GensokyoAtlas mansion=GensokyoAtlas.SCARLET;
        int cx=mansion.x>>4,cz=mansion.z>>4;
        return new LibraryRuinLayout.Site(cx,cz,mansion.y-52,0,LibraryRuinLayout.blockNoise(seed,cx,737,cz));
    }
    public static boolean contains(LibraryRuinLayout.Site site,BlockPos pos) {
        int[] local=LibraryRuinLayout.toLocal(pos.getX()-site.centerX(),pos.getZ()-site.centerZ(),site.turns);
        int y=pos.getY()-site.baseY;
        return y>=3 && y<=LibraryStructure.columnMaxY(local[0],local[1]);
    }
    public static IBlockState stateAt(LibraryRuinLayout.Site site,int x,int y,int z) {
        LibraryStructure.Column column=LibraryStructure.columnAt(x,z);
        if(y<0 || y>column.maxY)return null;
        if(y==3)for(int i=0;i<CHESTS.length;i++)if(x==CHESTS[i][0] && z==CHESTS[i][1])
            return Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING,FACING[i]);
        // The west service staircase meets the old south-west entrance aisle.
        if(x>=-52 && x<=-37 && z>=-46 && z<=-42 && y>=3 && y<=7)return Blocks.AIR.getDefaultState();
        IBlockState state=LibraryStructure.stateAt(site,column,y);
        if(state==null)return null;
        if(y==column.maxY || y==0 || column.wall)return Blocks.STONEBRICK.getDefaultState();
        if(y>=3 && y<column.maxY && column.interior!=null && column.interior[y]!=null)state=column.interior[y];
        if(column.shelfRing && Math.abs(x)>2 && Math.abs(z)>2 && y>=3 && y<=32 && state.getBlock()==Blocks.AIR)
            state=ModBlocks.RUINED_BOOKCASE.getDefaultState().withProperty(BlockHorizontal.FACING,column.facing);
        if(state.getBlock()==Blocks.WEB)return Blocks.AIR.getDefaultState();
        if(state.getBlock()==Blocks.MONSTER_EGG || state.getBlock()==Blocks.COBBLESTONE)return Blocks.STONEBRICK.getDefaultState();
        if(state.getBlock()==ModBlocks.RUINED_BOOKCASE && state.getValue(RuinedBookcase.DAMAGE)==RuinedBookcase.Damage.COLLAPSED)
            return state.withProperty(RuinedBookcase.DAMAGE,RuinedBookcase.Damage.SPARSE);
        return state;
    }
    public static void paint(ChunkPrimer primer,int cx,int cz,LibraryRuinLayout.Site site) {
        if(!LibraryRuinLayout.intersectsChunk(site,cx,cz))return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int lx=(cx<<4)+x-site.centerX(),lz=(cz<<4)+z-site.centerZ();
            int top=LibraryStructure.columnMaxY(lx,lz);
            for(int y=0;y<=top;y++) {
                IBlockState state=stateAt(site,lx,y,lz);
                if(state!=null)primer.setBlockState(x,site.baseY+y,z,state);
            }
        }
    }
    public static void installContainers(Chunk chunk,LibraryRuinLayout.Site site) {
        for(int i=0;i<CHESTS.length;i++) {
            BlockPos pos=new BlockPos(site.centerX()+CHESTS[i][0],site.baseY+3,site.centerZ()+CHESTS[i][1]);
            if(pos.getX()>>4!=chunk.x || pos.getZ()>>4!=chunk.z)continue;
            // Called only when a new chunk is constructed; loading a saved chunk does not reseed loot.
            if(chunk.getBlockState(pos).getBlock()!=Blocks.CHEST)continue;
            TileEntityChest tile=new TileEntityChest();
            tile.setLootTable(loot(i),LibraryRuinLayout.blockNoise(site.structureSeed,CHESTS[i][0],i,CHESTS[i][1]));
            if(i==7)tile.setCustomName("封存魔导书手稿");
            else if(i==6)tile.setCustomName("红茶室藏品");
            chunk.addTileEntity(pos,tile);
        }
    }
    private static ResourceLocation loot(int index) {
        return index<4?LibraryStructure.COMMON_LOOT:index<6?LibraryStructure.RESEARCH_LOOT:
                index==6?LibraryStructure.CORE_LOOT:LibraryStructure.SECRET_LOOT;
    }
}
