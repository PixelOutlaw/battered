/*
 * This file is part of Battered, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.battered;

import com.kill3rtaco.tacoserialization.InventorySerialization;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.shade.config.SmartYamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BatteredPlugin extends FacePlugin implements Listener {

    private Map<UUID, String> inventoryMap;
    private SmartYamlConfiguration dataFile;

    @Override
    public void enable() {
        inventoryMap = new HashMap<UUID, String>();
        dataFile = new SmartYamlConfiguration(new File(getDataFolder(), "data.yml"));
        for (String s : dataFile.getKeys(false)) {
            UUID uuid = UUID.fromString(s);
            String value = dataFile.getString(s);
            inventoryMap.put(uuid, value);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void disable() {
        for (Map.Entry<UUID, String> entry : inventoryMap.entrySet()) {
            dataFile.set(entry.getKey().toString(), entry.getValue());
        }
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!inventoryMap.containsKey(player.getUniqueId())) {
            return;
        }
        ItemStack[] itemStacks = InventorySerialization.getInventory(inventoryMap.get(player.getUniqueId()),
                InventoryType.PLAYER.getDefaultSize());
        for (ItemStack itemStack : itemStacks) {
            player.getInventory().addItem(itemStack);
        }
        inventoryMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerInventory playerInventory = player.getInventory();
        int nullChecker = 0;
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack doubleDeathChecker = playerInventory.getItem(i);
            if (doubleDeathChecker == null || doubleDeathChecker.getType() == Material.AIR) {
                nullChecker++;
            }
        }
        if (nullChecker >= playerInventory.getSize()) {
            return;
        }
        List<ItemStack> drops = new ArrayList<ItemStack>();
        List<ItemStack> keeps = new ArrayList<ItemStack>();
        event.getDrops().clear();
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack itemStack = playerInventory.getItem(i);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            // if the index is greater than 8, then it's in the main part of the inventory
            if (i >= 9) {
                drops.add(itemStack);
                continue;
            }

            int amount = itemStack.getAmount();
            int newAmount = (int) (0.25 * amount);
            int droppedAmount = amount - newAmount;
            if (newAmount < 0 && amount > 1) {
                continue;
            }
            if (itemStack.getType() == Material.WOOD_AXE || itemStack.getType() == Material.STONE_AXE ||
                    itemStack.getType() == Material.IRON_AXE || itemStack.getType() == Material.GOLD_AXE ||
                    itemStack.getType() == Material.DIAMOND_AXE || itemStack.getType() == Material.DIAMOND_SWORD ||
                    itemStack.getType() == Material.GOLD_SWORD || itemStack.getType() == Material.IRON_SWORD ||
                    itemStack.getType() == Material.STONE_SWORD || itemStack.getType() == Material.WOOD_SWORD ||
                    itemStack.getType() == Material.BOW) {
                keeps.add(itemStack);
            } else {
                itemStack.setAmount(droppedAmount);
                ItemStack dropItemStack = itemStack.clone();
                dropItemStack.setAmount(Math.max(droppedAmount, 1));
                drops.add(dropItemStack);
                itemStack.setAmount(newAmount);
                keeps.add(itemStack);
            }
        }

        Inventory inventory = Bukkit.createInventory(player, InventoryType.PLAYER);
        for (ItemStack keep : keeps) {
            inventory.addItem(keep);
        }
        String serialized = InventorySerialization.serializeInventoryAsString(inventory);
        inventoryMap.put(player.getUniqueId(), serialized);

        for (ItemStack drop : drops) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

}
