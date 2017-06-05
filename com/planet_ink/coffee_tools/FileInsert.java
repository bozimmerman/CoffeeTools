package com.planet_ink.coffee_tools;

import java.io.*;
import java.util.Arrays;

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
public class FileInsert 
{
	public static void main(String[] args)
	{
		if(args.length<4)
		{
			System.out.println("USAGE: FileInsert [DATA TO INSERT] [DATA LENGTH] [TARGET FILE] [TARGET OFFSET]");
			System.out.println("[DATA TO INSERT] - Pick One:");
			System.out.println("   [FILE] - A file path, optionally ending with @X to start at offset X");
			System.out.println("   [X] - A number X from 0-255 for a byte to insert");
			System.out.println("[DATA LENGTH] - Pick One:");
			System.out.println("   [X] - A number X of bytes to insert");
			System.out.println("   <[N] - Insert enough bytes to make the target file length N");
			System.out.println("   ALL - Insert all bytes (only works with source fila data)");
			System.out.println("[TARGET FILE] - full file path of the file to insert into");
			System.out.println("[TARGET OFFSET] - where in the file to insert.  Pick One:");
			System.out.println("   [X] - A byte offset X, where 0=the beginning");
			System.out.println("   END - Add the bytes to the end of the file");
			System.exit(-1);
		}
		try
		{
			File targF = new File(args[2]);
			if(!targF.exists())
				throw new IOException("Target file doesn't exist: "+targF.getAbsolutePath());
			byte[] bytesToInsert;
			try
			{
				int b=Integer.parseInt(args[0]);
				bytesToInsert = new byte[] { (byte)(b&0xFF) };
			}
			catch(Exception e)
			{
				int offset = 0;
				String fName=args[0];
				int x=fName.lastIndexOf('@');
				if(x>0)
				{
					try
					{
						offset = Integer.parseInt(fName.substring(x+1));
						fName=fName.substring(0, x);
					}
					catch(Exception e1)
					{
					}
				}
				File srcF=new File(fName);
				if(!srcF.exists())
					throw new IOException("Not a byte number, or data file doesn't exist: "+args[0]);
				FileInputStream fin = new FileInputStream(srcF);
				bytesToInsert=new byte[(int)srcF.length()];
				int dex=0;
				while(dex<bytesToInsert.length)
				{
					int justRead = fin.read(bytesToInsert, dex, bytesToInsert.length-dex);
					if(justRead >= 0)
						dex+= justRead;
				}
				if((offset > 0)&&(offset<bytesToInsert.length))
					bytesToInsert = Arrays.copyOfRange(bytesToInsert, offset, bytesToInsert.length);
				fin.close();
			}
			if(!args[1].equalsIgnoreCase("all"))
			{
				int numBytes = bytesToInsert.length;
				try
				{
					if(args[1].startsWith("<"))
					{
						int x=Integer.parseInt(args[1].substring(1));
						if(x < targF.length())
							throw new IOException("Target length less than existing target: "+x+" < " + targF.length());
						numBytes = x - (int)targF.length();
					}
					else
					{
						numBytes=Integer.parseInt(args[1].substring(1));
					}
					if(numBytes < bytesToInsert.length)
						bytesToInsert = Arrays.copyOfRange(bytesToInsert, 0, numBytes);
					else
					if(numBytes > bytesToInsert.length)
					{
						int startPos = bytesToInsert.length;
						bytesToInsert = Arrays.copyOf(bytesToInsert, numBytes);
						while(startPos < bytesToInsert.length)
						{
							for(int x=0;x<startPos && ((startPos+x)<bytesToInsert.length);x++)
								bytesToInsert[startPos + x]=bytesToInsert[x];
							startPos += startPos;
						}
					}
				}
				catch(Exception e)
				{
					throw new IOException("Not a length, or the word all: "+args[1]);
				}
			}
			FileInputStream fin = new FileInputStream(targF);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			long startPos = targF.length();
			if(!args[3].equalsIgnoreCase("end"))
			{
				try
				{
					startPos=Integer.parseInt(args[3]);
				}
				catch(Exception e)
				{
					fin.close();
					throw new IOException("Not a target offset number, or the word end: "+args[3]);
				}
			}
			long pos = 0;
			for(;pos<startPos;pos++)
				bout.write(fin.read());
			bout.write(bytesToInsert);
			for(;pos<targF.length();pos++)
				bout.write(fin.read());
			fin.close();
			bout.close();
			FileOutputStream fout = new FileOutputStream(targF);
			fout.write(bout.toByteArray());
			fout.close();
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
}
