package com.planet_ink.coffee_tools;

import java.io.*;
import java.util.Vector;

public class Rename 
{
	
	public static String rightReplacement(StringBuffer repl, String filename)
	{
		String srepl=repl.toString();
		if(srepl.equalsIgnoreCase("\\L@")||srepl.equalsIgnoreCase("\\L%"))
			return filename.toLowerCase();
		else
		if(srepl.equalsIgnoreCase("\\U@")||srepl.equalsIgnoreCase("\\U%"))
			return filename.toUpperCase();
		else
		if(srepl.equalsIgnoreCase("@")||srepl.equalsIgnoreCase("%"))
			return filename;
		else
		if(srepl.startsWith("\\L"))
			return srepl.substring(2).toLowerCase();
		else
		if(srepl.startsWith("\\U"))
			return srepl.substring(2).toUpperCase();
		else
			return srepl;
	}
	
	public static void main(String[] args)
	{
		if(args.length<2)
		{
			System.out.println("USAGE: Rename (\\R)[OLD_MASK (@=*, %=.)] [REPLACEMENT EQUATES (@=repl1 %=repl2 X=repl3)]");
			System.out.println("USAGE: Mask sections are divided by @ or %");
			System.out.println("USAGE: Each section must have its own replacement equate");
			System.out.println("USAGE: Use @ to match 0 or more characters");
			System.out.println("USAGE: Use % to match 1 character");
			System.out.println("USAGE: Use X as a replacement for mask constant sections");
			System.out.println("USAGE: A replacement prefixed with \\L will convert to lowercase.");
			System.out.println("USAGE: A replacement prefixed with \\U will convert to uppercase.");
			System.exit(-1);
		}
		boolean recurse=false;
		Vector<String> paths = new Vector<String>();
		String path=".";
		String mask=args[0];
		if(mask.startsWith("\\R"))
		{
			mask=mask.substring(2);
			recurse=true;
		}
		int x=mask.lastIndexOf(File.separatorChar);
		if(x>=0)
		{
			path=mask.substring(0,x+1);
			mask=mask.substring(x+1);
		}
		if(mask.trim().length()==0)
		{
			System.err.println("No mask found.");
			System.exit(-1);
		}
		paths.addElement(path);
		for(int p=0;p<paths.size();p++)
		{
			path = (String)paths.elementAt(p);
			File pathF=new File(path);
			char[] m=mask.toCharArray();
			if(pathF.exists()&&pathF.isDirectory())
			{
				File[] files=pathF.listFiles();
				for(int f=0;f<files.length;f++)
				{
					File F=files[f];
					if(F.isDirectory()&&recurse)
						paths.addElement(F.getAbsolutePath());
					StringBuffer newName=new StringBuffer("");
					char[] n=F.getName().toCharArray();
					int y=0;
					boolean no=false;
					int replDex=1;
					StringBuffer repl=new StringBuffer("");
					for(x=0;x<m.length;x++)
						if(m[x]=='@')
						{
							if(repl.length()>0)
							{
								if((replDex>=args.length)
								||(!args[replDex].startsWith("X=")))
								{
									System.err.println("Bad1 replacement, "+repl+" doesn't have matching X "+(replDex));
									System.exit(-1);
								}
								String r=args[replDex].substring(2);
								if(r.toString().equalsIgnoreCase("@")||r.toString().equalsIgnoreCase("%"))
									newName.append(repl);
								else
									newName.append(r);
								replDex++;
								repl.setLength(0);
							}
							if((replDex>=args.length)
							||(!args[replDex].startsWith("@=")))
							{
								System.err.println("Bad2 replacement, "+repl+" doesn't match @ "+(replDex));
								System.exit(-1);
							}
							repl.append(args[replDex].substring(2));
							replDex++;
							if(x==m.length-1)
							{
								newName.append(rightReplacement(repl,F.getName().substring(y)));
								y=F.getName().length();
								repl.setLength(0);
								break;
							}
							else
							{
								char stopChar=m[x+1];
								int oy=y;
								while(y<n.length)
								{
									if(n[y]==stopChar)
									{
										newName.append(rightReplacement(repl,F.getName().substring(oy,y)));
										repl.setLength(0);
										break;
									}
									else
										y++;
								}
								if(y==n.length)
								{
									no=true;
									break;
								}
								repl.setLength(0);
							}
						}
						else
						if(m[x]=='%')
						{
							if(repl.length()>0)
							{
								if((replDex>=args.length)
								||(!args[replDex].startsWith("X=")))
								{
									System.err.println("Bad3 replacement, "+repl+" doesn't have matching X "+(replDex));
									System.exit(-1);
								}
								String r=args[replDex].substring(2);
								if(r.toString().equalsIgnoreCase("@")||r.toString().equalsIgnoreCase("%"))
									newName.append(repl);
								else
									newName.append(r);
								replDex++;
								repl.setLength(0);
							}
							if((replDex>=args.length)
							||(!args[replDex].startsWith("%=")))
							{
								System.err.println("Bad4 replacement, "+repl+" doesn't match % "+(replDex));
								System.exit(-1);
							}
							repl.append(args[replDex].substring(2));
							replDex++;
							if(y==n.length)
							{
								no=true;
								break;
							}
							newName.append(rightReplacement(repl,""+n[y]));
							y++;
							repl.setLength(0);
						}
						else
						if(y>=n.length)
						{
							no=true;
							break;
						}
						else
						if(m[x]==n[y])
						{
							repl.append(n[y]);
							y++;
						}
						else
						{
							no=true;
							break;
						}
					if(!no)
					{
						if(repl.length()>0)
						{
							if((replDex>=args.length)
							||(!args[replDex].startsWith("X=")))
							{
								System.err.println("Bad5 replacement, "+repl+" doesn't have matching X "+(replDex));
								System.exit(-1);
							}
							String r=args[replDex].substring(2);
							if(r.toString().equalsIgnoreCase("@")||r.toString().equalsIgnoreCase("%"))
								newName.append(repl);
							else
								newName.append(r);
							replDex++;
							repl.setLength(0);
						}
						if(!path.endsWith(File.separator))
							path+=File.separator;
						System.out.println("Rename "+F.getName()+" to "+newName.toString());
						F.renameTo(new File(path+newName.toString()));
					}
				}
			}
			else
				System.err.println("Error: "+path+" is not a valid path.");
		}
		System.exit(-1);
	}
}
