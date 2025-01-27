package net.cessio.displaydaycounter;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
			dispatcher.register(CommandManager.literal(MOD_ID)
					.then(CommandManager.argument("day", IntegerArgumentType.integer())
							.suggests(new DayCounterSuggestionProvider())
							.executes(context -> {
								int day = IntegerArgumentType.getInteger(context, "day");
								ServerWorld world = context.getSource().getWorld();
								world.setTimeOfDay(world.getTime() + day * 24000L);
								world.setTimeOfDay(day * 24000L);
								context.getSource().sendFeedback(() -> Text.literal("Day set to: "+ day), true);
								return 1;
							})
					)
					.then(CommandManager.argument("toggle", StringArgumentType.string())
							.suggests(new DayCounterSuggestionProvider())
							.executes(context -> {
								String toggle = StringArgumentType.getString(context, "toggle");
								DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
								if(!toggle.contains("on") && !toggle.contains("off"))
								{
									context.getSource().sendFeedback(() -> Text.literal("Invalid Argument: only \"on\", \"off\", or numerical day inputs are valid."), false);
								}
								if(toggle.contains("on"))
								{
									client.setDisplay(true);
									context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now on!"),false);
								}
								else if(toggle.contains("off"))
								{
									client.setDisplay(false);
									context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now off!"),false);
								}
								return 1;
							}))
			);
		});
	}
}