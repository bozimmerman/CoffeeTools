package com.planet_ink.coffee_tools;

import java.io.*;
import java.util.*;

public class FilenameMixUpper
{
	public static void main(String[] args)
	{
		if(args.length==0)
		{
			System.out.println("Hey! Give a path!");
			System.exit(-1);
		}
		
		final StringBuilder str=new StringBuilder("");
		for(String s : args)
			str.append(s).append(" ");
		final String filename = str.toString().trim();
		
		final File dirF=new File(filename);
		if((!dirF.exists())||(!dirF.isDirectory()))
		{
			System.out.println("Hey! Give a valid directory, not "+filename);
			System.exit(-1);
		}
		final Map<File, String[]> mixMap=new Hashtable<File, String[]>();
		List<String> names = new ArrayList<String>();
		for(File F : dirF.listFiles())
		{
			if(!F.isDirectory())
			{
				mixMap.put(F, new String[]{F.getName()});
				names.add(F.getName());
			}
		}
		final Random r=new Random(System.nanoTime());
		for(File F : mixMap.keySet())
		{
			int n=r.nextInt(names.size());
			String name = names.remove(n);
			mixMap.get(F)[0] = name;
		}
		final Map<File,File> safetyMap=new Hashtable<File,File>();
		int x=0;
		for(File F : mixMap.keySet())
		{
			File newFile=new File(F.getParentFile(),(x++)+".temp");
			safetyMap.put(F, newFile);
			System.out.println("Rename: "+F.getAbsolutePath()+" to "+newFile.getAbsolutePath());
			F.renameTo(newFile);
		}
		
		for(File F : mixMap.keySet())
		{
			File newFile=new File(F.getParentFile(),mixMap.get(F)[0]);
			System.out.println("Rename: "+F.getAbsolutePath()+" to "+newFile.getAbsolutePath());
			File safeFile = safetyMap.get(F);
			safeFile.renameTo(newFile);
		}
	}
}
