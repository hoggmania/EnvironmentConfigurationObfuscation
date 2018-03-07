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
package org.hoggmania.client.util.protection;

import java.util.Map;


/**
 * Factory and utility class to encrypt/decrypt a secret
 * 
 * @author james.holland
 */
public class PassphraseObfuscatorFactory {

	/**
	 * Retrieves, instantiates the adapter instance. If no
	 * implementation class is specified in the map the following selection
	 * process occurs: -<br>
	 * <li>If OS is Windows then the DPAPI implementation
	 * (PassphraseObfuscatorDPAPI) will be used</li> <li>If OS is not Windows
	 * then the Hash implementation (PassphraseObfuscatorHash) will be used</li>
	 * 
	 * @param map
	 *            contains details on how to retrieve the adapter
	 * @return instance of a PassphraseObfuscatorInf implementation
	 */
	public static final ObfuscatorInf getAdapter(Map<String, String> map) throws Exception {
		ObfuscatorInf inf = null;
		String tmp = null;
		String clazz = null;
		if (map != null) {
			tmp = map.get(ObfuscatorInf.PWB_IMPLEMENTATION);
			if (tmp != null && tmp.trim().length() != 0)
				clazz = tmp.trim();
		}
		if (clazz != null && clazz.length() > 0) {
			try {
				Class<?> callbackClass = Class.forName(clazz);
				inf = (ObfuscatorInf) callbackClass.newInstance();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			throw new Exception("No recognisable ["+tmp+"] ObfuscatorInf defined in map entry "+ ObfuscatorInf.PWB_IMPLEMENTATION);
		}
		inf.initializeParameters(map);
		return inf;
	}
	
	
	/**
	 * Retrieves, instantiates the adapter instance. If no
	 * implementation class is specified in the map the following selection
	 * process occurs: -<br>
	 * <li>If OS is Windows then the DPAPI implementation
	 * (PassphraseObfuscatorDPAPI) will be used</li> <li>If OS is not Windows
	 * then the Hash implementation (PassphraseObfuscatorHash) will be used</li>
	 * 
	 * @param map
	 *            contains details on how to retrieve the adapter
	 * @return instance of a PassphraseObfuscatorInf implementation
	 */
	public static final ObfuscatorInf getAdapter(EncryptedInfo info) throws Exception {
		if (info == null) throw new RuntimeException("EncryptedInfo is null");
		//If no code set use the info parameters to get the impl
		if(info.getCode() == null || info.getCode().trim().length() ==0) {
			return getAdapter(info.getParameters());
		}
		String clazz = info.getCode().trim();

		
		@SuppressWarnings("unchecked")
		Class<ObfuscatorInf> obj=(Class<ObfuscatorInf>) Class.forName(clazz);
		
		ObfuscatorInf inf = obj.newInstance();

		inf.initializeParameters(info.getParameters());	
		return inf;
	}


}
