package com.colorvariants.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Commands for managing block colors.
 */
public class ColorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("color")
                        .requires(source -> source.hasPermission(2))

                        // /color set <pos> <hue> <saturation> <brightness>
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("hue", FloatArgumentType.floatArg(0, 360))
                                                .then(Commands.argument("saturation", FloatArgumentType.floatArg(0, 2))
                                                        .then(Commands
                                                                .argument("brightness",
                                                                        FloatArgumentType.floatArg(0, 2))
                                                                .executes(ColorCommand::setColor))))))

                        // /color remove <pos>
                        .then(Commands.literal("remove")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ColorCommand::removeColor)))

                        // /color clear
                        .then(Commands.literal("clear")
                                .executes(ColorCommand::clearAll))

                        // /color info <pos>
                        .then(Commands.literal("info")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ColorCommand::getInfo)))

                        // /color stats
                        .then(Commands.literal("stats")
                                .executes(ColorCommand::getStats)));
    }

    private static int setColor(CommandContext<CommandSourceStack> ctx) {
        try {
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
            float hue = FloatArgumentType.getFloat(ctx, "hue");
            float sat = FloatArgumentType.getFloat(ctx, "saturation");
            float bright = FloatArgumentType.getFloat(ctx, "brightness");

            ServerLevel level = ctx.getSource().getLevel();
            ColorTransformManager manager = ColorTransformManager.get(level);

            ColorTransform transform = new ColorTransform(hue, sat, bright);
            manager.setTransform(pos, transform);

            ctx.getSource().sendSuccess(
                    () -> Component.literal(String.format(
                            "Applied color to block at %s: H=%.1f S=%.2f B=%.2f",
                            pos.toShortString(), hue, sat, bright)),
                    true);

            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int removeColor(CommandContext<CommandSourceStack> ctx) {
        try {
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
            ServerLevel level = ctx.getSource().getLevel();
            ColorTransformManager manager = ColorTransformManager.get(level);

            manager.removeTransform(pos);

            ctx.getSource().sendSuccess(
                    () -> Component.literal("Removed color from block at " + pos.toShortString()),
                    true);

            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearAll(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ColorTransformManager manager = ColorTransformManager.get(level);

        int count = manager.getColoredBlockCount();
        manager.clearAll();

        int finalCount = count;
        ctx.getSource().sendSuccess(
                () -> Component.literal("Cleared all " + finalCount + " colored blocks"),
                true);

        return 1;
    }

    private static int getInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
            ServerLevel level = ctx.getSource().getLevel();
            ColorTransformManager manager = ColorTransformManager.get(level);

            ColorTransform transform = manager.getTransform(pos);

            if (transform.isNone()) {
                ctx.getSource().sendSuccess(
                        () -> Component.literal("Block at " + pos.toShortString() + " has no color"),
                        false);
            } else {
                ctx.getSource().sendSuccess(
                        () -> Component.literal(String.format(
                                "Block at %s: H=%.1f S=%.2f B=%.2f",
                                pos.toShortString(),
                                transform.getHueShift(),
                                transform.getSaturation(),
                                transform.getBrightness())),
                        false);
            }

            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int getStats(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ColorTransformManager manager = ColorTransformManager.get(level);

        ctx.getSource().sendSuccess(
                () -> Component.literal(manager.getStats()),
                false);

        return 1;
    }
}
