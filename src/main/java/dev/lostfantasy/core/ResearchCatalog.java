package dev.lostfantasy.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** One record per single cabinet block; assignment is stable for each library. */
public final class ResearchCatalog {
    public static final int COUNT=8, EMPTY=-1;
    private static final int[][] LOW={{-6,-5},{-6,5},{6,-5},{6,5}};
    private ResearchCatalog() {}
    public static int indexAt(int x,int y,int z) {
        if(y!=4)return -1;
        if(x>=10 && x<=13 && z==14)return x-10;
        for(int i=0;i<LOW.length;i++)if(x==LOW[i][0] && z==LOW[i][1])return i+4;
        return -1;
    }
    public static int document(long seed,int cabinet) {
        if(cabinet<0 || cabinet>=COUNT)return EMPTY;
        Random random=new Random(seed^0x544f554b494e3037L);
        int[] documents=new int[COUNT];Arrays.fill(documents,EMPTY);
        // Keep the matching sample in a western low cabinet and its growth record in the study bank.
        int sample=4+random.nextInt(2),growth=random.nextInt(4);
        documents[sample]=ResearchEvidence.SAMPLE_MATCH;
        documents[growth]=ResearchEvidence.CHOICES+ResearchEvidence.GROWTH_MATCH;
        List<Integer> free=new ArrayList<>();
        for(int i=0;i<COUNT;i++)if(i!=sample && i!=growth)free.add(i);
        Collections.shuffle(free,random);
        int next=0;
        for(int id=0;id<ResearchEvidence.CHOICES*2;id++)
            if(id!=ResearchEvidence.SAMPLE_MATCH && id!=ResearchEvidence.CHOICES+ResearchEvidence.GROWTH_MATCH)
                documents[free.get(next++)]=id;
        return documents[cabinet];
    }
    public static int evidence(int document) {
        return document<0 || document>=ResearchEvidence.CHOICES*2?0
                :document<ResearchEvidence.CHOICES?ResearchProgress.SAMPLE:ResearchProgress.GROWTH;
    }
    public static int choice(int document) {return document<0?-1:document%ResearchEvidence.CHOICES;}
    public static String label(int cabinet,int turns) {
        if(cabinet<4)return "研究区·"+(cabinet+1)+"号文档柜";
        int x=LOW[cabinet-4][0],z=LOW[cabinet-4][1];
        for(int i=0;i<turns;i++) {int previous=x;x=-z;z=previous;}
        return (x<0?"西":"东")+(z<0?"北":"南")+"侧矮文档柜";
    }
}
