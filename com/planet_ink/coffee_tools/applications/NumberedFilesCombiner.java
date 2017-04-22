package com.planet_ink.coffee_tools.applications;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.TreeMap;

public class NumberedFilesCombiner 
{
	
	public static boolean isNumber(String s)
	{
		if(s.length()==0)
			return false;
		for(int i=0;i<s.length();i++)
			if(!Character.isDigit(s.charAt(i)))
				return false;
		return true;
	}
	
	public static Integer s_Number(String s)
	{
		if(s.length()==0)
			return null;
		while(s.charAt(0)=='0')
			s=s.substring(1);
		return Integer.valueOf(Integer.parseInt(s));
	}
	
	public static void main(String[] args)
	{
		if(args.length==0)
		{
			System.err.println("Usage: FileCombiner [ROOT DIR] [FILE]");
			System.exit(-1);
		}
		try
		{
			String rootDirStr=args[0];
			String targetPath=args[1];
			File targetFile = new File(targetPath);
			String targetFilename = targetFile.getName();
			File srcDir = new File(rootDirStr);
			TreeMap<Integer,File> map = new TreeMap<Integer,File>();
			for(File F : srcDir.listFiles())
			{
				if(F.isDirectory() && isNumber(F.getName()) && new File(F,targetFilename).exists())
					map.put(s_Number(F.getName()), F);
			}
			BufferedOutputStream fo = new BufferedOutputStream(new FileOutputStream(targetFile));
			int prev=0;
			byte[] buffer = new byte[64 * 1024 * 1024];
			for(Integer I : map.keySet())
			{
				if(I.intValue() == (prev + 1))
				{
					final File F=new File(map.get(I),targetFilename);
					BufferedInputStream fi = new BufferedInputStream(new FileInputStream(F));
					long bytesRead = 0;
					while(bytesRead < F.length())
					{
						int bytes = fi.read(buffer);
						if(bytes > 0)
						{
							bytesRead += bytes;
							fo.write(buffer,0,bytes);
						}
					}
					fi.close();
					prev = I.intValue();
				}
				else
				{
					System.err.println("The following directory had no prior: "+I.intValue());
					System.exit(-1);
				}
			}
			fo.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
