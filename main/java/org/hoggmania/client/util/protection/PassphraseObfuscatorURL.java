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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

import org.hoggmania.client.util.protection.ObfuscatorInf;
import org.hoggmania.client.util.protection.utils.FindNetworkEntropy;
import org.hoggmania.client.util.protection.utils.ObfuscateSecretsHelper;


/**
 * This class obfuscates a password using a URL to define the entropy. The url can be a file or website and the default is http://www.google.com/
 * 
 * The rest of the parameters are the same as the {@link PassphraseObfuscatorHash}
 * 
 * @author james.holland
 *
 */
public class PassphraseObfuscatorURL extends PassphraseObfuscatorImp implements ObfuscatorInf {


	private String	 url;
	private boolean  doNotgenerateEntropyFile;	
	
	
	public String getDefauktEntropyFile() {
		String defaultEntropyFile = null;
		String packName = new Object(){}.getClass().getPackage().getName();  
		packName = packName.substring(0, packName.indexOf('.', 5));
		defaultEntropyFile = System.getProperty("user.home")+"/"+packName+".entropy";
		return defaultEntropyFile;
	}
	
	@Override
	public void initializeParameters(Map<String, String> map) throws Exception {		
		super.initializeParameters(map);	
		this.url = ObfuscateSecretsHelper.getMapValue(map, PWB_URL, getDefauktEntropyFile(), false);
		this.doNotgenerateEntropyFile = Boolean.parseBoolean(ObfuscateSecretsHelper.getMapValue(map, PWB_DO_NOT_GENERATE_ENTROPY, "false", false));		


		
		
	}
	
	protected final void generateEntropy() {
		
		Writer writer = null;

		try {
		    SecureRandom random = SecureRandom.getInstanceStrong();
		    
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(getDefauktEntropyFile()), "utf-8"));
		    
		    
		    LongStream ls = random.longs(100);
		    
		    PrimitiveIterator.OfLong o = ls.iterator();
		    while(o.hasNext()){
		    		writer.write(Long.toHexString(o.next()));  
		    }  

		    writer.write(FindNetworkEntropy.getNetworkEntropy().replaceAll("-", "").replaceAll(":", ""));
		    ls = random.longs(100);
		    o = ls.iterator();
		    while(o.hasNext()){
		    		writer.write(Long.toHexString(o.next()));  
		    }  

		    		
		    
		} catch (NoSuchAlgorithmException | IOException ex) {
		    // Report
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	
	@Override
	protected final String findEntropy() throws Exception  {
		String result = null;
		try {
			result = readEntropy();	
		} catch (Exception e) {
			
			if (!doNotgenerateEntropyFile) {
				
				System.out.println("No entropy found so auto generating entropy file at "+ getDefauktEntropyFile());
				generateEntropy();
				result = readEntropy();
			} else {
				System.out.println("No entropy found  please create entropy file at "+ getDefauktEntropyFile()+" or define the file location with parameter "+PWB_URL);
				//e.printStackTrace();
			}
			
		}
		return result;
		
	}

	private final String readEntropy() throws Exception {
		String entropy = null;
		try {
			InputStream stream = null;
			if (this.url != null) {
				if (this.url.trim().toLowerCase().startsWith("http"))  {
					URL resource_url = new URL(this.url);
					URLConnection uc = resource_url.openConnection();
					stream = uc.getInputStream();
				} else {
					stream = new FileInputStream(new File(this.url));
				}
			} 
			
			if (stream == null) {				
				throw new Exception("Remote or local entropy file not set");
			}
			

			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String inputLine;	
			while ((inputLine = in.readLine()) != null) {
				if (entropy == null) entropy="";
				entropy +=inputLine;
			}

			in.close();
		} catch (Throwable e) {
		}

		if (entropy == null || entropy.trim().length() == 0)
			throw new Exception("Cannot get local information to generate password");

		return entropy;
	}

}
