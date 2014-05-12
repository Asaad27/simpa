package LLTest;


import java.util.HashMap;
import java.util.Map;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.SUL;
import de.learnlib.oracles.AbstractSingleQueryOracle;

/**
 * naive implementation of a dictionary mechanism to interface with learnlib oracles
 * @author Laurent
 *
 * @param <I> input type
 * @param <O> output type
 */


public class DictionnaryOracle<I, O> extends AbstractSingleQueryOracle<I, Word<O>>  implements MealyMembershipOracle<I,O>{
	
	private SUL<I,O> sul;
	public Map<Word<I>,Word<O>> dictionary;
	
	public DictionnaryOracle(){
		dictionary = new HashMap<Word<I>,Word<O>>();
	}

	public DictionnaryOracle(SUL<I,O> sul){
		this.sul=sul;
		dictionary = new HashMap<Word<I>,Word<O>>();
	}
	

	@Override
	public Word<O> answerQuery(Word<I> prefix, Word<I> suffix) {
		Word<I> input = prefix.concat(suffix);
		for(Word<I> w : dictionary.keySet()){
			if(input.isPrefixOf(w)){
				
				
				return dictionary.get(w).subWord(prefix.length(),prefix.length()+suffix.length());
				
			}
		}
		sul.pre();
		
		// Prefix:
		WordBuilder<O> wb = new WordBuilder<>(input.length());
		for (I sym : prefix)
			wb.add(sul.step(sym));

		// Suffix
		for (I sym : suffix)
			wb.add(sul.step(sym));

		Word<O> answer = wb.toWord();
		dictionary.put(input, answer);

		return answer.suffix(suffix.length());
		
		
	}
	
	public boolean checkConsistency(){
		for(Word<I> pre : dictionary.keySet()){
			for(Word<I> w : dictionary.keySet()){
				if(pre.isPrefixOf(w)){
					try{
						if(!dictionary.get(pre).equals(dictionary.get(w).prefix(pre.length()))){
							System.out.println("pre :"+pre);
							System.out.println("w :"+w);
							System.out.println("pre out :"+dictionary.get(pre));
							System.out.println("w out :"+dictionary.get(w));
							System.out.println("w out cut:"+dictionary.get(w).prefix(pre.length()));
							return false;
						}
					}
						catch(java.lang.IndexOutOfBoundsException e){
							System.out.println("pre :"+pre);
							System.out.println("pre length :"+pre.length());
							System.out.println("w :"+w);
							throw(e);
						}
					
					
				}
			}
		}
		return true;
	}
	
	
	
	

		
		
	

}
