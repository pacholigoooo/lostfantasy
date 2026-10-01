package dev.lostfantasy.client;

import dev.lostfantasy.core.*;
import dev.lostfantasy.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** A fixed fragment beside the record actually stored in the clicked cabinet. */
public final class ResearchScreen extends GuiScreen {
    private ResearchMessage state;
    private final boolean copy;
    private final ResearchProgress progress=new ResearchProgress();
    private final List<String> lines=new ArrayList<>();
    private int left,top,main,page,pendingTicks;
    private static final int WIDTH=568,HEIGHT=352,ROWS=17;
    private float scale=1;
    public ResearchScreen(ResearchMessage state) {this.state=state;copy=false;progress.restore(state.flags);}
    public ResearchScreen() {copy=true;}
    public boolean matches(ResearchMessage message) {return !copy && state.dimension==message.dimension && state.pos.equals(message.pos);}
    public void receive(ResearchMessage message) {
        if(!matches(message))return;
        state=message;progress.restore(message.flags);pendingTicks=0;initGui();
    }
    private boolean cabinet() {return !copy && state.role==1;}
    private int evidence() {return cabinet()?ResearchCatalog.evidence(state.document):0;}
    @Override public boolean doesGuiPauseGame() {return false;}
    @Override public void setWorldAndResolution(Minecraft client,int w,int h) {
        scale=Math.min(1,Math.min(w/584f,h/368f));
        super.setWorldAndResolution(client,Math.round(w/scale),Math.round(h/scale));
    }
    private void button(int id,int x,int y,int w,String label) {buttonList.add(new GuiButton(id,x,y,w,20,label));}
    @Override public void initGui() {
        left=(width-WIDTH)/2;top=(height-HEIGHT)/2;main=left+194;buttonList.clear();
        button(2,left+WIDTH-30,top+12,18,"×");
        if(cabinet()) {
            if(evidence()!=0)button(25,main,top+294,354,"与残页对照");
        } else {
            button(0,main,top+266,65,"上一页");button(1,main+289,top+266,65,"下一页");
            if(!copy) {
                button(10,main,top+298,173,"记下线索");button(11,main+181,top+298,173,"返回研习页");
                button(12,main,top+324,173,"抄一份带走");button(13,main+181,top+324,173,"重看桌面演示");
            }
        }
        loadText();updateButtons();
    }
    private void loadText() {
        lines.clear();
        ResourceLocation resource=new ResourceLocation("lostfantasy:texts/research_"+(copy?"copy":"fragment")+"_zh_cn.txt");
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(mc.getResourceManager().getResource(
                resource).getInputStream(),StandardCharsets.UTF_8))) {
            String line;
            while((line=reader.readLine())!=null) {
                if(!copy)line=line.replace("{direction}",direction()).replace("{sampleDirection}",sampleDirection());
                if(line.isEmpty())lines.add("");else lines.addAll(fontRenderer.listFormattedStringToWidth(line,346));
            }
        } catch(IOException e) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("Cannot read text resource "+resource,e);
            lines.clear();lines.add("这页暂时读不出来。");
        }
        page=Math.min(page,pageCount()-1);
    }
    private String direction() {return new String[]{"东南","西南","西北","东北"}[state.turns];}
    private String sampleDirection() {return new String[]{"西侧","北侧","东侧","南侧"}[state.turns];}
    private int pageCount() {return Math.max(1,(lines.size()+ROWS-1)/ROWS);}
    private void updateButtons() {
        page=Math.max(0,Math.min(page,pageCount()-1));
        boolean done=progress.has(8),found=progress.has(1);
        for(GuiButton b:buttonList) {
            if(b.id==0)b.enabled=page>0;
            if(b.id==1)b.enabled=page+1<pageCount();
            if(copy)continue;
            if(b.id==10) {b.displayString=done?"记录已合好":found?"合起记录，查看演示":"记下残页线索";b.enabled=pendingTicks==0 && !done && (!found || progress.ready());}
            if(b.id==12) {b.visible=done;b.enabled=pendingTicks==0 && !state.hasCopy;}
            if(b.id==13) {b.visible=done;b.enabled=pendingTicks==0;}
            if(b.id==25) {
                boolean matched=progress.has(evidence());
                b.displayString=matched?"这项线索已记下":"与残页对照";b.enabled=pendingTicks==0 && found && !matched;
            }
        }
    }
    @Override public void updateScreen() {
        if(!copy && (mc.player==null || !mc.player.isEntityAlive() || mc.world==null || mc.player.dimension!=state.dimension || mc.player.getDistanceSqToCenter(state.pos)>36)) {
            mc.displayGuiScreen(null);return;
        }
        if(pendingTicks>0)pendingTicks--;updateButtons();
    }
    @Override public void drawScreen(int x,int y,float partial) {
        try(RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.scale(scale,scale,1);
            drawDefaultBackground();
            drawRect(left-3,top-3,left+WIDTH+3,top+HEIGHT+3,0xff263d35);
            drawRect(left,top,left+WIDTH,top+HEIGHT,0xffeee6d2);drawRect(left,top,left+WIDTH,top+39,0xff324f44);
            fontRenderer.drawString(copy?"土金研究抄本":"未归档的一页",left+18,top+15,0xf0e4bf);
            fontRenderer.drawString(copy?"随身抄本":cabinet()?ResearchCatalog.label(state.catalog,state.turns):"中央研究桌",left+200,top+15,0xc2d7c4);
            sidebar();
            if(cabinet())drawComparison();else drawReader();
            super.drawScreen(Math.round(x/scale),Math.round(y/scale),partial);
        }
    }
    private void sidebar() {
        drawRect(left+12,top+50,left+178,top+272,0xffddd3b9);
        fontRenderer.drawString("残页上的线索",left+24,top+62,0x3f5145);
        for(int i=0;i<3;i++)drawRect(left+30+i*18,top+89,left+42+i*18,top+91,0xff526856);
        fontRenderer.drawString("三道短线",left+24,top+104,0x655b49);
        wrapped("翠绿色的尖顶棱柱。",left+24,top+131,138,0x413e33);
        wrapped("圆圈旁的箭头朝外。近处先起，远处随后。",left+24,top+164,138,0x413e33);
        wrapped("末尾写着：稍停，再沉回地面。",left+24,top+210,138,0x413e33);
        fontRenderer.drawString(copy?"土金卷 T-07":"卷号的末尾还剩一个7",left+22,top+251,0x77684f);
        fontRenderer.drawString("样本："+(copy||progress.has(2)?"已对上":"未找到"),left+22,top+286,0x355c48);
        fontRenderer.drawString("生长："+(copy||progress.has(4)?"已对上":"未找到"),left+22,top+305,0x355c48);
        if(!copy)wrapped(progress.has(8)?"原件留在桌上，抄本可以带走。":progress.ready()?"回研究桌，把两页合起来。":"样本在"+sampleDirection()+"文档柜；生长记录在"+direction()+"研究区。",left+22,top+326,146,0x75674f);
    }
    private void drawReader() {
        for(int i=0;i<ROWS && page*ROWS+i<lines.size();i++)fontRenderer.drawString(lines.get(page*ROWS+i),main,top+55+i*12,0x423d32);
        String number=(page+1)+" / "+pageCount();fontRenderer.drawString(number,main+(354-fontRenderer.getStringWidth(number))/2,top+272,0x786d58);
        if(!copy)fontRenderer.drawString(fontRenderer.trimStringToWidth(notice(),354),main,top+286,0x7b533c);
    }
    private void drawComparison() {
        drawRect(main,top+52,main+354,top+282,0xffe2dac6);
        if(evidence()==0) {
            String[] titles={"借阅登记","旧书目","抽屉"};
            String[] descriptions={"几张借阅登记夹在隔板间，书名旁写着借出和归还的日期。","里面放着书脊补签和一册旧书目，纸边已经发黄。","抽屉里落着薄灰，角落剩下几张空白标签。"};
            int variant=state.catalog%3;
            fontRenderer.drawString(titles[variant],main+12,top+65,0x365348);
            wrapped(descriptions[variant],main+12,top+108,328,0x443e32);
            return;
        }
        boolean growth=evidence()==ResearchProgress.GROWTH;int choice=ResearchCatalog.choice(state.document);
        fontRenderer.drawString(growth?"夹在柜中的生长记录":"柜中的样本登记卡",main+12,top+65,0x75674f);
        fontRenderer.drawString(ResearchEvidence.title(growth,choice),main+12,top+100,0x365348);
        if(!growth && choice!=1) {
            ResearchScene.sampleInset(main+180,top+231,23);
            for(int i=0;i<(choice==2?3:1);i++)drawRect(main+280,top+156+i*12,main+302+(choice==0?15:0),top+158+i*12,0xff566d59);
        } else if(!growth || choice==2) {
            for(int i=0;i<15;i++) {int px=main+125+(i*31)%90,py=top+175+(i*13)%30;drawRect(px,py,px+11,py+7,0xff9c9c91);}
        } else {
            for(int i=0;i<4;i++) {
                int order=choice==1?i:3-i;
                drawRect(main+42+i*73,top+190-order*10,main+73+i*73,top+208,0xff4d9c78);
                fontRenderer.drawString(Integer.toString(order+1),main+53+i*73,top+215,0x48634e);
            }
            fontRenderer.drawString("内圈                         外圈",main+36,top+140,0x5c6e59);
        }
        wrapped(ResearchEvidence.description(growth,choice),main+12,top+241,328,0x443e32);
        wrapped(notice(),main,top+324,354,state.notice==2?0x954b3b:0x496347);
    }
    private void wrapped(String text,int x,int y,int maxWidth,int color) {
        int row=0;for(String line:fontRenderer.listFormattedStringToWidth(text,maxWidth))fontRenderer.drawString(line,x,y+row++*12,color);
    }
    private String notice() {
        String[] messages={"","已记下纸上的颜色、记号和箭头。","内容或页边记号对不上，再看看左边的残页。","先找到样本和生长记录，再回桌边。","这页与残页对得上。","抄本已放进背包。","背包满了，腾出空位后再来抄。","你已经带着一份抄本。"};
        if(state.notice>0)return messages[state.notice];
        if(progress.has(8))return "记录已合好，可以抄一份带走，或重看桌面演示。";
        if(!progress.has(1))return cabinet()?"先回中央研究桌，记下残页上的线索。":"记下残页，再去柜中找另外两页。";
        return progress.ready()?"两份记录都找到了，回中央研究桌吧。":"看看形状、记号和生长的先后顺序。";
    }
    @Override protected void actionPerformed(GuiButton b) {
        if(b.id==0 || b.id==1) {page+=b.id==0?-1:1;updateButtons();}
        else if(b.id==2)mc.displayGuiScreen(null);
        else if(b.id==11)mc.displayGuiScreen(new BarrierStudyScreen(state.pos,Spell.EMERALD_CITY));
        else if(!copy) {
            int action=b.id==10?(progress.has(1)?ResearchRequest.COMPLETE:ResearchRequest.DISCOVER)
                    :b.id==12?ResearchRequest.COPY:b.id==13?ResearchRequest.REPLAY
                    :evidence()==ResearchProgress.SAMPLE?ResearchRequest.SAMPLE:ResearchRequest.GROWTH;
            Network.research(state.pos,action,b.id==25?ResearchCatalog.choice(state.document):0);pendingTicks=8;updateButtons();
        }
    }
    @Override protected void keyTyped(char c,int key) throws IOException {
        if(!cabinet() && (key==Keyboard.KEY_LEFT || key==Keyboard.KEY_RIGHT)) {
            page+=key==Keyboard.KEY_LEFT?-1:1;updateButtons();
        } else super.keyTyped(c,key);
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0 && !cabinet()) {page+=wheel>0?-1:1;updateButtons();}
    }
}
