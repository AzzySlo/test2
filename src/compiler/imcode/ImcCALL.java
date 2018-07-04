package compiler.imcode;

import java.util.*;

import compiler.*;
import compiler.frames.*;

/**
 * Klic funkcije.
 * 
 * @author sliva
 */
public class ImcCALL extends ImcExpr {

	/** Labela funkcije.  */
	public FrmLabel label;

	/** Argumenti funkcijskega klica (vkljucno s FP).  */
	public LinkedList<ImcExpr> args;

	/**
	 * Ustvari nov klic funkcije.
	 * 
	 * @param label Labela funkcije.
	 */
	public ImcCALL(FrmLabel label) {
		this.label = label;
		this.args = new LinkedList<>();
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "CALL label=" + label.name());
		Iterator<ImcExpr> arguments = this.args.iterator();
		while (arguments.hasNext()) {
			ImcExpr arg = arguments.next();
			arg.dump(indent + 2);
		}
	}

	@Override
	public ImcESEQ linear() {
		ImcSEQ linStmt = new ImcSEQ();
		ImcCALL linCall = new ImcCALL(label);
		Iterator<ImcExpr> arguments = this.args.iterator();
		while (arguments.hasNext()) {
			FrmTemp temp = new FrmTemp();
			ImcExpr arg = arguments.next();
			ImcESEQ linArg = arg.linear();
			linStmt.stmts.addAll(((ImcSEQ)linArg.stmt).stmts);
			linStmt.stmts.add(new ImcMOVE(new ImcTEMP(temp), linArg.expr));
			linCall.args.add(new ImcTEMP(temp));
		}
		FrmTemp temp = new FrmTemp();
		linStmt.stmts.add(new ImcMOVE(new ImcTEMP(temp), linCall));
		return new ImcESEQ(linStmt, new ImcTEMP(temp));
	}

}
