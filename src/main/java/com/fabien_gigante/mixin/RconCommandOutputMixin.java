package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.rcon.RconCommandOutput;
import net.minecraft.text.Text;

@Mixin(RconCommandOutput.class)
public class RconCommandOutputMixin {
	@Redirect(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;getString()Ljava/lang/String;"))
	public String getString(Text message) { return message.getString() + "\n"; }
}
