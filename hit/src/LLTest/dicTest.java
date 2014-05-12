package LLTest;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

/**
 * test class for the DictionnaryOracle
 * @author Laurent
 *
 */

public class dicTest {

	public static void main(String[] args) {
		Alphabet<String> input = new SimpleAlphabet();
		input.add("a");
		input.add("b");
		input.add("c");
		input.add("d");
		DictionnaryTreeOracle<String,String> dicOracle= new DictionnaryTreeOracle(input);
		Word<String> w0 = Word.fromSymbols("a","b","c","d");
		Word<String> w1 = Word.fromSymbols("0","1","2","3");
		dicOracle.put(w0,w1);
		
		Word<String> p = Word.fromSymbols("a","b");
		Word<String> s = Word.fromSymbols("c","d");
		
		System.out.println(dicOracle.answerQuery(p, s).toString());

	}

}
