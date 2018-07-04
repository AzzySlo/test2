package compiler.imcode;

import java.util.*;

import compiler.*;

/**
 * Zaporedje stavkov.
 * 
 * @author sliva
 */
public class ImcSEQ extends ImcStmt {

	/* Stavki.  */
	public LinkedList<ImcStmt> stmts;

	/**
	 * Ustvari zaporedje stavkov.
	 */
	public ImcSEQ() {
		stmts = new LinkedList<>();
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "SEQ");
		Iterator<ImcStmt> stavki = this.stmts.iterator();
		while (stavki.hasNext()) {
			ImcStmt stmt = stavki.next();
			stmt.dump(indent + 2);
		}
	}

	@Override
	public ImcSEQ linear() {
		ImcSEQ lin = new ImcSEQ();
		Iterator<ImcStmt> stavki = this.stmts.iterator();
		while (stavki.hasNext()) {
			ImcStmt stmt = stavki.next();
			ImcSEQ linStmt = stmt.linear();
			lin.stmts.addAll(linStmt.stmts);
		}
		return lin;
	}

}
