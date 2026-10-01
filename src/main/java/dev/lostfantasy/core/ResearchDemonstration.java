package dev.lostfantasy.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.Vec3d;

/** A slow, replayable desk demonstration. No combat session, damage or world mutation. */
public final class ResearchDemonstration {
    public static final int DURATION=180;
    public static final List<EmeraldCity.Column> COLUMNS;
    static {
        List<EmeraldCity.Column> columns=new ArrayList<>();
        for(int ring=0;ring<EmeraldCity.RINGS;ring++)for(int i=0;i<EmeraldCity.columnsInRing(ring);i++) {
            Vec3d p=EmeraldCity.direction(0,ring,i).scale(EmeraldCity.ringRadius(ring));
            columns.add(new EmeraldCity.Column(p.x,0,p.z,EmeraldCity.HEIGHT,ring));
        }
        COLUMNS=Collections.unmodifiableList(columns);
    }
    private ResearchDemonstration() {}
    public static double fraction(double age,int ring) {
        return smooth((age-24-ring*12)/18)*(1-smooth((age-126-ring*4)/24));
    }
    private static double smooth(double t) { t=Math.max(0,Math.min(1,t));return t*t*(3-2*t); }
    public static String phase(double age) {
        if(age<24)return "地纹从脚边展开";
        if(age<48)return "内圈先破土";
        if(age<80)return "外圈接着升起";
        if(age<126)return "晶柱留在原处";
        if(age<168)return "晶柱沉回地面";
        return "演示结束";
    }
}
