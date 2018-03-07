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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.hoggmania.client.util.protection.EncryptedInfo;
import org.hoggmania.client.util.protection.ObfuscatorInf;
import org.hoggmania.client.util.protection.utils.*;

import com.migcomponents.migbase64.Base64;


/**
 * Implementation to abstract duplicate work on obfuscation of secrets
 * 
 * @author james.holland
 * 
 */
public abstract class PassphraseObfuscatorImp implements ObfuscatorInf {

	protected Map<String, String>	map				= null;
	protected String				provider		= null;
	protected String				algorithm_hash	= null;
	protected String				algorithm_pbe	= null;
	
	private String	salt, password, entropy, username, userhome;
	private int		count	= 0;
	
	public static final String HASH_DEFAULT = "SHA-512";
	public static final String PBE_DEFAULT = "PBEWithSHA1AndDESede";
	

	/**
	 * @param ALGORITM_PBE
	 *            Standard JCE available PBE algorithms are: - <li>
	 *            PBEWithSHA1AndDESede(default)</li> <li>PBEWithSHA1AndRC2_40</li>
	 *            <li>PBEWithMD5AndDES</li> <li>PBEWithMD5AndTripleDES</li>
	 * @param ALGORITM_HASH
	 *            Standard JCE available PBE algorithms are: - <li>
	 *            SHA-256{default}</li> <li>SHA-256</li> <li>PBEWithMD5AndDES</li>
	 *            <li>PBEWithMD5AndTripleDES</li>
	 * 
	 */
	public void initializeParameters(Map<String, String> map) throws Exception {		
		//This is used to re-initial the obfuscater
		this.provider		= null;
		this.algorithm_hash	= null;
		this.algorithm_pbe	= null;
		this.map = map;
		this.algorithm_pbe = ObfuscateSecretsHelper.getMapValue(map, PWB_ALGORITM_PBE, PBE_DEFAULT, false);
		this.algorithm_hash = ObfuscateSecretsHelper.getMapValue(map, PWB_ALGORITM_HASH, HASH_DEFAULT, false);
		this.provider = ObfuscateSecretsHelper.getMapValue(map, PWB_PROVIDER, null, true);
		this.entropy = ObfuscateSecretsHelper.getMapValue(map, PWB_OVERRIDE_NETWORK_ENTROPY, null, true);
		this.salt = ObfuscateSecretsHelper.getMapValue(map, PWB_OVERRIDE_SALT, null, true);
		this.username = ObfuscateSecretsHelper.getMapValue(map, PWB_OVERRIDE_USERNAME, null, true);
		this.userhome = ObfuscateSecretsHelper.getMapValue(map, PWB_OVERRIDE_USERHOME, null, true);
		this.count = Integer.parseInt(ObfuscateSecretsHelper.getMapValue(map, PWB_OVERRIDE_ITERATION, "0", true));
	}

	
	protected String getEntropy() throws Exception {
		if (entropy == null) {
			entropy = findEntropy();
		}
		return entropy;
	}
	
	abstract String findEntropy() throws Exception;
	
	protected final String generateSalt() throws Exception {
		if (salt == null) {
			MessageDigest md;
			md = getDigest();
			md.update(getEntropy().getBytes());
			md.update(getUsername().getBytes());
			salt = ObfuscateSecretsHelper.stripToASCII(Base64.encodeToString(md.digest(), true));
			while (salt.length() < 8) salt += salt;
			salt = salt.substring(0, 8);
		}
		return salt;
	}


	
	protected final int generateIteration() throws Exception {
		if (count == 0) {
			for (int i = 0; i < getEntropy().length(); i++) {
				count += getEntropy().charAt(i);
			}
		}
		return count;
	}

	
	protected final String generatePassword() throws Exception {
		if (password == null) {
			String uhome = getUserhome();
			if (uhome == null || uhome.trim().length() <= getEntropy().length() / 2)
				uhome = getUsername();
			MessageDigest md;
			md = getDigest();
			md.update(getEntropy().getBytes());
			md.update(uhome.getBytes());
			password = ObfuscateSecretsHelper.stripToASCII(Base64.encodeToString(md.digest(), true));
		}
		return password;
	}



	private String getUsername() {
		if (username == null) username = System.getProperty("user.name");
		return username;
	}
	

	private String getUserhome() {
		if (userhome == null) userhome = System.getProperty("user.home");
		return userhome;
	}



	/**
	 * Retrieves the JCE implementation of the required hashing algorithm and
	 * will reset it.
	 * 
	 * @param map
	 *            parameter required for defining the algorithm
	 * @return MessageDigest to use for encoding
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws CloneNotSupportedException
	 * @throws HashEncoderException
	 */
	protected MessageDigest getDigest() {
		MessageDigest md = null;
		try {
			if (provider == null) {
				md = MessageDigest.getInstance(algorithm_hash);
				//System.out.println("MessageDigest provider is "+md.getProvider().getName());
			} else {
				md =MessageDigest.getInstance(algorithm_hash, provider);
			}
			return md;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param salt
	 * @param iteration
	 * @param password
	 * @param cipher_type
	 * @param algorithm
	 * @return
	 */
	protected final Cipher generateKey(final String salt, final int iteration, final String password,
			final int cipher_type, final String algorithm) {
		Cipher pbeCipher = null;
		try {
			// Create PBE parameter set
			PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt.getBytes(), iteration);
			PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());

			SecretKeyFactory keyFac = null;
			if (provider == null) {
				keyFac = SecretKeyFactory.getInstance(algorithm);
				//System.out.println("PBE provider is "+keyFac.getProvider().getName());
			} else {
				
				keyFac = SecretKeyFactory.getInstance(algorithm, provider);
			}
			// Generate PBE
			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

			// Create PBE Cipher ready for use
			pbeCipher = Cipher.getInstance(algorithm);
			// Initialize PBE Cipher with key and parameters
			pbeCipher.init(cipher_type, pbeKey, pbeParamSpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return pbeCipher;
	}

	/**
	 * Encrypts a secret with machine generated information
	 * 
	 * @param value
	 * @return encrypted with PBE and Base64 encoded
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public final EncryptedInfo encrypt(byte[] value) throws Exception {
		Cipher enc = generateKey(ObfuscateSecretsHelper.stripToASCII(generateSalt()), generateIteration(),
				ObfuscateSecretsHelper.stripToASCII(generatePassword()), Cipher.ENCRYPT_MODE, this.algorithm_pbe);
		return new EncryptedInfo(enc.doFinal(value), this.getClass().getCanonicalName(), this.map);
	}

	/**
	 * Decrypt a secret
	 * 
	 * @param encrypted_value
	 *            in Base64 encoding
	 * @return
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public final byte[] decrypt(final EncryptedInfo encrypted_value) throws Exception {
		// Extract the algorithm used. The format is
		// "{algorithm}encrypted_value"
		if (encrypted_value == null) {
			throw new RuntimeException("Encrypted container is null");
		}
		if (encrypted_value.getBytes().length == 0) {
			throw new RuntimeException("Encrypted value is null");
		}
		//This causes the impl to be reset for the encrypted parameters
		this.initializeParameters(encrypted_value.getParameters());

		//Decrypt the value 
		Cipher enc = generateKey(ObfuscateSecretsHelper.stripToASCII(generateSalt()), generateIteration(),
				ObfuscateSecretsHelper.stripToASCII(generatePassword()), Cipher.DECRYPT_MODE, this.algorithm_pbe);
		return enc.doFinal(encrypted_value.getBytes());
	}

	
	public Map<String, String> getInitializeParameters() {
		return map;
	}
}
