package io.github.thecsdev.tcdcommons.mixin.events;

import static io.github.thecsdev.tcdcommons.TCDCommons.getModID;

import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.thecsdev.tcdcommons.command.argument.PlayerBadgeIdentifierArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

@Mixin(ArgumentTypes.class)
public abstract class MixinArgumentTypes
{
	private static @Shadow Map<Class<?>, ArgumentSerializer<?, ?>> CLASS_MAP;
	
	@Inject(method = "register(Lnet/minecraft/registry/Registry;)Lnet/minecraft/command/argument/serialize/ArgumentSerializer;", at = @At("RETURN"))
	private static void onRegister(
			Registry<ArgumentSerializer<?, ?>> registry,
			CallbackInfoReturnable<ArgumentSerializer<?, ?>> callback)
	{
		//register the player badge command argument type
		final var catId = new Identifier(getModID(), "player_badge_identifier");
		final var clazz = PlayerBadgeIdentifierArgumentType.class;
		final Supplier<PlayerBadgeIdentifierArgumentType> catSupplier = PlayerBadgeIdentifierArgumentType::pbId;
		final var catSerializer = ConstantArgumentSerializer.of(catSupplier);
		
		CLASS_MAP.put(clazz, catSerializer);
		Registry.register(Registries.COMMAND_ARGUMENT_TYPE, catId, catSerializer);
		
		//invoke the event
		//TASK - add a command argument type registration event? maybe?
		//depends on how arguments work and if they're "dynamic" like commands are
	}
}