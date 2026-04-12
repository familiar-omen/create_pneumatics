package com.familiar_omen.create_pneumatics.compat.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.equipment.armor.BacktankBlock;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankRenderer;

import net.minecraft.core.Direction;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import com.familiar_omen.create_pneumatics.CreatePneumatics;

public class BacktankCurioRenderer implements ICurioRenderer {
  
  @Override
  public <T extends LivingEntity, M extends EntityModel<T>> void render(
    ItemStack stack,
    SlotContext slotContext,
    PoseStack matrixStack,
    RenderLayerParent<T, M> renderLayerParent,
    MultiBufferSource renderTypeBuffer,
    int light, float limbSwing,
    float limbSwingAmount,
    float partialTicks,
    float ageInTicks,
    float netHeadYaw,
    float headPitch
  ){
    // Render code goes here
    // if (entity.getPose() == Pose.SLEEPING)
		// 	return;

		BacktankItem item = (BacktankItem)stack.getItem();
		if (item == null)
			return;

		M entityModel = renderLayerParent.getModel();
    
		if (!(entityModel instanceof HumanoidModel<?> model))
			return;

		boolean hasGlint = stack.hasFoil();
		VertexConsumer vc = ItemRenderer.getFoilBuffer(renderTypeBuffer, Sheets.cutoutBlockSheet(), false, true);
		BlockState renderedState = item.getBlock().defaultBlockState()
			.setValue(BacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
		SuperByteBuffer backtank = CachedBuffers.block(renderedState);
		SuperByteBuffer cogs = CachedBuffers.partial(BacktankRenderer.getCogsModel(renderedState), renderedState);
		SuperByteBuffer nob = CachedBuffers.partial(BacktankRenderer.getShaftModel(renderedState), renderedState);

		matrixStack.pushPose();

		model.body.translateAndRotate(matrixStack);
		matrixStack.translate(-1 / 2f, 10 / 16f, 1f);
		matrixStack.scale(1, -1, -1);

		backtank.disableDiffuse()
			.light(light)
			.renderInto(matrixStack, vc);

		nob.disableDiffuse()
			.translate(0, -3f / 16, 0)
			.light(light)
			.renderInto(matrixStack, vc);

		cogs.center()
			.rotateYDegrees(180)
			.uncenter()
			.translate(0, 6.5f / 16, 11f / 16)
			.rotate(AngleHelper.rad(2 * ageInTicks % 360), Direction.EAST)
			.translate(0, -6.5f / 16, -11f / 16);

		cogs.disableDiffuse()
			.light(light)
			.renderInto(matrixStack, vc);

		matrixStack.popPose();
  
  }
}