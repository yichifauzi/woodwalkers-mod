package tocraft.walkers;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

import tocraft.craftedcore.events.client.ClientTickEvents;
import tocraft.craftedcore.registration.client.KeyMappingRegistry;
import tocraft.walkers.ability.AbilityOverlayRenderer;
import tocraft.walkers.api.model.EntityArms;
import tocraft.walkers.api.model.EntityUpdaters;
import tocraft.walkers.impl.tick.KeyPressHandler;
import tocraft.walkers.network.ClientNetworking;

public class WalkersClient {
	public static final KeyMapping UNLOCK_KEY = new KeyMapping("key.walkers_unlock", InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_U, "key.categories.walkers");
	public static final KeyMapping TRANSFORM_KEY = new KeyMapping("key.walkers", InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_G, "key.categories.walkers");
	public static final KeyMapping ABILITY_KEY = new KeyMapping("key.walkers_ability", InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_R, "key.categories.walkers");
	public static final KeyMapping SPECIAL_TRANSFORM_KEY = new KeyMapping("key.walkers_special", InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_V, "key.categories.walkers");

	public void initialize() {
		KeyMappingRegistry.register(ABILITY_KEY);
		KeyMappingRegistry.register(TRANSFORM_KEY);
		KeyMappingRegistry.register(UNLOCK_KEY);
		if (Walkers.hasSpecialShape(UUIDTypeAdapter.fromString(Minecraft.getInstance().getUser().getUuid())))
			KeyMappingRegistry.register(SPECIAL_TRANSFORM_KEY);

		// Register client-side event handlers
		EntityUpdaters.init();
		AbilityOverlayRenderer.register();
		EntityArms.init();

		// Register event handlers
		ClientTickEvents.CLIENT_PRE.register(new KeyPressHandler());
		ClientNetworking.registerPacketHandlers();
	}
}
