import syntaxtree.*;
import visitor.*;
import java.util.*;

public class J2V {
	public static void main(String[] args) {
		try {
			Node root = new MiniJavaParser(System.in).Goal();
			//System.out.println("Parse successful");
			
			//Load all classes
			List<ClassType> classList = new ArrayList<ClassType>();
			
			ClassVisitor cv = new ClassVisitor();
			root.accept(cv, classList);		
			
			//Load extends class 
			ClassExtendsVisitor cev = new ClassExtendsVisitor();
			root.accept(cev, classList);

			//Load all methods to classes
			MethodVisitor mv = new MethodVisitor(classList);
			root.accept(mv, null);
			
			//Fields load 
			FieldVisitor fv = new FieldVisitor(classList);
			root.accept(fv, null);
			
			//Print Class and Methods
			Vapor.class_method(classList); 
			
			
		}catch (ParseException e) {
			Helper.exit(0);
		}
	}
}

class Vapor{
	public static void class_method(List<ClassType> classList){
		for (ClassType ct : classList){
			System.out.println("const vmt_" + ct.class_name);
			for (Method m : ct.methods){
				System.out.println("  :" + ct.class_name + "." + m.method_name);
			}
		}
		System.out.println("");
		return; 
	}
}
	

/*
System.out.println("---CLASS DECLARATIONS---");
for (ClassType ct : classList){
	System.out.println(ct.class_name);
	if(ct.super_class != null){
		System.out.println("Superclass is :" + ct.super_class.class_name);
	}
}

System.out.println("---FIELD DECLARATIONS---");
for (ClassType ct : classList) {
	System.out.print(ct.class_name + ":");
	for (int i = 0; i < ct.fields.size(); i++){
			System.out.print(ct.fields.get(i).toString() + "  " + ct.fields_name.get(i) + ", ");
	}
	System.out.println("");
}

System.out.println("---METHOD DECLARATIONS---");
for (ClassType ct : classList) {
	for (Method m : ct.methods) {
		System.out.print(ct.class_name + ": " + m.return_value.toString() + " " + m.method_name + " (");
		for (int i = 0; i < m.args.size(); i++){
			System.out.print(m.args.get(i).toString() + "  " + m.args_name.get(i) + ", ");
		}
		System.out.print(")\n{");
		for (int i = 0; i < m.vars.size(); i++){
			System.out.print(m.vars.get(i).toString() + "  " + m.vars_name.get(i) + ", ");
		}
		System.out.println("}");
	}
}

System.out.println("Typecheck Starts");
*/
