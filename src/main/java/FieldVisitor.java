import syntaxtree.*;
import visitor.*;
import java.util.*;


public class FieldVisitor extends GJVoidDepthFirst<List<GType>>{

	List<ClassType> classList;
	private String className;

	FieldVisitor(List<ClassType> classList){
		this.classList = classList;
	}
	
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