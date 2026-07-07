package org.framework.cors;
import org.springframework.context.support.ClassPathXmlApplicationContext;
public class testspring {
public static void main(String[] args) {
	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applications.xml");
	/* Classic music = context.getBean( "classic", Classic.class); */
	/* Musicsetup plauer = new Musicsetup(music); */
	/*
	 * Musicsetup music = context.getBean( "Musicsetup", Musicsetup.class);
	 * Musicsetup music1 = context.getBean( "Musicsetup", Musicsetup.class);
	 * 
//	 Musicsetup plauer = new Musicsetup(music); music.playmusic();
//	 * 
	 * System.out.println(music.getVolume());
	 * System.out.println(music1.getVolume()); music.setName("Perfect");
	 * System.out.println(music.getName()); System.out.println(music1.getName());
	 */
	
	Musicsetup musicsetup = context.getBean("Music",Musicsetup.class);
	musicsetup.Main();
//	System.out.println(musicsetup.getVolume());
//	System.out.println(musicsetup.getName());
	context.close();

}
}
