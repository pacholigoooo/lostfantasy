package dev.lostfantasy.client;

import java.io.DataInputStream;
import java.io.FileInputStream;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiganModelTest {
    private static final class Sprite extends TextureAtlasSprite {
        Sprite() {super("lostfantasy:blocks/gohei_white");setIconWidth(16);setIconHeight(16);initSprite(256,256,64,32,false);}
    }
    @Test public void denseFieldMeshesBakeWithinBudgetAndDetailedItemsRemainAvailable() throws Exception {
        for(String name:new String[]{"spider_lily","spider_lily_field","floating_lily"})
            try(DataInputStream input=new DataInputStream(new FileInputStream("src/main/resources/assets/lostfantasy/meshes/"+name+".lfm"))) {
                int count=MeshItemModel.bake(input,new Sprite(),false).size();
                assertTrue(name,count>100);assertTrue(name,count<(name.contains("field")||name.equals("floating_lily")?600:2000));
            }
    }
}
