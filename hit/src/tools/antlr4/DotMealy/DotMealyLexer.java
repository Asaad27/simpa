// Generated from DotMealy.g4 by ANTLR 4.4
package tools.antlr4.DotMealy;
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
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__9=1, T__8=2, T__7=3, T__6=4, T__5=5, T__4=6, T__3=7, T__2=8, T__1=9, 
		T__0=10, STRICT=11, GRAPH=12, DIGRAPH=13, NODE=14, EDGE=15, SUBGRAPH=16, 
		NUMBER=17, ID=18, HTML_STRING=19, COMMENT=20, LINE_COMMENT=21, PREPROC=22, 
		WS=23;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'", "'\\u0012'", 
		"'\\u0013'", "'\\u0014'", "'\\u0015'", "'\\u0016'", "'\\u0017'"
	};
	public static final String[] ruleNames = {
		"T__9", "T__8", "T__7", "T__6", "T__5", "T__4", "T__3", "T__2", "T__1", 
		"T__0", "STRICT", "GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "NUMBER", 
		"DIGIT", "ID", "HTML_STRING", "LETTER", "TAG", "COMMENT", "LINE_COMMENT", 
		"PREPROC", "WS"
	};


	public DotMealyLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DotMealy.g4"; }

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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\31\u00e7\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6"+
		"\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\3\22\5\22w\n\22\3\22\3\22\6\22{\n\22\r\22\16\22"+
		"|\3\22\6\22\u0080\n\22\r\22\16\22\u0081\3\22\3\22\7\22\u0086\n\22\f\22"+
		"\16\22\u0089\13\22\5\22\u008b\n\22\5\22\u008d\n\22\3\23\3\23\3\24\3\24"+
		"\3\24\7\24\u0094\n\24\f\24\16\24\u0097\13\24\3\24\3\24\3\24\7\24\u009c"+
		"\n\24\f\24\16\24\u009f\13\24\5\24\u00a1\n\24\3\25\3\25\3\25\7\25\u00a6"+
		"\n\25\f\25\16\25\u00a9\13\25\3\25\3\25\3\26\3\26\3\27\3\27\7\27\u00b1"+
		"\n\27\f\27\16\27\u00b4\13\27\3\27\3\27\3\30\3\30\3\30\3\30\7\30\u00bc"+
		"\n\30\f\30\16\30\u00bf\13\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3"+
		"\31\7\31\u00ca\n\31\f\31\16\31\u00cd\13\31\3\31\5\31\u00d0\n\31\3\31\3"+
		"\31\3\31\3\31\3\32\3\32\7\32\u00d8\n\32\f\32\16\32\u00db\13\32\3\32\3"+
		"\32\3\32\3\32\3\33\6\33\u00e2\n\33\r\33\16\33\u00e3\3\33\3\33\6\u00b2"+
		"\u00bd\u00cb\u00d9\2\34\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f"+
		"\27\r\31\16\33\17\35\20\37\21!\22#\23%\2\'\24)\25+\2-\2/\26\61\27\63\30"+
		"\65\31\3\2\24\4\2UUuu\4\2VVvv\4\2TTtt\4\2KKkk\4\2EEee\4\2IIii\4\2CCcc"+
		"\4\2RRrr\4\2JJjj\4\2FFff\4\2PPpp\4\2QQqq\4\2GGgg\4\2WWww\4\2DDdd\4\2>"+
		">@@\6\2C\\aac|\u0082\u0101\5\2\13\f\17\17\"\"\u00f6\2\3\3\2\2\2\2\5\3"+
		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2"+
		"\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3"+
		"\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2\'\3\2\2\2\2)"+
		"\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\3\67\3\2\2"+
		"\2\59\3\2\2\2\7<\3\2\2\2\t>\3\2\2\2\13@\3\2\2\2\rB\3\2\2\2\17D\3\2\2\2"+
		"\21G\3\2\2\2\23I\3\2\2\2\25K\3\2\2\2\27M\3\2\2\2\31T\3\2\2\2\33Z\3\2\2"+
		"\2\35b\3\2\2\2\37g\3\2\2\2!l\3\2\2\2#v\3\2\2\2%\u008e\3\2\2\2\'\u00a0"+
		"\3\2\2\2)\u00a2\3\2\2\2+\u00ac\3\2\2\2-\u00ae\3\2\2\2/\u00b7\3\2\2\2\61"+
		"\u00c5\3\2\2\2\63\u00d5\3\2\2\2\65\u00e1\3\2\2\2\678\7\61\2\28\4\3\2\2"+
		"\29:\7/\2\2:;\7@\2\2;\6\3\2\2\2<=\7$\2\2=\b\3\2\2\2>?\7]\2\2?\n\3\2\2"+
		"\2@A\7=\2\2A\f\3\2\2\2BC\7}\2\2C\16\3\2\2\2DE\7/\2\2EF\7/\2\2F\20\3\2"+
		"\2\2GH\7?\2\2H\22\3\2\2\2IJ\7_\2\2J\24\3\2\2\2KL\7\177\2\2L\26\3\2\2\2"+
		"MN\t\2\2\2NO\t\3\2\2OP\t\4\2\2PQ\t\5\2\2QR\t\6\2\2RS\t\3\2\2S\30\3\2\2"+
		"\2TU\t\7\2\2UV\t\4\2\2VW\t\b\2\2WX\t\t\2\2XY\t\n\2\2Y\32\3\2\2\2Z[\t\13"+
		"\2\2[\\\t\5\2\2\\]\t\7\2\2]^\t\4\2\2^_\t\b\2\2_`\t\t\2\2`a\t\n\2\2a\34"+
		"\3\2\2\2bc\t\f\2\2cd\t\r\2\2de\t\13\2\2ef\t\16\2\2f\36\3\2\2\2gh\t\16"+
		"\2\2hi\t\13\2\2ij\t\7\2\2jk\t\16\2\2k \3\2\2\2lm\t\2\2\2mn\t\17\2\2no"+
		"\t\20\2\2op\t\7\2\2pq\t\4\2\2qr\t\b\2\2rs\t\t\2\2st\t\n\2\2t\"\3\2\2\2"+
		"uw\7/\2\2vu\3\2\2\2vw\3\2\2\2w\u008c\3\2\2\2xz\7\60\2\2y{\5%\23\2zy\3"+
		"\2\2\2{|\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\u008d\3\2\2\2~\u0080\5%\23\2\177"+
		"~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082"+
		"\u008a\3\2\2\2\u0083\u0087\7\60\2\2\u0084\u0086\5%\23\2\u0085\u0084\3"+
		"\2\2\2\u0086\u0089\3\2\2\2\u0087\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088"+
		"\u008b\3\2\2\2\u0089\u0087\3\2\2\2\u008a\u0083\3\2\2\2\u008a\u008b\3\2"+
		"\2\2\u008b\u008d\3\2\2\2\u008cx\3\2\2\2\u008c\177\3\2\2\2\u008d$\3\2\2"+
		"\2\u008e\u008f\4\62;\2\u008f&\3\2\2\2\u0090\u0095\5+\26\2\u0091\u0094"+
		"\5+\26\2\u0092\u0094\5%\23\2\u0093\u0091\3\2\2\2\u0093\u0092\3\2\2\2\u0094"+
		"\u0097\3\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096\3\2\2\2\u0096\u00a1\3\2"+
		"\2\2\u0097\u0095\3\2\2\2\u0098\u009d\5%\23\2\u0099\u009c\5+\26\2\u009a"+
		"\u009c\5%\23\2\u009b\u0099\3\2\2\2\u009b\u009a\3\2\2\2\u009c\u009f\3\2"+
		"\2\2\u009d\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u00a1\3\2\2\2\u009f"+
		"\u009d\3\2\2\2\u00a0\u0090\3\2\2\2\u00a0\u0098\3\2\2\2\u00a1(\3\2\2\2"+
		"\u00a2\u00a7\7>\2\2\u00a3\u00a6\5-\27\2\u00a4\u00a6\n\21\2\2\u00a5\u00a3"+
		"\3\2\2\2\u00a5\u00a4\3\2\2\2\u00a6\u00a9\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a7"+
		"\u00a8\3\2\2\2\u00a8\u00aa\3\2\2\2\u00a9\u00a7\3\2\2\2\u00aa\u00ab\7@"+
		"\2\2\u00ab*\3\2\2\2\u00ac\u00ad\t\22\2\2\u00ad,\3\2\2\2\u00ae\u00b2\7"+
		">\2\2\u00af\u00b1\13\2\2\2\u00b0\u00af\3\2\2\2\u00b1\u00b4\3\2\2\2\u00b2"+
		"\u00b3\3\2\2\2\u00b2\u00b0\3\2\2\2\u00b3\u00b5\3\2\2\2\u00b4\u00b2\3\2"+
		"\2\2\u00b5\u00b6\7@\2\2\u00b6.\3\2\2\2\u00b7\u00b8\7\61\2\2\u00b8\u00b9"+
		"\7,\2\2\u00b9\u00bd\3\2\2\2\u00ba\u00bc\13\2\2\2\u00bb\u00ba\3\2\2\2\u00bc"+
		"\u00bf\3\2\2\2\u00bd\u00be\3\2\2\2\u00bd\u00bb\3\2\2\2\u00be\u00c0\3\2"+
		"\2\2\u00bf\u00bd\3\2\2\2\u00c0\u00c1\7,\2\2\u00c1\u00c2\7\61\2\2\u00c2"+
		"\u00c3\3\2\2\2\u00c3\u00c4\b\30\2\2\u00c4\60\3\2\2\2\u00c5\u00c6\7\61"+
		"\2\2\u00c6\u00c7\7\61\2\2\u00c7\u00cb\3\2\2\2\u00c8\u00ca\13\2\2\2\u00c9"+
		"\u00c8\3\2\2\2\u00ca\u00cd\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cb\u00c9\3\2"+
		"\2\2\u00cc\u00cf\3\2\2\2\u00cd\u00cb\3\2\2\2\u00ce\u00d0\7\17\2\2\u00cf"+
		"\u00ce\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d2\7\f"+
		"\2\2\u00d2\u00d3\3\2\2\2\u00d3\u00d4\b\31\2\2\u00d4\62\3\2\2\2\u00d5\u00d9"+
		"\7%\2\2\u00d6\u00d8\13\2\2\2\u00d7\u00d6\3\2\2\2\u00d8\u00db\3\2\2\2\u00d9"+
		"\u00da\3\2\2\2\u00d9\u00d7\3\2\2\2\u00da\u00dc\3\2\2\2\u00db\u00d9\3\2"+
		"\2\2\u00dc\u00dd\7\f\2\2\u00dd\u00de\3\2\2\2\u00de\u00df\b\32\2\2\u00df"+
		"\64\3\2\2\2\u00e0\u00e2\t\23\2\2\u00e1\u00e0\3\2\2\2\u00e2\u00e3\3\2\2"+
		"\2\u00e3\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6"+
		"\b\33\2\2\u00e6\66\3\2\2\2\26\2v|\u0081\u0087\u008a\u008c\u0093\u0095"+
		"\u009b\u009d\u00a0\u00a5\u00a7\u00b2\u00bd\u00cb\u00cf\u00d9\u00e3\3\b"+
		"\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}