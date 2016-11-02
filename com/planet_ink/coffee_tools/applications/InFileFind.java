package com.planet_ink.coffee_tools.applications;

import java.io.*;

public class InFileFind 
{

	public InFileFind() {
		// TODO Auto-generated constructor stub
	}

	private static class BufferedRandomAccessFile
	{
		private long length;
		private byte[] buf;
		final RandomAccessFile rF;
		private volatile long posStart=-1;
		private volatile int bufLength = -1;
		private final char codeName;
		private static final int[] codeNum = { 0 };
		
		public BufferedRandomAccessFile(final String fileName, final int bufferSize) throws IOException
		{
			rF = new RandomAccessFile(fileName,"r");
			length = rF.length();
			buf = new byte[bufferSize];
			synchronized(codeNum)
			{
				codeName = (char)((int)'A' + codeNum[0]);
				codeNum[0]++;
			}
		}
		
		public long length()
		{
			return length;
		}
		
		public int get(long pos) throws IOException
		{
			if((pos<0)||(pos >= length))
				throw new IOException("Index out of range!");
			if((pos >= posStart)&&(pos < posStart + bufLength))
			{
				return buf[(int)(pos - posStart)];
			}
			final long oldPosStart = posStart;
			final int oldBufLength = bufLength;
			if((pos < posStart)&&(pos > posStart - buf.length))
			{
				posStart = posStart - buf.length;
				if(posStart < 0)
					posStart = 0;
			}
			else
				posStart = pos;
			rF.seek(posStart);
			bufLength = buf.length;
			if(posStart + bufLength >= length)
				bufLength = (int)(length - posStart)-1;
			System.out.println(codeName+": "+oldPosStart+"-"+oldBufLength+"  : "+pos+" : "+posStart +"-"+bufLength);
			rF.readFully(buf,0,bufLength);
			return buf[(int)(pos - posStart)];
		}
	}
	
	public static long fileFile(final String parentFileName, final String subFileName) throws IOException
	{
		
		BufferedRandomAccessFile pF = new BufferedRandomAccessFile(parentFileName,65536);
		BufferedRandomAccessFile sF = new BufferedRandomAccessFile(subFileName,65536);
		long i = 0;
		while(i<pF.length())
		{
			long j = 0;
			while(j < sF.length() && i+j < pF.length() && pF.get(i+j) == sF.get(j))
			{ //match
				j++;
			}
			if(j == sF.length()) 
				return i;
			else 
			{ //shift
				if(i+sF.length() < pF.length())
				{
					for(j = sF.length()-1; j >= 0; j--)
					{
						if(sF.get(j) == pF.get(i+sF.length()))
						{
							break;
						}
					}
				}
				i += sF.length()-j;
			}
		}
		return -1;
	}
	
	public static void main(String[] args)
	{
		if(args.length<2)
		{
			System.err.println("Usage: InFileFind [PARENT FILE] [SUB FILE]");
			System.exit(-1);
		}
		try
		{
			String parentFileName=args[0];
			String subFileName=args[1];
			long pos = InFileFind.fileFile(parentFileName, subFileName);
			if(pos < 0)
				System.out.println("Not found");
			else
				System.out.println("Found at index "+pos);
		}		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
