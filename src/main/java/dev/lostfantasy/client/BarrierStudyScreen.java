package dev.lostfantasy.client;

import dev.lostfantasy.core.Spell;
import dev.lostfantasy.network.ActionMessage;
import dev.lostfantasy.network.Network;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class BarrierStudyScreen extends GuiScreen {
    private final BlockPos pos;
    private final Spell spell;
    private final List<String> lines = new ArrayList<>();
    private int left, top, paperWidth, paperHeight, rows, page;
    private GuiButton previous, next, study;
    public BarrierStudyScreen(BlockPos pos) { this(pos, Spell.FOURFOLD_BARRIER); }
    public BarrierStudyScreen(BlockPos pos, Spell spell) { this.pos = pos.toImmutable(); this.spell = spell; }
    private boolean emerald() { return spell == Spell.EMERALD_CITY; }
    @Override public boolean doesGuiPauseGame() { return false; }
    @Override public void initGui() {
        paperWidth = Math.min(330, width-16); paperHeight = Math.min(310, height-16);
        left = (width-paperWidth)/2; top = (height-paperHeight)/2;
        rows = Math.max(1,(paperHeight-112)/12);
        lines.clear(); buttonList.clear();
        ResourceLocation resource=new ResourceLocation("lostfantasy:texts/"+(emerald()?"emerald":"barrier")+"_study_zh_cn.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mc.getResourceManager().getResource(
                resource).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line=reader.readLine())!=null) {
                if (line.isEmpty()) lines.add(""); else lines.addAll(fontRenderer.listFormattedStringToWidth(line, paperWidth-32));
            }
        } catch (IOException e) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("Cannot read text resource "+resource,e);
            lines.clear();lines.add("札记暂时无法读取。");
        }
        previous = new GuiButton(0,left+12,top+paperHeight-66,48,20,"上一页");
        next = new GuiButton(1,left+paperWidth-60,top+paperHeight-66,48,20,"下一页");
        study = new GuiButton(2,left+12,top+paperHeight-28,emerald()?(paperWidth-30)/2:paperWidth-24,20,"");
        buttonList.add(previous); buttonList.add(next); buttonList.add(study);
        if(emerald()) buttonList.add(new GuiButton(4,left+18+study.width,top+paperHeight-28,study.width,20,"查看桌边残页"));
        buttonList.add(new GuiButton(3,left+paperWidth-26,top+7,18,18,"×"));
        updateScreen();
    }
    @Override public void updateScreen() {
        if (mc.player == null || mc.player.getDistanceSqToCenter(pos)>36 || !mc.player.isEntityAlive()) { mc.displayGuiScreen(null); return; }
        page=Math.max(0,Math.min(pageCount()-1,page)); previous.enabled=page>0; next.enabled=page+1<pageCount();
        boolean known=ClientProxy.state.knows(spell), qualifies=ClientProxy.state.qualifies(spell);
        boolean researched=!emerald() || ClientProxy.state.research.has(dev.lostfantasy.core.ResearchProgress.COMPLETED);
        study.enabled=!known && ClientProxy.state.canStudy(spell);
        study.displayString=known?"已经掌握":!researched?"先完成残页调查":qualifies
                ?(emerald()?"研习翡翠巨城":"研习四重结界"):(emerald()?"需要魔法使阶段":"研习需要达到大妖怪阶段");
    }
    private int pageCount() { return Math.max(1,(lines.size()+rows-1)/rows); }
    @Override public void drawScreen(int x,int y,float partial) {
        drawDefaultBackground();
        drawRect(left-2,top-2,left+paperWidth+2,top+paperHeight+2,0xff54356e);
        drawRect(left,top,left+paperWidth,top+paperHeight,0xffeee5d1);
        fontRenderer.drawString(emerald()?"土金术式研习页":"四重结界研习札记",left+16,top+14,0x51365d);
        drawRect(left+14,top+30,left+paperWidth-14,top+31,0xffae8b56);
        for(int i=0;i<rows && page*rows+i<lines.size();i++) fontRenderer.drawString(lines.get(page*rows+i),left+16,top+40+i*12,0x45364d);
        drawCenteredString(fontRenderer,(page+1)+" / "+pageCount(),left+paperWidth/2,top+paperHeight-60,0xffa68ca8);
        fontRenderer.drawString("原稿留在桌上，可供每位来访者研习。",left+14,top+paperHeight-41,0x6e5b63);
        super.drawScreen(x,y,partial);
    }
    @Override protected void actionPerformed(GuiButton button) {
        if(button.id==3) mc.displayGuiScreen(null);
        else if(button.id==4) Network.research(pos,dev.lostfantasy.network.ResearchRequest.OPEN,0);
        else if(button.id==2) Network.action(emerald()?ActionMessage.LEARN_EMERALD:ActionMessage.LEARN_BARRIER,pos.getX(),pos.getZ());
        else { page+=button.id==0?-1:1; updateScreen(); }
    }
    @Override protected void keyTyped(char typed,int key) throws IOException {
        if(key==Keyboard.KEY_LEFT || key==Keyboard.KEY_RIGHT) { page+=key==Keyboard.KEY_LEFT?-1:1; updateScreen(); }
        else super.keyTyped(typed,key);
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput(); int wheel=Mouse.getEventDWheel();
        if(wheel!=0) { page+=wheel>0?-1:1; updateScreen(); }
    }
}
