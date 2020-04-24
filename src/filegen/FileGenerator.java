package filegen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public abstract class FileGenerator {
	public final void generate(String folder, String packagename) throws IOException {
		String dir_name = folder + "/" + packagename.replace(".", "/");
		new File(dir_name).mkdirs();
		String file_name = dir_name + "/" + this.name() + ".java";
		File f = new File(file_name);
		if(f.exists()) {
			f.delete();
		}
		if(!f.createNewFile()) {
			throw new IOException("Could not generate " + file_name);
		}
		PrintStream ps = new PrintStream(new FileOutputStream(f));
		generate(ps, packagename);
		ps.close();
	}
	protected abstract void generate(PrintStream file, String packagename);
	protected abstract String name();
}
