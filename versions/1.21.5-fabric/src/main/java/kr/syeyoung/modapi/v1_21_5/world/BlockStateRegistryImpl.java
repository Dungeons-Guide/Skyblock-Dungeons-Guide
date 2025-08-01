package kr.syeyoung.modapi.v1_21_5.world;

import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.UBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.collection.IdList;

import java.util.*;

public class BlockStateRegistryImpl implements IBlockRegistry {

    private IdList<UBlockStateImpl> map = new IdList<>();
    private Map<Block, UBlockImpl> blockMap = new HashMap<>();


    private Set<UBlockState>[] blockTypeStateMapping;
    public void init() {
        blockMap = new HashMap<>();
        for (Block block : Registries.BLOCK) {
            blockMap.put(block, new UBlockImpl(block));
        }

        map = new IdList<>();
        for (BlockState blockStateId : Block.STATE_IDS) {
            int stateId = Block.STATE_IDS.getRawId(blockStateId);
            map.set(new UBlockStateImpl(blockStateId, blockMap.get(blockStateId.getBlock()), this), stateId);
        }

        setupBlockTypes();
    }

    public UBlockState getByStateId(int stateId) {
        return map.get(stateId);
    }
    public UBlockState getByState(BlockState state) {
        return map.get(Block.STATE_IDS.getRawId(state));
    }


    @Override
    public UBlockState fromSerializedSeting(String id) {
        return null;
    }

    @Override
    public UBlockState oneFromWellknown(BlockType blockType) {
        if (blockType == BlockType.AIR) {
            return getByState(Blocks.AIR.getDefaultState());
        } else if (blockType == BlockType.BEDROCK) {
            return getByState(Blocks.BEDROCK.getDefaultState());
        } else if (blockType == BlockType.CHEST) {
            return getByState(Blocks.CHEST.getDefaultState());
        } else if (blockType == BlockType.SKULL){
            return getByState(Blocks.PLAYER_HEAD.getDefaultState());
        } else if (blockType == BlockType.LEVER) {
            return getByState(Blocks.LEVER.getDefaultState());
        }
        throw new IllegalArgumentException("Unsupported blocktype: "+blockType);
    }

    @Override
    public UBlockState fromOldId(int stateId) {
        return map.get(stateId);
    }


    @Override
    public Set<UBlockState> getStatesByType(BlockType blockType) {
        return blockTypeStateMapping[blockType.ordinal()];
    }


    private void addAllVariantsOf(Block b, Set<UBlockState> blockStates) {
        for (BlockState state : b.getStateManager().getStates()) {
            UBlockState blockState1 = getByState(state);
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
                    addAllVariantsOf(Blocks.AIR, b);
                    break;
                case CHEST:
                    addAllVariantsOf(Blocks.CHEST, b);
                    break;
                case TRAP_CHEST:
                    addAllVariantsOf(Blocks.TRAPPED_CHEST, b);
                    break;
                case LEVER:
                    addAllVariantsOf(Blocks.LEVER, b);
                    break;
                case SKULL:
                    addAllVariantsOf(Blocks.SKELETON_SKULL, b);
                    addAllVariantsOf(Blocks.SKELETON_WALL_SKULL, b);
                    addAllVariantsOf(Blocks.WITHER_SKELETON_SKULL, b);
                    addAllVariantsOf(Blocks.WITHER_SKELETON_WALL_SKULL, b);
                    addAllVariantsOf(Blocks.PLAYER_HEAD, b);
                    addAllVariantsOf(Blocks.PLAYER_WALL_HEAD, b);
                    break;
                case STONE:
                    addAllVariantsOf(Blocks.STONE, b); // not sure
                    break;
                case CARPET:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_carpet"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case SPONGE:
                    addAllVariantsOf(Blocks.SPONGE, b);
                    addAllVariantsOf(Blocks.WET_SPONGE, b);
                    break;
                case BARRIER:
                    addAllVariantsOf(Blocks.BARRIER, b);
                    break;
                case COAL_BLOCK:
                    addAllVariantsOf(Blocks.COAL_BLOCK, b);
                    break;
                case PRISMARINE:
                    addAllVariantsOf(Blocks.PRISMARINE, b);
                    break;
                case IRON_BARS:
                    addAllVariantsOf(Blocks.IRON_BARS, b);
                    break;
                case STONE_SLAB:
                    addAllVariantsOf(Blocks.SMOOTH_STONE_SLAB, b);
                    addAllVariantsOf(Blocks.STONE_SLAB, b);
                    addAllVariantsOf(Blocks.STONE_BRICK_SLAB, b);
                    break;
                case SEA_LANTERN:
                    addAllVariantsOf(Blocks.SEA_LANTERN, b);
                    break;
                case STONE_BRICK:
                    addAllVariantsOf(Blocks.STONE_BRICKS, b);
                    break;
                case WOOD_PLANKS:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_planks"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case STONE_BUTTON:
                    addAllVariantsOf(Blocks.STONE_BUTTON, b);
                    break;
                case FLOWING_WATER, STATIONARY_WATER:
                    addAllVariantsOf(Blocks.WATER, b);
                    break;
                case HARDENED_CLAY:
                    addAllVariantsOf(Blocks.TERRACOTTA, b);
                    break;
                case END_PORTAL_FRAME:
                    addAllVariantsOf(Blocks.END_PORTAL_FRAME, b);
                    break;
                case DOUBLE_STONE_SLAB:
                    addAllVariantsOf(Blocks.STONE_SLAB, b);
                    break;
                case STAINED_HARDENED_CLAY:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("terracotta"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case BEDROCK:
                    addAllVariantsOf(Blocks.BEDROCK, b);
                    break;
                case TAG_SLAB:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_slab"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case TAG_STAIR:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_stair"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case TAG_WALL:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_wall"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case TAG_FENCE:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_fence"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case WALL_SIGN:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_wall_sign"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case QUARTZ_ORE:
                    addAllVariantsOf(Blocks.NETHER_QUARTZ_ORE, b);
                    break;
                case STANDING_SIGN:
                    for (Map.Entry<RegistryKey<Block>, Block> registryKeyBlockEntry : Registries.BLOCK.getEntrySet()) {
                        if (registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_sign") && !registryKeyBlockEntry.getKey().getValue().getPath().endsWith("_wall_sign"))
                            addAllVariantsOf(registryKeyBlockEntry.getValue(), b);
                    }
                    break;
                case DISPENSER:
                    addAllVariantsOf(Blocks.DISPENSER, b);
                    break;
            }
        }
        for (int i = 0; i < blockTypeStateMapping.length; i++) {
            blockTypeStateMapping[i] = Collections.unmodifiableSet(blockTypeStateMapping[i]);
        }
    }
}
