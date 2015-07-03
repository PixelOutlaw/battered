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
package com.kill3rtaco.tacoserialization;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of dyed leather armor. The Red, Green, and Blue values are saved
 * appropriately.
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class LeatherArmorSerialization {
	
	protected LeatherArmorSerialization() {
	}
	
	/**
	 * Serialize LeatherArmorMeta, saving the Color's rgb value.
	 * @param meta The LeatherArmorMeta to serialize
	 * @return The serialized meta information
	 */
	public static JSONObject serializeArmor(LeatherArmorMeta meta) {
		try {
			JSONObject root = new JSONObject();
			root.put("color", ColorSerialization.serializeColor(meta.getColor()));
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serializes the LeatherArmorMeta using serializeArmor() and returns the String form.
	 * @param meta The LeatherArmorMeta to serialized
	 * @return The serialization string
	 */
	public static String serializeArmorAsString(LeatherArmorMeta meta) {
		return serializeArmorAsString(meta, false);
	}
	
	/**
	 * Serializes the LeatherArmorMeta using serializeArmor() and returns the String form.
	 * @param meta The LeatherArmorMeta to serialized
	 * @param pretty Whether the resulting String should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeArmorAsString(LeatherArmorMeta meta, boolean pretty) {
		return serializeArmorAsString(meta, pretty, 5);
	}
	
	/**
	 * Serializes the LeatherArmorMeta using serializeArmor() and returns the String form.
	 * @param meta The LeatherArmorMeta to serialized
	 * @param pretty Whether the resulting String should be 'pretty' or not
	 * @param indentFactor the amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializeArmorAsString(LeatherArmorMeta meta, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeArmor(meta).toString(indentFactor);
			} else {
				return serializeArmor(meta).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets LeatherArmorMeta from the a JSONObject constructed from the given String
	 * @param json The String to use
	 * @return LeatherArmorMeta taken from a JSONObject constructed from the given String
	 */
	public static LeatherArmorMeta getLeatherArmorMeta(String json) {
		try {
			return getLeatherArmorMeta(new JSONObject(json));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets LeatherArmorMeta from the given JSONObject
	 * @param json The JSONObject to decode
	 * @return LeatherArmorMeta taken from the given JSONObject as a reference
	 */
	public static LeatherArmorMeta getLeatherArmorMeta(JSONObject json) {
		try {
			ItemStack dummyItems = new ItemStack(Material.LEATHER_HELMET, 1);
			LeatherArmorMeta meta = (LeatherArmorMeta) dummyItems.getItemMeta();
			if(json.has("color")) {
				meta.setColor(ColorSerialization.getColor(json.getJSONObject("color")));
			}
			return meta;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
