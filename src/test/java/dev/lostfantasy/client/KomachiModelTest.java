package dev.lostfantasy.client;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;

public class KomachiModelTest {
    @Test public void heldToolsHaveValidNormalsUvsAndSmallTriangleBudget() throws Exception {
        int vertices=0;
        for(String key:KomachiMesh.PARTS)try(DataInputStream in=new DataInputStream(new FileInputStream("src/main/resources/assets/lostfantasy/meshes/komachi/"+key+".lfm"))) {
            TexturedMesh.Part part=TexturedMesh.read(in);vertices+=part.colors.length;
            assertEquals(key,0,part.colors.length%36);
            for(int o=0;o<part.values.length;o+=8) {
                double norm=0;for(int a=5;a<8;a++)norm+=part.values[o+a]*part.values[o+a];assertEquals(key,1,norm,.0001);
            }
        }
        assertTrue(vertices>100);assertTrue("held tool budget",vertices<600);
    }
    @Test public void blinkOnlyChangesTheFacePixels() throws Exception {
        BufferedImage open=ImageIO.read(new File("src/main/resources/assets/lostfantasy/textures/entity/komachi.png"));
        BufferedImage closed=ImageIO.read(new File("src/main/resources/assets/lostfantasy/textures/entity/komachi_blink.png"));
        assertEquals(128,open.getWidth());assertEquals(128,open.getHeight());int changed=0;
        for(int y=0;y<128;y++)for(int x=0;x<128;x++)if(open.getRGB(x,y)!=closed.getRGB(x,y)) {
            changed++;assertTrue(((x>=18 && x<22) || (x>=26 && x<30)) && y>=24 && y<28);
        }
        assertTrue(changed>=20 && changed<=32);
    }
    @Test public void standardAlexBaseFacesAreOpaqueAndSecondLayerKeepsCutouts() throws Exception {
        BufferedImage skin=ImageIO.read(new File("src/main/resources/assets/lostfantasy/textures/entity/komachi.png"));
        int[][] parts={{0,0,8,8,8},{16,16,8,12,4},{40,16,3,12,4},{32,48,3,12,4},{0,16,4,12,4},{16,48,4,12,4}};
        for(int[] p:parts) {
            int u=p[0],v=p[1],w=p[2],h=p[3],d=p[4];
            for(int y=2*(v+d);y<2*(v+d+h);y++)for(int x=2*u;x<2*(u+2*w+2*d);x++)assertEquals("base face",255,skin.getRGB(x,y)>>>24);
            for(int y=2*v;y<2*(v+d);y++)for(int x=2*(u+d);x<2*(u+d+2*w);x++)assertEquals("base cap",255,skin.getRGB(x,y)>>>24);
        }
        assertEquals("clear eye overlay",0,skin.getRGB(86,24)>>>24);
        assertEquals("outer hair",255,skin.getRGB(84,16)>>>24);
        assertEquals("slim arm unused column",0,skin.getRGB(108,42)>>>24);
    }
}
