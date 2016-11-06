package com.planet_ink.coffee_tools.applications;

import java.io.*;
import java.util.*;

public class FileCompare 
{
	public static boolean compareFiles(File file1, File file2) 
	{
		if((!file1.exists())||(!file1.canRead()))
		{
			System.out.println("Can not open file: "+file1.getName());
			System.exit(-1);
		}
		if((!file2.exists())||(!file2.canRead()))
		{
			System.out.println("Can not open file: "+file2.getName());
			System.exit(-1);
		}
		try {
			FileInputStream fstream1=new FileInputStream(file1);
			FileInputStream fstream2=new FileInputStream(file2);
			int c1=0;
			int c2=0;
			long pos=0;
			long firstFailPos=-1;
			int fc1=0;
			int fc2=0;
			long lastFailPos=-1;
			long totalFails=0;
			long DOT_INTERVAL = ((file1.length() > file2.length()) ? file2.length() : file1.length() / 80);
			long NEXTDOT = DOT_INTERVAL;
			while((c1>=0)&&(c2>=0)) 
			{
				c1=fstream1.read();
				c2=fstream2.read();
				if(c1!=c2) 
				{
					if(firstFailPos<0)
					{
						firstFailPos=pos;
						fc1=c1;
						fc2=c2;
					}
					totalFails++;
					lastFailPos=pos;
				}
				pos++;
				if(pos > NEXTDOT)
				{
					NEXTDOT += DOT_INTERVAL;
					System.out.print(".");
				}
			}
			if(totalFails>0)
			{
				System.out.println("Fail.  Mismatches "+totalFails+"/"+pos+" ("+Math.round((double)totalFails/(double)pos*100.0)+"%)");
				System.out.println("First @"+firstFailPos+", Last @"+lastFailPos+", first char mismatch="+fc1+"!="+fc2);
			}
			else
				System.out.println("They are the same.");
			fstream1.close();
			fstream2.close();
		} 
		catch(java.io.IOException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
	}
	
	public static void compareAll(File file, boolean delDups) 
	{
		if((!file.exists())||(!file.canRead())||(!file.isDirectory()))
		{
			System.out.println("Can not open directory: "+file.getName());
			System.exit(-1);
		}
		Hashtable<String,Long> filenameHash=new Hashtable<String,Long>();
		Hashtable<Long,Vector<Object>> chksumHash=new Hashtable<Long,Vector<Object>>();
		File[] files=file.listFiles();
		for(int f=0;f<files.length;f++) 
		{
			File F=files[f];
			try 
			{
				FileInputStream fstream1=new FileInputStream(F);
				int c1=0;
				long pos=0;
				long chksum=0;
				while(c1>=0) 
				{
					c1=fstream1.read();
					pos++;
					chksum += (pos ^ c1);
				}
				filenameHash.put(F.getName(),new Long(chksum));
				Vector<Object> V=chksumHash.get(new Long(chksum));
				if(V==null) 
				{
					V=new Vector<Object>();
					chksumHash.put(new Long(chksum),V);
				}
				V.addElement(F);
				fstream1.close();
			} 
			catch(java.io.IOException e) 
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		for(Enumeration<Long> e=chksumHash.keys();e.hasMoreElements();) 
		{
			Long L=e.nextElement();
			Vector<Object> V=chksumHash.get(L);
			if(V.size()>0) 
			{
				for(int v1=0;v1<V.size();v1++)
				{
					for(int v2=v1+1;v2<V.size();v2++)
					{
						File F1=(File)V.elementAt(v1);
						File F2=(File)V.elementAt(v2);
						if(compareFiles(F1,F2)) 
						{
							if(delDups) 
							{
								System.out.println(F2.getName() +" deleted");
								V.remove(F2);
								F2.delete();
								v2--;
							} 
							else 
								System.out.println(F1.getName() +" same as " + F2.getName());
						} 
						else 
						{
							System.out.println(F1.getName() +" different than " + F2.getName());
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) 
	{
		
		if(args.length!=2) 
		{
			System.out.println("USAGE: FileCompare File1 File2");
			System.out.println("USAGE: FileCompare ALL DirPath");
			System.out.println("USAGE: FileCompare DELDUPS DirPath");
			System.exit(-1);
		}
		if(args[0].equalsIgnoreCase("ALL")) 
		{
			File file=new File(args[1]);
			compareAll(file,false);
			
		} 
		else
		if(args[0].equalsIgnoreCase("DELDUPS")) 
		{
			File file=new File(args[1]);
			compareAll(file,true);
			
		} 
		else 
		{
			File file1=new File(args[0]);
			File file2=new File(args[1]);
			compareFiles(file1,file2);
		}
	}
}
