package filegen;

import java.io.FileOutputStream;
import java.io.PrintStream;

public abstract class ClassFileGenerator extends FileGenerator {

	protected String[] imports() {
		return new String[] { };
	}
	protected String extend() {
		return "";
	}
	protected String[] implement() {
		return new String[] { };
	}
	protected String[] methods() {
		return new String[] { };
	}
	protected String[] fields() {
		return new String[] { };
	}
	
	protected void generate(PrintStream file, String packagename) {
		file.println("package " + packagename + ";\n");
		String[] imports = this.imports();
		for(String _import : imports) {
			file.println(String.format("import %s;", _import));
		}
		if(imports.length > 0) {
			file.println();
		}
		file.print("public class " + this.name());
		String e = this.extend();
		file.print(e.length() > 0 ? " extends " + e : "");
		String[] interfaces = implement();
		if(interfaces.length > 0) {
			file.print(" implements");
		}
		boolean first_interface = true;
		for(String i : interfaces) {
			if(first_interface) {
				first_interface = false;
				file.print(" ");
			}else {
				file.print(", ");
			}
			file.print(i);
		}
		file.println(" {");
		for(String field : this.fields()) {
			file.println(field);
		}
		for(String method : this.methods()) {
			file.println(method);
		}
		file.println("}");
	}
}
