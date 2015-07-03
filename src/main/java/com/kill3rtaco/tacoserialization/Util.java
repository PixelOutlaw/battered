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

public class Util {

	protected Util() {}
	
	/**
	 * Method used to test whether a string is an Integer or not
	 * @param s The string to test
	 * @return Whether the given string is an Integer
	 */
	public static boolean isNum(String s){
		try{
			Integer.parseInt(s);
			return true;
		} catch(NumberFormatException e){
			return false;
		}
	}
	
	/**
	 * Test 
	 * @param material
	 * @return True if the given material is Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
	 * Material.LEATHER_LEGGINGS, or  Material.LEATHER_BOOTS;
	 */
	public static boolean isLeatherArmor(Material material){
		return material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE || 
				material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS;
	}
	
	public static boolean keyFound(String[] array, String key){
		for(String s : array){
			if(s.equalsIgnoreCase(key));
		}
		return false;
	}

}
