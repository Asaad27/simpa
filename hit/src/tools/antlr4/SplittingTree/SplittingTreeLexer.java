// Generated from SplittingTree.g4 by ANTLR 4.4
package tools.antlr4.SplittingTree;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SplittingTreeLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__2=1, T__1=2, T__0=3, NUMBER=4, ID=5, COMMENT=6, LINE_COMMENT=7, PREPROC=8, 
		WS=9;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'"
	};
	public static final String[] ruleNames = {
		"T__2", "T__1", "T__0", "NUMBER", "DIGIT", "ID", "LETTER", "COMMENT", 
		"LINE_COMMENT", "PREPROC", "WS"
	};


	public SplittingTreeLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SplittingTree.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\13~\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\3\2\3\2\3\3\3\3\3\4\3\4\3\5\5\5!\n\5\3\5\3\5\6\5%\n\5\r\5"+
		"\16\5&\3\5\6\5*\n\5\r\5\16\5+\3\5\3\5\7\5\60\n\5\f\5\16\5\63\13\5\5\5"+
		"\65\n\5\5\5\67\n\5\3\6\3\6\3\7\3\7\3\7\7\7>\n\7\f\7\16\7A\13\7\3\7\3\7"+
		"\3\7\7\7F\n\7\f\7\16\7I\13\7\5\7K\n\7\3\b\3\b\3\t\3\t\3\t\3\t\7\tS\n\t"+
		"\f\t\16\tV\13\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\7\na\n\n\f\n\16\n"+
		"d\13\n\3\n\5\ng\n\n\3\n\3\n\3\n\3\n\3\13\3\13\7\13o\n\13\f\13\16\13r\13"+
		"\13\3\13\3\13\3\13\3\13\3\f\6\fy\n\f\r\f\16\fz\3\f\3\f\5Tbp\2\r\3\3\5"+
		"\4\7\5\t\6\13\2\r\7\17\2\21\b\23\t\25\n\27\13\3\2\4\6\2C\\aac|\u0082\u0101"+
		"\4\2\13\f\17\17\u008b\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2"+
		"\2\r\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\3\31"+
		"\3\2\2\2\5\33\3\2\2\2\7\35\3\2\2\2\t \3\2\2\2\138\3\2\2\2\rJ\3\2\2\2\17"+
		"L\3\2\2\2\21N\3\2\2\2\23\\\3\2\2\2\25l\3\2\2\2\27x\3\2\2\2\31\32\7*\2"+
		"\2\32\4\3\2\2\2\33\34\7+\2\2\34\6\3\2\2\2\35\36\7=\2\2\36\b\3\2\2\2\37"+
		"!\7/\2\2 \37\3\2\2\2 !\3\2\2\2!\66\3\2\2\2\"$\7\60\2\2#%\5\13\6\2$#\3"+
		"\2\2\2%&\3\2\2\2&$\3\2\2\2&\'\3\2\2\2\'\67\3\2\2\2(*\5\13\6\2)(\3\2\2"+
		"\2*+\3\2\2\2+)\3\2\2\2+,\3\2\2\2,\64\3\2\2\2-\61\7\60\2\2.\60\5\13\6\2"+
		"/.\3\2\2\2\60\63\3\2\2\2\61/\3\2\2\2\61\62\3\2\2\2\62\65\3\2\2\2\63\61"+
		"\3\2\2\2\64-\3\2\2\2\64\65\3\2\2\2\65\67\3\2\2\2\66\"\3\2\2\2\66)\3\2"+
		"\2\2\67\n\3\2\2\289\4\62;\29\f\3\2\2\2:?\5\17\b\2;>\5\17\b\2<>\5\13\6"+
		"\2=;\3\2\2\2=<\3\2\2\2>A\3\2\2\2?=\3\2\2\2?@\3\2\2\2@K\3\2\2\2A?\3\2\2"+
		"\2BG\5\13\6\2CF\5\17\b\2DF\5\13\6\2EC\3\2\2\2ED\3\2\2\2FI\3\2\2\2GE\3"+
		"\2\2\2GH\3\2\2\2HK\3\2\2\2IG\3\2\2\2J:\3\2\2\2JB\3\2\2\2K\16\3\2\2\2L"+
		"M\t\2\2\2M\20\3\2\2\2NO\7\61\2\2OP\7,\2\2PT\3\2\2\2QS\13\2\2\2RQ\3\2\2"+
		"\2SV\3\2\2\2TU\3\2\2\2TR\3\2\2\2UW\3\2\2\2VT\3\2\2\2WX\7,\2\2XY\7\61\2"+
		"\2YZ\3\2\2\2Z[\b\t\2\2[\22\3\2\2\2\\]\7\61\2\2]^\7\61\2\2^b\3\2\2\2_a"+
		"\13\2\2\2`_\3\2\2\2ad\3\2\2\2bc\3\2\2\2b`\3\2\2\2cf\3\2\2\2db\3\2\2\2"+
		"eg\7\17\2\2fe\3\2\2\2fg\3\2\2\2gh\3\2\2\2hi\7\f\2\2ij\3\2\2\2jk\b\n\2"+
		"\2k\24\3\2\2\2lp\7%\2\2mo\13\2\2\2nm\3\2\2\2or\3\2\2\2pq\3\2\2\2pn\3\2"+
		"\2\2qs\3\2\2\2rp\3\2\2\2st\7\f\2\2tu\3\2\2\2uv\b\13\2\2v\26\3\2\2\2wy"+
		"\t\3\2\2xw\3\2\2\2yz\3\2\2\2zx\3\2\2\2z{\3\2\2\2{|\3\2\2\2|}\b\f\2\2}"+
		"\30\3\2\2\2\23\2 &+\61\64\66=?EGJTbfpz\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}