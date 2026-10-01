package dev.lostfantasy.client;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class ManuscriptScreen extends GuiScreen {
    private final List<String> lines=new ArrayList<>();private int page,left,top,rows,paperWidth,paperHeight;
    private GuiButton previous,next;
    @Override public boolean doesGuiPauseGame() {return false;}
    @Override public void initGui() {
        paperWidth=Math.min(312,width-20);paperHeight=Math.min(300,height-20);
        left=(width-paperWidth)/2;top=(height-paperHeight)/2;rows=Math.max(1,(paperHeight-76)/12);lines.clear();buttonList.clear();
        ResourceLocation resource=new ResourceLocation("lostfantasy:texts/manuscript_zh_cn.txt");
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(mc.getResourceManager().getResource(resource).getInputStream(),StandardCharsets.UTF_8))) {
            String line;while((line=reader.readLine())!=null) {
                if(line.isEmpty())lines.add("");else lines.addAll(fontRenderer.listFormattedStringToWidth(line,paperWidth-38));
            }
        }catch(IOException e){
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("Cannot read text resource "+resource,e);
            lines.clear();lines.add("手稿资源读取失败。");
        }
        previous=new GuiButton(0,left+12,top+paperHeight-29,56,20,"上一张");next=new GuiButton(1,left+paperWidth-68,top+paperHeight-29,56,20,"下一张");
        buttonList.add(previous);buttonList.add(next);buttonList.add(new GuiButton(2,left+paperWidth-26,top+6,18,18,"x"));
        updatePage();
    }
    @Override public void drawScreen(int x,int y,float partial) {
        drawDefaultBackground();
        drawRect(left+5,top+5,left+paperWidth+5,top+paperHeight+5,0xffb9b3a3);
        drawRect(left+2,top+2,left+paperWidth+2,top+paperHeight+2,0xffd8d0bc);
        drawRect(left,top,left+paperWidth,top+paperHeight,0xfff1ead6);
        drawRect(left+12,top+26,left+paperWidth-12,top+27,0xffc4b89c);
        fontRenderer.drawString("皇家烈焰研究手稿",left+16,top+12,0x493d37);
        for(int i=0;i<rows&&page*rows+i<lines.size();i++)fontRenderer.drawString(lines.get(page*rows+i),left+18,top+34+i*12,0x382c36);
        String number="散页 "+(page+1)+" / "+pageCount();
        fontRenderer.drawString(number,left+(paperWidth-fontRenderer.getStringWidth(number))/2,top+paperHeight-23,0x665846);super.drawScreen(x,y,partial);
    }
    private int pageCount() {return Math.max(1,(lines.size()+rows-1)/rows);}
    private void updatePage() {page=Math.max(0,Math.min(pageCount()-1,page));previous.enabled=page>0;next.enabled=page+1<pageCount();}
    @Override protected void actionPerformed(GuiButton b) {if(b.id==2)mc.displayGuiScreen(null);else {page+=b.id==0?-1:1;updatePage();}}
    @Override protected void keyTyped(char c,int key) throws IOException {
        if(key==org.lwjgl.input.Keyboard.KEY_LEFT || key==org.lwjgl.input.Keyboard.KEY_RIGHT) {page+=key==org.lwjgl.input.Keyboard.KEY_LEFT?-1:1;updatePage();}
        else super.keyTyped(c,key);
    }
    @Override public void handleMouseInput() throws IOException {super.handleMouseInput();int wheel=org.lwjgl.input.Mouse.getEventDWheel();if(wheel!=0){page+=wheel>0?-1:1;updatePage();}}
}
