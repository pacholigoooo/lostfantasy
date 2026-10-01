package dev.lostfantasy.client;

import dev.lostfantasy.entity.EntityKomachi;
import dev.lostfantasy.entity.EntityRiverFerry;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;

/** Vanilla Alex geometry and skin layers, with a separate held tool. */
public final class RenderKomachi extends RenderLiving<EntityKomachi> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("lostfantasy:textures/entity/komachi.png");
    private static final ResourceLocation BLINK = new ResourceLocation("lostfantasy:textures/entity/komachi_blink.png");
    private static final ResourceLocation TOOLS = new ResourceLocation("lostfantasy:textures/entity/komachi_tools.png");

    public RenderKomachi(RenderManager manager) {
        super(manager, new Alex(), .32f);
        addLayer(new HeldTool());
    }

    @Override protected ResourceLocation getEntityTexture(EntityKomachi e) {
        return (e.ticksExisted + Math.abs(e.getEntityId() % 31)) % 103 < 3 ? BLINK : TEXTURE;
    }

    @Override protected void renderModel(EntityKomachi e, float swing, float amount, float age,
                                         float yaw, float pitch, float scale) {
        try (RenderState ignored = new RenderState()) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.alphaFunc(516, .001f);
            super.renderModel(e, swing, amount, age, yaw, pitch, scale);
        }
    }

    @Override protected boolean canRenderName(EntityKomachi e) {
        return renderManager.pointedEntity == e && e.getDistanceSq(renderManager.renderViewEntity) < 36;
    }

    private final class HeldTool implements LayerRenderer<EntityKomachi> {
        @Override public void doRenderLayer(EntityKomachi e, float swing, float amount, float partial,
                                            float age, float yaw, float pitch, float scale) {
            if (e.isInvisible()) return;
            boolean rowing = e.getRidingEntity() instanceof EntityRiverFerry
                && ((EntityRiverFerry)e.getRidingEntity()).sailing();
            Alex model = (Alex)mainModel;
            try (RenderState ignored = new RenderState()) {
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                try(RenderMatrix matrix=new RenderMatrix()) {
                    if (rowing) {
                        KomachiRowingPose pose = model.rowingPose;
                        GlStateManager.translate(pose.upper.x * scale, pose.upper.y * scale, pose.upper.z * scale);
                        GlStateManager.rotate((float)Math.toDegrees(Math.atan2(pose.direction.x, -pose.direction.y)), 0, 0, 1);
                        GlStateManager.rotate((float)Math.toDegrees(Math.asin(-pose.direction.z)), 1, 0, 0);
                        GlStateManager.rotate(pose.feather, 0, 1, 0);
                        GlStateManager.scale(KomachiRowingPose.OAR_SCALE, KomachiRowingPose.OAR_SCALE, KomachiRowingPose.OAR_SCALE);
                        // Existing mesh: shaft center X/Z, upper grip near the top of its local Y axis.
                        GlStateManager.translate(-.3121875, -1.5, -.12375);
                    } else {
                        model.postRenderArm(scale, EnumHandSide.RIGHT);
                        GlStateManager.translate(-.0625, .60, 0);
                        GlStateManager.scale(1, -1, -1);
                        GlStateManager.translate(-5.55 * .05625, -13 * .05625, -2.2 * .05625);
                    }
                    bindTexture(TOOLS);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    KomachiMesh.INSTANCE.draw(rowing ? "oar" : "scythe", 1);
                }

            }
        }

        @Override public boolean shouldCombineTextures() {return false;}
    }

    private static final class Alex extends ModelPlayer {
        private KomachiRowingPose rowingPose;
        Alex() {super(0, true);}

        @Override public void setRotationAngles(float swing, float amount, float age, float yaw,
                                                float pitch, float scale, Entity entity) {
            // The ferryman stands on the deck while the boat carries her as a passenger.
            isRiding = false;
            super.setRotationAngles(swing, amount, age, yaw, pitch, scale, entity);
            EntityKomachi e = (EntityKomachi)entity;
            boolean rowing = e.getRidingEntity() instanceof EntityRiverFerry
                && ((EntityRiverFerry)e.getRidingEntity()).sailing();
            if (e.getRidingEntity() instanceof EntityRiverFerry) {
                // Riding world motion must not turn into walking feet on the deck.
                bipedRightLeg.rotateAngleX = bipedRightLeg.rotateAngleY = bipedRightLeg.rotateAngleZ = 0;
                bipedLeftLeg.rotateAngleX = bipedLeftLeg.rotateAngleY = bipedLeftLeg.rotateAngleZ = 0;
                copyModelAngles(bipedRightLeg, bipedRightLegwear);
                copyModelAngles(bipedLeftLeg, bipedLeftLegwear);
            }
            if (rowing) {
                rowingPose = KomachiRowingPose.at(age);
                rowingPose.apply(bipedLeftArm, bipedRightArm);
            } else {
                rowingPose = null;
                bipedRightArm.rotateAngleX = -.08f;
                bipedRightArm.rotateAngleY = 0;
                bipedRightArm.rotateAngleZ = .025f;
            }
            copyModelAngles(bipedRightArm, bipedRightArmwear);
            copyModelAngles(bipedLeftArm, bipedLeftArmwear);
        }
    }
}
