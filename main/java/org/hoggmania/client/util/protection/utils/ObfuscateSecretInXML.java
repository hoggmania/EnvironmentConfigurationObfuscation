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

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.hoggmania.client.util.protection.EncryptedInfo;
import org.hoggmania.client.util.protection.ObfuscatorInf;
import org.hoggmania.client.util.protection.PassphraseObfuscatorFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class ObfuscateSecretInXML
{
  private static Map<String, String> map = new HashMap<String, String> ();
  
  public ObfuscateSecretInXML() {}
  
  private static void encryptedByElementSearch(Document doc, String expression, String attributeName, boolean obfuscate)
    throws Exception
  {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    XPathExpression expr = xpath.compile(expression);
    Object result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList)result;
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node == null) {
        return;
      }
      if (attributeName == null) {
        if (obfuscate) {
          node.setTextContent(encrypt(node.getTextContent()));
        } else {
          node.setTextContent(decrypt(node.getTextContent()));
        }
      }
      else {
        NamedNodeMap attrs = node.getAttributes();
        Node attr = attrs.getNamedItem(attributeName);
        if (attr != null) {
          if (obfuscate) {
            attr.setNodeValue(encrypt(attr.getNodeValue()));
          } else {
            attr.setNodeValue(decrypt(attr.getNodeValue()));
          }
        }
      }
    }
  }
  
  private static String encrypt(String text) throws Exception
  {
    ObfuscatorInf pg = PassphraseObfuscatorFactory.getAdapter(map);
    
    EncryptedInfo sec = pg.encrypt(text.getBytes());
    String encryptedPropValue = ObfuscateSecretsHelper.writeObfuscatedSecret(sec);
    
    return encryptedPropValue;
  }
  
  private static String decrypt(String encryptedText) throws Exception {
    EncryptedInfo info = ObfuscateSecretsHelper.parseObfuscatedSecret(encryptedText);
    
    ObfuscatorInf pg = PassphraseObfuscatorFactory.getAdapter(info);
    
    String clear = new String(pg.decrypt(info));
    return clear;
  }
  
  private static Document loadDocument(String file) throws Exception {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    
    domFactory.setNamespaceAware(true);
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    Document doc = builder.parse(file);
    return doc;
  }
  
  private static void saveDocument(Document document, String file) throws Exception
  {
    FileWriter fw = new FileWriter(file);
    Class<?> clazz = null;
    if (System.getProperty("java.vendor").startsWith("Sun")) {
      clazz = Class.forName("com.sun.org.apache.xml.internal.serialize.XMLSerializer");
    }
    else if (System.getProperty("java.vendor").startsWith("IBM")) {
      clazz = Class.forName("org.apache.xml.serialize.XMLSerializer");
    }
    Object o = clazz.newInstance();
    Method method1 = o.getClass().getMethod("setOutputCharStream", new Class[] { Writer.class });
    Method method2 = o.getClass().getMethod("serialize", new Class[] { Document.class });
    method1.invoke(o, new Object[] { fw });
    method2.invoke(o, new Object[] { document });
    
    fw.close();
  }
  
  private static void printUsage()
  {
    System.out.println("Usage: ObfuscateSecretInXML <xml file> <true/false true=obfuscate false=de-ofuscate> <xpath expression>");
    
    System.out.println("       ObfuscateSecretInXML <xml-file> <true/false true=obfuscate false=de-ofuscate> <xpath expression> <attribute name>");
  }
  
  public static void main(String[] args)
    throws Exception
  {
    if (args.length < 4) {
      printUsage();
    } else {
      Document doc = loadDocument(args[0]);
      
      boolean obfuscate = Boolean.parseBoolean(args[1]);
      
      if (args.length > 3) {
        encryptedByElementSearch(doc, args[2], args[3], obfuscate);
      } else {
        encryptedByElementSearch(doc, args[2], null, obfuscate);
      }
      saveDocument(doc, args[0]);
    }
  }
}
