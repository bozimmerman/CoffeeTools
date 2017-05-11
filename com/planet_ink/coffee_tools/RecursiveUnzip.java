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
import java.util.regex.*;
import java.util.zip.*;

public class RecursiveUnzip
{
	boolean lowerCase=false;
	boolean recursive=false;
	boolean deleteAfter=false;
	boolean dirLowercase=false;
	Pattern extractMask=null;
	StringBuilder filename=new StringBuilder("");
	
	long totalFiles = 0;
	
	static final String HELP_STR="RecursiveUnzip [OPTIONS] [DIRECTORY/FILENAME]\n\r" +
								 "--lowercase      make extracted files lowercase\n\r" +
								 "--dirlowercase   make already directories lowercase\n\r" +
								 "--purge          purge zips after extraction\n\r" +
								 "--extract [MASK] purge zips after extraction\n\r" +
								 "--recursive      recurse into directories";
	
	public void unzip(File F)
	{
		if(F.isDirectory())
		{
			for(File F2 : F.listFiles())
			{
				if(F2.isFile() || recursive)
					unzip(F2);
			}
			if(dirLowercase)
				F.renameTo(new File(F.getParent(),F.getName().toLowerCase()));
		}
		else
		if(F.getName().toLowerCase().endsWith(".zip"))
		{
			try
			{
				java.util.zip.ZipInputStream zis=new java.util.zip.ZipInputStream(new BufferedInputStream(new FileInputStream(F)));
				ZipEntry entry;
				boolean deleteZipAfter=deleteAfter;
				while((entry = zis.getNextEntry()) != null) 
				{
					System.out.print(".");
					int count;
					final int bufSize=2048;
					byte data[] = new byte[bufSize];
					String fileName = lowerCase ? entry.getName().toLowerCase() : entry.getName();
					CharSequence fnSequence = fileName.subSequence(0, fileName.length());
					if((extractMask == null)
					|| extractMask.matcher(fnSequence).matches())
					{
						FileOutputStream fos = new FileOutputStream(new File(F.getParent(),fileName));
						BufferedOutputStream dest = new BufferedOutputStream(fos, bufSize);
						while ((count = zis.read(data, 0, bufSize)) != -1) 
						{
							dest.write(data, 0, count);
						}
						dest.flush();
						dest.close();
						totalFiles++;
					}
					else
					if(extractMask != null)
					{
						System.err.println(F.getName()+" error: No match for "+entry.getName());
						deleteZipAfter=false;
						while ((count = zis.read(data, 0, bufSize)) != -1);
					}
				 }
				 zis.close();
				 if(deleteZipAfter)
				 {
					 if(!F.delete())
					 {
						 System.err.println("Unable to delete "+F.getName());
					 }
				 }
			}
			catch(Exception e)
			{
				System.err.println(F.getName()+" error: "+e.getMessage());
			}
		}
	}
	
	public static void main(String[] args)
	{
		RecursiveUnzip o=new RecursiveUnzip();
		int exitCode=0;
		for(int a=0;a<args.length;a++)
		{
			String lca=args[a].toLowerCase();
			if(lca.startsWith("-"))
			{
				StringBuilder cmds=new StringBuilder("");
				if(lca.startsWith("--"))
				{
					if(lca.substring(2).equals("lowercase"))
						cmds.append('l');
					else
					if(lca.substring(2).equals("recursive"))
						cmds.append('r');
					else
					if(lca.substring(2).equals("purge"))
						cmds.append('p');
					else
					if(lca.substring(2).equals("dirlowercase"))
						cmds.append('d');
					else
					if(lca.substring(2).equals("extract"))
						cmds.append('e');
				}
				else
				if(lca.length()>1)
					cmds.append(lca.substring(1));
				if(cmds.length()==0)
					cmds.append(' ');
				for(int i=0;i<cmds.length();i++)
					switch(cmds.charAt(i))
					{
					case 'l':
					{
						o.lowerCase=true;
						break;
					}
					case 'r':
					{
						o.recursive=true;
						break;
					}
					case 'p':
					{
						o.deleteAfter=true;
						break;
					}
					case 'd':
					{
						o.dirLowercase=true;
						break;
					}
					case 'e':
					{
						if(a<args.length-1)
						{
							o.extractMask=Pattern.compile(args[a+1],Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
							a++;
						}
						break;
					}
					default:
						System.err.println("\n\r'"+lca+" is an unknown command.\n\r");
						exitCode=-1;
					case 'h': 
					{
						System.out.println(HELP_STR);
						System.exit(exitCode);
					}
					}
				o.filename.setLength(0);
			}
			else
			{
				if(o.filename.length()>0)
					o.filename.append(' ');
				o.filename.append(args[a]);
			}
		}
		File openingFile=null;
		if(o.filename.length()>00)
			openingFile=new File(o.filename.toString());
		if((openingFile==null)||(!openingFile.exists()))
		{
			System.err.println("\n\rA filename is required.\n\r");
			exitCode=-1;
			System.out.println(HELP_STR);
			System.exit(exitCode);
		}
		System.out.print("Working...");
		o.unzip(openingFile);
		System.out.println("\n\r\n\r"+o.totalFiles+" extracted");
	}
}
