package org.framework.cors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("Music")
public class Musicsetup {
	/* @Autowired */
	
	private Musicplayer minus;
	private Musicplayer minu1;
	//  @Value("${Musicsetup.property}")
	  private String name; 
	//  @Value("${Musicsetup.property}")
	  private int volume;
	public Musicsetup(@Qualifier("Classic") Musicplayer minu1,@Qualifier("Liryc") Musicplayer minus)  {
	
	
	
		this.minus = minus;
	   this.minu1 = minu1;
	}

	 
	 
	  
	  public void myMusic() { System.out.println("Your music"); }
	  
	  public void playmusic() {
	  
	  System.out.println(" Play  " + minus.Song()); }
	  
	  public String getName() { return name; }
	  
	  public void setName(String name) { this.name = name; }
	  
	  public  int getVolume() { return volume; }
	  
	 public void setVolume(int volume) { this.volume = volume; }
	  
	  public void myEnd() { System.out.println("Your music is end"); }
	

	
public void Main() {
	System.out.println(" Play " + minu1.Song() + "," + minus.Song()); }
}