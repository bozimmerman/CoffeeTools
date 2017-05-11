package com.planet_ink.coffee_tools;

import java.io.*;

public class RLETool 
{
	public RLETool() 
	{
	}

	public static void main(String[] args) 
	{
		if((args.length!=3)
		||((!args[0].equalsIgnoreCase("COMPRESS"))&&(!args[0].equalsIgnoreCase("DECOMPRESS"))))
		{
			System.err.println("USAGE: RLETool COMPRESS RleFile TargetFile");
			System.err.println("USAGE: RLETool DECOMPRESS File RleFile");
			System.exit(-1);
		}
		File file1=new File(args[1]);
		File file2=new File(args[2]);
		if((!file1.exists())||(file1.isDirectory()))
		{
			System.err.println("File not found: "+file1.getAbsolutePath());
			System.exit(-1);
		}
		boolean compress = args[0].equalsIgnoreCase("COMPRESS");
		BufferedInputStream bin=null;
		try
		{
			bin = new BufferedInputStream(new FileInputStream(file1));
			FileOutputStream fout=null;
			try
			{
				fout=new FileOutputStream(file2);
				if(compress)
				{
					int mode=-1;
					int a=bin.read();
					final ByteArrayOutputStream bout=new ByteArrayOutputStream();
					bout.write(a);
					int b=bin.read();
					if(a>=0)
					{
						while(b>=0)
						{
							switch(mode)
							{
							case -1:
								bout.write(b);
								if(a == b)
								{
									mode=2;
								}
								else
								{
									mode=1;
								}
								break;
							case 1:
								if(a == b)
								{
									if(bout.size()>1)
									{
										fout.write((bout.size()-1)&0xff);
										fout.write(bout.toByteArray(),0,bout.size()-1);
									}
									bout.reset();
									bout.write(a);
									bout.write(b);
									mode=2;
								}
								else
								if(bout.size() < 128)
								{
									bout.write(b);
								}
								else
								{
									fout.write(bout.size()&0xff);
									fout.write(bout.toByteArray(),0,bout.size());
									bout.reset();
									bout.write(b);
									mode=-1;
								}
								break;
							case 2:
								if(a != b)
								{
									if(bout.size()>0)
									{
										fout.write(128 | (bout.size()&0xff));
										fout.write(a&0xff);
									}
									bout.reset();
									bout.write(b);
									mode=1;
								}
								else
								if(bout.size() < 127)
								{
									bout.write(b);
								}
								else
								{
									fout.write(128 | (bout.size()&0x7f));
									fout.write(b&0xff);
									bout.reset();
									bout.write(b);
									mode=-1;
								}
							}
							a=b;
							b=bin.read();
						}
						switch(mode)
						{
						case -1:
						case 1:
							if(bout.size()>0)
							{
								fout.write(bout.size()&0xff);
								fout.write(bout.toByteArray(),0,bout.size());
							}
							break;
						case 2:
							if(bout.size()>0)
							{
								fout.write(128 | (bout.size()&0xff));
								fout.write(a&0xff);
							}
						}
					}
				}
				else
				{
					int b=bin.read();
					while(b>=0)
					{
						if(b < 129)
						{
							int i=b;
							while(i-->0)
							{
								b=bin.read();
								if(b<0)
									throw new IOException("Bad byte found.");
								fout.write(b);
							}
						}
						else
						{
							int i = b & 0x7f;
							b=bin.read();
							if(b<0)
								throw new IOException("Bad byte found.");
							while(i-->0)
								fout.write(b);
						}
						b=bin.read();
					}
				}
			}
			finally
			{
				if(fout != null)
					fout.close();
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		finally
		{
			if(bin != null)
			{
				try 
				{
					bin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
