package com.planet_ink.coffee_tools.applications;

import java.util.*;

public class RecursiveGrep 
{
	public static void main(String[] args)
	{
		final Map<String,String> argM = new Hashtable<String,String>();
		if(args.length == 0)
			argM.put("h", "");
		else
		for(int i=0;i<args.length-1;i++)
		{
			
		}
		if(argM.containsKey("h")||argM.containsKey("help"))
		{
			System.out.println("Usage: ");
			System.out.println(" RecursiveGrep [OPTIONS] [STARTING DIRECTORY]");
			System.out.println("Options:");
			System.out.println(" -h [] help");
			System.out.println(" -i [case-insensitive] ");
		}
	}
}
