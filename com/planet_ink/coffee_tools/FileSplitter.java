package com.planet_ink.coffee_tools;

import java.io.*;

public class FileSplitter {
	public static void main(String[] args)
	{
		if(args.length==0)
		{
			System.err.println("Usage: FileSplitter [FILE] [SIZE]");
			System.exit(-1);
		}
		BufferedInputStream fi = null;
		try
		{
			String filename=args[0];
			long size = Long.parseLong(args[1]);
			File srcFile = new File(filename);
			fi = new BufferedInputStream(new FileInputStream(srcFile));
			File baseDir = srcFile.getParentFile();
			long allremaining = srcFile.length();
			int num = 1;
			long pos = 0;
			byte[] buffer = new byte[64 * 1024 * 1024];
			while(allremaining > 0)
			{
				allremaining -=size;
				String longStr=("00000"+num);
				File newDir = new File(baseDir,longStr.substring(longStr.length()-3));
				newDir.mkdir();
				File tgtFile = new File(newDir,srcFile.getName());
				FileOutputStream fo=new FileOutputStream(tgtFile);
				long bytesForFile = size;
				long bytesTotalWritten = 0;
				long nextBytes = bytesForFile;
				if(nextBytes > buffer.length)
					nextBytes = buffer.length;
				int bytesRead = fi.read(buffer, 0, (int)nextBytes);
				while(bytesRead > 0)
				{
					bytesTotalWritten += bytesRead;
					bytesForFile -= bytesRead;
					fo.write(buffer,0,bytesRead);
					nextBytes = bytesForFile;
					if(nextBytes > buffer.length)
						nextBytes = buffer.length;
					bytesRead = fi.read(buffer,0,(int)nextBytes);
				}
				System.out.println(pos+"-"+(pos+(bytesTotalWritten-1))+"/"+srcFile.length());
				pos += bytesTotalWritten;
				fo.close();
				num++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(fi != null)
			{
				try 
				{
					fi.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
