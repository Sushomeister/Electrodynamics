package electrodynamics.common.tile;

import electrodynamics.DeferredRegisters;
import electrodynamics.common.tile.generic.GenericTileTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileTankHSLA extends GenericTileTank {

	public static final int CAPACITY = 128000;
	private static String name = "hsla";

	public TileTankHSLA(BlockPos pos, BlockState state) {
		super(DeferredRegisters.TILE_TANKHSLA.get(), CAPACITY, name, pos, state);
	}

}
