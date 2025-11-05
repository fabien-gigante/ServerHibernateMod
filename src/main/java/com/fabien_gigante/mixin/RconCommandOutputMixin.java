package com.fabien_gigante.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.rcon.RconConsoleSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RconConsoleSource.class)
public class RconCommandOutputMixin {
	@Redirect(method = "sendSystemMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getString()Ljava/lang/String;"))
	public String getString(Component message) { return message.getString() + "\n"; } // Fix bug MC-7569
}
