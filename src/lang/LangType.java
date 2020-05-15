package lang;

public abstract class LangType {

	public LangType union(LangType... types) {
		return new UnionType(types);
	}
	public LangType tuple(LangType... types) {
		return new TupleType(types);
	}
	public LangType generic(LangType... constraints) {
		return new GenericType(constraints);
	}
	public LangType array(LangType type) {
		return new ArrayType(type);
	}
	public LangType not(LangType type) {
		return new NotType(type);
	}
	public LangType nullable(LangType type) {
		return new NullableType(type);
	}
	public LangType pointer(LangType type) {
		return new PointerType(type);
	}
	
	public boolean matches(LangType in) {
		return in.subs(this);
	}
	public boolean subs(LangType in) {
		return in.matches(this);
	}
	public LangType make(LangType in) {
		if(this.matches(in)) {
			return this;
		}
		return null;
	}
	
	private static void check(boolean b) throws Exception {
		if(b) {
			return;
		}
		throw new Exception();
	}
	
	// these are looking pretty good :)
	public static void main(String[] args) throws Exception {
		ClassType A = new ClassType(), B = new ClassType(), C = new ClassType();
		ClassType AA = new ClassType(A), AB = new ClassType(A), AC = new ClassType(A);
		ClassType BA = new ClassType(B), BB = new ClassType(B), BC = new ClassType(B);
		ClassType CA = new ClassType(C), CB = new ClassType(C), CC = new ClassType(C);
		ClassType AAA = new ClassType(AA), AAB = new ClassType(AA);
		
		LangType AA_AC = new UnionType(AA, AC), A_AB = new NotType(new UnionType(AA, AC, B, C));
		LangType all = new UnionType(A, B, C);
		
		check(A.matches(AAA));
		check(!AAA.matches(A));
		check(A_AB.matches(AB) && A_AB.matches(A));
		check(!A_AB.matches(AC));
		check(all.matches(AA_AC));
		check(!AA_AC.matches(all));
		check(new ArrayType(all).matches(new ArrayType(AA_AC)));
		check(!new ArrayType(AA_AC).matches(new ArrayType(all)));
		check(new PointerType(all).matches(new PointerType(AA_AC)));
		check(!new PointerType(AA_AC).matches(new PointerType(all)));
		check(new TupleType(all).matches(new TupleType(AC)));
	}
}

abstract class MultiTypeType extends LangType {
	
	protected final LangType[] types;
	public MultiTypeType(LangType... types) {
		this.types = types;
	}
}

abstract class SingleTypeType extends LangType {
	
	protected final LangType type;
	public SingleTypeType(LangType type) {
		this.type = type;
	}
}

class ArrayType extends SingleTypeType {
	
	public boolean matches(LangType in) {
		if(in instanceof ArrayType) {
			return this.type.matches(((ArrayType) in).type);
		}
		return in.subs(this);
	}
	
	public ArrayType(LangType type) {
		super(type);
	}
}

class NotType extends SingleTypeType {
	
	public boolean matches(LangType in) {
		return !this.type.matches(in);
	}
	public boolean subs(LangType in) {
		return !in.matches(this.type);
	}
	
	public NotType(LangType type) {
		super(type);
	}
}

class PointerType extends SingleTypeType {
	
	public boolean matches(LangType in) {
		if(in instanceof PointerType) {
			return this.type.matches(((PointerType) in).type);
		}
		return in.subs(this);
	}
	
	public PointerType(LangType type) {
		super(type);
	}
}

class NullableType extends SingleTypeType {
	
	public boolean matches(LangType in) {
		if(in instanceof NullableType) {
			return this.type.matches(((NullableType) in).type);
		}
		return this.type.matches(in);
	}
	
	public NullableType(LangType type) {
		super(type);
	}
}

class TupleType extends MultiTypeType {
	
	public boolean matches(LangType type) {
		if(type instanceof TupleType) {
			LangType[] others = ((TupleType) type).types;
			if(this.types.length != others.length) {
				return false;
			}
			for(int i = 0; i < this.types.length; i++) {
				if(!(this.types[i].matches(others[i]))) {
					return false;
				}
			}
			return true;
		}
		return type.subs(this);
	}
	
	public TupleType(LangType... types) {
		super(types);
	}
}

class UnionType extends MultiTypeType {
	
	public boolean matches(LangType type) {
		if(type instanceof UnionType) {
			LangType[] others = ((UnionType) type).types;
			for(LangType o : others) {
				boolean found = false;
				for(LangType t : this.types) {
					if(t.matches(o)) {
						found = true;
						break;
					}
				}
				if(!found) {
					return false;
				}
			}
			return true;
		}
		for(LangType t : this.types) {
			if(t.matches(type)) {
				return true;
			}
		}
		return false;
	}
	
	public UnionType(LangType... types) {
		super(types);
	}
}

class ClassType extends SingleTypeType {
	
	public static final ClassType object = new ClassType(null);
	
	public boolean matches(LangType in) {
		if(in instanceof ClassType) {
			return in.subs(this);
		}
		return false;
	}
	public boolean subs(LangType in) {
		if(in instanceof ClassType) {
			return this == in || (this.type != null && this.type.subs(in));
		}
		return false;
	}
	
	public ClassType() {
		this(object);
	}
	public ClassType(ClassType type) {
		super(type);
	}
}

class GenericType extends MultiTypeType {
	
	public boolean matches(LangType in) {
		for(LangType t : this.types) {
			if(!t.matches(in)) {
				return false;
			}
		}
		return true;
	}
	
	public LangType make(LangType in) {
		if(this.matches(in)) {
			return in;
		}
		return null;
	}
	
	public GenericType(LangType... constraints) {
		super(constraints);
	}
}
