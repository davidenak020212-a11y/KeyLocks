package com.example.keylocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeyLocks implements ModInitializer {
    public static final String MOD_ID = "keylocks";
    private static final String[] COLORS = {"blue", "red", "green", "yellow", "purple", "white", "black", "orange", "cyan", "pink"};
    private static final Map<String, Item> KEYS = new LinkedHashMap<>();
    private static final Map<String, Item> LOCKS = new LinkedHashMap<>();
    private static final LockStore STORE = new LockStore();
    public static Item LOCK_REMOVER;

    private static Identifier id(String path) { return Identifier.of(MOD_ID, path); }
    public static Item key(String color) { return KEYS.get(color); }
    public static Item lock(String color) { return LOCKS.get(color); }
    public static boolean isLocked(World world, BlockPos pos) { return STORE.get(world, normalizedDoorPos(world, pos)) != null; }

    @Override public void onInitialize() {
        for (String color : COLORS) {
            Item key = Registry.register(Registries.ITEM, id(color + "_key"), new Item(new Item.Settings()));
            Item lock = Registry.register(Registries.ITEM, id(color + "_lock"), new Item(new Item.Settings()));
            KEYS.put(color, key); LOCKS.put(color, lock);
        }
        LOCK_REMOVER = Registry.register(Registries.ITEM, id("lock_remover"), new Item(new Item.Settings().maxCount(1)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            for (String color : COLORS) { entries.add(KEYS.get(color)); entries.add(LOCKS.get(color)); }
            entries.add(LOCK_REMOVER);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(STORE::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(STORE::save);
        UseBlockCallback.EVENT.register(KeyLocks::onUseBlock);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        BlockPos clicked = hit.getBlockPos();
        BlockState state = world.getBlockState(clicked);
        if (!(state.getBlock() instanceof DoorBlock)) return ActionResult.PASS;
        BlockPos doorPos = normalizedDoorPos(world, clicked);
        ItemStack held = player.getStackInHand(hand);
        String keyColor = colorOf(KEYS, held.getItem());
        String lockColor = colorOf(LOCKS, held.getItem());
        String installed = STORE.get(world, doorPos);

        if (held.isOf(LOCK_REMOVER) && installed != null) {
            STORE.remove(world, doorPos);
            player.sendMessage(Text.literal("Замок снят!"), true);
            if (!player.getAbilities().creativeMode) held.decrement(1);
            return ActionResult.SUCCESS;
        }
        if (installed != null) {
            if (keyColor == null || !installed.equals(keyColor)) {
                player.sendMessage(Text.literal("Дверь заперта. Нужен " + colorName(installed) + " ключ."), true);
                return ActionResult.FAIL;
            }
            toggleDoor(world, doorPos);
            player.sendMessage(Text.literal("Замок " + colorName(installed) + " открыт/закрыт."), true);
            return ActionResult.SUCCESS;
        }
        if (lockColor != null) {
            STORE.put(world, doorPos, lockColor, player.getUuid().toString());
            setDoorOpen(world, doorPos, false);
            if (!player.getAbilities().creativeMode) held.decrement(1);
            player.sendMessage(Text.literal("Установлен " + colorName(lockColor) + " замок."), true);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static BlockPos normalizedDoorPos(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.contains(DoorBlock.HALF) && state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) return pos.down();
        return pos;
    }
    private static void toggleDoor(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof DoorBlock) || !state.contains(DoorBlock.OPEN)) return;
        setDoorOpen(world, pos, !state.get(DoorBlock.OPEN));
    }
    private static void setDoorOpen(World world, BlockPos pos, boolean open) {
        BlockState lower = world.getBlockState(pos);
        if (!(lower.getBlock() instanceof DoorBlock) || !lower.contains(DoorBlock.OPEN)) return;
        world.setBlockState(pos, lower.with(DoorBlock.OPEN, open), 10);
        BlockPos upperPos = pos.up();
        BlockState upper = world.getBlockState(upperPos);
        if (upper.getBlock() == lower.getBlock() && upper.contains(DoorBlock.OPEN)) world.setBlockState(upperPos, upper.with(DoorBlock.OPEN, open), 10);
    }
    private static String colorOf(Map<String, Item> map, Item item) {
        for (var entry : map.entrySet()) if (entry.getValue() == item) return entry.getKey();
        return null;
    }
    private static String colorName(String color) {
        return switch (color) {
            case "blue" -> "синий"; case "red" -> "красный"; case "green" -> "зелёный";
            case "yellow" -> "жёлтый"; case "purple" -> "фиолетовый"; case "white" -> "белый";
            case "black" -> "чёрный"; case "orange" -> "оранжевый"; case "cyan" -> "бирюзовый";
            case "pink" -> "розовый"; default -> color;
        };
    }
}
