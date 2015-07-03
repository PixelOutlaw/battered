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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class Serializer {
	
	/**
	 * Serialize a JSONObject
	 * @param object The object to serialize
	 * @return
	 */
	public static String toString(JSONObject object) {
		return toString(object, true);
	}
	
	/**
	 * Serialize a JSONObject
	 * @param object The object to serialize
	 * @param pretty Whether to add new lines or tabs
	 * @return
	 */
	public static String toString(JSONObject object, boolean pretty) {
		return toString(object, pretty, 5);
	}
	
	/**
	 * Serialize a JSONObject
	 * @param object The object to serialize
	 * @param pretty Whether to add new lines or tabs
	 * @param tabSize The tab size in spaces
	 * @return
	 */
	public static String toString(JSONObject object, boolean pretty, int tabSize) {
		try {
			if(pretty) {
				return object.toString(tabSize);
			} else {
				return object.toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get a JSONObject from a file
	 * @param file The file to use
	 * @return The JSONObject constructed from the text in the file
	 * @throws FileNotFoundException If the file was not found
	 * @throws JSONException If the text in the file does not make a JSONObject
	 */
	public static JSONObject getObjectFromFile(File file) throws FileNotFoundException, JSONException {
		return getObjectFromStream(new FileInputStream(file));
	}
	
	/**
	 * 
	 * @param stream The stream to use
	 * @return The JSONObject constructed from the text from the stream
	 * @throws JSONException If the text from the stream does not make a JSONObject
	 */
	public static JSONObject getObjectFromStream(InputStream stream) throws JSONException {
		return new JSONObject(getStringFromStream(stream));
	}
	
	/**
	 * Get a string from a file
	 * @param file
	 * @return The contents of the given text file
	 * @throws FileNotFoundException
	 */
	public static String getStringFromFile(File file) throws FileNotFoundException {
		return getStringFromStream(new FileInputStream(file));
	}
	
	/**
	 * Get a string from a stream
	 * @param stream The stream to use
	 * @return The string found within the given stream
	 */
	public static String getStringFromStream(InputStream stream) {
		Scanner x = new Scanner(stream);
		String str = "";
		while (x.hasNextLine()) {
			str += x.nextLine() + "\n";
		}
		x.close();
		return str.trim();
	}
	
}
