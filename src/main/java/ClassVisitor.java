import syntaxtree.*;
import visitor.*;
import java.util.*;

public class ClassVisitor extends GJVoidDepthFirst<List<ClassType>>{
	
	  /**
	   * f0 -> "class"
	   * f1 -> Identifier()
	   * f2 -> "{"
	   * f3 -> "public"
	   * f4 -> "static"
	   * f5 -> "void"
	   * f6 -> "main"
	   * f7 -> "("
	   * f8 -> "String"
	   * f9 -> "["
	   * f10 -> "]"
	   * f11 -> Identifier()
	   * f12 -> ")"
	   * f13 -> "{"
	   * f14 -> ( VarDeclaration() )*
	   * f15 -> ( Statement() )*
	   * f16 -> "}"
	   * f17 -> "}"
	   */
	public void visit(MainClass n, List<ClassType> classList) {
		String cname = n.f1.f0.toString();
		//check whether classname already exists
		if (Helper.getClass(cname, classList) != null){
			Helper.exit("Type error");
		}

		ClassType newclass = new ClassType(cname);
		newclass.isMain = true; 
		classList.add(newclass);
		
		Method m = new Method("main");
		newclass.methods.add(m);	
	}
	
	public void visit(ClassDeclaration n, List<ClassType> classList) {
		//load class into class tree
		String cname = n.f1.f0.toString();	
		//check whether classname already exists
		if (Helper.getClass(cname, classList) != null){
			Helper.exit("Type error");
		}

		ClassType newclass = new ClassType(cname);
		newclass.isMain = false;
		classList.add(newclass);
	}
	

	public void visit(ClassExtendsDeclaration n, List<ClassType> classList) {
		String cname = n.f1.f0.toString();
		//check whether classname already exists
		if (Helper.getClass(cname, classList) != null){
			Helper.exit("Type error");
		}
		ClassType newclass = new ClassType(cname);
		newclass.isMain = false;
		classList.add(newclass);
	}

}