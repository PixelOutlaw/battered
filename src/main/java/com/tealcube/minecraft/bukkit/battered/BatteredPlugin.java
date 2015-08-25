/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.battered;

import com.kill3rtaco.tacoserialization.InventorySerialization;
import com.kill3rtaco.tacoserialization.SingleItemSerialization;
import com.tealcube.minecraft.bukkit.config.SmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class BatteredPlugin extends FacePlugin implements Listener {

    private Map<UUID, String> inventoryMap;
    private SmartYamlConfiguration dataFile;
    private Set<UUID> diedRecently;

    @Override
    public void enable() {
        inventoryMap = new HashMap<>();
        diedRecently = new HashSet<>();
        dataFile = new SmartYamlConfiguration(new File(getDataFolder(), "data.yml"));
        dataFile.load();
        for (String s : dataFile.getKeys(false)) {
            UUID uuid = UUID.fromString(s);
            String value = dataFile.getString(s);
            dataFile.set(s, null);
            inventoryMap.put(uuid, value);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void disable() {
        for (Map.Entry<UUID, String> entry : inventoryMap.entrySet()) {
            dataFile.set(entry.getKey().toString(), entry.getValue());
        }
        dataFile.save();
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String items;
        if (!inventoryMap.containsKey(player.getUniqueId())) {
            items = dataFile.getString(player.getUniqueId().toString());
        } else {
            items = inventoryMap.get(player.getUniqueId());
        }
        if (items == null || items.isEmpty()) {
            return;
        }

        InventorySerialization.setPlayerInventory(player, items);

        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] armorContents = inventory.getArmorContents();

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                continue;
            }
            ItemStack itemStack = contents[i].clone();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            itemStack.setDurability((short) ((0.22 * itemStack.getType().getMaxDurability()) + itemStack.getDurability
                    ()));
            if (itemStack.getType().getMaxDurability() > 1 &&
                    itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                contents[i] = null;
                continue;
            }
            contents[i] = itemStack;
        }

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack itemStack = armorContents[i].clone();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            itemStack.setDurability((short) ((0.22 * itemStack.getType().getMaxDurability()) + itemStack.getDurability
                    ()));
            if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                armorContents[i] = null;
                continue;
            }
            armorContents[i] = itemStack;
        }

        inventory.clear();

        inventory.setContents(contents);
        inventory.setArmorContents(armorContents);
        player.updateInventory();

        inventoryMap.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        event.getDrops().clear();
        final Player player = event.getEntity();
        if (diedRecently.contains(player.getUniqueId())) {
            return;
        }
        diedRecently.add(player.getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                diedRecently.remove(player.getUniqueId());
            }
        }, 20L * 2);

        JSONObject invy = new JSONObject();
        JSONArray armor = InventorySerialization.serializeInventory(player.getEquipment().getArmorContents());
        JSONArray contents = new JSONArray();

        Set<ItemStack> drops = new HashSet<>();
        Inventory inventory = player.getInventory();
        for(int i = 0; i < inventory.getContents().length; i++) {
            ItemStack itemStack = inventory.getContents()[i];
            if (itemStack == null) {
                continue;
            }
            if (i >= 0 && i <= 8) {
                if (itemStack.getType() == Material.WOOD_SWORD || itemStack.getType() == Material.WOOD_AXE ||
                        itemStack.getType() == Material.STONE_SWORD || itemStack.getType() == Material.STONE_AXE ||
                        itemStack.getType() == Material.IRON_SWORD || itemStack.getType() == Material.IRON_AXE ||
                        itemStack.getType() == Material.GOLD_SWORD || itemStack.getType() == Material.GOLD_AXE ||
                        itemStack.getType() == Material.DIAMOND_SWORD || itemStack.getType() == Material.DIAMOND_AXE ||
                        itemStack.getType() == Material.BOW) {
                    JSONObject values = SingleItemSerialization.serializeItemInInventory(itemStack, i);
                    if(values != null) {
                        contents.put(values);
                    }

                } else {
                    ItemStack keep = itemStack.clone();
                    ItemStack drop = itemStack.clone();
                    int keepAmount = (int) (itemStack.getAmount() * 0.25);
                    keep.setAmount(keepAmount);
                    drop.setAmount(itemStack.getAmount() - keepAmount);
                    drops.add(drop);
                    JSONObject values = SingleItemSerialization.serializeItemInInventory(keep, i);
                    if(values != null) {
                        contents.put(values);
                    }
                }
            } else {
                drops.add(itemStack);
            }
        }

        try {
            invy.put("inventory", contents).put("armor", armor);
        } catch (JSONException e) {
            getLogger().warning(e.getMessage());
        }
        inventory.clear();
        event.getDrops().addAll(drops);

        inventoryMap.put(player.getUniqueId(), invy.toString());
    }

}
