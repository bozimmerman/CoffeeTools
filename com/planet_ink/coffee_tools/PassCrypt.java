package com.planet_ink.coffee_tools;
/*
Copyright 2017-2017 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.io.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

public class PassCrypt 
{
	private static String trimCR(String s)
	{
		while(s.startsWith("\r"))
			s=s.substring(1);
		while(s.startsWith("\n"))
			s=s.substring(1);
		while(s.startsWith("\r"))
			s=s.substring(1);
		while(s.endsWith("\n"))
			s=s.substring(0,s.length()-1);
		while(s.endsWith("\r"))
			s=s.substring(0,s.length()-1);
		while(s.endsWith("\n"))
			s=s.substring(0,s.length()-1);
		return s;
	}
	
	public static void main(String[] args)
	{
		if(args.length<2)
		{
			System.out.println("Usage: PassCrypt [PASSWORD] [FILEPATH]");
			System.out.println("Tips:");
			System.out.println("Always ignore encrypted part (first line) of the file.");
			System.out.println("Always add 1 blank line before new text to encrypt");
			System.exit(-1);
		}
		
		String pw=args[0];
		int x=16 % pw.length();
		while(pw.length()<16)
		{
			pw += pw.charAt(x++);
			if(x>=pw.length())
				x=0;
		}
		byte[] keyBytes = pw.getBytes();
		while(keyBytes.length > 16)
		{
			byte[] oldBytes = keyBytes;
			keyBytes = Arrays.copyOf(oldBytes, 16);
			for(int y=16;y<oldBytes.length;y++)
				keyBytes[y % 16] ^= oldBytes[y];
		}
		
		String filename="";
		for(int i=1;i<args.length;i++)
			filename+=args[i]+" ";
		filename=filename.trim();
		
		try 
		{
			BufferedReader br=new BufferedReader(new FileReader(filename));
			StringBuilder encryptedText = new StringBuilder("");
			StringBuilder unencryptedText = new StringBuilder("");
			String s=br.readLine();
			if(s!=null)
			{
				if(s.length()>0)
					encryptedText.append(s);
				s=br.readLine();
				while(s!=null)
				{
					if((s.length()>0)||(unencryptedText.length()>0))
						unencryptedText.append(trimCR(s)).append("\n");
					s=br.readLine();
				}
			}
			br.close();
			
			// wrap key data in Key/IV specs to pass to cipher
			SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decordedValue =  javax.xml.bind.DatatypeConverter.parseBase64Binary(encryptedText.toString());
			byte[] decValue = c.doFinal(decordedValue);
			String decryptedValue = trimCR(new String(decValue));
			
			if(unencryptedText.length()>0)
			{
				decryptedValue += "\n\n"+trimCR(unencryptedText.toString());
				
				c.init(Cipher.ENCRYPT_MODE, key);
				byte[] encVal = c.doFinal(decryptedValue.getBytes());
				String encryptedValue = javax.xml.bind.DatatypeConverter.printBase64Binary(encVal);//,B64Encoder.DONT_BREAK_LINES);
				FileWriter fw=new FileWriter(filename);
				fw.write(encryptedValue+"\n");
				fw.flush();
				fw.close();
			}
			
			System.out.println(decryptedValue);
			System.exit(0);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
