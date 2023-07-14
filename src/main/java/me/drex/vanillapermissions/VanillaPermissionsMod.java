package me.drex.vanillapermissions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import me.drex.vanillapermissions.event.CommandCallback;
import me.drex.vanillapermissions.mixin.CommandNodeAccessor;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

import static me.drex.vanillapermissions.Constants.build;

public class VanillaPermissionsMod implements DedicatedServerModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeServer() {
        //CommandRegistrationCallback.EVENT.addPhaseOrdering(VanillaPermissionsMod.MODIFY_VANILLA_PERMISSIONS_PHASE, new ResourceLocation("fabric", "default"));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> CommandCallback.EVENT.invoker().register(dispatcher));
        CommandCallback.EVENT.register(dispatcher -> {
            for (CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
                alterCommandChildNode(dispatcher, node, node.getRequirement());
            }
            LOGGER.info("Loaded Fabric Permissions");
        });
    }

    @SuppressWarnings("unchecked")
    private void alterCommandChildNode(CommandDispatcher<CommandSourceStack> dispatcher, CommandNode<CommandSourceStack> commandNode, Predicate<CommandSourceStack> fallback) {
        var name = build(dispatcher.getPath(commandNode).toArray(new String[]{}));
        LOGGER.debug("Alter command node {}", name);
        for (CommandNode<CommandSourceStack> child : commandNode.getChildren()) {
            alterCommandChildNode(dispatcher, child, fallback);
        }
        ((CommandNodeAccessor<CommandSourceStack>) commandNode).setRequirement(createPredicate(name, fallback));
    }

    private Predicate<CommandSourceStack> createPredicate(String name, Predicate<CommandSourceStack> fallback) {
        return source -> {
            try {
                TriState triState = Permissions.getPermissionValue(source, Constants.COMMAND.formatted(name));
                return triState.orElseGet(() -> fallback.test(source));
            } catch (Throwable ignored) {
                // Fallback if permission check failed
                return fallback.test(source);
            }
        };
    }

}
