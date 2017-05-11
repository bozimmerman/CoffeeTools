package com.planet_ink.coffee_tools;

import java.io.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExRename 
{
	public static void main(String[] args)
	{
		if(args.length<3)
		{
			System.out.println("USAGE: RegExRename (\\R)[PATH] [MATCH EXPRESSION] [REPLACE EXPRESSION]");
			System.out.println(". any char, \\d a digit, \\D non-digit, \\s whitespace, \\S nonwhitespace");
			System.out.println("\\w word char, \\W non-word char");
			System.out.println("? once or no, * zero or more, + one or more, {n} n times");
			System.out.println("^ beginning, $ end, () capture for replacements");
			System.out.println("Replacements: \\1, \\2, etc.. replace capture group");
			System.exit(-1);
		}
		boolean recurse=false;
		Vector<String> paths = new Vector<String>();
		String path=args[0];
		String mask=args[1];
		if(path.startsWith("\\R")||path.startsWith("\\r"))
		{
			path=path.substring(2);
			recurse=true;
		}
		if(mask.trim().length()==0)
		{
			System.err.println("No mask found.");
			System.exit(-1);
		}
		String repl=args[2];
		paths.addElement(path);
		Pattern P=Pattern.compile(mask);
		for(int p=0;p<paths.size();p++)
		{
			path = (String)paths.elementAt(p);
			File pathF=new File(path);
			if(pathF.exists()&&pathF.isDirectory())
			{
				File[] files=pathF.listFiles();
				for(int f=0;f<files.length;f++)
				{
					File F=files[f];
					if(F.isDirectory()&&recurse)
						paths.addElement(F.getAbsolutePath());
					else
					{
						Matcher M=P.matcher(F.getName().subSequence(0, F.getName().length()));
						if(M.matches())
						{
							StringBuffer newName=new StringBuffer("");
							M.appendReplacement(newName, repl);
							System.out.println("Rename "+F.getName()+" to "+newName.toString());
							F.renameTo(new File(pathF, newName.toString()));
						}
					}
				}
			}
			else
				System.err.println("Error: "+path+" is not a valid path.");
		}
		System.exit(-1);
	}
}
