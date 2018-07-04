package compiler.imcode;

import compiler.*;

/**
 * Prenos.
 * 
 * @author sliva
 */
public class ImcMOVE extends ImcStmt {

	/** Ponor.  */
	public ImcExpr dst;

	/** Izvor.  */
	public ImcExpr src;

	/** Ustvari nov prenos.
	 * 
	 * @param dst Ponor.
	 * @param src Izvor.
	 */
	public ImcMOVE(ImcExpr dst, ImcExpr src) {
		this.dst = dst;
		this.src = src;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "MOVE");
		dst.dump(indent + 2);
		src.dump(indent + 2);
	}

	@Override
	public ImcSEQ linear() {
		ImcSEQ lin = new ImcSEQ();
		ImcESEQ destination = this.dst.linear();
		ImcESEQ source = this.src.linear();
		lin.stmts.addAll(((ImcSEQ)destination.stmt).stmts);
		lin.stmts.addAll(((ImcSEQ)source.stmt).stmts);
		lin.stmts.add(new ImcMOVE(destination.expr, source.expr));
		return lin;
	}

}
