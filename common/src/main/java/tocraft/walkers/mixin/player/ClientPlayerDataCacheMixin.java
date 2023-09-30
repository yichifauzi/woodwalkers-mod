package tocraft.walkers.mixin.player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import tocraft.walkers.impl.PlayerDataProvider;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public class ClientPlayerDataCacheMixin {

	@Shadow
	@Final
	private Minecraft minecraft;
	@Unique
	private PlayerDataProvider dataCache = null;

	// This inject caches the custom data attached to this client's player before it
	// is reset when changing dimensions.
	// For example, switching from The End => Overworld will reset the player's NBT.
	@Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;createPlayer(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/stats/StatsCounter;Lnet/minecraft/client/ClientRecipeBook;ZZ)Lnet/minecraft/client/player/LocalPlayer;"))
	private void beforePlayerReset(ClientboundRespawnPacket packet, CallbackInfo ci) {
		dataCache = ((PlayerDataProvider) minecraft.player);
	}

	// This inject applies data cached from the previous inject.
	// Re-applying on the client will help to prevent sync blips which occur when
	// wiping data and waiting for the server to send a sync packet.
	@Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;", ordinal = 1))
	private void afterPlayerReset(ClientboundRespawnPacket packet, CallbackInfo ci) {
		if (dataCache != null && minecraft.player != null) {
			((PlayerDataProvider) minecraft.player).setCurrentShape(dataCache.getCurrentShape());
			((PlayerDataProvider) minecraft.player).set2ndShape(dataCache.get2ndShape());
			((PlayerDataProvider) minecraft.player).setAbilityCooldown(dataCache.getAbilityCooldown());
			((PlayerDataProvider) minecraft.player).setRemainingHostilityTime(dataCache.getRemainingHostilityTime());
		}

		dataCache = null;
	}
}
