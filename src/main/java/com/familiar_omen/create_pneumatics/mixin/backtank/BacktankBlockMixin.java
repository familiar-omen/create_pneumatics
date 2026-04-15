package com.familiar_omen.create_pneumatics.mixin.backtank;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.content.equipment.armor.BacktankBlock;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.content.equipment.wrench.WrenchItem;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.EquipmentSlot;

import com.familiar_omen.create_pneumatics.CreatePneumatics;

import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

@Mixin(BacktankBlock.class)
public abstract class BacktankBlockMixin {
	@Shadow
	public abstract ItemStack getCloneItemStack(LevelReader pLevel, BlockPos pos, BlockState state);

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void putIntoCurioBackSlot(ItemStack arg0, BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand arg5, BlockHitResult arg6, CallbackInfoReturnable<ItemInteractionResult> cir) {
		var stacks = player.getCapability(CuriosCapability.INVENTORY).getStacksHandler("back").get().getStacks();

		if (!level.isClientSide)
		if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
		for (int i = 0; i < stacks.getSlots(); i++) {
			if (stacks.getStackInSlot(i).isEmpty()) {
				level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .75f, 1);
				stacks.setStackInSlot(i, getCloneItemStack(level, blockPos, state));
				level.destroyBlock(blockPos, false);
				cir.setReturnValue(ItemInteractionResult.SUCCESS);
				break;
			}
		}
    }
}