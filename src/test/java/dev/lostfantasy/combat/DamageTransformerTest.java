package dev.lostfantasy.combat;

import dev.lostfantasy.asm.DamageTransformer;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class DamageTransformerTest implements Opcodes {
    private static final String ENTITY = "net/minecraft/entity/EntityLivingBase";
    private static final String SOURCE = "net/minecraft/util/DamageSource";
    private static final String HOOK = "(L" + ENTITY + ";L" + SOURCE + ";F)F";

    @Test public void onlyTheFinalDamageCallIsRedirectedInBothMappings() {
        for (String method : new String[]{"damageEntity", "func_70665_d"}) {
            byte[] transformed = new DamageTransformer().transform(ENTITY.replace('/', '.'), ENTITY.replace('/', '.'), fixture(method, 1));
            ClassNode type = new ClassNode(ASM5);
            new ClassReader(transformed).accept(type, 0);
            int finalCalls = 0, hurtCalls = 0;
            for (MethodNode body : type.methods) for (AbstractInsnNode instruction : body.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.owner.equals("dev/lostfantasy/combat/DamageTransactions") && call.name.equals("finish")) {
                    assertEquals(HOOK, call.desc);
                    finalCalls++;
                }
                if (call.owner.equals("net/minecraftforge/common/ForgeHooks")) {
                    assertEquals("onLivingHurt", call.name);
                    hurtCalls++;
                }
            }
            assertEquals(1, finalCalls);
            assertEquals(1, hurtCalls);
        }
    }

    @Test public void missingOrAmbiguousFinalCallFailsInsteadOfLeavingAnUnrestrictedBinding() {
        for (int calls : new int[]{0, 2}) {
            try {
                new DamageTransformer().transform(ENTITY.replace('/', '.'), ENTITY.replace('/', '.'), fixture("damageEntity", calls));
                fail("Expected incompatible final hook count " + calls);
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage().contains("onLivingDamage"));
            }
        }
    }

    private byte[] fixture(String method, int finalCalls) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC, ENTITY, null, "java/lang/Object", null);
        org.objectweb.asm.MethodVisitor code = writer.visitMethod(ACC_PROTECTED, method, "(L" + SOURCE + ";F)V", null, null);
        code.visitCode();
        for (int index = 0; index <= finalCalls; index++) {
            code.visitVarInsn(ALOAD, 0);
            code.visitVarInsn(ALOAD, 1);
            code.visitVarInsn(FLOAD, 2);
            code.visitMethodInsn(INVOKESTATIC, "net/minecraftforge/common/ForgeHooks", index == 0 ? "onLivingHurt" : "onLivingDamage", HOOK, false);
            code.visitVarInsn(FSTORE, 2);
        }
        code.visitInsn(RETURN);
        code.visitMaxs(0, 0);
        code.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }
}
