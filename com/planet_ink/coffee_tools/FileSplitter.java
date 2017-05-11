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
