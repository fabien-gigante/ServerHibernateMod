package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.raid.Raid;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Shadow public abstract boolean hasBonusWave();
    @Shadow public abstract int getGroupsSpawned();
    @Shadow @Final private int numGroups;
    
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerBossEvent;setName(Lnet/minecraft/network/chat/Component;)V", ordinal = 3))
    private void redirectSetName(ServerBossEvent raidEvent, Component name) {
        int totalWaves = this.hasBonusWave() ? this.numGroups + 1 : this.numGroups;
        int waveNumber = this.getGroupsSpawned() + 1;
        if (waveNumber <= totalWaves)
            raidEvent.setName(name.copy().append(" - ").append(Component.translatable("event.minecraft.raid.wave", waveNumber, totalWaves)));
        else raidEvent.setName(name);
    }
} 