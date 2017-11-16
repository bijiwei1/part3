import syntaxtree.*;
import visitor.*;
import java.util.*;

class J2V_visitor extends GJDepthFirst<Integer, ClassType> {
	private List<ClassType> classList;
	private int avail_reg;
	private Method curr_method;
	private VaporEnv ve; 

	J2V_visitor(List<ClassType> classList) {
		this.classList = classList;
		avail_reg = 0;
		ve = new VaporEnv(classList);
	}

	/**
	 * f0 -> MainClass() f1 -> ( TypeDeclaration() )* f2 -> <EOF>
	 */
	public Integer visit(Goal n, ClassType tmp) {
		System.out.println();
		n.f0.accept(this, null);
		n.f1.accept(this, null);
		n.f2.accept(this, null);
		return J2V_Type.FUNC;
	}
	
	/**
	 * Grammar production:
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
	public Integer visit(MainClass n, ClassType tmp) {
		ClassType curr_class = Helper.getClass(n.f1.f0.toString(), classList);
		System.out.println("func Main()");
		System.out.println("t.0 = HeapAllocZ(" + curr_class.fields.size() * 4 + ")");
		System.out.println("[t.0] = :vmt_" + curr_class.class_name);

		// read all statements
		n.f15.accept(this, curr_class);

		// return
		System.out.println("ret");
		return 0;
	}

	/**
	 * Grammar production: f0 -> "class" f1 -> Identifier() f2 -> "{" f3 -> (
	 * VarDeclaration() )* f4 -> ( MethodDeclaration() )* f5 -> "}"
	 */
	public Integer visit(ClassDeclaration n, ClassType tmp) {
		ClassType curr_class = Helper.getClass(n.f1.f0.toString(), classList);

		n.f4.accept(this, tmp);

		return 0;
	}

	/**
	 * Grammar production:
	 * f0 -> "public"
	 * f1 -> Type()
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( FormalParameterList() )?
	 * f5 -> ")"
	 * f6 -> "{"
	 * f7 -> ( VarDeclaration() )*
	 * f8 -> ( Statement() )*
	 * f9 -> "return"
	 * f10 -> Expression()
	 * f11 -> ";"
	 * f12 -> "}"
	 */
	public Integer visit(MethodDeclaration n, ClassType curr_class) {
		// System.out.println("Current_class is : " + curr_class.toString());
		// System.out.println("Current_method is : " + curr_method.toString());
		curr_method = Helper.getMethod(n.f2.f0.toString(), curr_class);
		System.out.print("func " + curr_class.toString() + "." + curr_method.method_name + "(");
		
		//print args
		if (curr_method.args.isEmpty()) {
			System.out.print("this");
		}else {
			System.out.print("this " + curr_method.args_name.get(0));
			for (int i = 1; i < curr_method.args.size() - 1; i++){
				System.out.print(", this " + curr_method.args_name.get(i));
			}
		}
		System.out.println(")");
		
		// statement
		avail_reg = 0; 
		n.f8.accept(this, curr_class);
		
		// Type
		GType return_type = GType.getType(n.f1, classList);
		// return expression
		GType return_value = n.f10.accept(this, curr_class);

		return 0;
	}
	
	  /**
	   * f0 -> Identifier()
	   * f1 -> "="
	   * f2 -> Expression()
	   * f3 -> ";"
	   */
	  public Integer visit(AssignmentStatement n, ClassType curr_class){
	    Integer _ret=null;
	    String lhs = n.f0.f0.toString();
	    
	    Integer a = n.f2.accept(this);
	    int ticket = env.getIdentifier(identifier);

	    VaporValue v1 = env.variable_map.get(ticket);
	    VaporValue v2 = env.variable_map.get(a);
	    if (v1.class_name != null ) {
	      v1.class_name = v2.class_name;
	    }
	    
	    stmtAssignment(ticket, ve.findVariableEnv(a)); 
	    stmtPrint(env.findVariableEnvStrict(ticket) + " = " + ve.findVariableEnv(a));
	    
	    _ret = ticket;
	    return _ret;
	  }
	  
	void statement_assign(String lhs, String rhs, ClassType curr_class){
		if (rhs == null) {
		      Helper.exit("Failed statement assignment");
		}
		ve.indent();
		System.out.println("[" + lhs + "]= " + rhs);
	}
	
}

