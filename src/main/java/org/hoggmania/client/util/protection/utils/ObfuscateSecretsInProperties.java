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

import java.io.*;
import java.util.Map;
import java.util.Properties;

import org.hoggmania.client.util.protection.EncryptedInfo;
import org.hoggmania.client.util.protection.ObfuscatorInf;
import org.hoggmania.client.util.protection.PassphraseObfuscatorFactory;

/**
 * Utility class to obfuscate secrets within a properties file
 * 
 * @author james.holland
 * 
 */
public class ObfuscateSecretsInProperties {
	
	private static final int FILE_MAX_SIZE = 150000;

	/**
	 * This utility is to obfuscate secrets within a properties file. <br>
	 * System options (-D) available are: - <li>PWB_ALGORITM_PBE</li> <li>
	 * PWB_ALGORITM_HASH</li> <li>PWB_PROVIDER</li> <li>PWB_IMPLEMENTATION</li> <br>
	 * Warning, these options must be used when decrypting the value as well.
	 * Example: -<BR>
	 * To obfuscate entries in a file<br>
	 * <code>java -DPWB_PROVIDER=BC ObfuscateSecretsInProperties password01 true HSM_PIN,JCE_PASSWORD,EMAIL_SERVER_PWD</code>
	 * To de-obfuscate entries in a file<br>
	 * <code>java -DPWB_PROVIDER=BC ObfuscateSecretsInProperties password01 false HSM_PIN,JCE_PASSWORD,EMAIL_SERVER_PWD</code>
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	  {
	    try
	    {
	      if (args.length != 4) {
	        usage();
	        System.exit(0);
	      }
	      String file = args[0];
	      
	      boolean obfuscate = Boolean.parseBoolean(args[1]);
	      
	      boolean commaSeparated = Boolean.parseBoolean(args[2]);
	      
	      String[] keys = args[3].split(",");
	      


	      Map<String, String> map = ObfuscateSecretsHelper.extractSystemOptions();
	      

	      if (obfuscate) {
	        obfuscateFile(file, keys, map, commaSeparated);
	      } else {
	        deObfuscateFile(file, keys, map, commaSeparated);
	      }
	      
	      System.out.println("File obfuscate.");
	    }
	    catch (Exception e) {
	      System.err.println(e.getMessage());
	    }
	  }
	  
	  private static void usage() {
	    System.out.println("Usage <property file> <true/false true=obfuscate false=de-ofuscate> <true/false true=comma-separated-values false=single value> <comma delimited keys to obfuscate (no spaces)> \n System options (-D) are <PWB_ALGORITM_PBE> <PWB_ALGORITM_HASH> <PWB_PROVIDER> <PWB_IMPLEMENTATION>");
	  }
	  
	  
	/**
	 * Obfuscate a files properties with the defined keys. The map passed
	 * dictates which obfuscater is used
	 * 
	 * @param filename
	 * @param propertyKeysToObfuscate
	 * @param map
	 * @param obfuscate
	 * @throws Exception
	 */

	  private static void processFile(String filename, String[] propertyKeysToObfuscate, Map<String, String> map, boolean obfuscate, boolean commaSeperated)
	    throws Exception
	  {
	    File propFile = new File(filename);
	    FileInputStream fin = new FileInputStream(propFile);
	    Properties props = new Properties();
	    props.load(fin);
	    fin.close();
	    
	    Properties result;
	    if (obfuscate) {
	      result = obfuscateFile(props, propertyKeysToObfuscate, map, commaSeperated);
	    } else {
	      result = deObfuscateFile(props, propertyKeysToObfuscate, map, commaSeperated);
	    }
	    



	    StringWriter fout = new StringWriter();
	    










	    BufferedReader bufReader = new BufferedReader(new FileReader(propFile));
	    
	    char[] buffer = new char[FILE_MAX_SIZE];
	    bufReader.read(buffer, 0, FILE_MAX_SIZE);
	    String readPropsFile = new String(buffer).trim();
	    String[] readPropsFiles = readPropsFile.split("\n\r|\r\n|\n|\r");
	    int rowCount = 0;
	    
	    String line = readPropsFiles[rowCount];
	    line.trim();
	    

	    do
	    {
	      for (int i = 0; i < propertyKeysToObfuscate.length; i++) {
	        if (line.startsWith(propertyKeysToObfuscate[i] + "=")) {
	          line = propertyKeysToObfuscate[i] + "=" + result.getProperty(propertyKeysToObfuscate[i]);
	        }
	      }
	      fout.write(line);
	      fout.write("\n");
	      rowCount++;
	      
	      if (rowCount < readPropsFiles.length) {
	        line = readPropsFiles[rowCount];
	        line.trim();
	      }
	      else {
	        line = null;
	      }
	      
	    } while ((line != null) && (rowCount < readPropsFiles.length));
	    fout.close();
	    bufReader.close();
	    
	    FileWriter propsWriter = new FileWriter(propFile);
	    propsWriter.write(fout.toString());
	    propsWriter.close();
	  }
	  








	  public static void obfuscateFile(String filename, String[] propertyKeysToObfuscate, Map<String, String> map, boolean commaSeparated)
	    throws Exception
	  {
	    processFile(filename, propertyKeysToObfuscate, map, true, commaSeparated);
	  }
	  








	  public static void deObfuscateFile(String filename, String[] propertyKeysToObfuscate, Map<String, String> map, boolean commaSeparated)
	    throws Exception
	  {
	    processFile(filename, propertyKeysToObfuscate, map, false, commaSeparated);
	  }
	  









	  public static Properties deObfuscateFile(Properties props, String[] propertyKeysToObfuscate, Map<String, String> map, boolean commaSeperated)
	    throws Exception
	  {
	    Properties result = (Properties)props.clone();
	    
	    for (int i = 0; i < propertyKeysToObfuscate.length; i++) {
	      String tmp = result.getProperty(propertyKeysToObfuscate[i]);
	      if (tmp != null) {
	        if (commaSeperated) {
	          StringBuffer sb = new StringBuffer();
	          String[] values = tmp.split(",");
	          String delim = "";
	          for (int j = 0; j < values.length; j++) {
	            String v = values[j].trim();
	            if (!v.startsWith("{")) {
	              System.out.println(propertyKeysToObfuscate[i] + " password is already in plain text");
	            }
	            else {
	              EncryptedInfo info = ObfuscateSecretsHelper.parseObfuscatedSecret(v);
	              ObfuscatorInf pg = PassphraseObfuscatorFactory.getAdapter(info);
	              sb.append(delim);
	              sb.append(new String(pg.decrypt(info)));
	              System.out.println(propertyKeysToObfuscate[i] + " at " + j + " de-obfuscated.");
	              delim = ",";
	            }
	          }
	          result.setProperty(propertyKeysToObfuscate[i], sb.toString());

	        }
	        else if (!tmp.startsWith("{")) {
	          System.out.println(propertyKeysToObfuscate[i] + " password is already in plain text");
	        }
	        else {
	          EncryptedInfo info = ObfuscateSecretsHelper.parseObfuscatedSecret(tmp);
	          ObfuscatorInf pg = PassphraseObfuscatorFactory.getAdapter(info);
	          result.setProperty(propertyKeysToObfuscate[i], new String(pg.decrypt(info)));
	          System.out.println(propertyKeysToObfuscate[i] + " de-obfuscated.");
	        }
	      }
	    }
	    
	    return result;
	  }
	  








	  public static Properties obfuscateFile(Properties props, String[] propertyKeysToObfuscate, Map<String, String> map, boolean commaSeperated)
	    throws Exception
	  {
	    Properties result = (Properties)props.clone();
	    ObfuscatorInf pg = PassphraseObfuscatorFactory.getAdapter(map);
	    for (int i = 0; i < propertyKeysToObfuscate.length; i++) {
	      String tmp = result.getProperty(propertyKeysToObfuscate[i]);
	      if (tmp != null) {
	        if (commaSeperated) {
	          String[] values = tmp.split(",");
	          StringBuffer sb = new StringBuffer();
	          String delim = "";
	          for (int j = 0; j < values.length; j++) {
	            String v = values[j].trim();
	            if (v.startsWith("{")) {
	              System.out.println(propertyKeysToObfuscate[i] + " at " + j + " password already obfuscated");
	            }
	            else {
	              EncryptedInfo sec = pg.encrypt(v.getBytes());
	              
	              String encryptedPropValue = ObfuscateSecretsHelper.writeObfuscatedSecret(sec);
	              sb.append(delim);
	              sb.append(encryptedPropValue);
	              System.out.println(propertyKeysToObfuscate[i] + " at " + j + " obfuscated.");
	              delim = ",";
	            }
	          }
	          result.setProperty(propertyKeysToObfuscate[i], sb.toString());

	        }
	        else if (tmp.startsWith("{")) {
	          System.out.println(propertyKeysToObfuscate[i] + " password already obfuscated");
	        }
	        else {
	          EncryptedInfo sec = pg.encrypt(tmp.getBytes());
	          
	          String encryptedPropValue = ObfuscateSecretsHelper.writeObfuscatedSecret(sec);
	          result.setProperty(propertyKeysToObfuscate[i], encryptedPropValue);
	          System.out.println(propertyKeysToObfuscate[i] + " obfuscated.");
	        }
	        
	      }
	      else {
	        System.out.println(propertyKeysToObfuscate[i] + " does not exist.");
	      }
	    }
	    return result;
	  }
	}
