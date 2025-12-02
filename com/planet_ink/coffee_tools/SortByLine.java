package com.planet_ink.coffee_tools;
/*
Copyright 2017-2025 Bo Zimmerman

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

import java.util.*;
import java.io.*;

public class SortByLine 
{

	public static final void main(String[] args)
	{
		if(args.length<3)
		{
			System.out.println("USAGE: SortByLine [FILE PATH] [# LINES PER GROUP] [SORT BY WHICH LINE]");
			System.out.println("Which line is 1-based, or the word BL to denote by a blank line.");
			System.exit(-1);
		}
		
		String path=args[0];
		try
		{
			final int numLines;
			final boolean blankLine;
			if(args[1].equalsIgnoreCase("BL"))
			{
				numLines=Integer.MAX_VALUE;
				blankLine=true;
			}
			else
			{
				numLines=Integer.parseInt(args[1]);
				blankLine=false;
				if(numLines<=0)
				{
					System.out.println("Illegal argument for number of lines: "+args[1]);
					System.exit(-1);
				}
			}
			final int whichLine;
			whichLine=Integer.parseInt(args[2]);
			if(whichLine>numLines)
			{
				System.out.println("Illegal argument for which line: "+args[2]);
				System.exit(-1);
			}
			if(whichLine<=0)
			{
				System.out.println("Illegal argument for which line: "+args[2]);
				System.exit(-1);
			}
			final StringBuilder eoln=new StringBuilder("");
			final FileReader fr=new FileReader(path);
			while(fr.ready())
			{
				char c=(char)fr.read();
				if((c=='\r')||(c=='\n'))
				{
					eoln.append(c);
					char c2=(char)fr.read();
					if(((c2=='\r')||(c2=='\n')) && (c2 != c))
						eoln.append(c2);
					break;
				}
			}
			fr.close();
			final List<List<String>> lines=new LinkedList<List<String>>();
			final BufferedReader br=new BufferedReader(new FileReader(path));
			String s=br.readLine();
			while(s!=null)
			{
				LinkedList<String> line=new LinkedList<String>();
				lines.add(line);
				line.add(s);
				if((!blankLine)||(s.trim().length()>0))
				{
					for(int i=2;i<=numLines;i++)
					{
						s=br.readLine();
						if(s==null)
						{
							System.out.println("Not enough in the file.");
							System.exit(-1);
						}
						line.add(s);
						if((blankLine)&&(s.trim().length()==0))
							break;
					}
				}
				s=br.readLine();
			}
			br.close();
			StringBuilder rewriteChk=new StringBuilder("");
			for(List<String> line : lines)
			{
				for(String s2 : line)
					rewriteChk.append(s2).append(eoln.toString());
			}
			StringBuilder rewrite=new StringBuilder("");
			Collections.sort(lines,new Comparator<List<String>>(){
				@Override
				public int compare(List<String> o1, List<String> o2) {
					return o1.get(whichLine-1).compareTo(o2.get(whichLine-1));
				}
			}
			);
			for(List<String> line : lines)
			{
				for(String s2 : line)
					rewrite.append(s2).append(eoln.toString());
			}
			if(rewriteChk.toString().equals(rewrite.toString()))
			{
				System.out.println("Nothing done.");
				System.exit(-1);
			}
			FileWriter fw=new FileWriter(path);
			fw.write(rewrite.toString());
			fw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
