package net.cessio.displaydaycounter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DayCounterDisplayClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register a callback to render the HUD
        isOn = true;
        HudRenderCallback.EVENT.register(this::renderDayInfo);
    }
    static DayCounterDisplayClient instance;
    boolean isOn;
    boolean playerOverwrite = false;

    public static DayCounterDisplayClient getInstance()
    {
        return instance;
    }

    public void setDisplay(boolean bool)
    {
        isOn = bool;
    }
    private void renderDayInfo(DrawContext context, RenderTickCounter tickDelta)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && !playerOverwrite)
        {
            //get the in-game time and calculate the day
            long dayTime = client.world.getTimeOfDay();
            long dayNumber = dayTime / 24000;

            //Display the day number in the top-left corner
            String dayText = "Day: " + dayNumber;
            if(isOn) {
                context.drawText(client.textRenderer, Text.literal(dayText), 10, 10, 0xFFFFFF, true);
            }
        } else if (playerOverwrite)
        {
//            long playerTime;
//            long dayTime = playerTime;
//            long dayNumber = dayTime / 24000;
//
//            //Display the day number in the top-left corner
//            String dayText = "Day: " + dayNumber;
//            context.drawText(client.textRenderer, Text.literal(dayText), 10, 10, 0xFFFFFF, true);
        }
    }
}
