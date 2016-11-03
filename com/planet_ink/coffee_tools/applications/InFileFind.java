package com.planet_ink.coffee_tools.applications;

import java.io.*;
import java.util.*;

public class InFileFind 
{
	public InFileFind() 
	{
	}

	private static class BufferedRandomAccessFile
	{
		private long length;
		private byte[] buf;
		final RandomAccessFile rF;
		private volatile long posStart=-1;
		private volatile int bufLength = -1;
		//private final char codeName;
		private static final int[] codeNum = { 0 };
		
		public BufferedRandomAccessFile(final String fileName, final int bufferSize) throws IOException
		{
			rF = new RandomAccessFile(fileName,"r");
			length = rF.length();
			buf = new byte[bufferSize];
			synchronized(codeNum)
			{
				//codeName = (char)((int)'A' + codeNum[0]);
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
			//long oldPosStart = posStart;
			//int oldLength = bufLength;
			if((pos < posStart)&&(pos > posStart - (buf.length/2)))
			{
				posStart = posStart - (buf.length/2);
				if(posStart < 0)
					posStart = 0;
			}
			else
				posStart = pos;
			rF.seek(posStart);
			bufLength = buf.length;
			if(posStart + bufLength >= length)
				bufLength = (int)(length - posStart);
			//System.out.println(codeName+": "+oldPosStart+"-"+oldLength+"  : "+pos+" : "+posStart +"-"+bufLength);
			rF.readFully(buf,0,bufLength);
			return buf[(int)(pos - posStart)];
		}
	}
	
	public static long fileFile(final String parentFileName, final String subFileName) throws IOException
	{
		final int BUFFER_SIZE = 65536 * 1024;
		BufferedRandomAccessFile pF = new BufferedRandomAccessFile(parentFileName,BUFFER_SIZE);
		BufferedRandomAccessFile sF = new BufferedRandomAccessFile(subFileName,BUFFER_SIZE);
		long[] sFmap=new long[256];
		Arrays.fill(sFmap, -1);
		int fillRemains = 256;
		sF.get(0); // preload from the front
		for(long j = sF.length()-1; j >= 0 && (fillRemains>0); j--)
		{
			if(sFmap[sF.get(j) & 0xff]<0)
			{
				sFmap[sF.get(j) & 0xff]=j;
				fillRemains--;
			}
		}
		long i = 0;
		long DOT_PACE=pF.length() / 80;
		long NEXT_DOT = DOT_PACE;
		while(i<pF.length())
		{
			long j = 0;
			while(j < sF.length() && i+j < pF.length() && pF.get(i+j) == sF.get(j))
			{ //match
				j++;
			}
			if(j == sF.length()) 
			{
				System.out.println("!");
				return i;
			}
			else 
			{ //shift
				if(i+sF.length() < pF.length())
				{
					long k=sFmap[pF.get(i+sF.length()) & 0xff];
					if(k >= 0)
						j=k;
				}
				i += sF.length()-j;
				while(i>NEXT_DOT)
				{
					NEXT_DOT += DOT_PACE;
					System.out.print(".");
				}
			}
		}
		System.out.println("!");
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
