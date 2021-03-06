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
			
			//set up environment used for Java to Vapor
			VaporEnv ve = new VaporEnv(classList);
			
			//Print Class and Methods
			Helper.class_method(classList); 
			
			J2V_visitor jv = new J2V_visitor(ve);
			root.accept(jv);
			
		}catch (ParseException e) {
			Helper.exit("Parse Failed");
		}
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
	public boolean isMain; 

	//constructor
	ClassType (String class_name){
		this.class_name = class_name;
		this.super_class=null;  //init super_class to null
		isMain = false;
	}

	public String toString() {
		return class_name;
	}

	public void setMain(){
		isMain = true;
	}
}


//Type: Method
class Method extends GType{
	String method_name;
	GType return_value;
	String return_name;
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
	public static void exit(String err_msg){
		System.out.println(err_msg);
		System.exit(1);
	}

	//check whether there are two same methods in same class
	public static void method_duplicate(String method_name, ClassType classList){
		for (Method m: classList.methods){
			if (m.equals(method_name)){
				Helper.exit("Duplicate Methods");
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

	//find method offset
	public static int getMethodIdx(String method_name, ClassType curr_class){ 
		int offset = 0; 

		for (int i = 0 ; i < curr_class.methods.size(); i++) {
			if (curr_class.methods.get(i).method_name.equals(method_name)) {
				return i;
			}
		}
		
		offset = curr_class.methods.size();

		ClassType tmp = curr_class.super_class;
		while (tmp != null) {
			for (int i = 0 ; i < tmp.methods.size(); i++) {
				if (tmp.methods.get(i).method_name.equals(method_name)) {
					return i + offset;
				}
			}
			tmp = tmp.super_class;
			offset = offset + tmp.methods.size();
		}

		return -1;
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

	//find fields in given class and all super classes
	public static GType getObject(String obj_name, ClassType curr_class){
			ClassType tmp = curr_class.super_class;
			int index; 

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
	
	
	public static void class_method(List<ClassType> classList){
		for (ClassType ct : classList){
			if (!ct.isMain) {
				List<String> allMethods = new ArrayList<String> ();

				System.out.println("const vmt_" + ct.class_name);
				for (Method m : ct.methods){
					System.out.println("  :" + ct.class_name + "." + m.method_name);
					allMethods.add("m.method_name");
				}

				ClassType tmp = ct.super_class;
				while (tmp != null) {
					for (Method m : tmp.methods){
						//If parent has its own specific methods (child did not overwrite this method)
						if (!allMethods.contains(m.method_name)){
							System.out.println("  :" + tmp.class_name + "." + m.method_name);
							allMethods.add("m.method_name");
						}
					}
					tmp = tmp.super_class;
				}
				System.out.println("");
			}
		}
		System.out.println("");
		return; 
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