package compiler.imcode;

import compiler.*;

/**
 * Binarna operacija.
 * 
 * @author sliva
 */
public class ImcBINOP extends ImcExpr {

	public static final int ADD = 0;
	public static final int SUB = 1;
	public static final int MUL = 2;
	public static final int DIV = 3;
	public static final int EQU = 4;
	public static final int NEQ = 5;
	public static final int LTH = 6;
	public static final int GTH = 7;
	public static final int LEQ = 8;
	public static final int GEQ = 9;
	public static final int AND = 10;
	public static final int OR  = 11;

	/** Operator.  */
	public int op;

	/** Koda levega podizraza.  */
	public ImcExpr limc;

	/** Koda desnega podizraza.  */
	public ImcExpr rimc;

	/**
	 * Ustvari novo binarno operacijo.
	 * 
	 * @param op Operator.
	 * @param limc Levi podizraz.
	 * @param rimc Desni podizraz.
	 */
	public ImcBINOP(int op, ImcExpr limc, ImcExpr rimc) {
		this.op = op;
		this.limc = limc;
		this.rimc = rimc;
	}

	@Override
	public void dump(int indent) {
		String operator = null;
		switch (this.op) {
		case ADD: operator = "+" ; break;
		case SUB: operator = "-" ; break;
		case MUL: operator = "*" ; break;
		case DIV: operator = "/" ; break;
		case EQU: operator = "=="; break;
		case NEQ: operator = "!="; break;
		case LTH: operator = "<" ; break;
		case GTH: operator = ">" ; break;
		case LEQ: operator = "<="; break;
		case GEQ: operator = ">="; break;
		case AND: operator = "&" ; break;
		case OR : operator = "|" ; break;
		}
		Report.dump(indent, "BINOP op=" + operator);
		limc.dump(indent + 2);
		rimc.dump(indent + 2);
	}

	@Override
	public ImcESEQ linear() {
		ImcESEQ Left = this.limc.linear();
		ImcESEQ Right = this.rimc.linear();
		ImcSEQ stmt = new ImcSEQ();
		stmt.stmts.addAll(((ImcSEQ)Left.stmt).stmts);
		stmt.stmts.addAll(((ImcSEQ)Right.stmt).stmts);
		ImcESEQ lin = new ImcESEQ(stmt, new ImcBINOP(op, Left.expr, Right.expr));
		return lin;
	}

}
