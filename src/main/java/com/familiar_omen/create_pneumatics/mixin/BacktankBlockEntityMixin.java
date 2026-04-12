package com.familiar_omen.create_pneumatics.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.familiar_omen.create_pneumatics.BacktankBlockEntityInterface;
import com.familiar_omen.create_pneumatics.BacktankMode;
import com.familiar_omen.create_pneumatics.CreatePneumatics;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.particle.AirParticleData;
import com.simibubi.create.foundation.utility.CreateLang;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BacktankBlockEntity.class)
public abstract class BacktankBlockEntityMixin extends KineticBlockEntity implements BacktankBlockEntityInterface{

    public BacktankMode mode = BacktankMode.Consume;

    // @Shadow
    // private Component defaultName;
    
    // @Shadow
	// private DataComponentPatch componentPatch;

    public BacktankBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
	// 	defaultName = getDefaultName(state);
	// 	componentPatch = DataComponentPatch.EMPTY;

    //     CreatePneumatics.LOGGER.info("Backtank entity made");
	// }

    // @Shadow
    // public abstract Component getDefaultName(BlockState state);

    // @Inject(method = "getGeneratedSpeed", at = @At("HEAD"), cancellable = true, locals = LocalCapture.PRINT)


    @Override
    public float getGeneratedSpeed() {
		if (mode == BacktankMode.Clockwise) {
            return 16f;
        }
		if (mode == BacktankMode.CounterClockwise) {
            return -16f;
        }
        return 0f;
    }


	// public void addSpeedDependingOnMode(CallbackInfoReturnable<Float> cir) {
	// 	if (should_output) {
    //         cir.setReturnValue(16f);
    //     }
	// }
    
    @Override
    public void flip() {
		if (mode == BacktankMode.Consume) {
            mode = BacktankMode.Clockwise;
        }
        else if (mode == BacktankMode.Clockwise) {
            mode = BacktankMode.CounterClockwise;
        }
		else if (mode == BacktankMode.CounterClockwise) {
            mode = BacktankMode.Consume;
        }
		reActivateSource = true;
    }

    public float TEMPcalculateStressApplied() {
		float impact = (float) BlockStressValues.getImpact(getStressConfigKey());
		this.lastStressApplied = impact;
		return impact;
	}

	public float TEMPcalculateAddedStressCapacity() {
		float capacity = (float) BlockStressValues.getCapacity(getStressConfigKey());
		this.lastCapacityProvided = capacity;
		return capacity;
	}


    @Override
	public float calculateStressApplied() {
        if (mode != BacktankMode.Consume)
            return TEMPcalculateAddedStressCapacity();
        else
            return super.calculateStressApplied();
	}

	public float calculateAddedStressCapacity() {
        if (mode != BacktankMode.Consume)
            return TEMPcalculateStressApplied();
        else
            return super.calculateAddedStressCapacity();
	}
    
    @Shadow
	public int airLevel;
    
    @Shadow 
	public int airLevelTimer;

    @Shadow
    public abstract int getComparatorOutput();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    // @Override
    public void tick(CallbackInfo ci) {
        if (mode != BacktankMode.Consume)
			ci.cancel();
        
		if (level.isClientSide) {
			Vec3 centerOf = VecHelper.getCenterOf(worldPosition);
			Vec3 v = VecHelper.offsetRandomly(centerOf, level.random, .65f);
			Vec3 m = v.subtract(centerOf);
			if (airLevel != 0)
				level.addParticle(new AirParticleData(1, .05f), centerOf.x, centerOf.y, centerOf.z, m.x, m.y, m.z);
		}
		else if (airLevelTimer > 0) {
			airLevelTimer--;
		} 
		else 
		{
            int prevComparatorLevel = getComparatorOutput();
            float abs = Math.abs(getSpeed());
            int increment = Mth.clamp(((int) abs - 100) / 20, 1, 5);
            airLevel = Math.max(0, airLevel - increment);
            if (getComparatorOutput() != prevComparatorLevel && !level.isClientSide)
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            if (airLevel == 0)
                mode = BacktankMode.Consume;
                reActivateSource = true;
                sendData();
				airLevelTimer = Mth.clamp((int) (128f - abs / 5f) - 108, 0, 20);
		}
		
		if (reActivateSource) {
			updateGeneratedRotation();
			reActivateSource = false;
		}
    }

    // Power Generation Code
    public boolean reActivateSource;

	protected void notifyStressCapacityChange(float capacity) {
		getOrCreateNetwork().updateCapacityFor(this, capacity);
	}

	@Override
	public void removeSource() {
		if (hasSource() && isSource())
			reActivateSource = true;
		super.removeSource();
	}

	@Override
	public void setSource(BlockPos source) {
		super.setSource(source);
		BlockEntity blockEntity = level.getBlockEntity(source);
		if (!(blockEntity instanceof KineticBlockEntity sourceBE))
			return;
		if (reActivateSource && Math.abs(sourceBE.getSpeed()) >= Math.abs(getGeneratedSpeed()))
			reActivateSource = false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!StressImpact.isEnabled())
			return added;

		float stressBase = calculateAddedStressCapacity();
		if (Mth.equal(stressBase, 0))
			return added;

		CreateLang.translate("gui.goggles.generator_stats")
			.forGoggles(tooltip);
		CreateLang.translate("tooltip.capacityProvided")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);

		float speed = getTheoreticalSpeed();
		if (speed != getGeneratedSpeed() && speed != 0)
			stressBase *= getGeneratedSpeed() / speed;

		float stressTotal = Math.abs(stressBase * speed);

		CreateLang.number(stressTotal)
			.translate("generic.unit.stress")
			.style(ChatFormatting.AQUA)
			.space()
			.add(CreateLang.translate("gui.goggles.at_current_speed")
				.style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);

		return true;
	}

	public void updateGeneratedRotation() {
		float speed = getGeneratedSpeed();
		float prevSpeed = this.speed;

		if (level == null || level.isClientSide)
			return;

		if (prevSpeed != speed) {
			if (!hasSource()) {
				SpeedLevel levelBefore = SpeedLevel.of(this.speed);
				SpeedLevel levelafter = SpeedLevel.of(speed);
				if (levelBefore != levelafter)
					effects.queueRotationIndicators();
			}

			applyNewSpeed(prevSpeed, speed);
		}

		if (hasNetwork() && speed != 0) {
			KineticNetwork network = getOrCreateNetwork();
			notifyStressCapacityChange(calculateAddedStressCapacity());
			getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
			network.updateStress();
		}

		onSpeedChanged(prevSpeed);
		sendData();
	}

	public void applyNewSpeed(float prevSpeed, float speed) {

		// Speed changed to 0
		if (speed == 0) {
			if (hasSource()) {
				notifyStressCapacityChange(0);
				getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
				return;
			}
			detachKinetics();
			setSpeed(0);
			setNetwork(null);
			return;
		}

		// Now turning - create a new Network
		if (prevSpeed == 0) {
			setSpeed(speed);
			setNetwork(createNetworkId());
			attachKinetics();
			return;
		}

		// Change speed when overpowered by other generator
		if (hasSource()) {

			// Staying below Overpowered speed
			if (Math.abs(prevSpeed) >= Math.abs(speed)) {
				if (Math.signum(prevSpeed) != Math.signum(speed))
					level.destroyBlock(worldPosition, true);
				return;
			}

			// Faster than attached network -> become the new source
			detachKinetics();
			setSpeed(speed);
			source = null;
			setNetwork(createNetworkId());
			attachKinetics();
			return;
		}

		// Reapply source
		detachKinetics();
		setSpeed(speed);
		attachKinetics();
	}

	public Long createNetworkId() {
		return worldPosition.asLong();
	}
}

// @Mixin(BacktankBlockEntity.class)
// public abstract class BacktankBlockEntityMixin extends GeneratingKineticBlockEntity implements BacktankBlockEntityInterface{

//     public boolean should_output;

//     @Shadow
//     private Component defaultName;
    
//     @Shadow
// 	private DataComponentPatch componentPatch;

//     public BacktankBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
// 		super(type, pos, state);
// 		defaultName = getDefaultName(state);
// 		componentPatch = DataComponentPatch.EMPTY;

//         CreatePneumatics.LOGGER.info("Backtank entity made");
// 	}

//     @Shadow
//     public abstract Component getDefaultName(BlockState state);

//     @Inject(method = "getGeneratedSpeed", at = @At("HEAD"), cancellable = true)
// 	public void addSpeedDependingOnMode(CallbackInfoReturnable<Float> cir) {
// 		if (should_output) {
//             cir.setReturnValue(16f);
//         }
// 	}
    
//     @Override
//     public void flip() {
//         should_output = !should_output;
//     }
// }