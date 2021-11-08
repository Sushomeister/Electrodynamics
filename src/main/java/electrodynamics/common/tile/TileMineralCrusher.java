package electrodynamics.common.tile;

import electrodynamics.DeferredRegisters;
import electrodynamics.SoundRegister;
import electrodynamics.api.electricity.CapabilityElectrodynamic;
import electrodynamics.api.particle.ParticleAPI;
import electrodynamics.api.sound.SoundAPI;
import electrodynamics.common.inventory.container.ContainerO2OProcessor;
import electrodynamics.common.inventory.container.ContainerO2OProcessorDouble;
import electrodynamics.common.inventory.container.ContainerO2OProcessorTriple;
import electrodynamics.common.item.ItemProcessorUpgrade;
import electrodynamics.common.recipe.ElectrodynamicsRecipeInit;
import electrodynamics.common.recipe.categories.o2o.specificmachines.MineralCrusherRecipe;
import electrodynamics.common.settings.Constants;
import electrodynamics.prefab.tile.GenericTileTicking;
import electrodynamics.prefab.tile.components.ComponentType;
import electrodynamics.prefab.tile.components.type.ComponentContainerProvider;
import electrodynamics.prefab.tile.components.type.ComponentDirection;
import electrodynamics.prefab.tile.components.type.ComponentElectrodynamic;
import electrodynamics.prefab.tile.components.type.ComponentInventory;
import electrodynamics.prefab.tile.components.type.ComponentPacketHandler;
import electrodynamics.prefab.tile.components.type.ComponentProcessor;
import electrodynamics.prefab.tile.components.type.ComponentProcessorType;
import electrodynamics.prefab.tile.components.type.ComponentTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileMineralCrusher extends GenericTileTicking {
    public long clientRunningTicks = 0;

    public TileMineralCrusher(BlockPos pos, BlockState state) {
	this(0, pos, state);
    }

    public TileMineralCrusher(int extra, BlockPos pos, BlockState state) {
	super(extra == 1 ? DeferredRegisters.TILE_MINERALCRUSHERDOUBLE.get()
		: extra == 2 ? DeferredRegisters.TILE_MINERALCRUSHERTRIPLE.get() : DeferredRegisters.TILE_MINERALCRUSHER.get(), pos, state);

	addComponent(new ComponentDirection());
	addComponent(new ComponentPacketHandler());
	addComponent(new ComponentTickable().tickClient(this::tickClient));
	addComponent(new ComponentElectrodynamic(this).relativeInput(Direction.NORTH)
		.voltage(CapabilityElectrodynamic.DEFAULT_VOLTAGE * 2 * Math.pow(2, extra)));
	addComponent(new ComponentInventory(this).size(5 + extra * 2)
		.valid((slot, stack) -> slot == 0 || slot == extra * 2 || extra == 2 && slot == 2
			|| slot != extra && slot != extra * 3 && slot != extra * 5 && stack.getItem() instanceof ItemProcessorUpgrade)
		.relativeFaceSlots(Direction.EAST, 0, extra * 2, extra * 4).relativeFaceSlots(Direction.UP, 0, extra * 2, extra * 4)
		.relativeFaceSlots(Direction.WEST, extra, extra * 2 - 1, extra * 3).relativeFaceSlots(Direction.DOWN, extra, extra * 2 - 1, extra * 3)
		.shouldSendInfo());
	addComponent(new ComponentContainerProvider("container.mineralcrusher" + extra).createMenu((id, player) -> (extra == 0
		? new ContainerO2OProcessor(id, player, getComponent(ComponentType.Inventory), getCoordsArray())
		: extra == 1 ? new ContainerO2OProcessorDouble(id, player, getComponent(ComponentType.Inventory), getCoordsArray())
			: extra == 2 ? new ContainerO2OProcessorTriple(id, player, getComponent(ComponentType.Inventory), getCoordsArray()) : null)));

	if (extra == 0) {

	    ComponentProcessor pr = new ComponentProcessor(this).upgradeSlots(2, 3, 4)
		    .canProcess(component -> component.canProcessO2ORecipe(component, MineralCrusherRecipe.class,
			    ElectrodynamicsRecipeInit.MINERAL_CRUSHER_TYPE))
		    .process(component -> component.processO2ORecipe(component, MineralCrusherRecipe.class))
		    .requiredTicks(Constants.MINERALCRUSHER_REQUIRED_TICKS).usage(Constants.MINERALCRUSHER_USAGE_PER_TICK)
		    .type(ComponentProcessorType.ObjectToObject);

	    addProcessor(pr);

	} else {
	    for (int i = 0; i <= extra; i++) {

		ComponentProcessor pr = new ComponentProcessor(this).upgradeSlots(2, 3, 4)
			.canProcess(component -> component.canProcessO2ORecipe(component, MineralCrusherRecipe.class,
				ElectrodynamicsRecipeInit.MINERAL_CRUSHER_TYPE))
			.process(component -> component.processO2ORecipe(component, MineralCrusherRecipe.class))
			.requiredTicks(Constants.MINERALCRUSHER_REQUIRED_TICKS).usage(Constants.MINERALCRUSHER_USAGE_PER_TICK)
			.type(ComponentProcessorType.ObjectToObject);

		addProcessor(pr);

		pr.inputSlot(i * 2);
		pr.outputSlot(i * 2 + 1);
	    }
	}
    }

    protected void tickClient(ComponentTickable tickable) {
	boolean has = getType() == DeferredRegisters.TILE_MINERALCRUSHERDOUBLE.get()
		? getProcessor(0).operatingTicks + getProcessor(1).operatingTicks > 0
		: getType() == DeferredRegisters.TILE_MINERALCRUSHERTRIPLE.get()
			? getProcessor(0).operatingTicks + getProcessor(1).operatingTicks + getProcessor(2).operatingTicks > 0
			: getProcessor(0).operatingTicks > 0;
	if (has) {
	    Direction direction = this.<ComponentDirection>getComponent(ComponentType.Direction).getDirection();
	    if (level.random.nextDouble() < 0.15) {
		double d4 = level.random.nextDouble();
		double d5 = direction.getAxis() == Direction.Axis.X ? direction.getStepX() * (direction.getStepX() == -1 ? 0 : 1) : d4;
		double d6 = level.random.nextDouble();
		double d7 = direction.getAxis() == Direction.Axis.Z ? direction.getStepZ() * (direction.getStepZ() == -1 ? 0 : 1) : d4;
		level.addParticle(ParticleTypes.SMOKE, worldPosition.getX() + d5, worldPosition.getY() + d6, worldPosition.getZ() + d7, 0.0D, 0.0D,
			0.0D);
	    }
	    double progress = Math.sin(0.05 * Math.PI * (clientRunningTicks % 20));
	    if (progress == 1) {
		SoundAPI.playSound(SoundRegister.SOUND_MINERALCRUSHER.get(), SoundSource.BLOCKS, 5, .75f, worldPosition);
	    } else if (progress < 0.3) {
		for (int i = 0; i < 5; i++) {
		    double d4 = level.random.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
		    double d6 = level.random.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
		    level.addParticle(ParticleTypes.SMOKE, worldPosition.getX() + d4 + direction.getStepX() * 0.2, worldPosition.getY() + 0.4,
			    worldPosition.getZ() + d6 + direction.getStepZ() * 0.2, 0.0D, 0.0D, 0.0D);
		}
		int amount = getType() == DeferredRegisters.TILE_MINERALCRUSHERDOUBLE.get() ? 2
			: getType() == DeferredRegisters.TILE_MINERALCRUSHERTRIPLE.get() ? 3 : 0;
		for (int in = 0; in < amount; in++) {
		    ItemStack stack = getProcessor(in).getInput();
		    if (stack.getItem()instanceof BlockItem it) {
			Block block = it.getBlock();
			for (int i = 0; i < 5; i++) {
			    double d4 = level.random.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
			    double d6 = level.random.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
			    ParticleAPI.addGrindedParticle(level, worldPosition.getX() + d4 + direction.getStepX() * 0.2, worldPosition.getY() + 0.4,
				    worldPosition.getZ() + d6 + direction.getStepZ() * 0.2, 0.0D, 0.0D, 0.0D, block.defaultBlockState(),
				    worldPosition);
			}
		    }
		}
	    }
	    clientRunningTicks++;
	}
    }

}
