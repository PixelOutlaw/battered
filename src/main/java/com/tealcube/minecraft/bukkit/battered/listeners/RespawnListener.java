/*
 * The MIT License
 * Copyright © 2015 Pixel Outlaw
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
package com.tealcube.minecraft.bukkit.battered.listeners;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class RespawnListener implements Listener {

  private final VersionedSmartYamlConfiguration config;

  public RespawnListener(VersionedSmartYamlConfiguration config) {
    this.config = config;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
    if (config.getStringList("ignored-worlds").contains(event.getPlayer().getWorld().getName())) {
      return;
    }
    final Player player = event.getPlayer();
    PlayerInventory inventory = player.getInventory();
    ItemStack[] contents = inventory.getContents();
    ItemStack[] armorContents = inventory.getArmorContents();

    player.sendMessage(ChatColor.YELLOW + "Your equipment lost some durability from dying!");

    for (int i = 0; i < contents.length; i++) {
      if (contents[i] == null) {
        continue;
      }
      ItemStack itemStack = contents[i].clone();
      if (itemStack.getType() == Material.AIR) {
        continue;
      }
      if (itemStack.getType().getMaxDurability() < 30 || itemStack.getType() == Material.SHEARS) {
        continue;
      }
      short dura = (short) ((0.22 * itemStack.getType().getMaxDurability()) + itemStack.getDurability());
      itemStack.setDurability((short) Math.min(dura, itemStack.getType().getMaxDurability()));
      if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
        sendMessage(player, "&cOne of your tools has dropped below zero durability and was destroyed!");
        contents[i] = null;
        continue;
      }
      if (itemStack.getDurability() > itemStack.getType().getMaxDurability() * 0.75) {
        sendMessage(player, "&eOne of your tools is low on durability and is in danger of breaking!");
      }
      contents[i] = itemStack;
    }

    for (int i = 0; i < armorContents.length; i++) {
      if (armorContents[i] == null) {
        continue;
      }
      ItemStack itemStack = armorContents[i].clone();
      if (itemStack.getType() == Material.AIR) {
        continue;
      }
      if (itemStack.getType().getMaxDurability() < 60) {
        continue;
      }
      short dura = (short) ((0.17 * itemStack.getType().getMaxDurability()) + itemStack.getDurability());
      itemStack.setDurability((short) Math.min(dura, itemStack.getType().getMaxDurability()));
      if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
        sendMessage(player, "&cA piece of your armor has dropped below zero durability and was destroyed!");
        armorContents[i] = null;
        continue;
      }
      if (itemStack.getDurability() > itemStack.getType().getMaxDurability() * 0.75) {
        sendMessage(player, "&eA piece of your armor is low on durability and is in danger of breaking!");
      }
      armorContents[i] = itemStack;
    }

    inventory.setContents(contents);
    inventory.setArmorContents(armorContents);

    player.updateInventory();
  }
}
