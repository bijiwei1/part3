import syntaxtree.*;
import visitor.*;
import java.util.*;

class ClassVisitor extends GJVoidDepthFirst<List<ClassType>>{
	
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

class ClassExtendsVisitor extends GJVoidDepthFirst<List<ClassType>> {

	public void visit(ClassExtendsDeclaration n, List<ClassType> classList) {
		String cname = n.f1.f0.toString();
		String super_cname = n.f3.f0.toString();
		ClassType super_class = Helper.getClass(super_cname, classList);
		ClassType curr_class = Helper.getClass(cname, classList);

		//check whether super class exists
		if (super_class == null || curr_class == null){
			Helper.exit("Type error");
		}
		curr_class.super_class = super_class;
	}
}

class MethodVisitor extends GJVoidDepthFirst<List<GType>>{
	
	//ClassType curr_class;
	List<ClassType> classList;
	private String className;
	private String methodName;

	//constructor
	MethodVisitor(List<ClassType> classList){
		this.classList = classList;
	}

	//TypeDeclaration  
	public void visit(Goal n, List<GType> tmp) {
		n.f1.accept(this, tmp);
	}

	public void visit(ClassDeclaration n, List<GType> tmp) {
		this.className = n.f1.f0.toString();
		//method
		n.f4.accept(this, tmp);
	}
	
	public void visit(ClassExtendsDeclaration n, List<GType> tmp) {
		this.className = n.f1.f0.toString();
		//method
		n.f6.accept(this, tmp);
	}
	
	public void visit(MethodDeclaration n, List<GType> tmp) {

		ClassType curr_class = Helper.getClass(className,classList);
		if (curr_class == null){
			Helper.exit("Type error");
		}

		this.methodName = n.f2.f0.toString();
		//check whether this method name already exist
		Helper.method_duplicate(this.methodName, curr_class);	
		Method m = new Method(methodName);
		curr_class.methods.add(m);

		//set return value
		GType returnValue = GType.getType(n.f1, classList);
		m.setRV(returnValue);
		//FormalParameterList
		n.f4.accept(this, tmp);
		//VarDeclaration
		n.f7.accept(this, tmp);
	}

	public void visit(VarDeclaration n, List<GType> tmp) {
		ClassType curr_class = Helper.getClass(this.className, classList); 
		if (curr_class == null){
			Helper.exit("Type error");
		}
		Method curr_method = Helper.getMethod(methodName, curr_class);
		if (curr_method == null){
			Helper.exit("Type error");
		}

		GType new_var = GType.getType(n.f0, classList);
		curr_method.vars.add(new_var);
		curr_method.vars_name.add(n.f1.f0.toString());
		//System.out.println("Add var "+ n.f1.f0.toString() + "for method: " +curr_method.toString());
	}

	public void visit(FormalParameter n, List<GType> tmp){
		ClassType curr_class = Helper.getClass(this.className, classList); 
		if (curr_class == null){
			Helper.exit("Type error");
		}
		Method curr_method = Helper.getMethod(methodName, curr_class);
		if (curr_method == null){
			Helper.exit("Type error");
		}

		GType new_arg = GType.getType(n.f0, classList);
		curr_method.args.add(new_arg);
		curr_method.args_name.add(n.f1.f0.toString());
		//System.out.println("Add arg "+ n.f1.f0.toString() + "for method: " +curr_method.toString());
	}
	
}


class FieldVisitor extends GJVoidDepthFirst<List<GType>>{

	List<ClassType> classList;
	private String className;

	FieldVisitor(List<ClassType> classList){
		this.classList = classList;
	}
	
	// 
	public void visit(Goal n,  List<GType> tmp) {
		n.f1.accept(this, tmp);
	}

	// VarDeclaration() 
	public void visit(ClassDeclaration n, List<GType> tmp) {
		this.className = n.f1.f0.toString();
		n.f3.accept(this, tmp);
	}
	
	// VarDeclaration() 
	public void visit(ClassExtendsDeclaration n, List<GType> tmp) {
		this.className = n.f1.f0.toString();
		n.f5.accept(this, tmp);
	}


	public void visit(VarDeclaration n, List<GType> tmp) {

		ClassType curr_class = Helper.getClass(className,classList);
		if (curr_class == null){
			Helper.exit("Type error");
		}

		String field_name = n.f1.f0.toString();
		GType var = GType.getType(n.f0, classList);
		curr_class.fields.add(var);	
		curr_class.fields_name.add(field_name);
	}

}
