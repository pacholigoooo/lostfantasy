package dev.lostfantasy.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Guard vanilla teleport entry points, including /tp and direct PlayerList dimension transfers. */
public final class HiganTransformer implements IClassTransformer,Opcodes {
    private static final String HOOK="dev/lostfantasy/world/HiganRestrictions";
    @Override public byte[] transform(String name,String transformedName,byte[] bytes) {
        if(bytes==null)return null;
        boolean entity=transformedName.equals("net.minecraft.entity.Entity"),living=transformedName.equals("net.minecraft.entity.EntityLivingBase"),
            player=transformedName.equals("net.minecraft.entity.player.EntityPlayer"),mp=transformedName.equals("net.minecraft.entity.player.EntityPlayerMP"),
            net=transformedName.equals("net.minecraft.network.NetHandlerPlayServer"),list=transformedName.equals("net.minecraft.server.management.PlayerList");
        if(!entity&&!living&&!player&&!mp&&!net&&!list)return bytes;
        ClassNode c=new ClassNode(ASM5);new ClassReader(bytes).accept(c,0);boolean changed=false;
        for(MethodNode m:c.methods) {
            InsnList call=new InsnList();int result=RETURN;
            if((living||player) && named(m,"travel","func_191986_a") && m.desc.equals("(FFF)V")) {
                call.add(new VarInsnNode(ALOAD,0));call.add(hook("ground","(Lnet/minecraft/entity/EntityLivingBase;)V"));
                m.instructions.insert(call);changed=true;continue;
            } else if(living && (named(m,"isElytraFlying","func_184613_cA") || named(m,"attemptTeleport","func_184595_k"))) {
                call.add(new VarInsnNode(ALOAD,0));call.add(hook("blocksTeleport","(Lnet/minecraft/entity/Entity;)Z"));result=IRETURN;
            } else if((entity||mp) && named(m,"setPositionAndUpdate","func_70634_a") && m.desc.equals("(DDD)V")) {
                call.add(new VarInsnNode(ALOAD,0));call.add(hook("blocksTeleport","(Lnet/minecraft/entity/Entity;)Z"));
            } else if(net && named(m,"setPlayerLocation","func_175089_a") && m.desc.equals("(DDDFFLjava/util/Set;)V")) {
                call.add(new VarInsnNode(ALOAD,0));call.add(new VarInsnNode(DLOAD,1));call.add(new VarInsnNode(DLOAD,3));call.add(new VarInsnNode(DLOAD,5));
                call.add(hook("blocksLocation","(Lnet/minecraft/network/NetHandlerPlayServer;DDD)Z"));
            } else if(list && (named(m,"transferPlayerToDimension","func_72356_a")) && m.desc.startsWith("(Lnet/minecraft/entity/player/EntityPlayerMP;I")) {
                call.add(new VarInsnNode(ALOAD,1));call.add(new VarInsnNode(ILOAD,2));call.add(hook("blocksTransfer","(Lnet/minecraft/entity/Entity;I)Z"));
            } else continue;
            LabelNode next=new LabelNode();call.add(new JumpInsnNode(IFEQ,next));
            if(result==IRETURN)call.add(new InsnNode(ICONST_0));
            call.add(new InsnNode(result));call.add(next);call.add(new FrameNode(F_SAME,0,null,0,null));
            m.instructions.insert(call);changed=true;
        }
        if(!changed)return bytes;
        ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);return w.toByteArray();
    }
    private static boolean named(MethodNode m,String mapped,String srg) {return m.name.equals(mapped)||m.name.equals(srg);}
    private static MethodInsnNode hook(String n,String d) {return new MethodInsnNode(INVOKESTATIC,HOOK,n,d,false);}
}
