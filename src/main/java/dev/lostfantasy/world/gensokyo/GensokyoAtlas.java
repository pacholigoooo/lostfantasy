package dev.lostfantasy.world.gensokyo;

import java.util.*;

/** Coordinates are an adaptation of the supplied map, not canonical measurements. */
public enum GensokyoAtlas {
    HAKUREI("博丽神社",738,488,116,83,86,Kind.BUILDING),
    FAIRY_TREE("三月精新居",745,465,124,23,23,Kind.BUILDING),
    FAIRY_OLD_TREE("三月精旧居",447,630,91,26,26,Kind.BUILDING),
    YOUKAI_TRAIL("妖怪兽道",702,493,99,0,0,Kind.PATH),
    MYSTIA("夜雀摊",682,501,93,17,14,Kind.BUILDING),
    KOURINDOU("香霖堂",668,518,90,31,28,Kind.BUILDING),
    VILLAGE("人间之里",560,501,83,470,235,Kind.SETTLEMENT),
    TERAKOYA("寺子屋",514,518,83,37,31,Kind.BUILDING),
    SUZUNAAN("铃奈庵",546,518,83,27,26,Kind.BUILDING),
    HIEDA("稗田邸",602,518,83,77,65,Kind.BUILDING),
    WILLOW_CANAL("柳之运河",459,493,76,0,0,Kind.WATER),
    ROPEWAY("索道人里站",422,414,87,35,27,Kind.BUILDING),
    RICE_FIELDS("稻田",468,443,85,0,0,Kind.REGION),
    ROPEWAY_MOUNTAIN("索道山上站",397,89,195,35,27,Kind.BUILDING),
    ABANDONED_POND("置行堀",461,402,80,0,0,Kind.WATER),
    CIRNO("琪露诺的家",366,455,72,21,20,Kind.BUILDING),
    SCARLET("红魔馆",288,400,87,117,111,Kind.BUILDING),
    MIST_LAKE("雾之湖",282,498,72,0,0,Kind.WATER),
    MOON_GATE("月都境界",303,502,76,16,16,Kind.PORTAL),
    MARISA("雾雨邸",410,588,89,37,36,Kind.BUILDING),
    GHOST_HOUSE("幽灵洋馆",486,588,95,36,33,Kind.BUILDING),
    EARTH_RAINBOW("地灵虹洞",477,604,88,25,27,Kind.CAVE),
    LARVA("拉尔瓦的地穴",552,612,96,19,19,Kind.CAVE),
    ALICE("玛格特洛依德邸",528,660,95,45,42,Kind.BUILDING),
    MOKOU("藤原邸",642,640,100,29,28,Kind.BUILDING),
    EIENTEI("永远亭",696,650,103,99,90,Kind.BUILDING),
    FAIRY_SHRINE("妖精神社",390,696,92,30,33,Kind.BUILDING),
    WITCH_MANOR("魔法使之家",412,732,101,47,44,Kind.BUILDING),
    SUN_GARDEN("太阳花田",380,772,89,0,0,Kind.REGION),
    NAMELESS_HILL("无名之丘",320,710,115,0,0,Kind.REGION),
    SUZURAN("铃兰花田",280,770,94,0,0,Kind.REGION),
    MUENZUKA("无缘塚",603,726,105,41,38,Kind.BUILDING),
    RECONSIDERATION("再思之道",574,746,101,0,0,Kind.PATH),
    MYOUREN("命莲寺",543,388,93,104,92,Kind.BUILDING),
    PALANQUIN("圣辇船",688,253,203,28,70,Kind.AIR),
    TANUKI_FOREST("妖怪狸之森",670,443,93,0,0,Kind.REGION),
    NETHER_GATE("幽明结界",772,606,140,33,32,Kind.PORTAL),
    SHINING_NEEDLE("辉针城",145,568,201,84,80,Kind.AIR),
    PHANTOM_MEADOW("幻草原",236,612,80,0,0,Kind.REGION),
    SPRING_PATH("春之小径",380,552,77,0,0,Kind.PATH),
    CHERRY_PATH("夜樱怪道",362,574,81,0,0,Kind.PATH),
    RUINED_MANSION("废弃洋馆",263,286,116,61,53,Kind.BUILDING),
    AQUATIC_MARKET("未来水妖集市",346,300,107,59,42,Kind.BUILDING),
    GEYSER("间歇泉地下中心",391,255,127,57,48,Kind.BUILDING),
    BLOWHOLE("幻想风穴",383,290,112,30,27,Kind.CAVE),
    SEA_OF_TREES("妖怪树海",502,343,115,0,0,Kind.REGION),
    MOUNTAIN_TOP("妖怪之山山顶·玄云海",318,93,238,0,0,Kind.REGION),
    MORIYA("守矢神社",422,76,179,105,114,Kind.BUILDING),
    WIND_LAKE("风神之湖",468,74,173,0,0,Kind.WATER),
    KASEN("茨华仙邸",602,59,178,68,62,Kind.PORTAL),
    WATERFALL("九天瀑布",423,142,137,0,0,Kind.WATER),
    TENGU("天狗聚落",327,174,121,360,326,Kind.SETTLEMENT),
    CUCUMBER_FACTORY("黄瓜工厂",423,179,132,53,42,Kind.BUILDING),
    KAPPA("河童基地",529,198,116,148,114,Kind.BUILDING),
    GENBU("玄武涧",500,245,112,0,0,Kind.REGION),
    RAINBOW_CAVE("虹龙洞",620,180,188,35,17,Kind.CAVE),
    NEMUNO("山姥居",546,129,157,36,34,Kind.BUILDING),
    SECRET_CLIFF("秘天崖",657,181,132,148,35,Kind.SETTLEMENT),
    FALSE_HEAVEN("伪天棚",637,157,181,49,42,Kind.BUILDING),
    YAKUMO("八云邸",719,153,158,85,76,Kind.BUILDING),
    PEONY_FIELD("芍药田",555,224,118,36,34,Kind.BUILDING),
    MAYOHIGA("迷途之家",624,234,128,128,112,Kind.SETTLEMENT),
    TOAD_POND("大蛤蟆之池",615,281,104,0,0,Kind.WATER),
    SNOW_ROAD("残雪之道",666,330,119,0,0,Kind.PATH),
    LIMINAL_ROAD("中有之道",209,170,108,76,138,Kind.SETTLEMENT),
    SAI_BANK("赛之河原",152,145,92,0,0,Kind.REGION),
    SANZU_PIER("三途河渡口",100,200,76,36,31,Kind.PORTAL),
    SANZU("三途河",70,161,71,0,0,Kind.WATER);

    public enum Kind { BUILDING, SETTLEMENT, CAVE, PORTAL, AIR, REGION, PATH, WATER }
    public final String title;
    public final int x,z,y,rx,rz;
    public final Kind kind;
    GensokyoAtlas(String title,int px,int pz,int y,int rx,int rz,Kind kind) {
        this.title=title; this.x=mapX(px); this.z=mapZ(pz); this.y=y;
        this.rx=rx; this.rz=rz; this.kind=kind;
    }
    public static int mapX(int pixel) {return (pixel-500)*8;}
    public static int mapZ(int pixel) {return (pixel-470)*8;}
    public boolean grounded() {return rx>0 && kind!=Kind.AIR && this!=CIRNO;}
    public boolean contains(int wx,int wz,int margin) {
        return Math.abs((long)wx-x)<=rx+margin && Math.abs((long)wz-z)<=rz+margin;
    }
    public int approachZ() {
        // These caves descend southwards. Their surface mouths face north, not the far end of the tunnel.
        if(this==BLOWHOLE || this==EARTH_RAINBOW)return z-34;
        if(this==LARVA)return z+28;
        return z+rz+5;
    }
    private static final Map<Long,List<GensokyoAtlas>> PLOTS=index();
    private static Map<Long,List<GensokyoAtlas>> index() {
        Map<Long,List<GensokyoAtlas>> result=new HashMap<>();
        // A region pad is blended first; individual plots may then refine it.
        for(Kind kind:new Kind[]{Kind.SETTLEMENT,Kind.BUILDING,Kind.CAVE,Kind.PORTAL})
            for(GensokyoAtlas site:values()) if(site.kind==kind) {
                int margin=48;
                for(int x=(site.x-site.rx-margin)>>7;x<=(site.x+site.rx+margin)>>7;x++)
                    for(int z=(site.z-site.rz-margin)>>7;z<=(site.z+site.rz+margin)>>7;z++)
                        result.computeIfAbsent(key(x,z),k->new ArrayList<>()).add(site);
            }
        result.replaceAll((k,v)->Collections.unmodifiableList(v));
        return Collections.unmodifiableMap(result);
    }
    static long key(int x,int z) {return (long)x<<32 ^ (z&0xffffffffL);}
    public static List<GensokyoAtlas> plots(int x,int z) {
        return PLOTS.getOrDefault(key(x>>7,z>>7),Collections.emptyList());
    }
    public static boolean reserved(int x,int z,int margin) {
        for(GensokyoAtlas site:plots(x,z))if(site.contains(x,z,margin))return true;
        return false;
    }
}
