package net.cessio.displaydaycounter;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

////ideas
/// add holidays (intermediate)
/// allow players to add their own custom background/overlay (demon difficulty)
/// allow players to count down to a day instead of count up (super easy)
/// add multiple fonts (demon difficulty)
/// add profile saving (probably pretty hard)
/// add automatic loading (pretty easy but after profile saving)

@Environment(EnvType.CLIENT)
public class DayCounterDisplayClient implements ClientModInitializer {

    public final String MOD_ID = "daycounterdisplay";
    private boolean getFirstTickCount = true;
    static DayCounterDisplayClient instance;
    public boolean dayIsOn;
    public boolean hourIsOn;
    public boolean dayHasShadow;
    public boolean hourHasShadow;
    public boolean dayPlayerOverwrite;
    public boolean tickSyncington; //There is no reason for the 'ington' suffix, it's just an in joke I have with my brother
    public long dayPlayerHeadCannon;
    public long hourPlayerHeadCannon;
    private boolean updateDayCooldown = true;
    private boolean updateHourCooldown = true;
    private long hours;
    public int dayColor;
    public int hourColor;
    //public boolean isSprinting = false; unfortunately unusable. I wanted to make a check for whether the server was sprinting and automatically pause the hour count, but it seems the client does not have access to this information.
    public String counterDisplayText;
    public String hourDisplayText;
    public int dayX;
    public int dayY;
    public float dayScale;
    public float hourScale;
    private boolean dayPaused = false;
    private boolean hourPaused = false;
    private boolean dayCountDown;
    private boolean hourCountDown;
    public int hourX;
    public int hourY;
    long dayTime;
    long dayNumber;
    public float tickRate;
    public float ticksWhenJoined;
    public float ticksLeftOver;
    MinecraftClient client = MinecraftClient.getInstance();
    public static DrawContext dcdRenderer;
    public HashMap<String, Integer> wordsToPos = new HashMap<>();

    public void firstTimeInitialize()
    {
        getFirstTickCount = true;
        dayIsOn = true;
        hourIsOn = true;
        dayHasShadow = true;
        hourHasShadow = true;
        dayPlayerOverwrite = false;
        tickSyncington = false;
        hours = 0;
        dayColor = 16777215;
        hourColor = 0xFFFFFF;
        counterDisplayText = "Day: ";
        hourDisplayText = "Hour: ";
        dayX = 10;
        dayY = 10;
        hourX = 10;
        hourY = 25;
        dayScale = 1;
        hourScale = 1;
        dayCountDown = false;
        hourCountDown = false;
        dayNumber = 0;
    }

    @Override
    public void onInitializeClient() {
        // Mark the tick count since the player joined the server
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            getFirstTickCount = true;
            System.out.println("you have joined the game!");
            System.out.println("reading profile...");
            loadProfile();
        });

        // Register a callback to render the HUD
        HudRenderCallback.EVENT.register(this::renderDayInfo);
        HudRenderCallback.EVENT.register(this::renderHourInfo);

        //Mark the percentage of an hour the client has left when leaving a world
        ClientPlayConnectionEvents.DISCONNECT.register((networkHandler, client) -> {
			ticksLeftOver = (client.world.getTime() - ticksWhenJoined) % (20 * 3600);
            System.out.println("Ticks left over: "+ticksLeftOver);
            getFirstTickCount = true;
            updateHourCooldown = true;
            saveProfile();
			System.out.println("Thanks for playing! you left with " + ticksLeftOver / (tickRate * 36) + "% of an hour! ");
			System.out.println("Additionally you were on for: "+(client.world.getTime()-ticksWhenJoined)+" ticks!");
			System.out.println("Ticks when joined: "+ticksWhenJoined);
			System.out.println("World time at leave: "+(client.world.getTime()));
        });

        ClientTickEvents.END_WORLD_TICK.register(clientWorld -> {
            // Mark the tick count since the player joined the server
            if(getFirstTickCount)
            {
                ticksWhenJoined = clientWorld.getTime();
                getFirstTickCount=false;
            }
        });

        // Register '/daycounterdisplay' command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher , registryAccess) -> {
            final CommandNode<FabricClientCommandSource> dayCounterDisplayCommand = dispatcher.register(literal(MOD_ID)
                            .then(
                                    literal("day")
                                            .then(
                                                    ClientCommandManager.argument("day", IntegerArgumentType.integer())
                                                            .suggests(getSuggestionProvider("day"))
                                                            .executes(context -> {
                                                                int day = IntegerArgumentType.getInteger(context, "day");
                                                                dayPlayerHeadCannon = day;
                                                                allFalse();
                                                                dayPlayerOverwrite = true;
                                                                context.getSource().sendFeedback(Text.literal("Day counter is now set to day " + day +"."));
                                                                return 1;
                                                            })
                                            )
                                            .then(
                                                    literal("pause")
                                                            .executes(context -> {
                                                                dayPaused = true;
                                                                context.getSource().sendFeedback(Text.literal("Day counter is now paused. run '/dcd day resume' to resume the count."));
                                                                return 1;
                                                            })
                                            )
                                            .then(
                                                    literal("resume")
                                                            .executes(context -> {
                                                                dayPaused = false;
                                                                context.getSource().sendFeedback(Text.literal("Day counter has resumed its count."));
                                                                return 1;
                                                            })
                                            )
                                            .then(
                                                    literal("sync")
                                                            .then(ClientCommandManager.argument("sync", StringArgumentType.string())
                                                                    .suggests(getSuggestionProvider("sync"))
                                                                    .executes(context -> {
                                                                        String sync = StringArgumentType.getString(context, "sync");
                                                                        MinecraftClient mcClient = MinecraftClient.getInstance();
                                                                        if (sync.contains("sync-server"))
                                                                        {
                                                                            allFalse();
                                                                            context.getSource().sendFeedback(Text.literal("Day counter is in sync with world data. Set to " + mcClient.world.getTimeOfDay()/24000));
                                                                        }
                                                                        else if(sync.contains("sync-real"))
                                                                        {
                                                                            allFalse();
                                                                            tickSyncington = true;
                                                                            context.getSource().sendFeedback(Text.literal("Day counter is in sync with total ticks elapsed. Set to " + mcClient.world.getTime()/24000));
                                                                        }
                                                                        return 1;
                                                                    })
                                                            )
                                            )
                                            .then(
                                                    literal("toggle")
                                                            .then(ClientCommandManager.argument("toggle", StringArgumentType.string())
                                                                    .suggests(getSuggestionProvider("toggle"))
                                                                    .executes(context -> {
                                                                        String toggle = StringArgumentType.getString(context, "toggle");
                                                                        if(!toggle.contains("display") && !toggle.contains("shadow") && !toggle.contains("countdown"))
                                                                        {
                                                                            throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: Provided Argument is unable to be toggled"));
                                                                        }
                                                                        if(toggle.contains("display"))
                                                                        {
                                                                            dayIsOn = !dayIsOn;
                                                                        }
                                                                        else if(toggle.contains("shadow"))
                                                                        {
                                                                            dayHasShadow = !dayHasShadow;
                                                                        }
                                                                        else if(toggle.contains("countdown"))
                                                                        {
                                                                            dayCountDown = !dayCountDown;
                                                                            context.getSource().sendFeedback(Text.literal("Day Countdown set to: "+dayCountDown));
                                                                        }
                                                                        return 1;
                                                                    })
                                                            )
                                            )
                                            .then(
                                                    literal("color")
                                                            .then(ClientCommandManager.argument("color", StringArgumentType.string())
                                                                    .suggests(getSuggestionProvider("color"))
                                                                    .executes( context -> {
                                                                        String color = StringArgumentType.getString(context, "color");
                                                                        if(color.equals("red"))
                                                                        {
                                                                            dayColor = 0xFF0000;
                                                                        }
                                                                        else if(color.contains("blue"))
                                                                        {
                                                                            dayColor = 0x0000FF;
                                                                        }
                                                                        else if(color.contains("green"))
                                                                        {
                                                                            dayColor = 0x00FF00;
                                                                        }
                                                                        else if(color.contains("yellow"))
                                                                        {
                                                                            dayColor = 0xFFFF00;
                                                                        }
                                                                        else if(color.contains("purple"))
                                                                        {
                                                                            dayColor = 0xA020F0;
                                                                        }
                                                                        else if(color.contains("pink"))
                                                                        {
                                                                            dayColor = 0xFFC0CB;
                                                                        }
                                                                        else if(color.equals("white"))
                                                                        {
                                                                            dayColor = 0xFFFFFF;
                                                                        }
                                                                        else{
                                                                            try {
                                                                                dayColor = Integer.decode(color);
                                                                            }
                                                                            catch (Exception e) {
                                                                                throw new IllegalArgumentException();
                                                                            }
                                                                        }
                                                                        return 1;
                                                                    })
                                                            )

                                            )
                                            .then(
                                                    literal("dimensions")
                                                            .then(ClientCommandManager.argument("x", StringArgumentType.string())
                                                                            .suggests(getSuggestionProvider("dimensionsX"))
                                                                            .executes(context -> {
                                                                                String xStr = StringArgumentType.getString(context, "x");
                                                                                int textWidth = client.textRenderer.getWidth(counterDisplayText+dayNumber);
                                                                                try {
                                                                                    dayX = Integer.parseInt(xStr);
                                                                                }
                                                                                catch (Exception e) {
                                                                                    initializeMap(counterDisplayText+dayNumber, "dayY");
                                                                                    switch (xStr) {

                                                                                        case "top" -> dayY = wordsToPos.get(xStr);
                                                                                        case "bottom" -> dayY = wordsToPos.get(xStr)-15;
                                                                                        case "middle" -> dayY = wordsToPos.get(xStr)-7;

                                                                                        case "query" ->
                                                                                                context.getSource().sendFeedback(Text.literal("Day X pos: " + dayX + "\nDay Y pos: " + dayY + "\nsize: " + dayScale + "\ncurrent text length: " + textWidth));
                                                                                        default -> {
                                                                                            throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: position not recognized."));
                                                                                        }
                                                                                    }
                                                                                }
                                                                                return 1;
                                                                            })
                                                                            .then(ClientCommandManager.argument("y", StringArgumentType.string())
                                                                                    .suggests(getSuggestionProvider("dimensionsY"))
                                                                                    .executes(context -> {
                                                                                        String xStr = StringArgumentType.getString(context, "x");
                                                                                        String yStr = StringArgumentType.getString(context, "y");
                                                                                        try {
                                                                                            dayX = Integer.parseInt(xStr);
                                                                                            dayY = Integer.parseInt(yStr);
                                                                                        } catch (Exception e)
                                                                                        {
                                                                                            initializeMap(counterDisplayText+dayNumber, "dayY");
                                                                                            dayY = wordsToPos.get(xStr)-15;
                                                                                            initializeMap(counterDisplayText+dayNumber, "X");
                                                                                            dayX = wordsToPos.get(yStr);
                                                                                            if(!wordsToPos.containsKey(xStr) || !wordsToPos.containsKey(yStr))
                                                                                            {
                                                                                                throw new IllegalArgumentException();
                                                                                            }
                                                                                        }
                                                                                        return 1;
                                                                                    })
                                                                                    .then(ClientCommandManager.argument("scale", StringArgumentType.string())
                                                                                            .executes(context -> {
                                                                                                String xStr = StringArgumentType.getString(context, "x");
                                                                                                String yStr = StringArgumentType.getString(context, "y");
                                                                                                String scaleStr = StringArgumentType.getString(context, "scale");
                                                                                                try {
                                                                                                    dayX = Integer.parseInt(xStr);
                                                                                                    dayY = Integer.parseInt(yStr);

                                                                                                    dayScale = Float.parseFloat(scaleStr);

                                                                                                } catch (Exception e)
                                                                                                {
                                                                                                    try {
                                                                                                        dayScale = Float.parseFloat(scaleStr);
                                                                                                    } catch (Exception f)
                                                                                                    {
                                                                                                        throw new IllegalArgumentException();
                                                                                                    }
                                                                                                    initializeMap(counterDisplayText+dayNumber, "dayY");
                                                                                                    if(!wordsToPos.containsKey(xStr))
                                                                                                    {
                                                                                                        throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: position not recognized."));
                                                                                                    }
                                                                                                    dayY = wordsToPos.get(xStr);
                                                                                                    initializeMap(counterDisplayText+dayNumber, "X");
                                                                                                    if(!wordsToPos.containsKey(yStr))
                                                                                                    {
                                                                                                        throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: position not recognized."));
                                                                                                    }
                                                                                                    dayX = wordsToPos.get(yStr);
                                                                                                }
                                                                                                return 1;
                                                                                            })
                                                                                    )
                                                                            )
                                                            )

                                            )
                                            .then(
                                                    literal("text")
                                                            .then(ClientCommandManager.argument("text", StringArgumentType.string())
                                                                    .suggests(getSuggestionProvider("textOptions"))
                                                                    .executes(context -> {
                                                                        String str = StringArgumentType.getString(context, "text");
                                                                            counterDisplayText = str+" ";
                                                                        return 1;
                                                                    })
                                                            )
                                            )
                            )
                            .then(
                                    literal("hour")
                                            .then(
                                                    ClientCommandManager.argument("hour", IntegerArgumentType.integer())
                                                            .suggests(getSuggestionProvider("hour"))
                                                            .executes(context -> {
                                                                int hour = IntegerArgumentType.getInteger(context, "hour");
                                                                hourPlayerHeadCannon = hour;
                                                                hours += hourPlayerHeadCannon;
                                                                allFalse();
                                                                context.getSource().sendFeedback(Text.literal("Hour counter is now set to hour " + hour +"."));
                                                                return 1;
                                                            })
                                            )
                                            .then(
                                                    literal("pause")
                                                            .executes(context -> {
                                                                hourPaused = true;
                                                                context.getSource().sendFeedback(Text.literal("Hour counter is now paused. run '/dcd hour resume' to resume the count."));
                                                                return 1;
                                                            })
                                            )
                                            .then(
                                                    literal("resume")
                                                            .executes(context -> {
                                                                hourPaused = false;
                                                                context.getSource().sendFeedback(Text.literal("Hour counter has resumed its count."));
                                                                return 1;
                                                            })
                                            )
                                            .then(
                                                    literal("sync")
                                                            .executes(context -> {
                                                                hours -= hourPlayerHeadCannon;
                                                                return 1;
                                                            })

                                            )
                                            .then(
                                                    literal("toggle")
                                                            .then(ClientCommandManager.argument("toggle", StringArgumentType.string())
                                                                    .suggests(getSuggestionProvider("toggle"))
                                                                    .executes(context -> {
                                                                        String toggle = StringArgumentType.getString(context, "toggle");
                                                                        if(!toggle.contains("display") && !toggle.contains("shadow") && !toggle.contains("countdown"))
                                                                        {
                                                                            throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: Provided Argument is unable to be toggled"));
                                                                        }
                                                                        if(toggle.contains("display"))
                                                                        {
                                                                            hourIsOn = !hourIsOn;
                                                                        }
                                                                        else if(toggle.contains("shadow"))
                                                                        {
                                                                            hourHasShadow = !hourHasShadow;
                                                                        }
                                                                        else if(toggle.contains("countdown"))
                                                                        {
                                                                            hourCountDown = !hourCountDown;
                                                                            context.getSource().sendFeedback(Text.literal("Hour Countdown set to: "+hourCountDown));
                                                                        }
                                                                        return 1;
                                                                    })
                                                            )
                                            )
                                            .then(
                                                    literal("color")
                                                            .then(ClientCommandManager.argument("color", StringArgumentType.string())
                                                                    .suggests(getSuggestionProvider("color"))
                                                                    .executes( context -> {
                                                                        String color = StringArgumentType.getString(context, "color");
                                                                        if(color.equals("red"))
                                                                        {
                                                                            hourColor = 0xFF0000;
                                                                        }
                                                                        else if(color.contains("blue"))
                                                                        {
                                                                            hourColor = 0x0000FF;
                                                                        }
                                                                        else if(color.contains("green"))
                                                                        {
                                                                            hourColor = 0x00FF00;
                                                                        }
                                                                        else if(color.contains("yellow"))
                                                                        {
                                                                            hourColor = 0xFFFF00;
                                                                        }
                                                                        else if(color.contains("purple"))
                                                                        {
                                                                            hourColor = 0xA020F0;
                                                                        }
                                                                        else if(color.contains("pink"))
                                                                        {
                                                                            hourColor = 0xFFC0CB;
                                                                        }
                                                                        else if(color.equals("white"))
                                                                        {
                                                                            hourColor = 0xFFFFFF;
                                                                        }
                                                                        else{
                                                                            try {
                                                                                hourColor = Integer.decode(color);
                                                                            }
                                                                            catch (Exception e) {
                                                                                throw new IllegalArgumentException();
                                                                            }
                                                                        }
                                                                        return 1;
                                                                    })
                                                            )

                                            )
                                            .then(
                                                    literal("dimensions")
                                                            .then(ClientCommandManager.argument("x", StringArgumentType.string())
                                                                            .suggests(getSuggestionProvider("dimensionsX"))
                                                                            .executes(context -> {
                                                                                String xStr = StringArgumentType.getString(context, "x");
                                                                                int textWidth = client.textRenderer.getWidth(hourDisplayText+hours);
                                                                                try {
                                                                                    hourX = Integer.parseInt(xStr);
                                                                                }
                                                                                catch (Exception e) {
                                                                                    initializeMap(hourDisplayText+hours, "hourY");
                                                                                    switch (xStr) {
                                                                                        case "top" -> hourY = wordsToPos.get(xStr)+15;
                                                                                        case "bottom" -> hourY = wordsToPos.get(xStr);
                                                                                        case "middle" -> hourY = wordsToPos.get(xStr)+7;
                                                                                        case "query" ->
                                                                                                context.getSource().sendFeedback(Text.literal("Hour X pos: " + hourX + "\nHour Y pos: " + hourY + "\nsize: " + hourScale + "\ncurrent text length: " + textWidth));
                                                                                        default -> {
                                                                                            throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: position not recognized."));
                                                                                        }
                                                                                    }
                                                                                }
                                                                                return 1;
                                                                            })
                                                                            .then(ClientCommandManager.argument("y", StringArgumentType.string())
                                                                                    .suggests(getSuggestionProvider("dimensionsY"))
                                                                                    .executes(context -> {
                                                                                        String xStr = StringArgumentType.getString(context, "x");
                                                                                        String yStr = StringArgumentType.getString(context, "y");
                                                                                        try {
                                                                                            hourX = Integer.parseInt(xStr);
                                                                                            hourY = Integer.parseInt(yStr);
                                                                                        } catch (Exception e)
                                                                                        {
                                                                                            initializeMap(hourDisplayText+hours, "hourY");
                                                                                            if(!wordsToPos.containsKey(xStr))
                                                                                            {
                                                                                                throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: position not recognized."));
                                                                                            }
                                                                                            hourY = wordsToPos.get(xStr);
                                                                                            initializeMap(hourDisplayText+hours, "X");
                                                                                            if(!wordsToPos.containsKey(yStr))
                                                                                            {
                                                                                                throw new CommandSyntaxException(new SimpleCommandExceptionType(Text.literal("")), Text.literal("Argument Exception: position not recognized."));
                                                                                            }
                                                                                            hourX = wordsToPos.get(yStr);
                                                                                        }
                                                                                        return 1;
                                                                                    })
                                                                                    .then(ClientCommandManager.argument("scale", StringArgumentType.string())
                                                                                            .executes(context -> {
                                                                                                String xStr = StringArgumentType.getString(context, "x");
                                                                                                String yStr = StringArgumentType.getString(context, "y");
                                                                                                String scaleStr = StringArgumentType.getString(context, "scale");
                                                                                                try {
                                                                                                    hourX = Integer.parseInt(xStr);
                                                                                                    hourY = Integer.parseInt(yStr);
                                                                                                    hourScale = Float.parseFloat(scaleStr);

                                                                                                } catch (Exception e)
                                                                                                {
                                                                                                    try {
                                                                                                        hourScale = Float.parseFloat(scaleStr);
                                                                                                    } catch (Exception f)
                                                                                                    {
                                                                                                        throw new IllegalArgumentException();
                                                                                                    }
                                                                                                    initializeMap(hourDisplayText+hours, "hourY");
                                                                                                    if(!wordsToPos.containsKey(xStr) || !wordsToPos.containsKey(yStr))
                                                                                                    {
                                                                                                        throw new IllegalArgumentException();
                                                                                                    }
                                                                                                    hourY = wordsToPos.get(xStr);
                                                                                                    initializeMap(hourDisplayText+hours, "X");
                                                                                                    hourX = wordsToPos.get(yStr);
                                                                                                }
                                                                                                return 1;
                                                                                            })
                                                                                    )
                                                                            )
                                                            )

                                            )
                                            .then(
                                                    literal("text")
                                                            .then(ClientCommandManager.argument("text", StringArgumentType.string())
                                                                    .suggests(getSuggestionProvider("textOptions"))
                                                                    .executes(context -> {
                                                                        String str = StringArgumentType.getString(context, "text");
                                                                            hourDisplayText = str+" ";
                                                                        return 1;
                                                                    })
                                                            )
                                            )
                            )
            );
            dispatcher.register(literal("dcd").redirect(dayCounterDisplayCommand));
        });
    }

    private static SuggestionProvider<FabricClientCommandSource> getSuggestionProvider(String commandContext)
    {
        return new SuggestionProvider<FabricClientCommandSource>() {
            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
                switch (commandContext) {
                    case "day" -> suggestionsBuilder.suggest("<day>");
                    case "hour" -> suggestionsBuilder.suggest("<hour>");
                    case "sync" -> {
                        suggestionsBuilder.suggest("sync-server");
                        suggestionsBuilder.suggest("sync-real");
                    }
                    case "color" -> {
                        suggestionsBuilder.suggest("red");
                        suggestionsBuilder.suggest("blue");
                        suggestionsBuilder.suggest("yellow");
                        suggestionsBuilder.suggest("green");
                        suggestionsBuilder.suggest("purple");
                        suggestionsBuilder.suggest("pink");
                    }
                    case "toggle" -> {
                        suggestionsBuilder.suggest("display");
                        suggestionsBuilder.suggest("shadow");
                        suggestionsBuilder.suggest("countdown");
                    }
                    case "textOptions" -> suggestionsBuilder.suggest("\"New Display Text\"");
                    case "dimensionsX" -> {
                        int maxX = dcdRenderer.getScaledWindowWidth();
                        suggestionsBuilder.suggest("[0 - " + maxX + "]");
                        suggestionsBuilder.suggest("top");
                        suggestionsBuilder.suggest("middle");
                        suggestionsBuilder.suggest("bottom");
                        suggestionsBuilder.suggest("query");
                    }
                    case "dimensionsY" -> {
                        int maxY = dcdRenderer.getScaledWindowHeight();
                        suggestionsBuilder.suggest("[0 - " + maxY + "]");
                        suggestionsBuilder.suggest("left");
                        suggestionsBuilder.suggest("center");
                        suggestionsBuilder.suggest("right");
                    }
                }
                return suggestionsBuilder.buildFuture();
            }
        };
    }

    public static DayCounterDisplayClient getInstance()
    {
        return instance;
    }

    public void allFalse()
    {
        dayPlayerOverwrite = false;
        tickSyncington = false;

    }
    public void updateDay()
    {
        if(dayTime%24000 < 50 && !updateDayCooldown && !dayPaused)
        {
            if(dayCountDown)
            {
                dayPlayerHeadCannon--;
            }
            else {
                dayPlayerHeadCannon++;
            }
            updateDayCooldown = true;
        }
        else if(dayTime%24000 > 50 && !dayPaused)
        {
            updateDayCooldown =false;
        }
    }

    public void saveProfile()
    {
        ArrayList<String> dcdConfig = new ArrayList<>();
            dcdConfig.add("dayIsOn:"+ dayIsOn+"\n");
            dcdConfig.add("hourIsOn:"+ hourIsOn+"\n");
            dcdConfig.add("dayHasShadow:"+dayHasShadow+"\n");
            dcdConfig.add("hourHasShadow:"+hourHasShadow+"\n");
            dcdConfig.add("dayPlayerOverwrite:"+dayPlayerOverwrite+"\n");
            dcdConfig.add("ticksSyncingEnabled:"+tickSyncington+"\n");
            dcdConfig.add("dayPlayerHeadCannon:"+dayPlayerHeadCannon+"\n");
            dcdConfig.add("hourPlayerHeadCannon:"+hourPlayerHeadCannon+"\n");
            dcdConfig.add("hours:"+hours+"\n");
            dcdConfig.add("days:"+dayNumber+"\n");
            dcdConfig.add("dayColor:"+dayColor+"\n");
            dcdConfig.add("hourColor:"+hourColor+"\n");
            dcdConfig.add("dayCounterDisplayText:"+counterDisplayText+"\n");
            dcdConfig.add("hourCounterDisplayText:"+hourDisplayText+"\n");
            dcdConfig.add("dayX:"+dayX+"\n");
            dcdConfig.add("dayY:"+dayY+"\n");
            dcdConfig.add("dayScale:"+dayScale+"\n");
            dcdConfig.add("hourX:"+hourX+"\n");
            dcdConfig.add("hourY:"+hourY+"\n");
            dcdConfig.add("hourScale:"+hourScale+"\n");
            dcdConfig.add("dayPaused:"+dayPaused+"\n");
            dcdConfig.add("hourPaused:"+dayPaused+"\n");
            dcdConfig.add("dayCountDown:"+dayCountDown+"\n");
            dcdConfig.add("hourCountDown:"+hourCountDown+"\n");
            saveProfile(dcdConfig);
    }

    public void initializeProfile(String str, String value) {
        switch (str) {
            case "hours" -> {
                hours = Long.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "dayNumber" -> {
                dayNumber = Long.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "hourIsOn" -> {
                hourIsOn = Boolean.valueOf(value);
                System.out.println(str + " initialized with value " + value);
            }
            case "dayIsOn" -> {
                dayIsOn = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "dayHasShadow" -> {
                dayHasShadow = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "hourHasShadow" -> {
                hourHasShadow = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "dayPlayerOverwrite" -> {
                dayPlayerOverwrite = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "ticksSyncingEnabled" -> {
                tickSyncington = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "dayPlayerHeadCannon" -> {
                dayPlayerHeadCannon = Long.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "hourPlayerHeadCannon" -> {
                hourPlayerHeadCannon = Long.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "dayColor" -> {
                dayColor = Integer.valueOf(value); // Assuming dayColor is a String
                System.out.println(str + " initialized");
            }
            case "hourColor" -> {
                hourColor = Integer.valueOf(value); // Assuming hourColor is a String
                System.out.println(str + " initialized");
            }
            case "dayCounterDisplayText" -> {
                counterDisplayText = value; // Assuming counterDisplayText is a String
                System.out.println(str + " initialized with value "+value);
            }
            case "hourCounterDisplayText" -> {
                hourDisplayText = value; // Assuming hourDisplayText is a String
                System.out.println(str + " initialized with value "+value);
            }
            case "dayX" -> {
                dayX = Integer.valueOf(value); // Assuming dayX is a float
                System.out.println(str + " initialized");
            }
            case "dayY" -> {
                dayY = Integer.valueOf(value); // Assuming dayY is a float
                System.out.println(str + " initialized");
            }
            case "dayScale" -> {
                dayScale = Float.valueOf(value); // Assuming dayScale is a float
                System.out.println(str + " initialized");
            }
            case "hourX" -> {
                hourX = Integer.valueOf(value); // Assuming hourX is a float
                System.out.println(str + " initialized");
            }
            case "hourY" -> {
                hourY = Integer.valueOf(value); // Assuming hourY is a float
                System.out.println(str + " initialized");
            }
            case "hourScale" -> {
                hourScale = Float.valueOf(value); // Assuming hourScale is a float
                System.out.println(str + " initialized");
            }
            case "dayPaused" -> {
                dayPaused = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "hourPaused" -> {
                hourPaused = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "dayCountDown" -> {
                dayCountDown = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            case "hourCountDown" -> {
                hourCountDown = Boolean.valueOf(value);
                System.out.println(str + " initialized");
            }
            default -> System.out.println("Unknown key: " + str);
        }
    }

//saveProfile, loadProfile, and Initialize were all written by deepseek.
//sorry

    private static final String MOD_FOLDER = "DayCounterDisplay";
    // Name of the save file
    private static final String SAVE_FILE = "daycounterdisplay-config.txt";
    private static File saveFile;
    //get dcd Client

    public static void saveProfile(ArrayList<String> args) {
        Initialize();


        // Write to the file using BufferedWriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            for(String str: args)
            {
                writer.write(str);
            }
            System.out.println("Progress saved to: " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + e.getMessage());
        }
    }

    public void loadProfile()
    {
        Initialize();
        // Read from the file using BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String str ="";
                String value ="";
                for(int i=0; i<line.length(); i++)
                {
                    if(line.substring(i, i+1).equals(":"))
                    {
                        str = line.substring(0, i);
                        value = line.substring(i+1);
                        break;
                    }

                }
                initializeProfile(str, value);
            }
            System.out.println("Read progress:\n " + content);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + e.getMessage());
            System.out.println("Running First time initialize:");
            firstTimeInitialize();
        }
    }

    public static void Initialize()
    {
        // Get the .minecraft folder path
        String userHome = System.getProperty("user.home");
        Path minecraftPath;

        // Determine the OS and set the .minecraft path accordingly
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            minecraftPath = Paths.get(userHome, "AppData", "Roaming", ".minecraft");
        } else if (os.contains("mac")) {
            minecraftPath = Paths.get(userHome, "Library", "Application Support", "minecraft");
        } else {
            minecraftPath = Paths.get(userHome, ".minecraft");
        }


        // Create the mod-specific folder if it doesn't exist
        Path modFolderPath = minecraftPath.resolve(MOD_FOLDER);
        File modFolder = modFolderPath.toFile();
        if (!modFolder.exists()) {
            if (modFolder.mkdirs()) {
                System.out.println("Created mod folder at: " + modFolderPath);
            } else {
                System.err.println("Failed to create mod folder: " + modFolderPath);
                return;
            }
        }

        // Create the save file path
        saveFile = modFolderPath.resolve(SAVE_FILE).toFile();
    }

    public void initializeMap(String str, String dimensionalVariable)
    {
        wordsToPos.put("top", 10);
        if(dimensionalVariable.equals("dayY")) {
            wordsToPos.put("middle", (dcdRenderer.getScaledWindowHeight()/2)-10);
        }
        else if(dimensionalVariable.equals("hourY"))
        {
            wordsToPos.put("middle", dcdRenderer.getScaledWindowHeight()/2);
        }
        else if(dimensionalVariable.equals("X"))
        {
            wordsToPos.put("middle", (int) (((dcdRenderer.getScaledWindowWidth()/2.000000000) - (client.textRenderer.getWidth(counterDisplayText+dayNumber)))*(1-(0.034955437*(dayScale-1)))));
        }
        else
        {
            throw new IllegalCallerException();
        }
        wordsToPos.put("bottom",(int) ((dcdRenderer.getScaledWindowHeight()*0.885416666667)-(5*(dayScale-1))));
        wordsToPos.put("left", 10);
        wordsToPos.put("center", (dcdRenderer.getScaledWindowWidth()/2 - client.textRenderer.getWidth(str)/2));
        wordsToPos.put("right", (dcdRenderer.getScaledWindowWidth() - (client.textRenderer.getWidth(str)+10)));
    }
    public void updateHour()
    {
        float currentOscillation = client.world.getTime()-ticksWhenJoined;
        if(client.world.getTime()-ticksWhenJoined-ticksLeftOver < 0)
        {
            currentOscillation = ticksLeftOver;
        }
        if(!hourPaused && !getFirstTickCount && currentOscillation%(tickRate*3600.0F) < 50 && !updateHourCooldown) {

            if(hourCountDown)
            {
                hours--;
            }
            else {
                hours++;
            }
                System.out.println("hour added! Tick Delta: "+currentOscillation+" currently dividing by "+tickRate*3600+"!");
            updateHourCooldown = true;
        }
        else if(!hourPaused && !getFirstTickCount && currentOscillation%(tickRate*3600.0F) > 50)
        {
            updateHourCooldown = false;
        }
    }
    private void renderModifiableDayInfo(DrawContext context, String text, int x, int y, float scale, int color, boolean shadow)
    {
        MatrixStack matrix = context.getMatrices();
        matrix.push();
        matrix.scale(scale, scale, 1.0f);
        context.drawText(client.textRenderer, text, (int)(x/scale), (int)(y/scale), color, shadow);
        matrix.pop();
    }
    private void renderDayInfo(DrawContext context, RenderTickCounter tickDelta)
    {
        if(client.world != null)
        {
            tickRate = client.world.getTickManager().getTickRate();
            dayTime = client.world.getTimeOfDay();
            dayNumber = dayTime / 24000;
            if(dayPlayerOverwrite) {

                dayNumber = dayPlayerHeadCannon;
            }
            else if(tickSyncington)
            {
                dayNumber = client.world.getTime()/24000;
            }

            updateDay();
            //Display the day number in the top-left corner
            String dayText = counterDisplayText + dayNumber;
            if(dayIsOn) {
                renderModifiableDayInfo(context, dayText, dayX, dayY, dayScale, dayColor, dayHasShadow);
                //client.textRenderer, Text.literal(dayText), dayX, dayY, dayColor,10, dayHasShadow
            }
        }
        else {
            dayTime = 0;
        }
    }

    private void renderHourInfo(DrawContext context, RenderTickCounter tickDelta) {
        dcdRenderer = context;
        if(client.world != null) {
            updateHour();
            String hourText = hourDisplayText + hours;
            if (hourIsOn) {
                renderModifiableDayInfo(context, hourText, hourX, hourY, hourScale, hourColor, hourHasShadow);
            }
        }
    }

}
