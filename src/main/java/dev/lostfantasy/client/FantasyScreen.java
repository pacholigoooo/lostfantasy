package dev.lostfantasy.client;
import dev.lostfantasy.LostFantasy;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.core.Growth;
import dev.lostfantasy.SpiritCrafting;
import net.minecraft.item.ItemStack;
import dev.lostfantasy.core.SpellPage;
import dev.lostfantasy.core.Rules;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.network.ActionMessage;
import dev.lostfantasy.network.Network;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FantasyScreen extends GuiScreen {
    private static final int NOTE_ROWS = 12;
    private static final int PREVIOUS_SPELL_PAGE = 302, NEXT_SPELL_PAGE = 303;
    private final List<String> noteLines = new ArrayList<>();
    private final SpellPage spells = new SpellPage(Spell.catalog().stream().mapToInt(spell -> spell.networkId).toArray());
    private Spell viewedSpell = Spell.catalog().get(0);
    private int tab,slot,left,top,panelWidth,guidePage,routePage,recipePage;
    private float viewScale=1;
    public FantasyScreen(int tab) {this.tab=tab;}
    @Override public boolean doesGuiPauseGame() {return false;}
    @Override public void setWorldAndResolution(Minecraft client,int screenWidth,int screenHeight) {
        viewScale=Math.min(1,Math.min(screenWidth/440f,screenHeight/290f));
        super.setWorldAndResolution(client,Math.round(screenWidth/viewScale),Math.round(screenHeight/viewScale));
    }
    @Override public void initGui() {
        panelWidth=Math.min(420,width-20);left=(width-panelWidth)/2;top=Math.max(10,(height-276)/2);buttonList.clear();
        String[] tabs={"符卡准备","种族 / 职业","灵力制作","笔记"};
        for(int i=0;i<4;i++)buttonList.add(new GuiButton(10+i,left+12+i*(panelWidth-24)/4,top+34,(panelWidth-30)/4,20,tabs[i]));
        buttonList.add(new GuiButton(99,left+panelWidth-26,top+7,18,18,"×"));
        if(tab==0) {
            for(int i=0;i<Rules.LOADOUT_SIZE;i++)buttonList.add(new GuiButton(100+i,left+12,top+67+i*31,130,25,""));
            for(int row=0;row<spells.visibleCount();row++)buttonList.add(new GuiButton(200+row,left+154,top+67+row*31,panelWidth-166,25,""));
            buttonList.add(new GuiButton(300,left+12,top+195,62,20,"取消准备"));
            buttonList.add(new GuiButton(301,left+80,top+195,62,20,"选用"));
            if (spells.pageCount() > 1) {
                buttonList.add(new GuiButton(PREVIOUS_SPELL_PAGE,left+154,top+195,58,20,"上一页"));
                buttonList.add(new GuiButton(NEXT_SPELL_PAGE,left+panelWidth-70,top+195,58,20,"下一页"));
            }
        } else if(tab==1) {
            if(Growth.Route.values().length>3) {
                buttonList.add(new GuiButton(410,left+14,top+158,60,18,"上一页"));
                buttonList.add(new GuiButton(411,left+80,top+158,60,18,"下一页"));
            }
            buttonList.add(new GuiButton(400,left+12,top+181,panelWidth-24,20,""));
        } else if(tab==2) {
            for(int i=0;i<4 && recipePage*4+i<SpiritCrafting.count();i++)buttonList.add(new GuiButton(500+i,left+panelWidth-78,top+66+i*42,64,20,"制作"));
            if(SpiritCrafting.count()>4) {
                buttonList.add(new GuiButton(510,left+14,top+218,60,18,"上一页"));
                buttonList.add(new GuiButton(511,left+80,top+218,60,18,"下一页"));
            }
        }
        else {
            loadNotes();
            buttonList.add(new GuiButton(600,left+12,top+231,panelWidth-24,20,"阅读皇家烈焰研究手稿"));
            buttonList.add(new GuiButton(601,left+12,top+204,80,20,"上一页"));
            buttonList.add(new GuiButton(602,left+panelWidth-92,top+204,80,20,"下一页"));
        }
    }
    @Override public void drawScreen(int mx,int my,float partial) {
        try(RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.scale(viewScale,viewScale,1);
            drawPanel(Math.round(mx/viewScale),Math.round(my/viewScale),partial);
        }
    }
    private void drawPanel(int mx,int my,float partial) {
        drawDefaultBackground();PlayerData d=ClientProxy.state;
        UiDraw.panel(left,top,panelWidth,270);
        UiDraw.inset(left+8,top+60,panelWidth-16,160);
        drawRect(left+9,top+61,left+panelWidth-9,top+219,0xffb1b1b1);
        fontRenderer.drawString(tab==3?I18n.format("item.lostfantasy.fantasy_guide.name"):"幻想符卡",left+12,top+13,0x404040);
        String resources=String.format(Locale.ROOT,"灵力 %.1f/%d  ·  P %d/5",d.spirit(),d.capacity(),d.power());
        fontRenderer.drawString(resources,left+panelWidth-fontRenderer.getStringWidth(resources)-35,top+16,0x404040);
        for(GuiButton b:buttonList) {
            if(b.id>=10&&b.id<14)b.enabled=b.id!=10+tab;
            if(b.id>=100&&b.id<100+Rules.LOADOUT_SIZE) {
                int i=b.id-100;Spell s=Spell.byId(d.spellInSlot(i));
                String prefix=(slot==i?"> ":"")+(i+1)+(d.selectedSlot()==i?" * ":" ");
                b.displayString=fitButton(prefix+(s==null?"未准备":I18n.format(s.translationKey())),b.width);
            }
            if(b.id>=200&&b.id<200+SpellPage.ROWS) {
                Spell s=Spell.byId(spells.spellIdAt(b.id-200));
                if(s!=null)b.displayString=fitButton((d.knows(s)?"准备 · ":"未学 · ")+I18n.format(s.translationKey()),b.width);
            }
            if(b.id==PREVIOUS_SPELL_PAGE)b.enabled=spells.page()>0;
            if(b.id==NEXT_SPELL_PAGE)b.enabled=spells.page()+1<spells.pageCount();
            if(b.id==400)b.displayString="贤者反击："+(d.sageRetaliation?"开启":"关闭")+"（需要隙间秘钥）";
            if(b.id==601)b.enabled=guidePage>0;
            if(b.id==602)b.enabled=guidePage+1<notePageCount();
        }
        if(tab==0) {
            if(spells.pageCount()>1)drawCenteredString(fontRenderer,(spells.page()+1)+" / "+spells.pageCount(),left+(panelWidth+142)/2,top+202,0x404040);
            fontRenderer.drawString("当前终符："+d.preparedUltimateCount()+" / 1  ·  准备上限："+Rules.LOADOUT_SIZE,left+12,top+222,0x404040);
            drawWrapped(I18n.format(viewedSpell.translationKey()+".desc"),left+12,top+234,panelWidth-24,0x404040,3);
        } else if(tab==1) {
            Growth.Route[] routes=Growth.Route.values();
            for(int row=0;row<3 && routePage*3+row<routes.length;row++) {
                Growth.Route route=routes[routePage*3+row];
                String line=(route.kind==Growth.Kind.PROFESSION?"职业":"种族")+" · "+route.label+"："+route.stageName(d.growth.stage(route));
                if(!route.progressLabel.isEmpty())line+="   "+route.progressLabel+"："+d.growth.progress(route);
                fontRenderer.drawString(line,left+14,top+69+row*20,0x404040);
            }
            fontRenderer.drawString(d.growth.reincarnated()?"已转生 · 可修习多条路线":"尚未转生 · 职业与种族合计限一条",left+14,top+129,0x404040);
            fontRenderer.drawString(String.format(Locale.ROOT,"结界耐久 %.1f / %.1f  ·  重构 %.1fs",d.shield,d.shieldMaximum(),d.shieldBrokenTicks/20f),left+14,top+149,0x404040);
        } else if(tab==2) {
            for(int row=0;row<4 && recipePage*4+row<SpiritCrafting.count();row++) {
                int id=recipePage*4+row;
                StringBuilder materials=new StringBuilder();
                for(ItemStack ingredient:SpiritCrafting.ingredients(id)) {
                    if(materials.length()>0)materials.append("，");
                    materials.append(ingredient.getDisplayName()).append("×").append(ingredient.getCount());
                }
                String name=new ItemStack(SpiritCrafting.result(id)).getDisplayName();
                String text=String.format(Locale.ROOT,"%s · %s格灵力\n%s",name,formatCost(SpiritCrafting.spiritCost(id)),materials);
                drawWrapped(text,left+14,top+68+row*42,panelWidth-100,0x404040,2);
            }
            drawWrapped("材料与灵力充足时即可制作。背包已满，成品会掉落在脚下。",left+14,top+242,panelWidth-28,0x404040,2);
        } else {
            int firstLine = guidePage * NOTE_ROWS;
            for (int row = 0; row < NOTE_ROWS && firstLine + row < noteLines.size(); row++) {
                fontRenderer.drawString(noteLines.get(firstLine + row),left+14,top+67+row*11,0x404040);
            }
            drawCenteredString(fontRenderer,(guidePage+1)+" / "+notePageCount(),left+panelWidth/2,top+210,0x404040);
        }
        super.drawScreen(mx,my,partial);
    }
    private void drawWrapped(String text,int x,int y,int width,int color,int maximumLines) {int n=0;for(String line:fontRenderer.listFormattedStringToWidth(text,width)){if(n>=maximumLines)break;fontRenderer.drawString(line,x,y+n++*11,color);}}

    private static String formatCost(float value) {return value==(int)value?Integer.toString((int)value):Float.toString(value);}

    private String fitButton(String text, int buttonWidth) {
        int width = buttonWidth - 10;
        return fontRenderer.getStringWidth(text) <= width ? text
                : fontRenderer.trimStringToWidth(text, width - fontRenderer.getStringWidth("...")) + "...";
    }

    private void loadNotes() {
        noteLines.clear();
        ResourceLocation resource = new ResourceLocation("lostfantasy:texts/patchouli_notes_zh_cn.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                mc.getResourceManager().getResource(resource).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) noteLines.add("");
                else noteLines.addAll(fontRenderer.listFormattedStringToWidth(line, panelWidth - 28));
            }
        } catch (IOException e) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("Cannot read text resource "+resource,e);
            noteLines.clear();
            noteLines.add("笔记资源读取失败。");
        }
        guidePage = Math.max(0, Math.min(guidePage, notePageCount() - 1));
    }

    private int notePageCount() {
        return Math.max(1, (noteLines.size() + NOTE_ROWS - 1) / NOTE_ROWS);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.id==410 || button.id==411 || button.id==510 || button.id==511) {
            if(button.id<500)routePage=Math.max(0,Math.min((Growth.Route.values().length-1)/3,routePage+(button.id==410?-1:1)));
            else recipePage=Math.max(0,Math.min((SpiritCrafting.count()-1)/4,recipePage+(button.id==510?-1:1)));
            initGui();return;
        }
        if (button.id == PREVIOUS_SPELL_PAGE || button.id == NEXT_SPELL_PAGE) {
            spells.move(button.id == PREVIOUS_SPELL_PAGE ? -1 : 1);
            Spell first = Spell.byId(spells.spellIdAt(0));
            if (first != null) viewedSpell = first;
            initGui();
            return;
        }
        if (button.id == 601) {
            guidePage = Math.max(0, guidePage - 1);
            return;
        }
        if (button.id == 602) {
            guidePage = Math.min(notePageCount() - 1, guidePage + 1);
            return;
        }
        if (button.id >= 10 && button.id < 14) {
            tab = button.id - 10;
            initGui();
        } else if (button.id == 99) {
            mc.displayGuiScreen(null);
        } else if (button.id >= 100 && button.id < 100 + Rules.LOADOUT_SIZE) {
            slot = button.id - 100;
        } else if (button.id >= 200 && button.id < 200 + SpellPage.ROWS) {
            Spell spell = Spell.byId(spells.spellIdAt(button.id - 200));
            if (spell == null) return;
            viewedSpell = spell;
            if (ClientProxy.state.knows(spell)) {
                Network.action(ActionMessage.EQUIP_SPELL, slot, spell.networkId);
            }
        } else if (button.id == 300) {
            Network.action(ActionMessage.EQUIP_SPELL, slot, -1);
        } else if (button.id == 301) {
            Network.action(ActionMessage.SELECT_SLOT, slot, 0);
        } else if (button.id == 400) {
            Network.action(ActionMessage.TOGGLE_RETALIATION, 0, 0);
        } else if (button.id >= 500 && button.id < 504) {
            Network.action(ActionMessage.CRAFT_ITEM, recipePage*4+button.id - 500, 0);
        } else if (button.id == 600) {
            LostFantasy.PROXY.openBook();
        }
    }
}
