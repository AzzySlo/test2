package compiler.frames;

/**
 * Opis zacasne spremenljivke v programu.
 * 
 * @author sliva
 */
public class FrmTemp {

	/** Stevec zacasnih spremenljivk.  */
	private static int count = 0;

	/** Ime te zacasne spremenljivke.  */
	private final int num;

	/**
	 * Ustvari novo zacasno spremenljivko.
	 */
	public FrmTemp() {
		num = count++;
	}

	/**
	 * Vrne ime zacasne spremenljivke.
	 * 
	 * @return Ime zacasne spremenljivke.
	 */
	public String name() {
		return "T" + num;
	}

	@Override
	public boolean equals(Object t) {
            if (getClass() == null);
            return num == ((FrmTemp)t).num;
	}

        @Override
        public int hashCode() {
            return 61 * 7 + this.num;
        }

}
