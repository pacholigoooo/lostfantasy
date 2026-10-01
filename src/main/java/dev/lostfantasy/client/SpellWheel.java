package dev.lostfantasy.client;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.network.ActionMessage;
import dev.lostfantasy.network.Network;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.io.IOException;
public final class SpellWheel extends GuiScreen {
    private int hovered;private boolean selected;
    @Override public void initGui() {hovered=ClientProxy.state.selectedSlot();}
    @Override public boolean doesGuiPauseGame() {return false;}
    @Override public void drawScreen(int mx,int my,float partial) {
        int cx=width/2,cy=height/2;
        if((mx-cx)*(mx-cx)+(my-cy)*(my-cy)>33*33) {
            double angle=Math.atan2(my-cy,mx-cx)+Math.PI/4;if(angle<0)angle+=Math.PI*2;hovered=((int)(angle/(Math.PI/2)))%4;
        }
        UiDraw.spellWheel(cx,cy,hovered);
        for(int i=0;i<4;i++) {
            Spell s=Spell.byId(ClientProxy.state.spellInSlot(i));double a=i*Math.PI/2;
            String label=s==null?I18n.format("ui.lostfantasy.empty"):I18n.format(s.translationKey());
            int tx=cx+(int)(Math.cos(a)*70),ty=cy+(int)(Math.sin(a)*70);
            drawCenteredString(fontRenderer,Integer.toString(i+1),tx,ty-14,0xe7cf90);
            java.util.List<String> lines=fontRenderer.listFormattedStringToWidth(label,76);int n=0;for(String line:lines)drawCenteredString(fontRenderer,line,tx,ty+n++*11,0xf4eafa);
        }
        drawCenteredString(fontRenderer,"P "+ClientProxy.state.power()+" / 5",cx,cy-4,0xffa9cf);
        drawCenteredString(fontRenderer,I18n.format("ui.lostfantasy.wheel_hint"),cx,Math.min(height-12,cy+123),0xe9dff6);
        super.drawScreen(mx,my,partial);
    }
    @Override public void updateScreen() {
        int key=ClientEvents.SELECT.getKeyCode();
        boolean held=key>0&&key<Keyboard.KEYBOARD_SIZE?Keyboard.isKeyDown(key):key<0&&key+100>=0&&key+100<Mouse.getButtonCount()&&Mouse.isButtonDown(key+100);
        if(!held)choose();
    }
    private void choose() {
        if (!selected) {
            selected = true;
            Network.action(ActionMessage.SELECT_SLOT, hovered, 0);
        }
        mc.displayGuiScreen(null);
    }
    @Override protected void mouseClicked(int x,int y,int button) throws IOException {if(button==0)choose();else super.mouseClicked(x,y,button);}
    @Override protected void keyTyped(char c,int key) throws IOException {if(key==Keyboard.KEY_ESCAPE) {selected=true;mc.displayGuiScreen(null);}else super.keyTyped(c,key);}
}
