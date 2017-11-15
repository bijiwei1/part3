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
	
//generic type for mini java
class GType {

	public static GType getType(Type n, List<ClassType> classList) {
		GType obj;
		if (n.f0.choice instanceof IntegerType) {
			obj = new IntType();
		} else if (n.f0.choice instanceof BooleanType) {
			obj = new BoolType();
		} else if (n.f0.choice instanceof syntaxtree.ArrayType) {
			obj = new ArrayType();
		} else if (n.f0.choice instanceof Identifier) {
			obj = Helper.getClass(((Identifier)(n.f0.choice)).f0.toString(), classList);
		} else {
			obj = null;
		}
		return obj;
	}
}

//Type: Class 
class ClassType extends GType{
	public String class_name;
	public ClassType super_class;
	public GType args;
	public List<Method> methods = new ArrayList<Method>();
	public List<GType> fields = new ArrayList<GType>();
	public List<String> fields_name = new ArrayList<String>();

	//constructor
	ClassType (String class_name){
		this.class_name = class_name;
		this.super_class=null;  //init super_class to null
	}

	public String toString() {
		return class_name;
	}
}


//Type: Method
class Method extends GType{
	String method_name;
	GType return_value;
	List<GType> args = new ArrayList<GType>();
	List<String> args_name = new ArrayList<String>();
	List<GType> vars = new ArrayList<GType>();
	List<String> vars_name = new ArrayList<String>();

	//constructor 
	Method(String method_name){
		this.method_name = method_name;
	}

	public boolean equals(Method m1, Method m2){
		if (m1.method_name != m2.method_name)
			return false;
		return true;
	}

	public void setRV(GType return_value){
		this.return_value = return_value;
	}

	public String toString() {
		return method_name;
	}
}

class Helper{

	//found error and print error message and quit
	public static void exit(int i){
		System.out.println("Type error");
		System.exit(1);
	}

	//check whether there are two same methods in same class
	public static void method_duplicate(String method_name, ClassType classList){
		for (Method m: classList.methods){
			if (m.equals(method_name)){
				Helper.exit(16);
			}
			
		}
	}

	//find class with given class name
	public static ClassType getClass(String class_name, List<ClassType> classList) {
		for (ClassType ct : classList){
			if (class_name.equals(ct.class_name)){
				return ct;
			}
		}
		return null;
	}

	//find method with given class
	public static Method getMethod(String method_name, ClassType curr_class){ 

		//check current class
		for (Method m : curr_class.methods){
			if (m.method_name.equals(method_name)){
				return m;
			}
		}

		//check its super class
		ClassType tmp = curr_class.super_class;
		while(tmp!= null){
			//check current class
			for (Method m : tmp.methods){
				if (m.method_name.equals(method_name)){
					return m;
				}
				tmp = tmp.super_class;
			}
		}
		

		return null; 
	}

	//find object with given object name and linkedlist
	public static GType getObject(String obj_name, Method curr_method, ClassType curr_class){
		ClassType tmp = curr_class.super_class;
		int index; 

		//Find obj in current method args and var
		index = curr_method.vars_name.indexOf(obj_name);
		if ( index != -1 ){
			return curr_method.vars.get(index);
		}

		index = curr_method.args_name.indexOf(obj_name);
		if ( index != -1 ){
			return curr_method.args.get(index);
		}

		//Find obj in current class fields 
		index = curr_class.fields_name.indexOf(obj_name);
		if ( index != -1 ){
			return curr_class.fields.get(index);
		}
		
		//check fields from super class
		while(tmp!= null){
			index = tmp.fields_name.indexOf(obj_name);
			if ( index != -1 ){
				return tmp.fields.get(index);
			}
			tmp = tmp.super_class;
		}

		return null; 
	}


	// check a = b (Type a >= Type b)
	public static boolean isCompatibleTypes(GType a, GType b) {
		if (a instanceof IntType && b instanceof IntType) {
			return true;
		} else if (a instanceof BoolType && b instanceof BoolType) {
			return true;
		} else if (a instanceof ArrayType && b instanceof ArrayType) {
			return true;
		} else if (a instanceof ClassType && b instanceof ClassType) {
			ClassType ca = (ClassType)a;
			ClassType cb = (ClassType)b;
			return isCompatibleClasses(ca, cb);
		}
		return false;
	}

	//check if a is super class of b
	public static boolean isCompatibleClasses(ClassType a, ClassType b){
		ClassType curr_class = b;
		//check if a = b
		if (a == b){
			return true;
		}
		//check if a > b
		while (curr_class.super_class != null){
			if (curr_class.super_class == a){
				return true; 
			}
			else{
				curr_class = curr_class.super_class;
			}
		}
		return false; 
	}

	public static boolean isCompatibleList(List<GType> a, List<GType> b){
		if (a.size() != b.size())
			return false;

		for (int i = 0; i < a.size(); i ++){
			if (!Helper.isCompatibleTypes(a.get(i), b.get(i)))
				return false;
		}

		return true; 

	}

}

class IntType extends GType{
	
	public String toString(){
		return "Int";
	}
}

class BoolType extends GType{
	public String toString(){
		return "Bool";
	}
}

class ArrayType extends GType{
	public String toString(){
		return "Array";
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
