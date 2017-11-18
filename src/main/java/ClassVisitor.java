import syntaxtree.*;
import visitor.*;
import java.util.*;

public class ClassVisitor extends GJVoidDepthFirst<List<ClassType>>{
	
	public void visit(MainClass n, List<ClassType> classList) {
		String cname = n.f1.f0.toString();
		//check whether classname already exists
		if (Helper.getClass(cname, classList) != null){
			Helper.exit("Type error");
		}

		ClassType newclass = new ClassType(cname);
		newclass.isMain = true; 
		classList.add(newclass);
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
		classList.add(newclass);
	}

}