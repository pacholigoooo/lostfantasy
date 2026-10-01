package dev.lostfantasy.client;

import dev.lostfantasy.Balance;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

/** Optional OptiFine bridge. The server never loads this client class. */
final class HiganShaderCompatibility {
    private static final BooleanSupplier OPTIFINE=probe(HiganShaderCompatibility.class.getClassLoader());
    private HiganShaderCompatibility() {}
    static boolean externalAtmosphere() {return Balance.higanShaderCompatibility || OPTIFINE.getAsBoolean();}
    static BooleanSupplier probe(ClassLoader loader) {
        try {return shaderState(Class.forName("Config",false,loader));}
        catch(ClassNotFoundException absent) {return ()->false;}
        catch(LinkageError ex) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn(
                    "Cannot load OptiFine Config; yielding local atmosphere to external rendering",ex);
            return ()->true;
        }
    }
    static BooleanSupplier shaderState(Class<?> config) {
        try {
            Method enabled=config.getMethod("isShaders");
            return new BooleanSupplier() {
                private boolean failed,reported;
                private long retryAt;
                @Override public boolean getAsBoolean() {
                    if(failed && System.nanoTime()-retryAt<0)return true;
                    try {
                        boolean active=Boolean.TRUE.equals(enabled.invoke(null));
                        failed=false;return active;
                    } catch(ReflectiveOperationException | LinkageError ex) {
                        failed=true;retryAt=System.nanoTime()+1_000_000_000L;
                        if(!reported) {
                            reported=true;
                            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn(
                                    "Cannot query "+config.getName()+".isShaders; yielding local atmosphere and retrying once per second",ex);
                        }
                        return true;
                    }
                }
            };
        } catch(ReflectiveOperationException | LinkageError ex) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn(
                    "Cannot access "+config.getName()+".isShaders; yielding local atmosphere to external rendering",ex);
            return ()->true;
        }
    }
}
