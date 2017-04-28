package com.planet_ink.coffee_tools.applications;

import java.io.*;
import java.util.*;

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

	public static List<Set<Long>> buildFile(String filename, final int hashLength) throws IOException
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
		return buildHashes(fileBytes, hashLength);
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
		if(args.length < 2)
		{
			System.out.println("Usage: SimilarFinder [options] [path to all similars] [path/file to search FOR]");
			System.out.println("Options: ");
			System.out.println("-r recursive similar path search");
			System.out.println("-depth -d [number] how deep to recurse (only with -r)");
			System.out.println("-matches -m [number/5] how many top matches to return");
			System.out.println("-length -l [number/4] length of the hash run");
			System.exit(-1);
		}
		
		String filename = args[args.length-1];
		String searchPath = args[args.length-2];
		Map<String,String> options = new Hashtable<String,String>();
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
		
		int hashLength = 4;
		if(options.containsKey("l"))
		{
			hashLength = Integer.valueOf(options.get("l"));
		}
		if(options.containsKey("length"))
		{
			hashLength = Integer.valueOf(options.get("length"));
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
			depth = Integer.valueOf(options.get("d"));
		}
		if(options.containsKey("depth"))
		{
			depth = Integer.valueOf(options.get("depth"));
		}
		int matches = 5;
		if(options.containsKey("m"))
		{
			matches = Integer.valueOf(options.get("m"));
		}
		if(options.containsKey("matches"))
		{
			matches = Integer.valueOf(options.get("matches"));
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
		List<String> srchNames=new LinkedList<String>();
		try
		{
			File s = new File(searchPath);
			final HashSet<String> done = new HashSet<String>();
			srchNames.addAll(fetchDirFiles(s,done,options.containsKey("r")||options.containsKey("recurse"),depth));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		for(final File F : filesToDo)
		{
			if(filesToDo.size()>0)
			{
				System.out.println("");
				System.out.println(F.getAbsolutePath()+": ");
			}
			try
			{
				List<Set<Long>> fileData = buildFile(F.getAbsolutePath(),hashLength);
				final Map<String,Double> scores = new TreeMap<String,Double>();
				for(String srchFile : srchNames)
				{
					List<Set<Long>> srchData = buildFile(srchFile,hashLength);
					double score1 = numMatches(fileData,srchData,hashLength);
					double score2 = numMatches(srchData,fileData,hashLength);
					double topScore = (fileData.get(0).size() + srchData.get(0).size()) / 2.0;
					double score = (score1 + score2) / 2.0;
					scores.put(srchFile, Double.valueOf(100.0 * ((double)score/(double)topScore)));
				}
				Collections.sort(srchNames,new Comparator<String>(){
					@Override
					public int compare(String arg0, String arg1) 
					{
						return scores.get(arg0).compareTo(scores.get(arg1));
					}
				});
				System.out.println("Most similar: ");
				for(int i=srchNames.size()-1;i>=0 && i>srchNames.size()-matches ;i--)
				{
					String path = srchNames.get(i);
					if(!path.equals(F.getAbsolutePath()))
					{
						String score=scores.get(path).toString();
						int x=score.indexOf('.');
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
