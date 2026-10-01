package dev.lostfantasy.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Adds a finally-scoped record without changing Forge's event or armour implementations. */
public final class DamageTransformer implements IClassTransformer, Opcodes {
    private static final String CONTEXT = "dev/lostfantasy/combat/DamageTransactions";
    private static final String SCOPE = CONTEXT + "$Scope";
    private static final String ENTITY = "net/minecraft/entity/EntityLivingBase";
    private static final String SOURCE = "net/minecraft/util/DamageSource";
    private static final String FORGE = "net/minecraftforge/common/ForgeHooks";
    private static final String DAMAGE_DESC = "(L" + SOURCE + ";F)V";
    private static final String HOOK_DESC = "(L" + ENTITY + ";L" + SOURCE + ";F)F";
    private static final String BODY = "lostfantasy$damageEntity";

    @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
        boolean entity = "net.minecraft.entity.EntityLivingBase".equals(transformedName)
                || "net.minecraft.entity.player.EntityPlayer".equals(transformedName);
        boolean forge = "net.minecraftforge.common.ForgeHooks".equals(transformedName);
        if (bytes == null || (!entity && !forge)) return bytes;
        ClassNode type = new ClassNode(ASM5);
        new ClassReader(bytes).accept(type, 0);
        if (entity) wrapDamage(type);
        else bindDamageEvent(type);
        // Preserve existing frames. Computing them here could recursively load Minecraft classes.
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        return writer.toByteArray();
    }

    private static void wrapDamage(ClassNode type) {
        MethodNode body = null;
        for (MethodNode method : type.methods) {
            if (BODY.equals(method.name)) throw incompatible(type, "damage method already wrapped");
            if (DAMAGE_DESC.equals(method.desc)
                    && ("damageEntity".equals(method.name) || "func_70665_d".equals(method.name))) body = method;
        }
        if (body == null) throw incompatible(type, "damageEntity method missing");
        int captures = 0, finalCalls = 0;
        for (AbstractInsnNode instruction : body.instructions.toArray()) {
            if (call(instruction, FORGE, "onLivingDamage", HOOK_DESC)) {
                body.instructions.set(instruction, hook("finish", HOOK_DESC));
                finalCalls++;
                continue;
            }
            if (!call(instruction, FORGE, "onLivingHurt", HOOK_DESC)) continue;
            InsnList capture = new InsnList();
            capture.add(new VarInsnNode(ALOAD, 0));
            capture.add(new VarInsnNode(ALOAD, 1));
            capture.add(hook("capture", "(FL" + ENTITY + ";L" + SOURCE + ";)F"));
            body.instructions.insert(instruction, capture);
            captures++;
        }
        if (captures != 1) throw incompatible(type, "expected one onLivingHurt call, found " + captures);
        if (finalCalls != 1) throw incompatible(type, "expected one onLivingDamage call, found " + finalCalls);
        MethodNode wrapper = new MethodNode(ASM5, body.access, body.name, body.desc,
                body.signature, body.exceptions.toArray(new String[0]));
        body.name = BODY;
        body.access = (body.access & ~(ACC_PUBLIC | ACC_PROTECTED)) | ACC_PRIVATE | ACC_SYNTHETIC;
        InsnList code = wrapper.instructions;
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(hook("enter", "(L" + ENTITY + ";L" + SOURCE + ";)L" + SCOPE + ";"));
        code.add(new VarInsnNode(ASTORE, 3));
        LabelNode start = new LabelNode(), end = new LabelNode(), failure = new LabelNode();
        code.add(start);
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new VarInsnNode(FLOAD, 2));
        code.add(new MethodInsnNode(INVOKESPECIAL, type.name, BODY, DAMAGE_DESC, false));
        code.add(end);
        code.add(new VarInsnNode(ALOAD, 3));
        code.add(hook("exit", "(L" + SCOPE + ";)V"));
        code.add(new InsnNode(RETURN));
        code.add(failure);
        code.add(new FrameNode(F_FULL, 4, new Object[]{type.name, SOURCE, FLOAT, SCOPE},
                1, new Object[]{"java/lang/Throwable"}));
        code.add(new VarInsnNode(ASTORE, 4));
        code.add(new VarInsnNode(ALOAD, 3));
        code.add(hook("exit", "(L" + SCOPE + ";)V"));
        code.add(new VarInsnNode(ALOAD, 4));
        code.add(new InsnNode(ATHROW));
        wrapper.tryCatchBlocks.add(new TryCatchBlockNode(start, end, failure, null));
        type.methods.add(wrapper);
    }

    private static void bindDamageEvent(ClassNode type) {
        int bindings = 0;
        for (MethodNode method : type.methods) {
            if (!"onLivingDamage".equals(method.name) || !HOOK_DESC.equals(method.desc)) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!call(instruction, "net/minecraftforge/fml/common/eventhandler/EventBus", "post",
                        "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z")) continue;
                InsnList bind = new InsnList();
                bind.add(new InsnNode(DUP));
                bind.add(hook("bind", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)V"));
                method.instructions.insertBefore(instruction, bind);
                bindings++;
            }
        }
        if (bindings != 1) throw incompatible(type, "expected one LivingDamageEvent post, found " + bindings);
    }

    private static boolean call(AbstractInsnNode instruction, String owner, String name, String descriptor) {
        if (!(instruction instanceof MethodInsnNode)) return false;
        MethodInsnNode method = (MethodInsnNode) instruction;
        return owner.equals(method.owner) && name.equals(method.name) && descriptor.equals(method.desc);
    }

    private static MethodInsnNode hook(String name, String descriptor) {
        return new MethodInsnNode(INVOKESTATIC, CONTEXT, name, descriptor, false);
    }

    private static IllegalStateException incompatible(ClassNode type, String detail) {
        return new IllegalStateException("Lost Fantasy damage integration cannot instrument " + type.name + ": " + detail);
    }
}
