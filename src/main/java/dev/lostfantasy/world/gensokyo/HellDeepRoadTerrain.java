package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

/** The descending cave north of the Old Capital. Heights are absolute world coordinates. */
final class HellDeepRoadTerrain {
    private static final int[][] BUTTRESSES={{-94,-815,28,42},{82,-750,31,66},
            {-48,-492,25,88},{-118,-357,34,50},{119,-332,27,43}};
    private final GensokyoNoise noise;
    HellDeepRoadTerrain(GensokyoNoise noise) {this.noise=noise;}

    Column column(int x,int z) {
        if(Math.abs(x)>250 || z<-960 || z>-170)return null;
        double wx=x+noise.value(x,z,81,966)*9;
        double wz=z+noise.value(x,z,93,967)*8;
        double d=wx*wx/(230.0*230)+(wz+568)*(wz+568)/(392.0*392);
        if(d>=1)return null;
        double edge=Math.pow(d,3);
        int floor=43+(int)Math.round(noise.value(x,z,79,968)*8+edge*69);
        int roof=Math.min(247,132+(int)Math.round(111*Math.sqrt(1-d)+noise.value(x,z,37,969)*5));
        // Folded stone shoulders break the shaft into connected bays, leaving the bridge visible.
        for(int[] pier:BUTTRESSES) {
            double distance=Math.hypot(x-pier[0],z-pier[1]);
            double radius=pier[2]*(1+noise.value(x,z,19,970)*.18);
            if(distance<radius) {
                floor+=Math.round(pier[3]*Math.pow(1-distance/radius,.7));
                roof-=Math.round(19*(1-distance/radius));
            }
        }
        if(roof<=floor+5)return null;
        double moss=noise.value(x,z,27,971);
        IBlockState skin=moss>.13?ModBlocks.MOSSY_BASALT.getDefaultState()
                :moss<-.3?Blocks.STONE.getStateFromMeta(5):ModBlocks.COLUMNAR_BASALT.getDefaultState();
        return new Column(floor,roof,skin);
    }

    static final class Column {
        final int floor,roof;final IBlockState skin;
        Column(int floor,int roof,IBlockState skin) {this.floor=floor;this.roof=roof;this.skin=skin;}
        boolean open(int y) {return y>floor && y<roof;}
        boolean surface(int y) {return y>=floor-3 && y<=floor || y>=roof && y<=roof+4;}
    }
}
