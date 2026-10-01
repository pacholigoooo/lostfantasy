package dev.lostfantasy.client;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.network.ActionMessage;
import dev.lostfantasy.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public final class ClientEvents {
    private boolean inWorld;
    @SubscribeEvent(priority=net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST)
    public void fieldOfView(net.minecraftforge.client.event.FOVUpdateEvent event) {
        event.setNewfov(event.getNewfov()*dev.lostfantasy.combat.BarrierCasting.fieldOfViewCorrection(event.getEntity()));
    }
    @SubscribeEvent public void movement(net.minecraftforge.client.event.InputUpdateEvent event) {
        if (!dev.lostfantasy.combat.BarrierCasting.restricted(event.getEntityPlayer())) return;
        net.minecraft.util.MovementInput input = event.getMovementInput();
        input.moveForward = input.moveStrafe = 0;
        input.jump = input.sneak = input.forwardKeyDown = input.backKeyDown = input.leftKeyDown = input.rightKeyDown = false;
        event.getEntityPlayer().setSprinting(false);
        Minecraft mc = Minecraft.getMinecraft();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }
    public static final KeyBinding SELECT=new KeyBinding("key.lostfantasy.select",Keyboard.KEY_Z,"key.categories.lostfantasy");
    public static final KeyBinding CAST=new KeyBinding("key.lostfantasy.cast",Keyboard.KEY_C,"key.categories.lostfantasy");
    public static void registerKeys() {ClientRegistry.registerKeyBinding(SELECT);ClientRegistry.registerKeyBinding(CAST);}
    @SubscribeEvent public void input(InputEvent.KeyInputEvent e) {handleInput();}
    @SubscribeEvent public void mouseInput(InputEvent.MouseInputEvent e) {handleInput();}
    private void handleInput() {
        Minecraft mc=Minecraft.getMinecraft();if(mc.player==null || mc.currentScreen!=null)return;
        if (SELECT.isPressed()) {
            Network.action(ActionMessage.SYNC_STATE, 0, 0);
            mc.displayGuiScreen(new SpellWheel());
        }
        if (CAST.isPressed()) Network.action(ActionMessage.CAST_SPELL, 0, 0);
    }
    @SubscribeEvent public void initGui(GuiScreenEvent.InitGuiEvent.Post e) {
        if(e.getGui() instanceof GuiInventory)e.getButtonList().add(new GuiButton(7301,e.getGui().width/2+5,Math.max(1,e.getGui().height/2-111),78,20,I18n.format("ui.lostfantasy.title")));
    }
    @SubscribeEvent
    public void clickGui(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getGui() instanceof GuiInventory && event.getButton().id == 7301) {
            Network.action(ActionMessage.SYNC_STATE, 0, 0);
            Minecraft.getMinecraft().displayGuiScreen(new FantasyScreen(0));
        }
    }
    @SubscribeEvent public void hud(RenderGameOverlayEvent.Post e) {
        Minecraft mc=Minecraft.getMinecraft();if(e.getType()!=RenderGameOverlayEvent.ElementType.ALL||mc.player==null||mc.player.isSpectator()||mc.gameSettings.hideGUI)return;
        int w=e.getResolution().getScaledWidth(),h=e.getResolution().getScaledHeight();
        UiDraw.spiritBar(w/2.0+9,h-53,ClientProxy.state.spirit(),ClientProxy.state.capacity());
        Spell spell=ClientProxy.state.selectedSpell();String name=spell==null?I18n.format("ui.lostfantasy.empty"):I18n.format(spell.translationKey());
        if(!mc.gameSettings.showDebugInfo) {
            mc.fontRenderer.drawStringWithShadow("P "+ClientProxy.state.power()+"/5",8,8,0xffff99b7);
            mc.fontRenderer.drawStringWithShadow(name,8,21,0xffeee2ff);
            int cooldown=ClientProxy.state.cooldownFor(spell);
            if(cooldown>0)mc.fontRenderer.drawStringWithShadow(String.format(java.util.Locale.ROOT,"冷却 %.1fs",cooldown/20f),8,34,0xffffcc88);
            dev.lostfantasy.network.EffectMessage effect = SpellVisuals.poseEffect(mc.player);
            if (effect != null && effect.spell() == Spell.FOURFOLD_BARRIER) {
                long age=mc.world.getTotalWorldTime()-effect.started;
                String phase=age<dev.lostfantasy.core.FourfoldBarrier.PROTECTION_END?"撑界 · 护身中":age<dev.lostfantasy.core.FourfoldBarrier.FADE_END?"撑界 · 可受击":"收招 · 可受击";
                mc.fontRenderer.drawStringWithShadow(phase,8,34,age<dev.lostfantasy.core.FourfoldBarrier.PROTECTION_END?0xffdabfff:0xffffaa88);
            }
            if (effect != null && effect.spell() == Spell.EMERALD_CITY)
                mc.fontRenderer.drawStringWithShadow("起手 · 无护身",8,34,0xffffaa88);
        }
    }
    @SubscribeEvent public void render(RenderWorldLastEvent e) {SpellVisuals.render(e.getPartialTicks());ResearchVisual.render(e.getPartialTicks());HiganAtmosphere.render(e.getPartialTicks());}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
        if(e.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getMinecraft();
        ResearchVisual.tick();
        FortuneVisual.tick();
        if(mc.world==null) {
            if(inWorld) {SpellVisuals.clear();ClientProxy.state=new dev.lostfantasy.data.PlayerData();inWorld=false;}
            return;
        }
        inWorld=true;
        if(mc.isGamePaused())return;
        GapAtmosphere.tick(mc);SpellVisuals.tickParticles();WaterfallAtmosphere.tick(mc);GenunkaiClouds.tick(mc);
    }
    @SubscribeEvent public void unload(net.minecraftforge.event.world.WorldEvent.Unload e) {
        if(e.getWorld().isRemote) {SpellVisuals.clear();RibbonItemRenderer.beginFrame();GenunkaiClouds.clear();}
    }
    @SubscribeEvent public void pose(RenderPlayerEvent.Pre e) {SpellVisuals.beforePlayer(e);RibbonItemRenderer.beforePlayer(e);}
    @SubscribeEvent public void poseEnd(RenderPlayerEvent.Post e) {SpellVisuals.afterPlayer(e);}
    @SubscribeEvent public void frameEnd(TickEvent.RenderTickEvent e) {if(e.phase==TickEvent.Phase.END)SpellVisuals.restorePlayers();else RibbonItemRenderer.beginFrame();}
}
