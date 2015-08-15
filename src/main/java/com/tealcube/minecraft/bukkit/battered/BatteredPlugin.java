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

import com.google.common.collect.Sets;
import com.kill3rtaco.tacoserialization.InventorySerialization;
import com.kill3rtaco.tacoserialization.SingleItemSerialization;
import com.tealcube.minecraft.bukkit.config.SmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.hilt.HiltItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
            inventoryMap.put(uuid, value);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void disable() {
        for (String key : dataFile.getKeys(true)) {
            dataFile.set(key, null);
        }
        for (Map.Entry<UUID, String> entry : inventoryMap.entrySet()) {
            dataFile.set(entry.getKey().toString(), entry.getValue());
        }
        dataFile.save();
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!diedRecently.contains(player.getUniqueId())) {
            return;
        }
        diedRecently.remove(player.getUniqueId());
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
            ItemStack itemStack = contents[i].clone();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            itemStack.setDurability((short) (0.75 * itemStack.getDurability()));
            if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                contents[i] = null;
                continue;
            }
            itemStack.setAmount(Math.max(1, (int) (itemStack.getAmount() * 0.75)));
            contents[i] = itemStack;
        }

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack itemStack = armorContents[i].clone();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            itemStack.setDurability((short) (0.75 * itemStack.getDurability()));
            if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
                armorContents[i] = null;
                continue;
            }
            itemStack.setAmount(Math.max(1, (int) (itemStack.getAmount() * 0.75)));
            armorContents[i] = itemStack;
        }

        inventory.setContents(contents);
        inventory.setArmorContents(armorContents);
        player.updateInventory();

        inventoryMap.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
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
        }, 20L * 5);
        event.getDrops().clear();
        inventoryMap.put(player.getUniqueId(), InventorySerialization.serializePlayerInventoryAsString(player.getInventory()));
    }

}
