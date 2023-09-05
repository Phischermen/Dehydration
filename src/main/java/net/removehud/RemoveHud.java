package net.removehud;

import me.shedaniel.autoconfig.AutoConfig;
import net.dehydration.access.ThirstManagerAccess;
import net.dehydration.init.EffectInit;
import net.dehydration.misc.ThirstTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class RemoveHud implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("removehud");

	private KeyBinding keynmap;
	private KeyBinding keynmap2;

	private KeyBinding keynmap3;

	private float targetPeekAlpha;
	public static float PeekAlpha;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModConfig.init();

		keynmap = new KeyBinding("key.removehud.toggle_mod", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F7,"key.category.removehud");
		keynmap2 = new KeyBinding("key.removehud.open_settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, "key.category.removehud");
		keynmap3 = new KeyBinding("key.removehud.peek_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.category.removehud");

		KeyBindingHelper.registerKeyBinding(keynmap);
		KeyBindingHelper.registerKeyBinding(keynmap2);
		KeyBindingHelper.registerKeyBinding(keynmap3);

		ClientTickEvents.END_CLIENT_TICK.register(this::removeHudToggleListener);

		ClientTickEvents.END_CLIENT_TICK.register(this::settingsMenuListener);

		ClientTickEvents.END_CLIENT_TICK.register(this::peekHudRoutine);

	}

	private void settingsMenuListener(MinecraftClient client) {
		while (keynmap2.wasPressed()) {
			Screen settings = AutoConfig.getConfigScreen(ModConfig.class, null).get();
			client.setScreen(settings);
		}
	}
	private void removeHudToggleListener(MinecraftClient client) {
		while (keynmap.wasPressed()) {
			ModConfig.INSTANCE.removeHud = !ModConfig.INSTANCE.removeHud;
		}
	}
	private void peekHudRoutine(MinecraftClient client){
		if (client.player == null) return;
		// Peek because...
		// ... player wants to.
		var b0 = keynmap3.isPressed();
		// ... player is holding food.
		var b1 = client.player.getMainHandStack().isFood() || client.player.getOffHandStack().isFood();
		// ... player is holding a drink.
		var b2 = !client.player.getMainHandStack().isEmpty() && !client.player.getMainHandStack().getTooltipData().isEmpty()
				&& client.player.getMainHandStack().getTooltipData().get() instanceof ThirstTooltipData
				|| !client.player.getOffHandStack().isEmpty() && !client.player.getOffHandStack().getTooltipData().isEmpty()
				&& client.player.getOffHandStack().getTooltipData().get() instanceof ThirstTooltipData;
		// ... player has a hunger/thirst effecting status effect.
		var b3 = client.player.hasStatusEffect(StatusEffects.HUNGER) || client.player.hasStatusEffect(StatusEffects.SATURATION)
				|| client.player.hasStatusEffect(EffectInit.THIRST) || client.player.hasStatusEffect(EffectInit.HYDRATION);
		// ... player is regenerating health.
		var b4 = client.player.canFoodHeal() &&
				(client.player.getHungerManager().getFoodLevel() >= 18 || ((ThirstManagerAccess)client.player).getThirstManager().getThirstLevel() >= 18);
		// ... player has half hunger/thirst.
		var b5 = (client.player.getHungerManager().getFoodLevel() == 10 || ((ThirstManagerAccess)client.player).getThirstManager().getThirstLevel() == 10);
		// ... player has less than a quarter (+1) hunger/thirst.
		var b6 = (client.player.getHungerManager().getFoodLevel() <= 5 || ((ThirstManagerAccess)client.player).getThirstManager().getThirstLevel() <= 6);
		if (b0 || b1 || b2 || b3 || b4 || b5 || b6){
			targetPeekAlpha = 1F;
		} else{
			targetPeekAlpha = 0F;
		}
		PeekAlpha = LinearInterp(PeekAlpha, targetPeekAlpha, 0.1f);
	}

	private float LinearInterp(float n0, float n1, float a){
		return (1.0f - a) * n0 + (a * n1);
	}

}
