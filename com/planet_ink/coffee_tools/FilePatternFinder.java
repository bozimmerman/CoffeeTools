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

public class FilePatternFinder 
{
	public static long[] findLongest(final byte[] bytes, List<List<Long>> matchSet, final int hashLength)
	{
		final long[] longest=new long[]{-1,-1,-1};
		for(int c0=0;c0<hashLength;c0++)
		{
			final List<Long> set=matchSet.get(c0);
			for(int h=0;h<set.size();h++)
			{
				final Long hash=set.get(h);
				final int startOfSeek0=c0 + (h*hashLength);
				for(int c=0;c<hashLength;c++)
				{
					final List<Long> hiOffSet=matchSet.get(c);
					for(int hi=h;hi<hiOffSet.size();hi++)
					{
						if((hiOffSet.get(hi).equals(hash))
						&&((c0!=c)||(hi!=h)))
						{
							int seek0=startOfSeek0;
							final int startOfMatch0=c + (hi*hashLength);
							int match0=startOfMatch0;
							if(((match0-seek0)>longest[2])
							&&((bytes.length-seek0)>longest[2])
							&&((bytes.length-match0)>longest[2]))
							{
								long length=0;
								while((seek0<bytes.length)
								&&(match0<bytes.length)
								&&(seek0<startOfMatch0)
								&&(bytes[seek0]==bytes[match0]))
								{
									seek0++;
									match0++;
									length++;
								}
								if(length > longest[2])
								{
									longest[0]=startOfSeek0;
									longest[1]=startOfMatch0;
									longest[2]=length;
								}
							}
						}
					}
				}
			}
		}
		return longest;
	}
	
	public static List<List<Long>> buildHashes(final byte[] bytes, final int hashLength)
	{
		List<List<Long>> finalMap = new ArrayList<List<Long>>();
		for(int b=0;b<hashLength;b++)
		{
			List<Long> ThisSet = new ArrayList<Long>();
			for(int i=b;i+hashLength-1<bytes.length;i+=hashLength)
			{
				long hash = 0;
				for(int x=0;x<hashLength;x++)
					hash = (hash << 8) | bytes[i+x];
				ThisSet.add(Long.valueOf(hash));
			}
			finalMap.add(ThisSet);
		}
		return finalMap;
	}

	public static byte[] getFileBytes(String filename) throws IOException
	{
		File f = new File(filename);
		BufferedInputStream fi = new BufferedInputStream(new FileInputStream(f));
		byte[] fileBytes = new byte[(int)f.length()];
		int totalBytesRead = 0;
		while(totalBytesRead < fileBytes.length)
		{
			int bytesRemaining = fileBytes.length - totalBytesRead;
			int bytesRead = fi.read(fileBytes, totalBytesRead, bytesRemaining); 
			if (bytesRead > 0)
			{
				totalBytesRead = totalBytesRead + bytesRead;
			}
		}
		fi.close();
		return fileBytes;
	}
	
	public static List<String> fetchDirFiles(File dirRoot, Set<String> done, final boolean recurse, final int depth)
	{
		List<String> fileList = new LinkedList<String>();
		if(dirRoot.isDirectory())
		{
			long hash=0;
			for(File f : dirRoot.listFiles())
			{
				hash ^= f.getName().intern().hashCode();
				hash ^= f.length();
			}
			final String key = dirRoot.getName()+hash;
			if(!done.contains(key))
			{
				done.add(key);
				for(File f : dirRoot.listFiles())
				{
					if(f.isFile() || (recurse && (depth > 0)))
					{
						fileList.addAll(fetchDirFiles(f,done,recurse,depth-1));
					}
				}
			}
		}
		else
			fileList.add(dirRoot.getAbsolutePath());
		return fileList;
	}
	
	public static void main(String[] args)
	{
		if(args.length < 1)
		{
			System.out.println("Usage: FilePatternFinder [options] \"[path/file to inspect]\"");
			System.out.println("Options: ");
			System.out.println("-r recursive similar path search");
			System.out.println("-depth -d [number] how deep to recurse (only with -r)");
			System.out.println("-length -l [number/4] length of the hash run");
			System.out.println("-t truncate files that have 1/2 mirror-matches");
			System.exit(-1);
		}
		
		String filename = args[args.length-1];
		Map<String,String> options = new Hashtable<String,String>();
		for(int i=0;i<args.length-1;i++)
		{
			if(args[i].startsWith("-")||args[i].startsWith("/"))
			{
				String option=args[i].substring(1).toLowerCase();
				String value=option;
				if((i<args.length-3)&&(!(args[i+1].startsWith("-")||args[i+1].startsWith("/"))))
				{
					value=args[i+1];
					i++;
				}
				options.put(option, value);
			}
		}
		
		int hashLength = 4;
		boolean truncate = options.containsKey("t");
		if(options.containsKey("l"))
		{
			hashLength = Integer.valueOf(options.get("l")).intValue();
		}
		if(options.containsKey("length"))
		{
			hashLength = Integer.valueOf(options.get("length")).intValue();
		}
		if((hashLength < 1) || (hashLength > 8))
		{
			System.err.println("illegal hash length: "+hashLength);
			System.exit(-1);
		}
		File baseF=new File(filename);
		java.util.regex.Pattern P=null;
		if((filename.indexOf('*')>=0)||(filename.indexOf('+')>=0))
		{
			int x=filename.lastIndexOf(File.separator);
			if(x<=0)
				x=filename.lastIndexOf('/');
			if(x<=0)
				x=filename.lastIndexOf('\\');
			if(x>=0)
			{
				P=java.util.regex.Pattern.compile(filename.substring(x+1));
				baseF=new File(filename.substring(0,x));
			}
		}
		int depth = Integer.MAX_VALUE;
		if(options.containsKey("d"))
		{
			depth = Integer.valueOf(options.get("d")).intValue();
		}
		if(options.containsKey("depth"))
		{
			depth = Integer.valueOf(options.get("depth")).intValue();
		}
		if(!baseF.exists())
		{
			System.err.println("not found: "+filename);
			System.exit(-1);
		}
		List<File> filesToDo=new LinkedList<File>();
		if(baseF.isDirectory())
		{
			try
			{
				LinkedList<File> dirsLeft=new LinkedList<File>();
				dirsLeft.add(baseF);
				while(dirsLeft.size()>0)
				{
					File dir=dirsLeft.removeFirst();
					for(File F : dir.listFiles())
					{
						if(F.isDirectory())
						{
							int dep=0;
							File F1=F;
							while((F1 != null)&&(!F1.getCanonicalPath().equals(baseF.getCanonicalPath())))
							{
								F1=F1.getParentFile();
								dep++;
							}
							if(dep <= depth)
								dirsLeft.add(F);
						}
						else
						if((P==null)||(P.matcher(F.getName().subSequence(0, F.getName().length())).matches()))
							filesToDo.add(F);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
		else
			filesToDo.add(baseF.getAbsoluteFile());
		for(final File F : filesToDo)
		{
			if(filesToDo.size()>0)
			{
				System.out.print(F.getAbsolutePath()+": ");
			}
			try
			{
				byte[] fileData = getFileBytes(F.getAbsolutePath());
				List<List<Long>> fileHashes = FilePatternFinder.buildHashes(fileData, hashLength);
				long[] longest=FilePatternFinder.findLongest(fileData, fileHashes, hashLength);
				if(longest[0]<0)
					System.out.print("No internal matches.");
				else
					System.out.print(longest[2]+" byte match at offsets "+longest[0]+" and "+longest[1]);
				if((truncate)
				&&(longest[1]==longest[2])
				&&(longest[0]==0)
				&&(longest[1]+longest[2]==fileData.length))
				{
					System.out.println("**Truncated**");
					FileOutputStream fo=new FileOutputStream(F);
					fo.write(fileData, 0, (int)longest[1]);
					fo.close();
				}
				System.out.println("");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
}
