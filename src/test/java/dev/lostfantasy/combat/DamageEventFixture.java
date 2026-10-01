package dev.lostfantasy.combat;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.ListenerList;

/** Real Forge event dispatch with the cancellable/listener-list overrides normally supplied by its class transformer. */
final class DamageEventFixture {
    private final EventBus bus=new EventBus();
    private final ListenerList hurtListeners=new ListenerList(),damageListeners=new ListenerList();
    Consumer<LivingHurtEvent> hurt=event->{};
    Consumer<LivingDamageEvent> damage=event->{};
    float beforeLateDamage;
    DamageEventFixture() {
        try {
            Field field=EventBus.class.getDeclaredField("busID");field.setAccessible(true);int id=field.getInt(bus);
            hurtListeners.register(id,EventPriority.LOWEST,event->hurt.accept((LivingHurtEvent)event));
            damageListeners.register(id,EventPriority.HIGHEST,event->beforeLateDamage=((LivingDamageEvent)event).getAmount());
            damageListeners.register(id,EventPriority.LOWEST,event->damage.accept((LivingDamageEvent)event));
        } catch(ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    float apply(EntityLivingBase victim,DamageSource source,float amount) {
        DamageTransactions.Scope scope=DamageTransactions.enter(victim,source);
        try {
            LivingHurtEvent hurtEvent=new LivingHurtEvent(victim,source,amount) {
                @Override public boolean isCancelable() { return true; }
                @Override public ListenerList getListenerList() { return hurtListeners; }
            };
            float afterHurt=DamageTransactions.capture(bus.post(hurtEvent)?0:hurtEvent.getAmount(),victim,source);
            if(afterHurt<=0)return 0;
            return DamageTransactions.finish(victim,source,afterHurt,(target,damageSource,value)->{
                LivingDamageEvent damageEvent=new LivingDamageEvent(target,damageSource,value) {
                    @Override public boolean isCancelable() { return true; }
                    @Override public ListenerList getListenerList() { return damageListeners; }
                };
                DamageTransactions.bind(damageEvent);
                return bus.post(damageEvent)?0:damageEvent.getAmount();
            });
        } finally { DamageTransactions.exit(scope); }
    }
}
