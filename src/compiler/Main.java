package compiler;

import compiler.lexan.*;
import compiler.synan.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.interpreter.Interpreter;

/**
 * Osnovni razred prevajalnika, ki vodi izvajanje celotnega procesa prevajanja.
 * 
 * @author sliva
 */
public class Main {

	/** Ime izvorne datoteke. */
	private static String sourceFileName;
        
        public static ImcCodeGen imcodegen;

	/**
	 * Metoda, ki izvede celotni proces prevajanja.
	 * 
	 * @param args
	 *            Parametri ukazne vrstice.
	 */
	public static void main(String[] args) {
		System.out.printf("This is PREV compiler, v0.1:\n");
                
                // Ime izvorne datoteke.
                if (sourceFileName == null)
                        sourceFileName = args[0];
                else
                        Report.warning("Source file name '" + sourceFileName + "' ignored.");
		
		if (sourceFileName == null)
			Report.error("Source file name not specified.");

		// Odpiranje datoteke z vmesnimi rezultati.
		Report.openDumpFile(sourceFileName);

		// Izvajanje faz prevajanja.
		
                // Leksikalna analiza.
                LexAn lexAn = new LexAn(sourceFileName, false);
                // Sintaksna analiza.
                SynAn synAn = new SynAn(lexAn, false);
                AbsTree source = synAn.parse();
                // Abstraktna sintaksa.
                Abstr ast = new Abstr(false);
                // Semanticna analiza.
                SemAn semAn = new SemAn(false);
                source.accept(new NameChecker());
                source.accept(new TypeChecker());
                // Klicni zapisi.
                Frames frames = new Frames(false);
                source.accept(new FrmEvaluator());
                // Vmesna koda.
                ImCode imcode = new ImCode(false);
                imcodegen = new ImcCodeGen();
                source.accept(imcodegen);
                imcodegen.chunks.stream().filter(
                        (chunk) -> (chunk instanceof ImcCodeChunk)).map(
                        (chunk) -> (ImcCodeChunk)chunk).forEach((a) -> {
                            a.lincode = a.imcode.linear();
                        });
                imcodegen.chunks.stream().filter(
                        (chunk) -> (chunk instanceof ImcCodeChunk)).map(
                        (chunk) -> (ImcCodeChunk)chunk).forEach((a) -> {
                            a.lincode.dump(0);
                        });
                int i = 0;
                for (ImcChunk chunk : imcodegen.chunks) {
                    if (chunk instanceof ImcCodeChunk){
                        ImcCodeChunk a = (ImcCodeChunk)chunk;
                        if (a.frame.label.name().toLowerCase().equals("_main")){
                            i++;
                            Interpreter interpreter = new Interpreter(a.frame,a.lincode.linear());
                            break;
                        }
                    }
                }
                if(i == 0)
                    Report.error("Missing main");

                

		// Zapiranje datoteke z vmesnimi rezultati.
		Report.closeDumpFile();

		System.out.printf(":-) Done.\n");
		System.exit(0);
	}
}
