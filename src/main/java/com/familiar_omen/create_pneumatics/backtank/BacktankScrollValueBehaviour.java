package com.familiar_omen.create_pneumatics.backtank;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class BacktankScrollValueBehaviour extends ScrollValueBehaviour {
	public static final String[] rowLabels = new String[] {"\u2699", "\u27f3", "\u27f2"}; 

	public BacktankScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
		super(label, be, slot);
		withFormatter(v -> String.valueOf(Math.abs(v)));
	}

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		ImmutableList<Component> rows = ImmutableList.of(
            Component.literal(rowLabels[0]).withStyle(ChatFormatting.BOLD),
            Component.literal(rowLabels[1]).withStyle(ChatFormatting.BOLD),
			Component.literal(rowLabels[2]).withStyle(ChatFormatting.BOLD));
		ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
		return new ValueSettingsBoard(label, 256, 32, rows, formatter);
	}

	@Override
	public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
		int value = Math.max(1, valueSetting.value());
		if (!valueSetting.equals(getValueSettings()))
			playFeedbackSound(this);
		
        switch(valueSetting.row()){
            case 0: 
                setValue(0);
                break;
            case 1: 
                setValue(-value);
                break;
            case 2: 
                setValue(value);
                break;
        }
	}

	@Override
	public ValueSettings getValueSettings() {
		int row = 0;
		
		if (value < 0)
			row = 1;
		if (value > 0)
			row = 2;
		
		int val = row == 0 ? 126 : Math.abs(value);
		
		return new ValueSettings(row, val);
	}

	public MutableComponent formatSettings(ValueSettings settings) {
		if (settings.row() == 0)
			return CreateLang.text("Charging").component();

		return CreateLang.number(Math.max(1, Math.abs(settings.value())))
			.add(CreateLang.text(rowLabels[settings.row()])
				.style(ChatFormatting.BOLD))
			.component();
	}

	@Override
	public String getClipboardKey() {
		return "Speed";
	}

}