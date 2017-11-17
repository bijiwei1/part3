import syntaxtree.*;
import visitor.*;
import java.util.*;

public class MethodVisitor extends GJVoidDepthFirst<List<GType>>{
	
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
