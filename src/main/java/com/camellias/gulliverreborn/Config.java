package com.tomo25neko.minihume;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	public static Configuration config;
	
	public static float MAX_SIZE;
	public static float HEALTH_MULTIPLIER;
	
	public static boolean SPEED_MODIFIER;
	public static boolean REACH_MODIFIER;
	public static boolean STRENGTH_MODIFIER;
	public static boolean HEALTH_MODIFIER;
	public static boolean HARVEST_MODIFIER;
	public static boolean JUMP_MODIFIER;
	
	public static boolean DO_ADJUSTED_RENDER;
	public static boolean PICKUP_SMALL_ENTITIES;
	public static boolean RIDE_BIG_ENTITIES;
	public static boolean CLIMB_SOME_BLOCKS;
	public static boolean CLIMB_WITH_SLIME;
	public static boolean GLIDE_WITH_PAPER;
	public static boolean HOT_BLOCKS_GIVE_LIFT;
	public static boolean ROSES_HURT;
	public static boolean PLANTS_SLOW_SMALL_DOWN;
	public static boolean SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS;
	public static boolean GIANTS_CRUSH_ENTITIES;
	public static boolean SCALED_FALL_DAMAGE;
	
	public static void init(File file)
	{
		config = new Configuration(file);
		
		String category;
		
		category = "config.category.minihume";
		config.addCustomCategoryComment(category, "");
		
		MAX_SIZE = config.getFloat("config.minihume.max_size", category, Float.MAX_VALUE, 1F, Float.MAX_VALUE, config.getString("config.minihume.max_size.tooltip", category, "Max player size"));
		HEALTH_MULTIPLIER = config.getFloat("config.minihume.health_multiplier", category, 1.0F, Float.MIN_VALUE, Float.MAX_VALUE, config.getString("config.minihume.health_multiplier.tooltip", category, "Health Multiplier"));
		DO_ADJUSTED_RENDER = config.getBoolean("config.minihume.do_adjusted_render", category, true, config.getString("config.minihume.do_adjusted_render.tooltip", category, "Enable the re-scaled player render?"));
		PICKUP_SMALL_ENTITIES = config.getBoolean("config.minihume.pickup_small_entities", category, true, config.getString("config.minihume.pickup_small_entities.tooltip", category, "Can players pick up smaller entities?"));
		RIDE_BIG_ENTITIES = config.getBoolean("config.minihume.ride_big_entities", category, true, config.getString("config.minihume.ride_big_entities.tooltip", category, "Can small players ride bigger entities with String?"));
		CLIMB_SOME_BLOCKS = config.getBoolean("config.minihume.climb_some_blocks", category, true, config.getString("config.minihume.climb_some_blocks.tooltip", category, "Are some blocks naturally climbable?"));
		CLIMB_WITH_SLIME = config.getBoolean("config.minihume.climb_with_slime", category, true, config.getString("config.minihume.climb_with_slime.tooltip", category, "Can small players climb with Slimeballs/Slime Blocks?"));
		GLIDE_WITH_PAPER = config.getBoolean("config.minihume.glide_with_paper", category, true, config.getString("config.minihume.glide_with_paper.tooltip", category, "Can small players glide with paper?"));
		HOT_BLOCKS_GIVE_LIFT = config.getBoolean("config.minihume.hot_blocks_give_lift", category, true, config.getString("config.minihume.hot_blocks_give_lift.tooltip", category, "Do hot blocks give lift?"));
		ROSES_HURT = config.getBoolean("config.minihume.roses_hurt", category, true, config.getString("config.minihume.roses_hurt.tooltip", category, "Do Rose Bushes/Poppies hurt small players?"));
		PLANTS_SLOW_SMALL_DOWN = config.getBoolean("config.minihume.plants_slow_small_down", category, true, config.getString("config.minihume.plants_slow_small_down.tooltip", category, "Do small players get slowed by plants?"));
		SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS = config.getBoolean("config.minihume.small_is_invisible_to_noncats_or_nonspiders", category, true, config.getString("config.minihume.small_is_invisible_to_noncats_or_nonspiders.tooltip", category, "Are small players undetected by non-ocelots/non-spiders?"));
		GIANTS_CRUSH_ENTITIES = config.getBoolean("config.minihume.giants_crush_entities", category, true, config.getString("config.minihume.giants_crush_entities.tooltip", category, "Can giants crush small entities?"));
		SCALED_FALL_DAMAGE = config.getBoolean("config.minihume.scaled_fall_damage", category, true, config.getString("config.minihume.scaled_fall_damage.tooltip", category, "Does fall damage scale with size?"));
		SPEED_MODIFIER = config.getBoolean("config.minihume.speed_modifier", category, true, config.getString("config.minihume.speed_modifier.tooltip", category, "Speed changes on resize"));
		REACH_MODIFIER = config.getBoolean("config.minihume.reach_modifier", category, true, config.getString("config.minihume.reach_modifier.tooltip", category, "Reach distance changes on resize"));
		STRENGTH_MODIFIER = config.getBoolean("config.minihume.strength_modifier", category, true, config.getString("config.minihume.strength_modifier.tooltip", category, "Strength changes on resize"));
		HEALTH_MODIFIER = config.getBoolean("config.minihume.health_modifier", category, true, config.getString("config.minihume.health_modifier.tooltip", category, "Health changes on resize"));
		HARVEST_MODIFIER = config.getBoolean("config.minihume.harvest_modifier", category, true, config.getString("config.minihume.harvest_modifier.tooltip", category, "Harvest speed is scaled with size"));
		JUMP_MODIFIER = config.getBoolean("config.minihume.jump_modifier", category, true, config.getString("config.minihume.jump_modifier.tooltip", category, "Jump height is scaled with size"));

		config.save();
	}
	
	public static void registerConfig(FMLPreInitializationEvent event)
	{
		GulliverReborn.config = new File(event.getModConfigurationDirectory() + "/" + GulliverReborn.MODID);
		GulliverReborn.config.mkdirs();
		init(new File(GulliverReborn.config.getPath(), GulliverReborn.MODID + ".cfg"));
	}
}
