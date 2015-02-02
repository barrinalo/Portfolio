package com.pluripotence.celltd;

public class JournalEntry {
	public String SmallIcon, MidShowcase, LeftShowcase, RightShowcase, Title, Description;
	
	public JournalEntry(String SmallIcon, String MidShowcase, String LeftShowcase, String RightShowcase, String Title, String Description)
	{
		this.SmallIcon = SmallIcon;
		this.MidShowcase = MidShowcase;
		this.LeftShowcase = LeftShowcase;
		this.RightShowcase = RightShowcase;
		this.Title = Title;
		this.Description = Description;
	}
}
