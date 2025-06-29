package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ObjectIntIdentityMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BlockStateRegistryImpl implements IBlockRegistry {

    private ObjectIntIdentityMap<UBlockStateImpl> map = new ObjectIntIdentityMap<>();
    private UBlockImpl[] byId;



    @Getter
    private PropertyDirection[] directionMap = new PropertyDirection[4096];

    private Set<UBlockState>[] blockTypeStateMapping;
    public void init() {
        byId = new UBlockImpl[4096];
        for (Block block : Block.blockRegistry) {
            byId[Block.getIdFromBlock(block)] = new UBlockImpl(block);
        }

        map = new ObjectIntIdentityMap<>();
        for (IBlockState blockStateId : Block.BLOCK_STATE_IDS) {
            int stateId = Block.getStateId(blockStateId);
            map.put(new UBlockStateImpl(blockStateId, byId[Block.getIdFromBlock(blockStateId.getBlock())], this), stateId);
        }

        directionMap = new PropertyDirection[4096];
        for (Block block : Block.blockRegistry) {
            Optional<PropertyDirection> propertyDirection = block.getDefaultState().getPropertyNames().stream()
                    .filter(a -> a instanceof PropertyDirection)
                    .map(PropertyDirection.class::cast).findFirst();
            directionMap[Block.getIdFromBlock(block)] = propertyDirection.orElse(null);
        }

        setupBlockTypes();
    }

    public UBlock getBlockById(int id) {
        return byId[id];
    }

    public UBlockState getByStateId(int stateId) {
        return map.getByValue(stateId);
    }

    @Override
    public UBlockState fromSerializedSeting(String id) {
        return null;
    }

    @Override
    public UBlockState oneFromWellknown(BlockType blockType) {
        if (blockType == BlockType.AIR) {
            return map.getByValue(0);
        } else if (blockType == BlockType.BEDROCK) {
            return map.getByValue(7 << 4); // bedrock state id.
        } else if (blockType == BlockType.CHEST) {
            return map.getByValue(Block.getStateId(Blocks.chest.getDefaultState()));
        } else if (blockType == BlockType.SKULL){
            return map.getByValue(Block.getStateId(Blocks.skull.getDefaultState()));
        } else if (blockType == BlockType.LEVER) {
            return map.getByValue(Block.getStateId(Blocks.lever.getDefaultState()));
        }
        throw new IllegalArgumentException("Unsupported blocktype: "+blockType);
    }

    @Override
    public UBlockState fromOldId(int stateId) {
        return map.getByValue(stateId);
    }


    @Override
    public Set<UBlockState> getStatesByType(BlockType blockType) {
        return blockTypeStateMapping[blockType.ordinal()];
    }


    private void addAllVariantsOf(Block b, Set<UBlockState> blockStates) {
        for (int i = 0; i < 16; i++) {
            IBlockState blockState = b.getStateFromMeta(i);
            UBlockState blockState1 = getByStateId(Block.getStateId(blockState));
            if (!blockStates.contains(blockState1))
                blockStates.add(blockState1);
        }
    }

    private void setupBlockTypes() {
        blockTypeStateMapping = new Set[BlockType.values().length];
        for (BlockType value : BlockType.values()) {
            Set<UBlockState> b = blockTypeStateMapping[value.ordinal()] = new HashSet<>();
            switch (value) {
                case AIR:
                    addAllVariantsOf(Blocks.air, b);
                    break;
                case CHEST:
                    addAllVariantsOf(Blocks.chest, b);
                    break;
                case TRAP_CHEST:
                    addAllVariantsOf(Blocks.trapped_chest, b);
                    break;
                case LEVER:
                    addAllVariantsOf(Blocks.lever, b);
                    break;
                case SKULL:
                    addAllVariantsOf(Blocks.skull, b);
                    break;
                case STONE:
                    addAllVariantsOf(Blocks.stone, b); // not sure
                    break;
                case CARPET:
                    addAllVariantsOf(Blocks.carpet, b);
                    break;
                case SPONGE:
                    addAllVariantsOf(Blocks.sponge, b);
                    break;
                case BARRIER:
                    addAllVariantsOf(Blocks.barrier, b);
                    break;
                case COAL_BLOCK:
                    addAllVariantsOf(Blocks.coal_block, b);
                    break;
                case PRISMARINE:
                    addAllVariantsOf(Blocks.prismarine, b);
                    break;
                case IRON_BARS:
                    addAllVariantsOf(Blocks.iron_bars, b);
                    break;
                case STONE_SLAB:
                    addAllVariantsOf(Blocks.stone_slab, b);
                    break;
                case SEA_LANTERN:
                    addAllVariantsOf(Blocks.sea_lantern, b);
                    break;
                case STONE_BRICK:
                    addAllVariantsOf(Blocks.stonebrick, b);
                    break;
                case WOOD_PLANKS:
                    addAllVariantsOf(Blocks.planks, b);
                    break;
                case STONE_BUTTON:
                    addAllVariantsOf(Blocks.stone_button, b);
                    break;
                case FLOWING_WATER:
                    addAllVariantsOf(Blocks.flowing_water, b);
                    break;
                case STATIONARY_WATER:
                    addAllVariantsOf(Blocks.water, b);
                    break;
                case HARDENED_CLAY:
                    addAllVariantsOf(Blocks.hardened_clay, b);
                    break;
                case END_PORTAL_FRAME:
                    addAllVariantsOf(Blocks.end_portal_frame, b);
                    break;
                case DOUBLE_STONE_SLAB:
                    addAllVariantsOf(Blocks.double_stone_slab, b);
                    break;
                case STAINED_HARDENED_CLAY:
                    addAllVariantsOf(Blocks.stained_hardened_clay, b);
                    break;
                case BEDROCK:
                    addAllVariantsOf(Blocks.bedrock, b);
                    break;
                case TAG_SLAB:
                    addAllVariantsOf(Blocks.stone_slab, b);
                    addAllVariantsOf(Blocks.double_stone_slab, b);
                    addAllVariantsOf(Blocks.wooden_slab, b);
                    addAllVariantsOf(Blocks.double_wooden_slab, b);
                    addAllVariantsOf(Blocks.stone_slab2, b);
                    addAllVariantsOf(Blocks.double_stone_slab2, b);
                    break;
                case TAG_STAIR:
                    for (Block block : Block.blockRegistry) {
                        if (block instanceof BlockStairs)
                            addAllVariantsOf(block, b);
                    }
                    break;
            }
        }
        for (int i = 0; i < blockTypeStateMapping.length; i++) {
            blockTypeStateMapping[i] = Collections.unmodifiableSet(blockTypeStateMapping[i]);
        }
    }
}
