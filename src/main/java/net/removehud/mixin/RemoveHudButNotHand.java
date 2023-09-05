package net.removehud.mixin;

import net.minecraft.client.MinecraftClient;
import net.dehydration.init.ConfigInit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import net.removehud.RemoveHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(InGameHud.class)
public abstract class RemoveHudButNotHand {

    @Shadow protected MinecraftClient client;

    @Shadow protected abstract void renderVignetteOverlay(DrawContext context, Entity entity);

    @Shadow protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Shadow protected abstract void renderHotbar(float tickDelta, DrawContext context);

    @Shadow public abstract void renderExperienceBar(DrawContext context, int x);

    @Shadow protected abstract void renderCrosshair(DrawContext context);

    @Shadow protected abstract void renderPortalOverlay(DrawContext context, float nauseaStrength);

    @Shadow protected abstract void renderSpyglassOverlay(DrawContext context, float scale);

    @Shadow protected abstract void renderStatusBars(DrawContext context);

    @Shadow public abstract void renderMountJumpBar(JumpingMount mount, DrawContext context, int x);

    @Shadow protected abstract void renderMountHealth(DrawContext context);

    @Shadow public abstract void renderHeldItemTooltip(DrawContext context);

    @Shadow protected abstract void renderStatusEffectOverlay(DrawContext context);

    @Shadow protected abstract void renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective);

    @Shadow protected abstract void renderAutosaveIndicator(DrawContext context);

    @Shadow protected abstract void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking);

    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/gui/DrawContext;)V"))
    public void renderHotBarMix(InGameHud instance, float tickDelta, DrawContext context) {
        if (ConfigInit.CONFIG.HotBar) {
            renderHotbar(tickDelta, context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderCrossHairsMix(InGameHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.Crosshairs) {
            renderCrosshair(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderVignetteOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/Entity;)V"))
    public void renderVignette(InGameHud instance, DrawContext context, Entity entity) {
        if (ConfigInit.CONFIG.Vignette) {
            renderVignetteOverlay(context, entity);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V"))
    public void renderOverlays(InGameHud instance, DrawContext context, Identifier texture, float opacity) {
        if (ConfigInit.CONFIG.OtherOverlays) {
            renderOverlay(context, texture, opacity);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPortalOverlay(Lnet/minecraft/client/gui/DrawContext;F)V"))
    public void renderPortalOverlays(InGameHud instance, DrawContext context, float nauseaStrength) {
        if (ConfigInit.CONFIG.PortalOverlay) {
            renderPortalOverlay(context, nauseaStrength);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSpyglassOverlay(Lnet/minecraft/client/gui/DrawContext;F)V"))
    public void renderSpyglassOverlays(InGameHud instance, DrawContext context, float scale) {
        if (ConfigInit.CONFIG.SpyglassOverlay) {
            renderSpyglassOverlay(context, scale);
        }
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"))
    private void inj(InGameHud instance, DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking) {

//        switch (ConfigInit.CONFIG.HpBarOrigin) {
//            case TOPLEFT -> {
//                x = 0;
//                y = 0;
//            }
//            case TOPRIGHT -> {
//                x = scaledWidth;
//                y = 0;
//            }
//            case BOTTOMLEFFT -> {
//                x =0;
//                y = scaledHeight;
//            }
//
//            case BOTTONRIGHT -> {
//                x = scaledWidth;
//                y = scaledHeight;
//            }
//            default -> {
//            }
//        }

        if (ConfigInit.CONFIG.HpBar) {
            renderHealthBar(context, player, x + ConfigInit.CONFIG.HpXOffset, y + ConfigInit.CONFIG.HpYOffset, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking);
        }
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0))
    private void inj(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        if (ConfigInit.CONFIG.ArmorBar) {
            instance.drawTexture(texture, x + ConfigInit.CONFIG.ArmorXOffset, y + ConfigInit.CONFIG.ArmorYOffset, u, v, width, height);
        }
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1))
    private void inj1(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        if (ConfigInit.CONFIG.ArmorBar) {
            instance.drawTexture(texture, x + ConfigInit.CONFIG.ArmorXOffset, y + ConfigInit.CONFIG.ArmorYOffset, u, v, width, height);
        }
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 2))
    private void inj2(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        if (ConfigInit.CONFIG.ArmorBar) {
            instance.drawTexture(texture, x + ConfigInit.CONFIG.ArmorXOffset, y + ConfigInit.CONFIG.ArmorYOffset, u, v, width, height);
        }
    }

    private void drawHungerBar(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {
        if (ConfigInit.CONFIG.HungerBar) {
            instance.setShaderColor(1f, 1f, 1f, RemoveHud.PeekAlpha);
            instance.drawTexture(texture, x + ConfigInit.CONFIG.FoodXOffset, y + ConfigInit.CONFIG.FoodYOffset, u, v, width, height);
            instance.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 3))
    private void inj3(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        drawHungerBar(instance, texture, x, y, u, v, width, height);
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 4))
    private void inj4(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        drawHungerBar(instance, texture, x, y, u, v, width, height);
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 5))
    private void inj5(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        drawHungerBar(instance, texture, x, y, u, v, width, height);
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 6))
    private void inj6(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        if (ConfigInit.CONFIG.AirBar) {
            instance.drawTexture(texture, x + ConfigInit.CONFIG.AirXOffset, y + ConfigInit.CONFIG.AirYOffset, u, v, width, height);
        }
    }
    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 7))
    private void inj7(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {

        if (ConfigInit.CONFIG.AirBar) {
            instance.drawTexture(texture, x + ConfigInit.CONFIG.AirXOffset, y + ConfigInit.CONFIG.AirYOffset, u, v, width, height);
        }

    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountHealth(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderMountHealthMix(InGameHud instance, DrawContext context) {

        if (ConfigInit.CONFIG.MountHealth) {
            renderMountHealth(context);
        }

    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountJumpBar(Lnet/minecraft/entity/JumpingMount;Lnet/minecraft/client/gui/DrawContext;I)V"))
    public void renderMountJumpMix(InGameHud instance, JumpingMount mount, DrawContext context, int x) {
        if (ConfigInit.CONFIG.MountJumpbar) {
            renderMountJumpBar(mount, context, x);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHeldItemTooltip(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderHeldItemTooltipMix(InGameHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.HeldItemTooltip) {
            renderHeldItemTooltip(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;renderSpectatorMenu(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderSpectatorMenu(SpectatorHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.SpectatorMenu) {
            instance.renderSpectatorMenu(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;render(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderSpectatorHud(SpectatorHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.SpectatorHud) {
            instance.render(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/BossBarHud;render(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderBossBar(BossBarHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.BossBar) {
            instance.render(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderStatusEffectOverlay(InGameHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.StatusEffectOverlay) {
            renderStatusEffectOverlay(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    public void renderScoreBoard(InGameHud instance, DrawContext context, ScoreboardObjective objective) {
        if (ConfigInit.CONFIG.ScoreBoard) {
            renderScoreboardSidebar(context, objective);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;render(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderDebugHud(DebugHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.DebugHud) {
            instance.render(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    public void renderPlayerList(PlayerListHud instance, DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
        if (ConfigInit.CONFIG.PlayerList) {
            instance.render(context, scaledWindowWidth, scoreboard, objective);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;III)V"))
    public void renderChatHud(ChatHud instance, DrawContext context, int currentTick, int mouseX, int mouseY) {
        if (ConfigInit.CONFIG.ChatHud) {
            instance.render(context, currentTick, mouseX, mouseY);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;)V"))
    public void renderAutosave(InGameHud instance, DrawContext context) {
        if (ConfigInit.CONFIG.Autosave) {
            renderAutosaveIndicator(context);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderExperienceBar(Lnet/minecraft/client/gui/DrawContext;I)V"))
    public void renderAutosave(InGameHud instance, DrawContext context, int x) {
        if (ConfigInit.CONFIG.ExpBar) {
            renderExperienceBar(context, x);
        }
    }

    @Inject(at=@At("HEAD"), method = "render", cancellable = true)
    public void render(CallbackInfo info) {
        if (ConfigInit.CONFIG.removeHud) {
            info.cancel();
        }
    }

}
