package com.tomo25neko.minihume;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.artemis.artemislib.util.attributes.ArtemisLibAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class OthersResizeCommand extends Commands
{
	private final List<String> aliases = Lists.newArrayList(MiniHume.MODID, "basesize", "bs");
	private static UUID uuidHeight = UUID.fromString("5440b01a-974f-4495-bb9a-c7c87424bca4");
	private static UUID uuidWidth = UUID.fromString("3949d2ed-b6cc-4330-9c13-98777f48ea51");
	private static UUID uuidReach1 = UUID.fromString("854e0004-c218-406c-a9e2-590f1846d80b");
	private static UUID uuidReach2 = UUID.fromString("216080dc-22d3-4eff-a730-190ec0210d5c");
	private static UUID uuidHealth = UUID.fromString("3b901d47-2d30-495c-be45-f0091c0f6fb2");
	private static UUID uuidStrength = UUID.fromString("558f55be-b277-4091-ae9b-056c7bc96e84");
	private static UUID uuidSpeed = UUID.fromString("f2fb5cda-3fbe-4509-a0af-4fc994e6aeca");

	@Override
	public String getName()
	{
		return "basesize";
	}

	@Override
	public String getUsage(CommandSourceStack sender)
	{
		return "minihume.commands.basesize.usage";
	}

	@Override
	public List<String> getAliases()
	{
		return aliases;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, CommandSourceStack sender)
	{
		return sender.hasPermission(2); // Updated permission check method
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{
		return index == 0;
	}

	@Override
	public List<String> getTabCompletions(CommandSourceStack sender, String[] args, BlockPos targetPos)
	{
		if(args.length == 0)
		{
			return Collections.emptyList();
		}
		else if(isUsernameIndex(args, args.length - 1))
		{
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		}

		return super.getTabCompletions(sender, args, targetPos);
	}

	@Override
	public void execute(MinecraftServer server, CommandSourceStack sender, String[] args) throws CommandException
	{
		if(args.length < 2) return;

		String s = args[1];
		float size;

		try
		{
			size = Float.parseFloat(s);
		}
		catch(NumberFormatException e)
		{
			sender.sendFailure(new TextComponent(ChatFormatting.RED + "Size Invalid"));
			return;
		}

		Player player = getPlayer(server, sender, args[0]);

		size = Mth.clamp(size, 0.125F, Config.MAX_SIZE);
		Multimap<String, AttributeModifier> attributes = HashMultimap.create();
		Multimap<String, AttributeModifier> removeableAttributes = HashMultimap.create();
		Multimap<String, AttributeModifier> removeableAttributes2 = HashMultimap.create();

		attributes.put(ArtemisLibAttributes.ENTITY_HEIGHT.getName(), new AttributeModifier(uuidHeight, "Player Height", size - 1, 2));
		attributes.put(ArtemisLibAttributes.ENTITY_WIDTH.getName(), new AttributeModifier(uuidWidth, "Player Width", Mth.clamp(size - 1, 0.4 - 1, Config.MAX_SIZE), 2));

		if(Config.SPEED_MODIFIER) attributes.put(Attributes.MOVEMENT_SPEED.getDescriptionId(), new AttributeModifier(uuidSpeed, "Player Speed", (size - 1) / 2, 2));
		if(Config.REACH_MODIFIER) removeableAttributes.put(Player.REACH_DISTANCE.getDescriptionId(), new AttributeModifier(uuidReach1, "Player Reach 1", size - 1, 2));
		if(Config.REACH_MODIFIER) removeableAttributes2.put(Player.REACH_DISTANCE.getDescriptionId(), new AttributeModifier(uuidReach2, "Player Reach 2", -Mth.clamp(size - 1, 0.33, Double.MAX_VALUE), 2));
		if(Config.STRENGTH_MODIFIER) attributes.put(Attributes.ATTACK_DAMAGE.getDescriptionId(), new AttributeModifier(uuidStrength, "Player Strength", size - 1, 0));
		if(Config.HEALTH_MODIFIER) attributes.put(Attributes.MAX_HEALTH.getDescriptionId(), new AttributeModifier(uuidHealth, "Player Health", (size - 1) * Config.HEALTH_MULTIPLIER, 2));

		if(size > 1)
		{
			player.getAttributeMap().addTransientAttributeModifiers(removeableAttributes);
		}
		else
		{
			player.getAttributeMap().removeAttributeModifiers(removeableAttributes);
		}

		if(size < 1)
		{
			player.getAttributeMap().addTransientAttributeModifiers(removeableAttributes2);
		}
		else
		{
			player.getAttributeMap().removeAttributeModifiers(removeableAttributes2);
		}

		player.getAttributeMap().addTransientAttributeModifiers(attributes);
		player.setHealth(player.getMaxHealth());

		sender.sendSuccess(new TextComponent(player.getDisplayName().getString() + "'s size set to " + size), true);

		MiniHume.LOGGER.info(sender.getName() + " set " + player.getDisplayName().getString() + "'s size to " + size);
	}
}