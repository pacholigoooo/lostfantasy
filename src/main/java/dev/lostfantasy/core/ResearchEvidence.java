package dev.lostfantasy.core;

/** The loose page and its three comparison candidates, shared with server-side answer checks. */
public final class ResearchEvidence {
    public static final int CHOICES=3, SAMPLE_MATCH=2, GROWTH_MATCH=1;
    private ResearchEvidence() {}
    public static boolean matches(int evidence,int choice) {
        return evidence==ResearchProgress.SAMPLE?choice==SAMPLE_MATCH
                :evidence==ResearchProgress.GROWTH && choice==GROWTH_MATCH;
    }
    public static String title(boolean growth,int choice) {
        return growth?new String[]{"夹页一 · 留存试排","夹页二 · 生长时序","夹页三 · 结块观察"}[choice]
                :new String[]{"样本一 · 旧登记卡","样本二 · 细粉","样本三 · 翠晶"}[choice];
    }
    public static String description(boolean growth,int choice) {
        return growth?new String[]{
                "外圈先升起，随后轮到内圈。所有晶柱长成后，一同收回。页边有三道短线。",
                "脚边的地纹先亮。晶柱一圈圈向外升起，停留片刻，再沉回地面。页边有三道短线。",
                "灰白细粉吸水结块，表面未长出晶柱。页边只有一道长线。"}[choice]
                :new String[]{
                "翠绿色棱柱，顶端尖。卡片右侧是一道长线，卷号末尾写着17。",
                "灰白细粉，受潮后结成小块。纸角磨损，卷号只剩一个7。",
                "翠绿色，多面棱柱，尖端收拢。右侧有三道短线，卷号为T-07。"}[choice];
    }
}
