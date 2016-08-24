package tools.antlr4;// Generated from DotMealy.g4 by ANTLR 4.5.3
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DotMealyLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		STRICT=10, GRAPH=11, DIGRAPH=12, NODE=13, EDGE=14, SUBGRAPH=15, NUMBER=16, 
		ID=17, HTML_STRING=18, COMMENT=19, LINE_COMMENT=20, PREPROC=21, WS=22;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"STRICT", "GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "NUMBER", "DIGIT", 
		"ID", "HTML_STRING", "LETTER", "TAG", "COMMENT", "LINE_COMMENT", "PREPROC", 
		"WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'{'", "'}'", "';'", "'['", "']'", "'='", "'\"'", "'/'", "'->'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, "STRICT", 
		"GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "NUMBER", "ID", "HTML_STRING", 
		"COMMENT", "LINE_COMMENT", "PREPROC", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public DotMealyLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DotMealy.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\30\u00e2\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3"+
		"\b\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16"+
		"\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\21\5\21r\n\21\3\21\3\21\6\21v\n\21\r\21\16\21w\3\21\6\21{\n\21\r\21"+
		"\16\21|\3\21\3\21\7\21\u0081\n\21\f\21\16\21\u0084\13\21\5\21\u0086\n"+
		"\21\5\21\u0088\n\21\3\22\3\22\3\23\3\23\3\23\7\23\u008f\n\23\f\23\16\23"+
		"\u0092\13\23\3\23\3\23\3\23\7\23\u0097\n\23\f\23\16\23\u009a\13\23\5\23"+
		"\u009c\n\23\3\24\3\24\3\24\7\24\u00a1\n\24\f\24\16\24\u00a4\13\24\3\24"+
		"\3\24\3\25\3\25\3\26\3\26\7\26\u00ac\n\26\f\26\16\26\u00af\13\26\3\26"+
		"\3\26\3\27\3\27\3\27\3\27\7\27\u00b7\n\27\f\27\16\27\u00ba\13\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\7\30\u00c5\n\30\f\30\16\30\u00c8"+
		"\13\30\3\30\5\30\u00cb\n\30\3\30\3\30\3\30\3\30\3\31\3\31\7\31\u00d3\n"+
		"\31\f\31\16\31\u00d6\13\31\3\31\3\31\3\31\3\31\3\32\6\32\u00dd\n\32\r"+
		"\32\16\32\u00de\3\32\3\32\6\u00ad\u00b8\u00c6\u00d4\2\33\3\3\5\4\7\5\t"+
		"\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\2%"+
		"\23\'\24)\2+\2-\25/\26\61\27\63\30\3\2\24\4\2UUuu\4\2VVvv\4\2TTtt\4\2"+
		"KKkk\4\2EEee\4\2IIii\4\2CCcc\4\2RRrr\4\2JJjj\4\2FFff\4\2PPpp\4\2QQqq\4"+
		"\2GGgg\4\2WWww\4\2DDdd\4\2>>@@\6\2C\\aac|\u0082\u0101\5\2\13\f\17\17\""+
		"\"\u00f1\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2%\3\2\2\2\2\'\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2"+
		"\2\2\3\65\3\2\2\2\5\67\3\2\2\2\79\3\2\2\2\t;\3\2\2\2\13=\3\2\2\2\r?\3"+
		"\2\2\2\17A\3\2\2\2\21C\3\2\2\2\23E\3\2\2\2\25H\3\2\2\2\27O\3\2\2\2\31"+
		"U\3\2\2\2\33]\3\2\2\2\35b\3\2\2\2\37g\3\2\2\2!q\3\2\2\2#\u0089\3\2\2\2"+
		"%\u009b\3\2\2\2\'\u009d\3\2\2\2)\u00a7\3\2\2\2+\u00a9\3\2\2\2-\u00b2\3"+
		"\2\2\2/\u00c0\3\2\2\2\61\u00d0\3\2\2\2\63\u00dc\3\2\2\2\65\66\7}\2\2\66"+
		"\4\3\2\2\2\678\7\177\2\28\6\3\2\2\29:\7=\2\2:\b\3\2\2\2;<\7]\2\2<\n\3"+
		"\2\2\2=>\7_\2\2>\f\3\2\2\2?@\7?\2\2@\16\3\2\2\2AB\7$\2\2B\20\3\2\2\2C"+
		"D\7\61\2\2D\22\3\2\2\2EF\7/\2\2FG\7@\2\2G\24\3\2\2\2HI\t\2\2\2IJ\t\3\2"+
		"\2JK\t\4\2\2KL\t\5\2\2LM\t\6\2\2MN\t\3\2\2N\26\3\2\2\2OP\t\7\2\2PQ\t\4"+
		"\2\2QR\t\b\2\2RS\t\t\2\2ST\t\n\2\2T\30\3\2\2\2UV\t\13\2\2VW\t\5\2\2WX"+
		"\t\7\2\2XY\t\4\2\2YZ\t\b\2\2Z[\t\t\2\2[\\\t\n\2\2\\\32\3\2\2\2]^\t\f\2"+
		"\2^_\t\r\2\2_`\t\13\2\2`a\t\16\2\2a\34\3\2\2\2bc\t\16\2\2cd\t\13\2\2d"+
		"e\t\7\2\2ef\t\16\2\2f\36\3\2\2\2gh\t\2\2\2hi\t\17\2\2ij\t\20\2\2jk\t\7"+
		"\2\2kl\t\4\2\2lm\t\b\2\2mn\t\t\2\2no\t\n\2\2o \3\2\2\2pr\7/\2\2qp\3\2"+
		"\2\2qr\3\2\2\2r\u0087\3\2\2\2su\7\60\2\2tv\5#\22\2ut\3\2\2\2vw\3\2\2\2"+
		"wu\3\2\2\2wx\3\2\2\2x\u0088\3\2\2\2y{\5#\22\2zy\3\2\2\2{|\3\2\2\2|z\3"+
		"\2\2\2|}\3\2\2\2}\u0085\3\2\2\2~\u0082\7\60\2\2\177\u0081\5#\22\2\u0080"+
		"\177\3\2\2\2\u0081\u0084\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0083\3\2\2"+
		"\2\u0083\u0086\3\2\2\2\u0084\u0082\3\2\2\2\u0085~\3\2\2\2\u0085\u0086"+
		"\3\2\2\2\u0086\u0088\3\2\2\2\u0087s\3\2\2\2\u0087z\3\2\2\2\u0088\"\3\2"+
		"\2\2\u0089\u008a\4\62;\2\u008a$\3\2\2\2\u008b\u0090\5)\25\2\u008c\u008f"+
		"\5)\25\2\u008d\u008f\5#\22\2\u008e\u008c\3\2\2\2\u008e\u008d\3\2\2\2\u008f"+
		"\u0092\3\2\2\2\u0090\u008e\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u009c\3\2"+
		"\2\2\u0092\u0090\3\2\2\2\u0093\u0098\5#\22\2\u0094\u0097\5)\25\2\u0095"+
		"\u0097\5#\22\2\u0096\u0094\3\2\2\2\u0096\u0095\3\2\2\2\u0097\u009a\3\2"+
		"\2\2\u0098\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u009c\3\2\2\2\u009a"+
		"\u0098\3\2\2\2\u009b\u008b\3\2\2\2\u009b\u0093\3\2\2\2\u009c&\3\2\2\2"+
		"\u009d\u00a2\7>\2\2\u009e\u00a1\5+\26\2\u009f\u00a1\n\21\2\2\u00a0\u009e"+
		"\3\2\2\2\u00a0\u009f\3\2\2\2\u00a1\u00a4\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2"+
		"\u00a3\3\2\2\2\u00a3\u00a5\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a5\u00a6\7@"+
		"\2\2\u00a6(\3\2\2\2\u00a7\u00a8\t\22\2\2\u00a8*\3\2\2\2\u00a9\u00ad\7"+
		">\2\2\u00aa\u00ac\13\2\2\2\u00ab\u00aa\3\2\2\2\u00ac\u00af\3\2\2\2\u00ad"+
		"\u00ae\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ae\u00b0\3\2\2\2\u00af\u00ad\3\2"+
		"\2\2\u00b0\u00b1\7@\2\2\u00b1,\3\2\2\2\u00b2\u00b3\7\61\2\2\u00b3\u00b4"+
		"\7,\2\2\u00b4\u00b8\3\2\2\2\u00b5\u00b7\13\2\2\2\u00b6\u00b5\3\2\2\2\u00b7"+
		"\u00ba\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b9\u00bb\3\2"+
		"\2\2\u00ba\u00b8\3\2\2\2\u00bb\u00bc\7,\2\2\u00bc\u00bd\7\61\2\2\u00bd"+
		"\u00be\3\2\2\2\u00be\u00bf\b\27\2\2\u00bf.\3\2\2\2\u00c0\u00c1\7\61\2"+
		"\2\u00c1\u00c2\7\61\2\2\u00c2\u00c6\3\2\2\2\u00c3\u00c5\13\2\2\2\u00c4"+
		"\u00c3\3\2\2\2\u00c5\u00c8\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c6\u00c4\3\2"+
		"\2\2\u00c7\u00ca\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c9\u00cb\7\17\2\2\u00ca"+
		"\u00c9\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00cd\7\f"+
		"\2\2\u00cd\u00ce\3\2\2\2\u00ce\u00cf\b\30\2\2\u00cf\60\3\2\2\2\u00d0\u00d4"+
		"\7%\2\2\u00d1\u00d3\13\2\2\2\u00d2\u00d1\3\2\2\2\u00d3\u00d6\3\2\2\2\u00d4"+
		"\u00d5\3\2\2\2\u00d4\u00d2\3\2\2\2\u00d5\u00d7\3\2\2\2\u00d6\u00d4\3\2"+
		"\2\2\u00d7\u00d8\7\f\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00da\b\31\2\2\u00da"+
		"\62\3\2\2\2\u00db\u00dd\t\23\2\2\u00dc\u00db\3\2\2\2\u00dd\u00de\3\2\2"+
		"\2\u00de\u00dc\3\2\2\2\u00de\u00df\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e1"+
		"\b\32\2\2\u00e1\64\3\2\2\2\26\2qw|\u0082\u0085\u0087\u008e\u0090\u0096"+
		"\u0098\u009b\u00a0\u00a2\u00ad\u00b8\u00c6\u00ca\u00d4\u00de\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}