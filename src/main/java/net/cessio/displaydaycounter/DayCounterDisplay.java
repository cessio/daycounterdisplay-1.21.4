package net.cessio.displaydaycounter;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;

public class DayCounterDisplay implements ModInitializer {
	public static final String MOD_ID = "daycounterdisplay";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private boolean sendAlert = true;
	//sending packets to update tick rate
	//public static final Identifier CLIENT_TICKRATE_UPDATE_PACKET_ID = Identifier.of("dcd", "server_tick_rate");


	@Override
	public void onInitialize() {
	//reference player client
//	DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();


		LOGGER.info("Hello Fabric world!");
		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
//			ticksWhenJoined = serverPlayNetworkHandler.getPlayer().getWorld().getTime();

		});

		//Mark the percentage of an hour the client has left when leaving a world
		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
//			ticksLeftOver = (serverPlayNetworkHandler.getPlayer().getWorld().getTime() - ticksWhenJoined) % (20 * 3600);
//			System.out.println("Thanks for playing! you left with " + ticksLeftOver / (tickRate * 36) + "% of an hour! ");
		});

//        Checks if the server is sprinting ticks in order to pause count.
		ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
			ServerTickManager tickManager = minecraftServer.getTickManager();
//			isSprinting = tickManager.isSprinting();
			if (tickManager.isSprinting() && sendAlert) {
				sendAlert = false;
				for (ServerPlayerEntity player : minecraftServer.getOverworld().getPlayers()) {

					player.sendMessage(Text.literal("Pause your hour counter until server finishes sprinting!"));
				}
			} else if (!tickManager.isSprinting()) {
				if (!sendAlert) {
					for (ServerPlayerEntity player : minecraftServer.getOverworld().getPlayers()) {
						player.sendMessage(Text.literal("Your hour counter can now resume its count :)"));
					}
				}
				sendAlert = true;
			}

		});
	}
}
		//assign a referenceable server variable

		// Mark the tick count since the player joined the server
//		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
//			ticksWhenJoined = serverPlayNetworkHandler.getPlayer().getWorld().getTime();
//
//		});

		//Mark the percentage of an hour the client has left when leaving a world
//		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
//			DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//			client.ticksLeftOver  = (serverPlayNetworkHandler.getPlayer().getWorld().getTime() - client.ticksWhenJoined)%(20*3600);
//			System.out.println("Thanks for playing! you left with "+client.ticksLeftOver/(client.tickRate*36)+"% of an hour! ");
//		});

		//Checks if the server is sprinting ticks in order to pause count.
//		ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
//			ServerTickManager tickManager = minecraftServer.getTickManager();
//			DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//			client.isSprinting = tickManager.isSprinting();
//			if(tickManager.isSprinting() && sendAlert)
//			{
//				sendAlert = false;
//				for(ServerPlayerEntity player : minecraftServer.getOverworld().getPlayers()) {
//					player.sendMessage(Text.literal("Your hour counter will be paused until server finishes sprinting!"));
//				}
//			}
//			else if(!tickManager.isSprinting()){
//				if(!sendAlert){
//					for(ServerPlayerEntity player : minecraftServer.getOverworld().getPlayers()) {
//						player.sendMessage(Text.literal("Your hour counter is now resuming its count!"));
//					}
//				}
//				sendAlert = true;
//			}
//
//		});

		// Register '/daycounterdisplay' command
//		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
//			final LiteralCommandNode<ServerCommandSource> dayCounterDisplayCommand = dispatcher.register(literal(MOD_ID)
//							.then(
//									literal("day")
//											.then(
//													CommandManager.argument("day", IntegerArgumentType.integer())
//															.suggests(getSuggestionProvider("day"))
//															.executes(context -> {
//																int day = IntegerArgumentType.getInteger(context, "day");
//																DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																MinecraftClient mcClient = MinecraftClient.getInstance();
//																	client.playerHeadCannon = day;
//																	client.allFalse();
//																	client.playerOverwrite = true;
//																	context.getSource().sendFeedback(() -> Text.literal("Day counter is now set to day " + day +"."), false);
//																return 1;
//															})
//											)
//											.then(
//													literal("sync")
//															.then(CommandManager.argument("sync", StringArgumentType.string())
//																	.suggests(getSuggestionProvider("sync"))
//																	.executes(context -> {
//																		String sync = StringArgumentType.getString(context, "sync");
//																		DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																		MinecraftClient mcClient = MinecraftClient.getInstance();
//																		if (sync.contains("sync-server"))
//																		{
//																			client.allFalse();
//																			context.getSource().sendFeedback(() -> Text.literal("Day counter is in sync with world data. Set to " + mcClient.world.getTimeOfDay()/24000), false);
//																		}
//																		else if(sync.contains("sync-real"))
//																		{
//																			client.allFalse();
//																			client.tickSyncington = true;
//																			context.getSource().sendFeedback(() -> Text.literal("Day counter is in sync with total ticks elapsed. Set to " + mcClient.world.getTime()/24000), false);
//																		}
//																		else if (sync.contains("sync-irl"))
//																		{
//																			client.allFalse();
//																			client.realWorldington = true;
//																			context.getSource().sendFeedback(() -> Text.literal("now counting your hours in the server..."), false);
//																			client.counterDisplayText = "Hours: ";
//																			System.out.println("Current percentage of an hour played: "+((mcClient.world.getTime()-client.ticksWhenJoined)/72000));
//																			System.out.println("Current amount of ticks elapsed since server join: "+((mcClient.world.getTime()-client.ticksWhenJoined)));
//																		}
//																		return 1;
//																	})
//															)
//											)
//											.then(
//													literal("toggle")
//															.then(CommandManager.argument("toggle", StringArgumentType.string())
//																	.suggests(getSuggestionProvider("toggle"))
//																	.executes(context -> {
//																		String toggle = StringArgumentType.getString(context, "toggle");
//																		DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																		if(!toggle.contains("on") && !toggle.contains("off") && !toggle.isEmpty())
//																		{
//																			throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: Must specify either 'on' or 'off'"));
//																		}
//																		if(toggle.contains("on"))
//																		{
//																			client.dayIsOn =true;
//																		}
//																		else if(toggle.contains("off"))
//																		{
//																			client.dayIsOn = false;
//																			context.getSource().sendFeedback(() -> Text.literal("Display toggled off. run '/dcd toggle on' to re-enable"),false);
//																		}
//																		else
//																		{
//																			client.dayIsOn = !client.dayIsOn;
//																		}
//																		return 1;
//																	})
//															)
//											)
//											.then(
//													literal("color")
//															.then(CommandManager.argument("color", StringArgumentType.string())
//																			.suggests(getSuggestionProvider("color"))
//																			.executes( context -> {
//																				String color = StringArgumentType.getString(context, "color");
//																				DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																				if(color.equals("red"))
//																				{
//																					client.dayColor = 0xFF0000;
//																				}
//																				else if(color.contains("blue"))
//																				{
//																					client.dayColor = 0x0000FF;
//																				}
//																				else if(color.contains("green"))
//																				{
//																					client.dayColor = 0x00FF00;
//																				}
//																				else if(color.contains("yellow"))
//																				{
//																					client.dayColor = 0xFFFF00;
//																				}
//																				else if(color.contains("purple"))
//																				{
//																					client.dayColor = 0xA020F0;
//																				}
//																				else if(color.contains("pink"))
//																				{
//																					client.dayColor = 0xFFC0CB;
//																				}
//																				else{
//																					try {
//																						client.dayColor = Integer.decode(color);
//																					}
//																					catch (Exception e) {
//																						throw new IllegalArgumentException();
//																					}
//																				}
//																				return 1;
//																			})
//															)
//
//											)
//											.then(
//													literal("shadow")
//															.then(CommandManager.argument("toggle", StringArgumentType.string())
//																	.suggests(getSuggestionProvider("toggle"))
//																	.executes(context -> {
//																		String toggle = StringArgumentType.getString(context, "toggle");
//																		DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																		if(!toggle.contains("on") && !toggle.contains("off") && !toggle.isEmpty())
//																		{
//																			throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: Must specify either 'on' or 'off'"));
//																		}
//																		if(toggle.contains("on"))
//																		{
//																			client.dayHasShadow = true;
//																		}
//																		else if(toggle.contains("off"))
//																		{
//																			client.dayHasShadow = false;
//																		}
//																		else
//																		{
//																			client.dayHasShadow = !client.dayHasShadow;
//																		}
//																		return 1;
//																	})
//															)
//											)
//											.then(
//													literal("dimensions")
//															.then(CommandManager.argument("x", StringArgumentType.string())
//																	.suggests(getSuggestionProvider("dimensionsX"))
//																	.executes(context -> {
//																		String xStr = StringArgumentType.getString(context, "x");
//																		DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																		int textWidth = client.client.textRenderer.getWidth(client.counterDisplayText+client.dayNumber);
//																		int userWindowWidth = client.dcdRenderer.getScaledWindowWidth();
//																		int userWindowHeight = client.dcdRenderer.getScaledWindowHeight();
//																		try {
//																			int xArg = Integer.parseInt(xStr);
//																		}
//																		catch (Exception e) {
//																			client.initializeMap(xStr, "Y");
//                                                                            switch (xStr) {
////                                                                                case "top-left" -> {
////                                                                                    client.dayX = 10;
////                                                                                    client.dayY = 10;
////                                                                                }
////                                                                                case "top-center" -> {
////                                                                                    client.dayX = userWindowWidth / 2 - (textWidth / 2);
////                                                                                    client.dayY = 10;
////                                                                                }
////                                                                                case "top-right" -> {
////                                                                                    client.dayX = userWindowWidth - (textWidth + 10);
////                                                                                    client.dayY = 10;
////                                                                                }
////                                                                                case "middle-left" -> {
////                                                                                    client.dayX = 10;
////                                                                                    client.dayY = userWindowHeight / 2;
////                                                                                }
////                                                                                case "middle-center" -> {
////                                                                                    client.dayX = userWindowWidth / 2 - (textWidth / 2);
////                                                                                    client.dayY = userWindowHeight / 2;
////                                                                                }
////                                                                                case "middle-right" -> {
////                                                                                    client.dayX = userWindowWidth - (textWidth + 10);
////                                                                                    client.dayY = userWindowHeight / 2;
////                                                                                }
////                                                                                case "bottom-left" -> {
////                                                                                    client.dayX = 10;
////                                                                                    client.dayY = (int) (userWindowHeight * 0.885416666667);
////                                                                                }
////                                                                                case "bottom-center" -> {
////                                                                                    client.dayX = userWindowWidth / 2 - (textWidth / 2);
////                                                                                    client.dayY = (int) (userWindowHeight * 0.885416666667);
////                                                                                }
////                                                                                case "bottom-right" -> {
////                                                                                    client.dayX = userWindowWidth - (textWidth + 10);
////                                                                                    client.dayY = (int) (userWindowHeight * 0.885416666667);
////                                                                                }
//                                                                                case "top", "bottom", "middle" ->
//                                                                                        client.dayY = client.wordsToPos.get(xStr);
//                                                                                case "query" ->
//                                                                                        context.getSource().sendFeedback(() -> Text.literal("Day X pos: " + client.dayX + "\nDay Y pos: " + client.dayY + "\nsize: " + client.dayScale + "\ncurrent text length: " + textWidth), false);
//                                                                                default -> {
//                                                                                    throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: position not recognized."));
//                                                                                }
//                                                                            }
//																		}
//																		return 1;
//																	})
//																	.then(CommandManager.argument("y", StringArgumentType.string())
//																			.suggests(getSuggestionProvider("dimensionsY"))
//																			.executes(context -> {
//																				String xStr = StringArgumentType.getString(context, "x");
//																				String yStr = StringArgumentType.getString(context, "y");
//																				DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																				try {
//																					client.dayX = Integer.parseInt(xStr);
//																					client.dayY = Integer.parseInt(yStr);
//																				} catch (Exception e)
//																				{
//																					client.initializeMap(client.counterDisplayText, "Y");
//																					client.dayY = client.wordsToPos.get(xStr);
//																					client.initializeMap(client.counterDisplayText, "X");
//																					client.dayX = client.wordsToPos.get(yStr);
//																					if(!client.wordsToPos.containsKey(xStr) || !client.wordsToPos.containsKey(yStr))
//																					{
//																						throw new IllegalArgumentException();
//																					}
//																				}
//																				return 1;
//																			})
//																			.then(CommandManager.argument("scale", StringArgumentType.string())
//																					.executes(context -> {
//																						DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																						String xStr = StringArgumentType.getString(context, "x");
//																						String yStr = StringArgumentType.getString(context, "y");
//																						String scaleStr = StringArgumentType.getString(context, "scale");
//																						try {
//																							client.dayX = Integer.parseInt(xStr);
//																							client.dayY = Integer.parseInt(yStr);
//
//																							client.dayScale = Float.parseFloat(scaleStr);
//
//																						} catch (Exception e)
//																						{
//																							try {
//																								client.dayScale = Float.parseFloat(scaleStr);
//																							} catch (Exception f)
//																							{
//																								throw new IllegalArgumentException();
//																							}
//																							client.initializeMap(client.counterDisplayText, "Y");
//																							if(!client.wordsToPos.containsKey(xStr) || !client.wordsToPos.containsKey(yStr))
//																							{
//																								throw new IllegalArgumentException();
//																							}
//																							client.dayY = client.wordsToPos.get(xStr);
//																							client.initializeMap(client.counterDisplayText, "X");
//																							client.dayX = client.wordsToPos.get(yStr);
//																						}
//																						return 1;
//																					})
//																			)
//																	)
//															)
//
//											)
//											.then(
//													literal("text")
//															.then(CommandManager.argument("text", StringArgumentType.string())
//																	.suggests(getSuggestionProvider("textOptions"))
//																	.executes(context -> {
//																		String str = StringArgumentType.getString(context, "text");
//																		DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//																		if(str.isEmpty())
//																		{
//																			client.dayTextChangedByPlayer = false;
//																		}
//																		else
//																		{
//																			client.dayTextChangedByPlayer = true;
//																			client.counterDisplayText = str;
//																		}
//
//																		return 1;
//																	})
//															)
//											)
//							)
//						//hours
//			);
//			dispatcher.register(literal("dcd").redirect(dayCounterDisplayCommand));
//		});
//	}
//
//	private static SuggestionProvider<ServerCommandSource> getSuggestionProvider(String commandContext)
//	{
//		return new SuggestionProvider<ServerCommandSource>() {
//			@Override
//			public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
//                switch (commandContext) {
//                    case "day" -> suggestionsBuilder.suggest("<day>");
//					case "sync" -> {
//						suggestionsBuilder.suggest("sync-server");
//						suggestionsBuilder.suggest("sync-real");
//						suggestionsBuilder.suggest("sync-irl");
//					}
//                    case "color" -> {
//                        suggestionsBuilder.suggest("red");
//                        suggestionsBuilder.suggest("blue");
//                        suggestionsBuilder.suggest("yellow");
//                        suggestionsBuilder.suggest("green");
//                        suggestionsBuilder.suggest("purple");
//                        suggestionsBuilder.suggest("pink");
//                    }
//                    case "toggle" -> {
//                        suggestionsBuilder.suggest("on");
//                        suggestionsBuilder.suggest("off");
//                    }
//                    case "textOptions" -> suggestionsBuilder.suggest("\"New Display Text\"");
//                    case "dimensionsX" -> {
//                        DayCounterDisplayClient dcdClient = DayCounterDisplayClient.getInstance();
//                        int maxX = dcdClient.dcdRenderer.getScaledWindowWidth();
//                        suggestionsBuilder.suggest("[0 - " + maxX + "]");
////                        suggestionsBuilder.suggest("top-left");
////                        suggestionsBuilder.suggest("top-right");
////                        suggestionsBuilder.suggest("top-center");
////                        suggestionsBuilder.suggest("middle-left");
////                        suggestionsBuilder.suggest("middle-right");
////                        suggestionsBuilder.suggest("middle-center");
////                        suggestionsBuilder.suggest("bottom-left");
////                        suggestionsBuilder.suggest("bottom-right");
////                        suggestionsBuilder.suggest("bottom-center");
//                        suggestionsBuilder.suggest("top");
//                        suggestionsBuilder.suggest("middle");
//                        suggestionsBuilder.suggest("bottom");
//                    }
//                    case "dimensionsY" -> {
//                        DayCounterDisplayClient dcdClient = DayCounterDisplayClient.getInstance();
//                        int maxY = dcdClient.dcdRenderer.getScaledWindowHeight();
//                        suggestionsBuilder.suggest("[0 - " + maxY + "]");
//						suggestionsBuilder.suggest("left");
//						suggestionsBuilder.suggest("center");
//						suggestionsBuilder.suggest("right");
//                    }
//                }
//				return suggestionsBuilder.buildFuture();
//			}
//		};
//	}
//}





//	public void onInitialize() {
//		LOGGER.info("Hello Fabric world!");
//		//assign a referenceable server variable
//
//		// Mark the tick count since the player joined the server
//		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
//			DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//			client.ticksWhenJoined = serverPlayNetworkHandler.getPlayer().getWorld().getTime();
//
//		});
//
//		//Mark the percentage of an hour the client has left when leaving a world
//		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
//			DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//			client.ticksLeftOver  = (serverPlayNetworkHandler.getPlayer().getWorld().getTime() - client.ticksWhenJoined)%(20*3600);
//			System.out.println("Thanks for playing! you left with "+client.ticksLeftOver/(client.tickRate*36)+"% of an hour! ");
//		});
//
//		//Checks if the server is sprinting ticks in order to pause count.
//		ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
//			ServerTickManager tickManager = minecraftServer.getTickManager();
//			DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//			client.isSprinting = tickManager.isSprinting();
//			if(tickManager.isSprinting() && sendAlert)
//			{
//				sendAlert = false;
//				for(ServerPlayerEntity player : minecraftServer.getOverworld().getPlayers()) {
//					player.sendMessage(Text.literal("Your hour counter will be paused until server finishes sprinting!"));
//				}
//			}
//			else if(!tickManager.isSprinting()){
//				if(!sendAlert){
//					for(ServerPlayerEntity player : minecraftServer.getOverworld().getPlayers()) {
//						player.sendMessage(Text.literal("Your hour counter is now resuming its count!"));
//					}
//				}
//				sendAlert = true;
//			}
//
//		});
//
//		// Register '/daycounterdisplay' command
//		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
//			final LiteralCommandNode<ServerCommandSource> dayCounterDisplayCommand = dispatcher.register(literal(MOD_ID)
//					.then(
//							literal("day")
//								.then(
//										CommandManager.argument("day", StringArgumentType.string())
//										.suggests(getSuggestionProvider("day"))
//										.executes(context -> {
//											String day = StringArgumentType.getString(context, "day");
//											DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//											MinecraftClient mcClient = MinecraftClient.getInstance();
//											//client.conncectedServer = context.getSource().getServer();
//											try{
////												client.commandExecutionOffset = (world.getTimeOfDay()/24000) - Integer.parseInt(day);
//												client.playerHeadCannon = Integer.parseInt(day);
//												client.allFalse();
//												client.playerOverwrite = true;
//												context.getSource().sendFeedback(() -> Text.literal("Day counter is now set to day " + day +"."), false);
//											} catch (Exception e) {
//												//potential for many different kinds of "sync"
//												//world.getTimeOfDay() returns the total amount of time passed in game = sync-server
//												//world.getTime() returns the total amount of ticks elapsed in game aka "real-in-game" time. sync-ticks
//												//There is potential for a "real time" version as well. assuming a conversion of ticks to seconds is able to be calculated in the code. sync-real
//												if (day.contains("sync-server"))
//												{
//													client.allFalse();
//													context.getSource().sendFeedback(() -> Text.literal("Day counter is in sync with world data. Set to " + mcClient.world.getTimeOfDay()/24000), false);
//												}
//												else if(day.contains("sync-real"))
//												{
//													client.allFalse();
//													client.tickSyncington = true;
//													context.getSource().sendFeedback(() -> Text.literal("Day counter is in sync with total ticks elapsed. Set to " + mcClient.world.getTime()/24000), false);
//												}
//												else if (day.contains("sync-irl"))
//												{
//													client.allFalse();
//													client.realWorldington = true;
//													context.getSource().sendFeedback(() -> Text.literal("now counting your hours in the server..."), false);
//													client.counterDisplayText = "Hours: ";
//													System.out.println("Current percentage of an hour played: "+((mcClient.world.getTime()-client.ticksWhenJoined)/72000));
//													System.out.println("Current amount of ticks elapsed since server join: "+((mcClient.world.getTime()-client.ticksWhenJoined)));
//												}
//												return 1;
//											}
//											return 1;
//										})
//								)
//					)
//					.then(
//							literal("toggle")
//									.then(CommandManager.argument("toggle", StringArgumentType.string())
//										.suggests(getSuggestionProvider("toggle"))
//										.executes(context -> {
//											String toggle = StringArgumentType.getString(context, "toggle");
//											DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//											if(!toggle.contains("on") && !toggle.contains("off"))
//											{
//												context.getSource().sendFeedback(() -> Text.literal("Invalid Argument: only \"on\", \"off\", or are valid inputs."), false);
//											}
//											if(toggle.contains("on"))
//											{
//												client.dayIsOn =true;
//												context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now on!"),false);
//											}
//											else if(toggle.contains("off"))
//											{
//												client.dayIsOn = false;
//												context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now off!"),false);
//											}
//											return 1;
//										})
//									)
//					)
//					.then(
//							literal("color")
//									.then(CommandManager.argument("color", StringArgumentType.string())
//											.suggests(getSuggestionProvider("color"))
//											.executes( context -> {
//												String color = StringArgumentType.getString(context, "color");
//												DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//												if(color.contains("red"))
//												{
//													client.dayColor = 0xFF0000;
//													context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now red!"),false);
//												}
//												else if(color.contains("blue"))
//												{
//													client.dayColor = 0x0000FF;
//													context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now blue!"),false);
//												}
//												else if(color.contains("green"))
//												{
//													client.dayColor = 0x00FF00;
//													context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now green!"),false);
//												}
//												else if(color.contains("yellow"))
//												{
//													client.dayColor = 0xFFFF00;
//													context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now yellow!"),false);
//												}
//												else if(color.contains("purple"))
//												{
//													client.dayColor = 0xA020F0;
//													context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now purple!"),false);
//												}
//												else if(color.contains("pink"))
//												{
//													client.dayColor = 0xFFC0CB;
//													context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now pink!"),false);
//												}
//												else{
//													try {
//														client.dayColor = Integer.decode(color);
//														context.getSource().sendFeedback(() -> Text.literal("Day Counter Display is now just right! :)"),false);
//													}
//													catch (Exception e) {
//														throw new IllegalArgumentException();
////														context.getSource().sendFeedback(() -> Text.literal("Invalid Argument: only \"red\", \"green\", \"blue\", " +
////																"\"yellow\", \"purple\", \"pink\", or a custom " +
////																"hex code (in the format 0xFFFFFF = white) are valid inputs."), false);
//													}
//												}
//												return 1;
//											})
//									)
//
//					)
//					.then(
//							literal("shadow")
//									.then(CommandManager.argument("toggle", StringArgumentType.string())
//											.suggests(getSuggestionProvider("toggle"))
//											.executes(context -> {
//												String toggle = StringArgumentType.getString(context, "toggle");
//												DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//												if(!toggle.contains("on") && !toggle.contains("off"))
//												{
//													context.getSource().sendFeedback(() -> Text.literal("Invalid Argument: only \"on\", \"off\", or are valid inputs."), false);
//												}
//												if(toggle.contains("on"))
//												{
//													client.dayHasShadow = true;
//													context.getSource().sendFeedback(() -> Text.literal("Shadow is now on!"),false);
//												}
//												else if(toggle.contains("off"))
//												{
//													client.dayHasShadow = false;
//													context.getSource().sendFeedback(() -> Text.literal("Shadow is now off!"),false);
//												}
//												return 1;
//											})
//									)
//					)
//					.then(
//						literal("dimensions")
//							.then(CommandManager.argument("x", StringArgumentType.string())
//									.suggests(getSuggestionProvider("dimensionsX"))
//									.executes(context -> {
//										String xStr = StringArgumentType.getString(context, "x");
//										DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//										int textWidth = client.client.textRenderer.getWidth(client.counterDisplayText+client.dayNumber);
//										int userWindowWidth = client.dcdRenderer.getScaledWindowWidth();
//										int userWindowHeight = client.dcdRenderer.getScaledWindowHeight();
//										try {
//											int xArg = Integer.parseInt(xStr);
//										}
//										catch (Exception e) {
//											client.initializeMap(xStr, "Y");
//											if(xStr.equals("top-left"))
//											{
//												client.dayX = 10;
//												client.dayY = 10;
//											}
//											else if(xStr.equals("top-center"))
//											{
//												client.dayX = userWindowWidth/2 - (textWidth/2);
//												client.dayY = 10;
//											}
//											else if(xStr.equals("top-right"))
//											{
//												client.dayX = userWindowWidth - (textWidth+10);
//												client.dayY = 10;
//											}
//											else if(xStr.equals("middle-left"))
//											{
//												client.dayX = 10;
//												client.dayY = userWindowHeight/2;
//											}
//											else if(xStr.equals("middle-center"))
//											{
//												client.dayX = userWindowWidth/2 - (textWidth/2);
//												client.dayY = userWindowHeight/2;
//											}
//											else if(xStr.equals("middle-right"))
//											{
//												client.dayX = userWindowWidth - (textWidth+10);
//												client.dayY = userWindowHeight/2;
//											}
//											else if(xStr.equals("bottom-left"))
//											{
//												client.dayX = 10;
//												client.dayY = (int) (userWindowHeight*0.885416666667);
//											}
//											else if(xStr.equals("bottom-center"))
//											{
//												client.dayX = userWindowWidth/2 - (textWidth/2);
//												client.dayY = (int) (userWindowHeight*0.885416666667);
//											}
//											else if(xStr.equals("bottom-right"))
//											{
//												client.dayX = userWindowWidth - (textWidth+10);
//												client.dayY = (int) (userWindowHeight*0.885416666667);
//											}
//											else if(xStr.equals("top") || xStr.equals("bottom") || xStr.equals("middle"))
//											{
//												client.dayY = client.wordsToPos.get(xStr);
//											}
//											else if(xStr.equals("query"))
//											{
//												context.getSource().sendFeedback(() -> Text.literal("Day X pos: "+client.dayX+"\nDay Y pos: "+client.dayY+"\nsize: "+client.dayScale+"\ncurrent text length: "+textWidth), false);
//											}
//											else {
//												SimpleCommandExceptionType oops = new SimpleCommandExceptionType(Text.literal("first error"));
//												throw new CommandSyntaxException(oops, Text.literal("second error"));
//											}
//										}
//										return 1;
//									})
//									.then(CommandManager.argument("y", StringArgumentType.string())
//											.suggests(getSuggestionProvider("dimensionsY"))
//											.executes(context -> {
//												String xStr = StringArgumentType.getString(context, "x");
//												String yStr = StringArgumentType.getString(context, "y");
//												DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//												try {
//													client.dayX = Integer.parseInt(xStr);
//													client.dayY = Integer.parseInt(yStr);
//												} catch (Exception e)
//												{
//													client.initializeMap(client.counterDisplayText, "Y");
//													client.dayY = client.wordsToPos.get(xStr);
//													client.initializeMap(client.counterDisplayText, "X");
//													client.dayX = client.wordsToPos.get(yStr);
//													if(!client.wordsToPos.containsKey(xStr) || !client.wordsToPos.containsKey(yStr))
//													{
//														throw new IllegalArgumentException();
//													}
//												}
//												return 1;
//											})
//											.then(CommandManager.argument("scale", StringArgumentType.string())
//													.executes(context -> {
//															DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//															String xStr = StringArgumentType.getString(context, "x");
//															String yStr = StringArgumentType.getString(context, "y");
//															String scaleStr = StringArgumentType.getString(context, "scale");
//															try {
//																client.dayX = Integer.parseInt(xStr);
//																client.dayY = Integer.parseInt(yStr);
//
//																client.dayScale = Float.parseFloat(scaleStr);
//
//															} catch (Exception e)
//															{
//																try {
//																	client.dayScale = Float.parseFloat(scaleStr);
//																} catch (Exception f)
//																{
//																	throw new IllegalArgumentException();
//																}
//																client.initializeMap(client.counterDisplayText, "Y");
//																if(!client.wordsToPos.containsKey(xStr) || !client.wordsToPos.containsKey(yStr))
//																{
//																	throw new IllegalArgumentException();
//																}
//																client.dayY = client.wordsToPos.get(xStr);
//																client.initializeMap(client.counterDisplayText, "X");
//																client.dayX = client.wordsToPos.get(yStr);
//															}
//															return 1;
//													})
//											)
//									)
//							)
//
//					)
//					.then(
//						literal("text")
//							.then(CommandManager.argument("text", StringArgumentType.string())
//									.suggests(getSuggestionProvider("textOptions"))
//											.executes(context -> {
//												String str = StringArgumentType.getString(context, "text");
//												DayCounterDisplayClient client = DayCounterDisplayClient.getInstance();
//												if(str.isEmpty())
//												{
//													client.dayTextChangedByPlayer = false;
//												}
//												else
//												{
//													client.dayTextChangedByPlayer = true;
//													client.counterDisplayText = str;
//												}
//
//												return 1;
//											})
//							)
//					)
//			);
//			dispatcher.register(literal("dcd").redirect(dayCounterDisplayCommand));
//		});
//	}
//
//	private static SuggestionProvider<ServerCommandSource> getSuggestionProvider(String commandContext)
//	{
//        return new SuggestionProvider<ServerCommandSource>() {
//            @Override
//            public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
//                if(commandContext.equals("day"))
//                {
//                    suggestionsBuilder.suggest("<day>");
//                    suggestionsBuilder.suggest("sync-server");
//                    suggestionsBuilder.suggest("sync-real");
//                    suggestionsBuilder.suggest("sync-irl");
//                }
//                else if(commandContext.equals("color"))
//                {
//                    suggestionsBuilder.suggest("red");
//                    suggestionsBuilder.suggest("blue");
//                    suggestionsBuilder.suggest("yellow");
//                    suggestionsBuilder.suggest("green");
//                    suggestionsBuilder.suggest("purple");
//                    suggestionsBuilder.suggest("pink");
//                }
//                else if (commandContext.equals("toggle"))
//                {
//                    suggestionsBuilder.suggest("on");
//                    suggestionsBuilder.suggest("off");
//                }
//				else if(commandContext.equals("textOptions"))
//				{
//					suggestionsBuilder.suggest("\"New Display Text\"");
//				}
//				else if(commandContext.equals("dimensionsX"))
//				{
//					DayCounterDisplayClient dcdClient = DayCounterDisplayClient.getInstance();
//					int maxX = dcdClient.dcdRenderer.getScaledWindowWidth();
//					suggestionsBuilder.suggest("[0 - "+maxX+"]");
//					suggestionsBuilder.suggest("top-left");
//					suggestionsBuilder.suggest("top-right");
//					suggestionsBuilder.suggest("top-center");
//					suggestionsBuilder.suggest("middle-left");
//					suggestionsBuilder.suggest("middle-right");
//					suggestionsBuilder.suggest("middle-center");
//					suggestionsBuilder.suggest("bottom-left");
//					suggestionsBuilder.suggest("bottom-right");
//					suggestionsBuilder.suggest("bottom-center");
//				}
//				else if(commandContext.equals("dimensionsY"))
//				{
//					DayCounterDisplayClient dcdClient = DayCounterDisplayClient.getInstance();
//					int maxY = dcdClient.dcdRenderer.getScaledWindowHeight();
//					suggestionsBuilder.suggest("[0 - "+maxY+"]");
//				}
//                return suggestionsBuilder.buildFuture();
//            }
//        };
//	}
//}