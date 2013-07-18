package tools.loggers;

public class LogManager {
	public static void logConsole(String s){
		System.out.flush();
		System.out.println("[+] " + s);
	}

	public static void logError(String s) {
		System.err.flush();
		System.err.println("[!] " + s);
	}
	
	public static void logFatalError(String s) {
		System.err.flush();
		System.err.println("[!] "+ s);
		System.exit(1);
	}

	public static void logException(String s, Exception e) {
		System.err.flush();
		System.err.println("[-] " + s);
		e.printStackTrace(System.err);
		System.exit(1);
	}
}