package com.planet_ink.coffee_tools;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDeInterleaver 
{
	public static void main(String[] args)
	{
		if(args.length<4)
		{
			System.out.println("USAGE: FileDeInterleaver (\\R)[PATH] [LOW FILE EXPR] [HIGH FILE EXPR] [COMBINED FILENAME]");
			System.out.println(". any char, \\d a digit, \\D non-digit, \\s whitespace, \\S nonwhitespace");
			System.out.println("\\w word char, \\W non-word char");
			System.out.println("? once or no, * zero or more, + one or more, {n} n times");
			System.out.println("^ beginning, $ end, () capture for replacements");
			System.out.println("Replacements: $1, $2, etc.. replace capture group");
			System.exit(-1);
		}
		boolean recurse=false;
		List<String> paths = new ArrayList<String>();
		String path=args[0];
		String lowMask=args[1];
		String highMask=args[2];
		String repl=args[3];
		if(path.startsWith("\\R")||path.startsWith("\\r"))
		{
			path=path.substring(2);
			recurse=true;
		}
		if(lowMask.trim().length()==0)
		{
			System.err.println("No low filename mask found.");
			System.exit(-1);
		}
		if(highMask.trim().length()==0)
		{
			System.err.println("No high filename mask found.");
			System.exit(-1);
		}
		if(repl.trim().length()==0)
		{
			System.err.println("No replacement/combined filename found.");
			System.exit(-1);
		}
		paths.add(path);
		Pattern lowP=Pattern.compile(lowMask);
		Pattern highP=Pattern.compile(highMask);
		for(int p=0;p<paths.size();p++)
		{
			path = paths.get(p);
			List<String[]> lowFiles=new ArrayList<String[]>();
			List<String[]> highFiles=new ArrayList<String[]>();
			File pathF=new File(path);
			if(pathF.exists()&&pathF.isDirectory())
			{
				File[] files=pathF.listFiles();
				for(int f=0;f<files.length;f++)
				{
					File F=files[f];
					if(F.isDirectory()&&recurse)
						paths.add(F.getAbsolutePath());
					else
					{
						Matcher lowM=lowP.matcher(F.getName().subSequence(0, F.getName().length()));
						Matcher highM=highP.matcher(F.getName().subSequence(0, F.getName().length()));
						if(lowM.matches())
						{
							if(highM.matches())
							{
								System.err.println("FAIL: Matches both HIGH and LOW: "+F.getAbsolutePath());
								continue;
							}
							StringBuffer newName=new StringBuffer("");
							lowM.appendReplacement(newName, repl);
							lowFiles.add(new String[]{F.getName(),newName.toString()});
						}
						else
						if(highM.matches())
						{
							StringBuffer newName=new StringBuffer("");
							highM.appendReplacement(newName, repl);
							highFiles.add(new String[]{F.getName(),newName.toString()});
						}
					}
				}
				while(lowFiles.size()>0)
				{
					final String[] low=lowFiles.remove(0);
					for(int x=0;x<highFiles.size();x++)
					{
						final String[] high=highFiles.get(x);
						if(low[1].equals(high[1]))
						{
							File lowF=new File(pathF,low[0]);
							File highF=new File(pathF,high[0]);
							highFiles.remove(x);
							if(lowF.length()==highF.length())
							{
								final String baseName;
								final String baseExt;
								int z=low[1].lastIndexOf('.');
								if(z>0)
								{
									baseName=low[1].substring(0, z);
									baseExt=low[1].substring(z);
								}
								else
								{
									baseName=low[1];
									baseExt="";
								}
								File combinedF=new File(baseName+baseExt);
								int appendNum=1;
								while(combinedF.exists())
								{
									combinedF=new File(baseName+" ("+appendNum+")"+baseExt);
									appendNum++;
								}
								final File parentF=combinedF.getParentFile();
								if(!parentF.exists())
									parentF.mkdirs();
								try
								{
									BufferedInputStream lowIn=new BufferedInputStream(new FileInputStream(lowF));
									BufferedInputStream highIn=new BufferedInputStream(new FileInputStream(highF));
									BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(combinedF));
									for(int i=0;i<lowF.length();i++)
									{
										out.write(lowIn.read());
										out.write(highIn.read());
									}
									lowIn.close();
									highIn.close();
									out.close();
									System.out.println("COMBINED: "+combinedF.getAbsolutePath());
								}
								catch (IOException e)
								{
									System.err.println("FAIL: ERROR: " +low[0]+"!="+high[0]+" in "+path);
									e.printStackTrace();
								}
							}
							else
								System.err.println("FAIL: Mismatch in size" +low[0]+"!="+high[0]+" in "+path);
							break;
						}
					}
				}
			}
			else
				System.err.println("Error: "+path+" is not a valid path.");
		}
	}
}
