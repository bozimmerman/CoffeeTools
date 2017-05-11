package com.planet_ink.coffee_tools;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;


public class ImageCopy {

	public static void main(String[] args)
	{
		if(args.length<2)
		{
			System.err.println("Usage: ImageCopy [DEVICE] [IMAGE FILE]");
			System.err.println("DEVICE = \\\\.\\GLOBALROOT\\ArcName\\multi(0)disk(0)rdisk(3)");
			System.exit(-1);
		}
		try
		{
			String imageDevice=args[0];
			String fileName=args[1];
			/*
			File diskRoot = new File ("\\\\.\\PhysicalDrive3");
			FileOutputStream fout = new FileOutputStream("c:\\tmp\\blah.iso");
			RandomAccessFile diskAccess = new RandomAccessFile (diskRoot, "r");
			int bytesRead = 0;
			byte[] content = new byte[4096];
			while(bytesRead >= 0)
			{
				bytesRead = diskAccess.read (content);
				if(bytesRead > 0)
					fout.write(content, 0, bytesRead);
			}
			fout.close();
			diskAccess.close();
			*/
			//String pathname;
			// Full drive:
			//pathname = "\\\\.\\PhysicalDrive3";
			// A partition (also works if windows doesn't recognize it):
			//pathname = "\\\\.\\PCIROOT(0)\\PCI(1404)\\PCI(0500)\\RAID(P02T00L00)";
			//pathname = "\\\\.\\GLOBALROOT\\Device\\ImDisk3\\";
			//pathname = "\\\\.\\GLOBALROOT\\ArcName\\multi(0)disk(0)rdisk(3)";

			Path diskRoot = ( new File( imageDevice ) ).toPath();

			FileChannel fc = FileChannel.open( diskRoot, StandardOpenOption.READ, StandardOpenOption.WRITE );
			FileOutputStream fout = new FileOutputStream(fileName);

			ByteBuffer bb = ByteBuffer.allocate( 4096 );
			int bytesRead = fc.read(bb);
			while(bytesRead >= 0)
			{
				fout.write(bb.array(),0,bytesRead);
				bb.clear();
				bytesRead = fc.read(bb);
			}
			fout.close();
			fc.close();
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
