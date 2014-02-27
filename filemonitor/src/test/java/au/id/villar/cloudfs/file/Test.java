package au.id.villar.cloudfs.file;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

		Path root = Paths.get("/home/rafael/Desktop/test");
		FileMonitor fileMonitor = new FileMonitor(root);
		RootInfo testingRoot = new RootInfo("XXX", Paths.get("./testings"));
		fileMonitor.start();
//		fileMonitor.registerRoot(testingRoot);

		while (true) Thread.sleep(1000);

//		String jsonString = "{\"uno\": 1, \"dos\": \"this is a simple quote: ', and this is an 'n' followed by a slash (escape for a new line in json): \\n \"}";
//		jsonString = "'" + jsonString.replaceAll("'", "'\\\\''") + "'";
//		System.out.println("echo " + jsonString);

	}

//	private static String encode(String str) {
//		StringBuilder buffer = new StringBuilder(str.length());
//		for(int index = 0; index < str.length(); index++) {
//			char ch = str.charAt(index);
//			if(Character.isLetterOrDigit(ch)) {
//				buffer.append(ch);
//			} else {
//				buffer.append(String.format("_%04X", (int)ch));
//			}
//		}
//		return buffer.toString();
//	}
//
//	private static String decode(String str) {
//		StringBuilder buffer = new StringBuilder(str.length());
//		int pos = 0;
//		int pos2;
//		while((pos2 = str.indexOf('_', pos)) != -1) {
//			buffer.append(str.substring(pos, pos2));
//			char ch = (char)(int)Integer.valueOf(str.substring(pos2 + 1, pos2 + 5), 16);
//			buffer.append(ch);
//			pos = pos2 + 5;
//		}
//		buffer.append(str.substring(pos));
//		return buffer.toString();
//	}

}
