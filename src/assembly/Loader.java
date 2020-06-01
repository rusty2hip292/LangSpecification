package assembly;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Loader {
	
	public static List<List<String>> load(String filename) {
		return load(new File(filename));
	}
	public static List<List<String>> load(File in) {
		List<List<String>> list = new LinkedList<List<String>>();
		try(Scanner s = new Scanner(in)) {
			while(s.hasNextLine()) {
				LinkedList<String> line = new LinkedList<String>();
				Scanner sline = new Scanner(s.nextLine());
				while(sline.hasNext()) {
					line.add(sline.next());
				}
				sline.close();
				if(line.size() > 0) {
					list.add(line);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
