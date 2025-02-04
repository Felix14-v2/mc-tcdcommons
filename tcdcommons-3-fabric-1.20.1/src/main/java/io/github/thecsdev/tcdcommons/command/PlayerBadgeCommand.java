package io.github.thecsdev.tcdcommons.command;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.thecsdev.tcdcommons.TCDCommons;
import io.github.thecsdev.tcdcommons.api.badge.ServerPlayerBadgeHandler;
import io.github.thecsdev.tcdcommons.command.argument.PlayerBadgeIdentifierArgumentType;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public final class PlayerBadgeCommand
{
	// ==================================================
	private PlayerBadgeCommand() {}
	// ==================================================
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		//# Permission levels:
		//Level 0 - Player
		//Level 1 - Moderator
		//Level 2 - Game-master
		//Level 3 - Administrator
		//Level 4 - Owner aka operator
		
		//register command with permission level 3
		final var config = TCDCommons.getInstance().getConfig();
		dispatcher.register(literal("badge").requires(scs -> config.enablePlayerBadges && scs.hasPermissionLevel(3))
				.then(literal("grant")
						.then(argument("targets", EntityArgumentType.players())
								.then(argument("badge_id", PlayerBadgeIdentifierArgumentType.pbId())
										.executes(context -> execute_grantOrRevoke(context, true)))))
				.then(literal("revoke")
						.then(argument("targets", EntityArgumentType.players())
								.then(argument("badge_id", PlayerBadgeIdentifierArgumentType.pbId())
										.executes(context -> execute_grantOrRevoke(context, false)))))
				.then(literal("list")
						.then(argument("target", EntityArgumentType.player())
								.executes(context -> execute_list(context))))
				.then(literal("clear")
						.then(argument("targets", EntityArgumentType.players())
								.executes(context -> execute_clear(context))))
		);
	}
	// ==================================================
	private static int execute_grantOrRevoke(CommandContext<ServerCommandSource> context, boolean grant)
	{
		try
		{
			//get parameter values
			final var targets = EntityArgumentType.getPlayers(context, "targets");
			final var badgeId = context.getArgument("badge_id", Identifier.class);
			
			//execute
			for(var target : targets)
			{
				//null check
				if(target == null) continue;
				
				//obtain SBH
				final var sbh = ServerPlayerBadgeHandler.getServerBadgeHandler(target);
				
				//grant or revoke
				if(grant)
				{
					if(sbh.getValue(badgeId) < 1)
						sbh.setValue(badgeId, 1);
				}
				else sbh.setValue(badgeId, 0);
			}
			
			//send feedback
			final var feedbackGoR = grant ?
					"commands.badge.grant.one.to_many.success" :
					"commands.badge.revoke.one.to_many.success";
			final var feedback = translatable(feedbackGoR,
					Objects.toString(badgeId),
					Objects.toString(targets.size()));
			context.getSource().sendFeedback(() -> feedback, false);
		}
		catch (CommandException | CommandSyntaxException e) { handleError(context, e); }
		return 1;
	}
	// --------------------------------------------------
	private static int execute_list(CommandContext<ServerCommandSource> context)
	{
		try
		{
			//get parameter values
			final var target = EntityArgumentType.getPlayer(context, "target");
			//get badges
			//final var badges = ServerPlayerBadgeHandler.getBadgeHandler(target).getBadges().stream() - deprecated
			final var badges = StreamSupport.stream(ServerPlayerBadgeHandler.getServerBadgeHandler(target).spliterator(), false)
					.filter(entry -> entry.getIntValue() > 0)
				    .map(entry -> Objects.toString(entry.getKey())) // convert each Identifier object to String
				    .collect(Collectors.joining(", ")); // join with a comma and space
			//send feedback
			final var feedback = translatable("commands.badge.list.of_one",
					target.getDisplayName().getString(),
					badges);
			context.getSource().sendFeedback(() -> feedback, false);
		}
		catch(CommandException | CommandSyntaxException e) { handleError(context, e); }
		return 1;
	}
	// --------------------------------------------------
	private static int execute_clear(CommandContext<ServerCommandSource> context)
	{
		try
		{
			//get parameter values
			final var targets = EntityArgumentType.getPlayers(context, "targets");
			//execute
			for(var target : targets)
			{
				//null check
				if(target == null) continue;
				//clear
				ServerPlayerBadgeHandler.getServerBadgeHandler(target).clearBadges();
				//send feedback
				final var feedback = translatable("commands.badge.clear.of_many",
						Objects.toString(targets.size()));
				context.getSource().sendFeedback(() -> feedback, false);
			}
		}
		catch(CommandException | CommandSyntaxException e) { handleError(context, e); }
		return 1;
	}
	// ==================================================
	private static void handleError(CommandContext<ServerCommandSource> context, Throwable e)
	{
		//handle command syntax errors
		if(e instanceof CommandSyntaxException)
			context.getSource().sendError(
					translatable("command.failed")
					.append(":\n    " + e.getMessage()));
		else if(e instanceof CommandException)
			context.getSource().sendError(
					translatable("command.failed")
					.append(":\n    ")
					.append(((CommandException)e).getTextMessage()));
	}
	// ==================================================
}