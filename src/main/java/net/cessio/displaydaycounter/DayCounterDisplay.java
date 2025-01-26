package net.cessio.displaydaycounter;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DayCounterDisplay implements ModInitializer {
	public static final String MOD_ID = "daycounterdisplay";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("setday")
					.then(CommandManager.argument("day", IntegerArgumentType.integer(0))
							.suggests(new DevCleanSuggestionProvider())
							.executes(context -> {
								int day = IntegerArgumentType.getInteger(context, "day");
								ServerWorld world = context.getSource().getWorld();
								world.setTimeOfDay(world.getTime() + day * 24000L);
								context.getSource().sendFeedback(() -> Text.literal("Day set to: "+ day), true);
								return 1;
							})
					)
			);
		});
	}
}