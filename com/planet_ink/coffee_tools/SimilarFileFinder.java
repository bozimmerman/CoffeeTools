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
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class SimilarFileFinder 
{
	public static int numMatches(List<Set<Long>> matchSet, List<Set<Long>> fileSet, final int hashLength)
	{
		int numFound = 0;
		for(Long hash : matchSet.get(0))
		{
			boolean found=false;
			for(int c=0;c<hashLength;c++)
			{
				if(fileSet.get(c).contains(hash))
				{
					found=true;
					break;
				}
			}
			if(found)
				numFound++;
		}
		return numFound;
	}
	
	public static List<Set<Long>> buildHashes(final byte[] bytes, final int hashLength)
	{
		List<Set<Long>> finalMap = new ArrayList<Set<Long>>();
		for(int b=0;b<hashLength;b++)
		{
			Set<Long> ThisSet = new TreeSet<Long>();
			for(int i=b;i+hashLength-1<bytes.length;i+=hashLength)
			{
				long hash = 0;
				for(int x=0;x<hashLength;x++)
					hash = (hash << 8) | bytes[i+x];
				if(!ThisSet.contains(Long.valueOf(hash)))
					ThisSet.add(Long.valueOf(hash));
			}
			finalMap.add(ThisSet);
		}
		return finalMap;
	}

	public static byte[] getFileBytes(String filename, boolean zipFlag) throws IOException
	{
		File f = new File(filename);
		InputStream fi;
		int len=(int)f.length();
		if(zipFlag && filename.toLowerCase().endsWith(".gz"))
		{
			final GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
			final byte[] lbuf = new byte[4096];
			int read=in.read(lbuf);
			final ByteArrayOutputStream bout=new ByteArrayOutputStream((int)(2*f.length()));
			while(read >= 0)
			{
				bout.write(lbuf,0,read);
				read=in.read(lbuf);
			}
			in.close();
			fi=new ByteArrayInputStream(bout.toByteArray());
			len=bout.size();
		}
		else
			fi = new BufferedInputStream(new FileInputStream(f));
		
		byte[] fileBytes = new byte[len];
		int totalBytesRead = 0;
		while(totalBytesRead < fileBytes.length)
		{
			final int bytesRemaining = fileBytes.length - totalBytesRead;
			final int bytesRead = fi.read(fileBytes, totalBytesRead, bytesRemaining); 
			if (bytesRead > 0)
			{
				totalBytesRead = totalBytesRead + bytesRead;
			}
		}
		fi.close();
		return fileBytes;
	}
	
	public static long getFileLength(String filename) throws IOException
	{
		File f = new File(filename);
		if(f.exists())
			return f.length();
		return 0;
	}
	
	public static List<Set<Long>> buildFile(String filename, final int hashLength, boolean zipFiles) throws IOException
	{
		final byte[] fileBytes = getFileBytes(filename, zipFiles);
		return buildHashes(fileBytes, hashLength);
	}
	
	public static List<String> fetchDirFiles(File dirRoot, Set<String> done, final boolean recurse, final int depth, final Pattern P)
	{
		List<String> fileList = new LinkedList<String>();
		if(dirRoot.isDirectory())
		{
			long hash=0;
			for(File f : dirRoot.listFiles())
			{
				if((P==null)||(P.matcher(f.getName().subSequence(0, f.getName().length())).matches()))
				{
					hash ^= f.getName().intern().hashCode();
					hash ^= f.length();
				}
			}
			final String key = dirRoot.getName()+hash;
			if(!done.contains(key))
			{
				done.add(key);
				for(File f : dirRoot.listFiles())
				{
					if(f.isFile() || (recurse && (depth > 0)))
					{
						fileList.addAll(fetchDirFiles(f,done,recurse,depth-1,P));
					}
				}
			}
		}
		else
		if((P==null)||(P.matcher(dirRoot.getName().subSequence(0, dirRoot.getName().length())).matches()))
			fileList.add(dirRoot.getAbsolutePath());
		return fileList;
	}
	
	public static void main(String[] args)
	{
		if(args.length < 2)
		{
			System.out.println("Usage: SimilarFinder [options] [path to all similars] [path/file to search FOR]");
			System.out.println("Options: ");
			System.out.println("-r recursive similar path search");
			System.out.println("-z decompress .gz files");
			System.out.println("-c cache size in mb");
			System.out.println("-depth -d [number] how deep to recurse (only with -r)");
			System.out.println("-matches -m [number/5] how many top matches to return");
			System.out.println("-length -l [number/4] length of the hash run");
			System.out.println("-minimum -n [number 0-100] minimum percentage match");
			System.out.println("More Options: ");
			System.out.println("-x exact match on file extensions");
			System.exit(-1);
		}
		
		String filename = args[args.length-1];
		String searchPath = args[args.length-2];
		Map<String,String> options = new Hashtable<String,String>();
		long cacheBytesRemain=16 * 1024 * 1024;
		for(int i=0;i<args.length-2;i++)
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
		
		if(options.containsKey("c"))
		{
			long mbs=Integer.valueOf(options.get("c"));
			if(mbs > 0)
				cacheBytesRemain = mbs * (1024L * 1024L);
		}
		final boolean zipFiles=options.containsKey("z");
		boolean matchExtensions=options.containsKey("x");
		int hashLength = 4;
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
		int minPctMatch = 0;
		if(options.containsKey("n"))
			minPctMatch=Integer.valueOf(options.get("n")).intValue();
		if(options.containsKey("minimum"))
			minPctMatch=Integer.valueOf(options.get("minimum")).intValue();
		
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
		int matches = 5;
		if(options.containsKey("m"))
		{
			matches = Integer.valueOf(options.get("m")).intValue();
		}
		if(options.containsKey("matches"))
		{
			matches = Integer.valueOf(options.get("matches")).intValue();
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
		final List<String> srchNames=new LinkedList<String>();
		try
		{
			int x=searchPath.lastIndexOf(File.separator);
			Pattern searchPattern=null;
			if(x>0)
			{
				String possMatch = searchPath.substring(x+1);
				if((possMatch.indexOf('*')>=0)||(possMatch.indexOf('?')>=0))
				{
					String regex = possMatch.replace("?", ".?").replace("*", ".*?");
					searchPattern = Pattern.compile(regex);
					searchPath = searchPath.substring(0, x);
				}
			}
			File s = new File(searchPath);
			final HashSet<String> done = new HashSet<String>();
			srchNames.addAll(fetchDirFiles(s,done,options.containsKey("r")||options.containsKey("recurse"),depth,searchPattern));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		final Map<String,List<Set<Long>>> cache=new HashMap<String,List<Set<Long>>>();
		final boolean perfectMatch =(minPctMatch >= 100); 
		for(final File F : filesToDo)
		{
			if(filesToDo.size()>0)
			{
				System.out.println("");
				System.out.println(F.getAbsolutePath()+": ");
			}
			try
			{
				int x=F.getName().lastIndexOf('.');
				String fileExt=(x>=0)?F.getName().substring(x+1):"";
				final byte[] fileBytes = SimilarFileFinder.getFileBytes(F.getAbsolutePath(),zipFiles); 
				List<Set<Long>> fileData = perfectMatch ? null : buildHashes(fileBytes, hashLength);
				final Map<String,Double> scores = new TreeMap<String,Double>();
				for(String srchFile : srchNames)
				{
					if(matchExtensions)
					{
						x=srchFile.lastIndexOf('.');
						String srchExt=(x>=0)?srchFile.substring(x+1):"";
						if(!srchExt.equalsIgnoreCase(fileExt))
							continue;
					}
					if(perfectMatch)
					{
						if(SimilarFileFinder.getFileLength(srchFile) != F.length())
							continue;
						final byte[] blk1=SimilarFileFinder.getFileBytes(srchFile,zipFiles);
						if(Arrays.equals(fileBytes, blk1))
							scores.put(srchFile, Double.valueOf(100.0));
						continue;
					}
					List<Set<Long>> srchData = cache.get(srchFile);
					if(srchData == null)
					{
						srchData = buildFile(srchFile,hashLength,zipFiles);
						long n=0;
						for(Set<Long> set : srchData)
							n += (set.size() * 24);
						if(cacheBytesRemain > n)
						{
							cacheBytesRemain -= n;
							cache.put(srchFile, srchData);
						}
					}
					final double score1 = numMatches(fileData,srchData,hashLength);
					final double score2 = numMatches(srchData,fileData,hashLength);
					final double topScore = (fileData.get(0).size() + srchData.get(0).size()) / 2.0;
					final double score = (score1 + score2) / 2.0;
					final Double pct=Double.valueOf(100.0 * (score/topScore));
					if(pct.doubleValue() >= minPctMatch)
						scores.put(srchFile, pct);
				}
				Collections.sort(srchNames,new Comparator<String>()
				{
					@Override
					public int compare(String arg0, String arg1) 
					{
						if(!scores.containsKey(arg0))
							return -1;
						if(!scores.containsKey(arg1))
							return 1;
						return scores.get(arg0).compareTo(scores.get(arg1));
					}
				});
				System.out.println("Most similar: ");
				for(int i=srchNames.size()-1;i>=0 && i>srchNames.size()-matches ;i--)
				{
					final String path = srchNames.get(i);
					if((!path.equals(F.getAbsolutePath()))
					&&(scores.containsKey(path)))
					{
						String score=scores.get(path).toString();
						x=score.indexOf('.');
						if((x>0)&&(x<score.length()-2))
							score=score.substring(0,x+3);
						System.out.println(score+"% "+path);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
}
