package top.microiot;

import java.io.PrintStream;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class IotPlatformApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(IotPlatformApplication.class);
		app.setBanner(new Banner() {

			@Override
			public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
				out.print(
						"\n" +
								"                                                                                                                                               \n" +
								"                                                                                                                                               \n" +
								"MMMMMMMM               MMMMMMMM  iiii                                                           IIIIIIIIII              TTTTTTTTTTTTTTTTTTTTTTT\n" +
								"M:::::::M             M:::::::M i::::i                                                          I::::::::I              T:::::::::::::::::::::T\n" +
								"M::::::::M           M::::::::M  iiii                                                           I::::::::I              T:::::::::::::::::::::T\n" +
								"M:::::::::M         M:::::::::M                                                                 II::::::II              T:::::TT:::::::TT:::::T\n" +
								"M::::::::::M       M::::::::::Miiiiiii     ccccccccccccccccrrrrr   rrrrrrrrr      ooooooooooo     I::::I     oooooooooooTTTTTT  T:::::T  TTTTTT\n" +
								"M:::::::::::M     M:::::::::::Mi:::::i   cc:::::::::::::::cr::::rrr:::::::::r   oo:::::::::::oo   I::::I   oo:::::::::::oo      T:::::T        \n" +
								"M:::::::M::::M   M::::M:::::::M i::::i  c:::::::::::::::::cr:::::::::::::::::r o:::::::::::::::o  I::::I  o:::::::::::::::o     T:::::T        \n" +
								"M::::::M M::::M M::::M M::::::M i::::i c:::::::cccccc:::::crr::::::rrrrr::::::ro:::::ooooo:::::o  I::::I  o:::::ooooo:::::o     T:::::T        \n" +
								"M::::::M  M::::M::::M  M::::::M i::::i c::::::c     ccccccc r:::::r     r:::::ro::::o     o::::o  I::::I  o::::o     o::::o     T:::::T        \n" +
								"M::::::M   M:::::::M   M::::::M i::::i c:::::c              r:::::r     rrrrrrro::::o     o::::o  I::::I  o::::o     o::::o     T:::::T        \n" +
								"M::::::M    M:::::M    M::::::M i::::i c:::::c              r:::::r            o::::o     o::::o  I::::I  o::::o     o::::o     T:::::T        \n" +
								"M::::::M     MMMMM     M::::::M i::::i c::::::c     ccccccc r:::::r            o::::o     o::::o  I::::I  o::::o     o::::o     T:::::T        \n" +
								"M::::::M               M::::::Mi::::::ic:::::::cccccc:::::c r:::::r            o:::::ooooo:::::oII::::::IIo:::::ooooo:::::o   TT:::::::TT      \n" +
								"M::::::M               M::::::Mi::::::i c:::::::::::::::::c r:::::r            o:::::::::::::::oI::::::::Io:::::::::::::::o   T:::::::::T      \n" +
								"M::::::M               M::::::Mi::::::i  cc:::::::::::::::c r:::::r             oo:::::::::::oo I::::::::I oo:::::::::::oo    T:::::::::T      \n" +
								"MMMMMMMM               MMMMMMMMiiiiiiii    cccccccccccccccc rrrrrrr               ooooooooooo   IIIIIIIIII   ooooooooooo      TTTTTTTTTTT      \n" +
								"                                                                                                                                               \n" +
								"                                                                                                                                               \n"
						);
			}
			
		});
		app.run(args);
	}
}