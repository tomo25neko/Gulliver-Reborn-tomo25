package com.tomo25neko.minihume;

import com.artemis.artemislib.compatibilities.sizeCap.ISizeCap;
import com.artemis.artemislib.compatibilities.sizeCap.SizeCapPro;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MiniHume.MODID)
public class MiniHume
{
	public static final String MODID = "minihume";
	public static final String NAME = "Mini Hume";
	public static final String VERSION = "1.0.0";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static File config;

	public static DamageSource causeCrushingDamage(LivingEntity entity)
	{
		return new EntityDamageSource(MODID + ".crushing", entity);
	}

	public MiniHume()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void setup(final FMLCommonSetupEvent event)
	{
		Config.registerConfig(event);
		MinecraftForge.EVENT_BUS.register(new GulliverReborn());
	}

	@EventHandler
	public void serverRegistries(final FMLServerStartingEvent event)
	{
		event.getServer().getCommandManager().registerCommand(new MyResizeCommand());
		event.getServer().getCommandManager().registerCommand(new OthersResizeCommand());
	}

	@SubscribeEvent
	public void onPlayerFall(LivingFallEvent event)
	{
		if (event.getEntityLiving() instanceof Player)
		{
			Player player = (Player) event.getEntityLiving();

			if (Config.SCALED_FALL_DAMAGE) event.setDistance(event.getDistance() / (player.getBbHeight() * 0.6F));
			if (player.getBbHeight() < 0.45F) event.setDistance(0);
		}
	}

	@SubscribeEvent
	public void onLivingTick(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		Level world = event.getEntityLiving().getCommandSenderWorld();

		for (LivingEntity entities : world.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox()))
		{
			if (!entity.isShiftKeyDown() && Config.GIANTS_CRUSH_ENTITIES)
			{
				if (entity.getBbHeight() / entities.getBbHeight() >= 4 && entities.getVehicle() != entity)
				{
					entities.hurt(causeCrushingDamage(entity), entity.getBbHeight() - entities.getBbHeight());
				}
			}
		}
	}

	@SubscribeEvent
	public void onTargetEntity(LivingSetAttackTargetEvent event)
	{
		if (event.getTarget() instanceof Player && event.getEntityLiving() instanceof Mob && Config.SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS)
		{
			Player player = (Player) event.getTarget();
			Mob entity = (Mob) event.getEntityLiving();

			if (!(entity instanceof EntitySpider || entity instanceof EntityOcelot))
			{
				if (player.getBbHeight() <= 0.45F)
				{
					entity.setTarget(null);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		Player player = event.player;
		Level world = event.player.getCommandSenderWorld();

		player.maxUpStep = player.getBbHeight() / 3F;
		player.flyingSpeed *= (player.getBbHeight() / 1.8F);

		if (player.getBbHeight() < 0.9F)
		{
			BlockPos pos = new BlockPos(player.getX(), player.getY(), player.getZ());
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			float ratio = (player.getBbHeight() / 1.8F) / 2;

			if (block instanceof FlowerBlock
					|| state.is(Blocks.TALL_FLOWER.defaultBlockState())
					&& Config.ROSES_HURT)
			{
				player.hurt(DamageSource.CACTUS, 1);
			}

			if (!player.getAbilities().flying
					&& Config.PLANTS_SLOW_SMALL_DOWN
					&& (block instanceof BushBlock)
					|| (block instanceof CarpetBlock)
					|| (block instanceof FlowerBlock)
					|| (block instanceof SugarCaneBlock)
					|| (block instanceof SnowBlock)
					|| (block instanceof CobwebBlock)
					|| (block instanceof SoulSandBlock))
			{
				player.setDeltaMovement(player.getDeltaMovement().multiply(ratio, ratio, ratio));
				if (block instanceof CobwebBlock) player.setDeltaMovement(player.getDeltaMovement().multiply(ratio, 1.0D, ratio));
			}
		}

		if (player.getBbHeight() <= 0.45F)
		{
			Direction facing = player.getDirection();
			BlockPos pos = new BlockPos(player.getX(), player.getY(), player.getZ());
			BlockState state = world.getBlockState(pos.relative(facing));
			Block block = state.getBlock();
			boolean canPass = block.isAir(state, world, pos.relative(facing));

			if (ClimbingHandler.canClimb(player, facing)
					&& Config.CLIMB_SOME_BLOCKS
					&& (block instanceof DirtBlock)
					|| (block instanceof GrassBlock)
					|| (block instanceof MyceliumBlock)
					|| (block instanceof LeavesBlock)
					|| (block instanceof SandBlock)
					|| (block instanceof SoulSandBlock)
					|| (block instanceof ConcretePowderBlock)
					|| (block instanceof FarmBlock)
					|| (block instanceof GrassPathBlock)
					|| (block instanceof GravelBlock)
					|| (block instanceof ClayBlock))
			{
				if (player.horizontalCollision)
				{
					if (!player.isShiftKeyDown())
					{
						player.setDeltaMovement(player.getDeltaMovement().add(0.0D, 0.1D, 0.0D));
					}

					if (player.isShiftKeyDown())
					{
						player.setDeltaMovement(player.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
					}
				}
			}

			for (ItemStack stack : player.getHandSlots())
			{
				if (stack.getItem() == Items.SLIME_BALL || stack.getItem() == Item.byBlock(Blocks.SLIME_BLOCK) && Config.CLIMB_WITH_SLIME)
				{
					if (ClimbingHandler.canClimb(player, facing))
					{
						if (player.horizontalCollision)
						{
							if (!player.isShiftKeyDown())
							{
								player.setDeltaMovement(player.getDeltaMovement().add(0.0D, 0.1D, 0.0D));
							}

							if (player.isShiftKeyDown())
							{
								player.setDeltaMovement(player.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
							}
						}
					}
				}

				if (stack.getItem() == Items.PAPER && Config.GLIDE_WITH_PAPER)
				{
					if (!player.isOnGround())
					{
						player.flyingSpeed = 0.02F * 1.75F;
						player.fallDistance = 0;

						if (player.getDeltaMovement().y < 0D)
						{
							player.setDeltaMovement(player.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
						}

						if (player.isShiftKeyDown())
						{
							player.flyingSpeed *= 3.50F;
						}

						for (double blockY = player.getY(); !player.isShiftKeyDown() &&
								((world.getBlockState(new BlockPos(player.getX(), blockY, player.getZ())).getBlock() == Blocks.AIR) ||
										(world.getBlockState(new BlockPos(player.getX(), blockY, player.getZ())).getBlock() == Blocks.LAVA) ||
										(world.getBlockState(new BlockPos(player.getX(), blockY, player.getZ())).getBlock() == Blocks.WATER)); blockY--)
						{
							player.flyingSpeed *= 0.50F;
							player.setDeltaMovement(player.getDeltaMovement().add(0.0D, 0.25D, 0.0D));
						}
					}
				}
			}
		}

		if (player.getBbHeight() >= 2.25F && Config.GIANTS_BREAK_BLOCKS)
		{
			for (BlockPos pos : BlockPos.betweenClosed(player.getBoundingBox().inflate(0.25D)))
			{
				BlockState state = world.getBlockState(pos);
				Block block = state.getBlock();

				if (!(block instanceof AirBlock))
				{
					world.destroyBlock(pos, true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		ISizeCap cap = SizeCapPro.get(entity);

		if (cap.isResized())
		{
			float f = entity.getBbHeight() / 1.8F;
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, f, 1.0D));
		}
	}

	@SubscribeEvent
	public void onEntityAttacked(AttackEntityEvent event)
	{
		if (event.getTarget() instanceof LivingEntity)
		{
			LivingEntity entity = (LivingEntity) event.getTarget();
			ISizeCap cap = SizeCapPro.get(entity);

			if (cap.isResized())
			{
				if (event.getTarget() instanceof Player)
				{
					Player player = (Player) event.getTarget();
					player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((player.getBbHeight() / 1.8F) * player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
				}
				else
				{
					entity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((entity.getBbHeight() / 1.8F) * entity.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
				}
			}
		}
	}
}