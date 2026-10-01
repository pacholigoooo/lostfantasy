package dev.lostfantasy.core;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class ResearchCatalogTest {
    @Test public void eachLibraryHasSixSeparateRecordsTwoUnrelatedCabinetsAndMatchesInDifferentAreas() {
        for(long seed=-50;seed<=50;seed++) {
            Set<Integer> records=new HashSet<>();int empty=0,sample=-1,growth=-1;
            for(int cabinet=0;cabinet<ResearchCatalog.COUNT;cabinet++) {
                int document=ResearchCatalog.document(seed,cabinet);
                assertEquals(document,ResearchCatalog.document(seed,cabinet));
                if(document<0) {empty++;continue;}
                assertTrue(records.add(document));
                if(document==2)sample=cabinet;
                if(document==4)growth=cabinet;
            }
            assertEquals(6,records.size());assertEquals(2,empty);
            assertTrue(sample==4 || sample==5);assertTrue(growth>=0 && growth<4);
        }
    }
    @Test public void eachCabinetHasExactlyOneBlockAndAdjacentRowsHaveNoEntry() {
        assertEquals(0,ResearchCatalog.indexAt(10,4,14));
        assertEquals(-1,ResearchCatalog.indexAt(10,3,14));assertEquals(-1,ResearchCatalog.indexAt(10,5,14));
        assertEquals(-1,ResearchCatalog.indexAt(-6,3,-5));assertEquals(4,ResearchCatalog.indexAt(-6,4,-5));
        assertEquals(-1,ResearchCatalog.indexAt(-6,5,-5));assertEquals(-1,ResearchCatalog.indexAt(9,3,14));
        assertEquals(-1,ResearchCatalog.document(42,-1));assertEquals(0,ResearchCatalog.evidence(-1));
        int[] positions=new int[ResearchCatalog.COUNT];
        for(int x=-15;x<=15;x++)for(int z=-16;z<=16;z++)for(int y=0;y<=7;y++) {
            int cabinet=ResearchCatalog.indexAt(x,y,z);if(cabinet>=0)positions[cabinet]++;
        }
        assertArrayEquals(new int[]{1,1,1,1,1,1,1,1},positions);
    }
}
