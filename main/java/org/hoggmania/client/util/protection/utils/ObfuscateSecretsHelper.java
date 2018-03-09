/*
*******************************************************************************
*   Environment Configuration Obfuscation
*   (c) 2018 James Holland
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*******************************************************************************
*/
package org.hoggmania.client.util.protection.utils;


import java.util.Iterator;
import java.util.Map;

import org.hoggmania.client.util.protection.EncryptedInfo;
import org.hoggmania.client.util.protection.ObfuscatePlain;
import org.hoggmania.client.util.protection.ObfuscatorInf;
import org.hoggmania.client.util.protection.PassphraseObfuscatorFactory;

import com.migcomponents.migbase64.Base64;


/**
 * Helper class for obfuscation
 * 
 * @author james.holland
 * 
 */
public class ObfuscateSecretsHelper {

	public static EncryptedInfo parseObfuscatedSecret(final String encodedSecret) throws Exception {
		return parseObfuscatedSecret(encodedSecret, true);
	}
		
	/**
	 * Takes a encrypted String and converts to EncryptedInfo object used for
	 * decryption {@link writeSecret} for format of this string
	 * 
	 * @param encodedSecret
	 *            Base64 encode String in format {code:comma delimited parameters}encrypted_value or {code}encrypted_value for no parameters
	 * @return EncryptedInfo used for decryption
	 * @throws IOException
	 */
	public static EncryptedInfo parseObfuscatedSecret(final String encodedSecret, boolean allowPlainTxtPassthrough) throws Exception {
		// Extract the algorithm used. The format is
		// "{algorithm}encrypted_value"
		String encrypted_value = null, def = null, code = null, params = null;
		String value = encodedSecret.replace(" ", "");
		Map<String,String> map = null;
		int beginIndex;
		if (value != null) {
			beginIndex = value.indexOf("}");
			if (beginIndex < 0) {
				if (allowPlainTxtPassthrough) {
					return new EncryptedInfo(encodedSecret.getBytes(), ObfuscatePlain.class.getCanonicalName(), null);
				} else {
					throw new RuntimeException("Encrypted value does not contain the {code:comma delimited parameters}encrypted_value or {code}encrypted_value for no parameters");
				}
			}
			
			encrypted_value = value.substring(beginIndex + 1);
			def = value.substring(1, beginIndex);
			
			beginIndex = def.indexOf(":"); 
			if (beginIndex < 0) {
				//No parameters
				code = def;
			} else {
				code = def.substring(0, beginIndex);
				params = new String(Base64.decode(def.substring(beginIndex + 1)), "UTF-8");
				
				String[] p = params.split(",");
				map = new java.util.HashMap<String, String>();
				for (int i = 0; i < p.length; i++) {
					String[] item = p[i].split("=");
					map.put(item[0], item[1]);
				}
			}
		} else {
			encrypted_value = value;
		}

		return new EncryptedInfo( Base64.decode(encrypted_value), code, map);
	}

	/**
	 * Use to convert the encrypted data into a writable string in the format: -<br>
	 * <code>{obfuscator.code:obfuscator override parameters(name/value pairs comma delimited)}obfuscated value</code><br>
	 * An example of this format is: -<br>
	 * {HASH_V1:PWB_ALGORITM_HASH=SHA-256,PWB_ALGORITM_PBE=PBEWithSHA1AndDESede}Jc4cMY6GrnjX5Vjyh/faWlAuqNR0vE5hchAiYnrf7AoRsmVphrnrjg==
	 * 
	 * @param value
	 *            EncryptedInfo which contains the encrypted information
	 * @return Base64 encoded representation of an encrypted value
	 */
	public static String writeObfuscatedSecret(EncryptedInfo value) {

		String def = "";
		String params = "";
		Map<String,String> map = value.getParameters();
		if (!(map == null || map.isEmpty())) {
			for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				if (!key.equals(ObfuscatorInf.PWB_IMPLEMENTATION)) params = params.concat(key+"="+map.get(key)+",");	
			}
		}
		if (params.trim().length() == 0) {
			def = "{" + value.getCode() + "}";
		} else {
			//Remove the end comma or colon
			params = params.substring(0, params.length()-1);
			def = "{" + value.getCode() + ":"+ Base64.encodeToString(params.getBytes(), true) +"}";
		}
		
		def = def + Base64.encodeToString(value.getBytes(), true);
		return def.replace(" ", "");
	}

	/**
	 * Extracts the system options (-D) available for obfuscation. These are: -
	 * <li>PWB_ALGORITM_PBE</li> <li>PWB_ALGORITM_HASH</li> <li>PWB_PROVIDER</li>
	 * <li>PWB_IMPLEMENTATION</li>
	 * 
	 * <br>
	 * Warning, these options must be used when decrypting the value as well.
	 * Example: -<BR>
	 * <code>java -DPWB_PROVIDER=BC xxxxxx</code>
	 * 
	 * @param args
	 * @return
	 */
	public static Map<String, String> extractSystemOptions() {
		// Get any system options for the hashing or provider
		Map<String, String> map = null;
		String PWB_ALGORITM_PBE = System.getProperty(ObfuscatorInf.PWB_ALGORITM_PBE);
		String PWB_ALGORITM_HASH = System.getProperty(ObfuscatorInf.PWB_ALGORITM_HASH);
		String PWB_PROVIDER = System.getProperty(ObfuscatorInf.PWB_PROVIDER);

		if (PWB_ALGORITM_PBE != null || PWB_ALGORITM_HASH != null || PWB_PROVIDER != null) {
			map = new java.util.HashMap<String, String>();
			if (PWB_ALGORITM_PBE != null)
				map.put(ObfuscatorInf.PWB_ALGORITM_PBE, PWB_ALGORITM_PBE);
			if (PWB_ALGORITM_HASH != null)
				map.put(ObfuscatorInf.PWB_ALGORITM_HASH, PWB_ALGORITM_HASH);
			if (PWB_PROVIDER != null)
				map.put(ObfuscatorInf.PWB_PROVIDER, PWB_PROVIDER);
		}
		return map;
	}

	/**
	 * Used to strip a string to ascii charaters only so a-z, A-Z and 0-9
	 * 
	 * @param input
	 * @return ascii string
	 */
	public static String stripToASCII(String input) {
		return input == null ? "" : input.replaceAll("[^0-9a-fA-F]", "");
	}

	/**
	 * Retrieves the value from the map, and if not found will return the
	 * default
	 * 
	 * @param map
	 *            containing the key/value pairs
	 * @param key
	 *            to lookup in the map
	 * @param defaultValue
	 *            to use if the map contains no valid value for the key
	 * @param isNullAllowed
	 *            to define to throw error is no valid value found to return
	 * @return value from map
	 */
	public static String getMapValue(Map<String, String> map, String key, String defaultValue, boolean isNullAllowed) {
		String result = null;
		if (map == null)
			return defaultValue;
		String value = map.get(key);
		if ((value == null) || (value.trim().length() == 0)) {
			if ((defaultValue == null) || (defaultValue.trim().length() == 0)) {
				result = null;
			} else {
				result = defaultValue;
			}
		} else {
			result = value.trim();
		}
		if (result == null && !isNullAllowed)
			throw new RuntimeException("REQUIRES_MISSING_PARAMETER=" + key);

		return result;

	}

	/**
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public static String deObfuscate(EncryptedInfo secret) throws Exception {
		ObfuscatorInf pg = PassphraseObfuscatorFactory.getAdapter(secret);
		return new String(pg.decrypt(secret));
	}

	/**
	 * 
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public static String parseObfuscatedSecretAndDecrypt(String secret) throws Exception {
		return ObfuscateSecretsHelper.deObfuscate(ObfuscateSecretsHelper.parseObfuscatedSecret(secret, true));
	}
	
	/**
	 * 
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public static EncryptedInfo obfuscate(String plainText, Map<String,String> map) throws Exception {
		if (plainText == null) return null;
		Map<String, String> imap = map;
		if (imap == null) imap = extractSystemOptions();
		ObfuscatorInf pg = PassphraseObfuscatorFactory.getAdapter(imap);
		return pg.encrypt(plainText.getBytes());
	}
	
	/**
	 * 
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public static String obfuscatedSecretAndWrite(String plainText, Map<String,String> map) throws Exception {
		return writeObfuscatedSecret(obfuscate(plainText, map));
	}
	

}
