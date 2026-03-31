package shake1227.trashtreasure.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shake1227.trashtreasure.TrashTreasure;

@Mod.EventBusSubscriber(modid = TrashTreasure.MODID, value = Dist.CLIENT)
public class ClientHudEvents {
    public static int currentTicks = -1;
    public static int totalTicks = -1;

    public static float displayProgress = 0f, prevDisplayProgress = 0f;
    public static float targetProgress = 0f;

    public static float barYOffset = 20f, prevBarYOffset = 20f;
    public static float barAlpha = 0f, prevBarAlpha = 0f;

    public static float textYOffset = 20f, prevTextYOffset = 20f;
    public static float textAlpha = 0f, prevTextAlpha = 0f;
    public static int state = 0;
    public static int finishTimer = 0;

    public static void startAppraisal() {
        state = 1;
        displayProgress = 0f; prevDisplayProgress = 0f;
        targetProgress = 0f;
        barYOffset = 20f; prevBarYOffset = 20f;
        barAlpha = 0f; prevBarAlpha = 0f;
        textAlpha = 0f; prevTextAlpha = 0f;
    }

    public static void triggerFinishAnimation() {
        if (state == 1) state = 2;
    }

    public static void cancelAppraisal() {
        if (state == 1) {
            state = 4;
        } else {
            state = 0;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            prevDisplayProgress = displayProgress;
            prevBarYOffset = barYOffset;
            prevBarAlpha = barAlpha;
            prevTextYOffset = textYOffset;
            prevTextAlpha = textAlpha;

            if (state == 1) {
                float targetBarY = -5f;
                float targetBarA = 1f;

                if (totalTicks > 0) targetProgress = 1.0f - ((float) currentTicks / totalTicks);

                barYOffset += (targetBarY - barYOffset) * 0.2f;
                barAlpha += (targetBarA - barAlpha) * 0.2f;
                displayProgress += (targetProgress - displayProgress) * 0.15f;

            } else if (state == 2) {
                float targetBarY = 20f;
                float targetBarA = 0f;

                barYOffset += (targetBarY - barYOffset) * 0.2f;
                barAlpha += (targetBarA - barAlpha) * 0.2f;
                displayProgress += (1.0f - displayProgress) * 0.25f;

                if (barAlpha < 0.02f) {
                    state = 3;
                    finishTimer = 80;
                    textYOffset = 20f; prevTextYOffset = 20f;
                    textAlpha = 0f; prevTextAlpha = 0f;
                    barAlpha = 0f; prevBarAlpha = 0f;

                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.player.level().playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F, false);
                    }
                }
            } else if (state == 3) {
                float targetTextY = -5f;
                float targetTextA = 1f;

                if (finishTimer > 0) {
                    finishTimer--;
                    if (finishTimer < 15) {
                        targetTextY = 20f;
                        targetTextA = 0f;
                    }
                } else {
                    targetTextY = 20f;
                    targetTextA = 0f;
                    if (textAlpha < 0.02f) {
                        state = 0;
                        textAlpha = 0f; prevTextAlpha = 0f;
                        textYOffset = 20f; prevTextYOffset = 20f;
                        return;
                    }
                }

                textYOffset += (targetTextY - textYOffset) * 0.2f;
                textAlpha += (targetTextA - textAlpha) * 0.2f;

            } else if (state == 4) {
                float targetBarY = 20f;
                float targetBarA = 0f;

                barYOffset += (targetBarY - barYOffset) * 0.2f;
                barAlpha += (targetBarA - barAlpha) * 0.2f;

                if (barAlpha < 0.02f) {
                    state = 0;
                    barAlpha = 0f; prevBarAlpha = 0f;
                    return;
                }
            } else if (state == 0) {
                barAlpha = 0f; prevBarAlpha = 0f;
                textAlpha = 0f; prevTextAlpha = 0f;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;
        if (state == 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics gg = event.getGuiGraphics();
        float pt = event.getPartialTick();

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int baseY = screenHeight / 2 + 15;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float currentBarA = net.minecraft.util.Mth.lerp(pt, prevBarAlpha, barAlpha);
        float currentTextA = net.minecraft.util.Mth.lerp(pt, prevTextAlpha, textAlpha);

        if (currentBarA > 0.01f) {
            int a = (int) (currentBarA * 255);
            a = Math.max(0, Math.min(255, a));
            float currentBarY = net.minecraft.util.Mth.lerp(pt, prevBarYOffset, barYOffset);
            int yPos = baseY + (int) currentBarY;

            int barWidth = 100;
            int barHeight = 6;
            int startX = centerX - barWidth / 2;

            gg.fill(startX - 1, yPos - 1, startX + barWidth + 1, yPos + barHeight + 1, (a << 24) | 0x000000);
            gg.fill(startX, yPos, startX + barWidth, yPos + barHeight, (a << 24) | 0x444444);

            float currentProg = net.minecraft.util.Mth.lerp(pt, prevDisplayProgress, displayProgress);
            int fillWidth = (int) (barWidth * currentProg);
            if (fillWidth > 0) {
                gg.fillGradient(startX, yPos, startX + fillWidth, yPos + barHeight, (a << 24) | 0xFFA500, (a << 24) | 0x8B4500);
            }

            String pctText = String.format("%d%%", (int)(currentProg * 100));
            gg.drawCenteredString(mc.font, pctText, centerX, yPos + 10, (a << 24) | 0xFFFFFF);
        }

        if (currentTextA > 0.01f) {
            int a = (int) (currentTextA * 255);
            a = Math.max(0, Math.min(255, a));
            float currentTextY = net.minecraft.util.Mth.lerp(pt, prevTextYOffset, textYOffset);
            int yPos = baseY + (int) currentTextY;

            gg.drawCenteredString(mc.font, Component.translatable("message.trashtreasure.appraise_success"), centerX, yPos - 5, (a << 24) | 0xFFFFFF);
        }

        RenderSystem.disableBlend();
    }
}