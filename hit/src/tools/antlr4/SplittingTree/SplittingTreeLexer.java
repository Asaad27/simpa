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
		T__2=1, T__1=2, T__0=3, NUMBER=4, ID=5, COMMENT=6, LINE_COMMENT=7, PREPROC=8;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'"
	};
	public static final String[] ruleNames = {
		"T__2", "T__1", "T__0", "NUMBER", "DIGIT", "ID", "LETTER", "COMMENT", 
		"LINE_COMMENT", "PREPROC"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\nu\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\3\2\3\2\3\3\3\3\3\4\3\4\3\5\5\5\37\n\5\3\5\3\5\6\5#\n\5\r\5\16\5$"+
		"\3\5\6\5(\n\5\r\5\16\5)\3\5\3\5\7\5.\n\5\f\5\16\5\61\13\5\5\5\63\n\5\5"+
		"\5\65\n\5\3\6\3\6\3\7\3\7\3\7\7\7<\n\7\f\7\16\7?\13\7\3\7\3\7\3\7\7\7"+
		"D\n\7\f\7\16\7G\13\7\5\7I\n\7\3\b\3\b\3\t\3\t\3\t\3\t\7\tQ\n\t\f\t\16"+
		"\tT\13\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\7\n_\n\n\f\n\16\nb\13\n\3"+
		"\n\5\ne\n\n\3\n\3\n\3\n\3\n\3\13\3\13\7\13m\n\13\f\13\16\13p\13\13\3\13"+
		"\3\13\3\13\3\13\5R`n\2\f\3\3\5\4\7\5\t\6\13\2\r\7\17\2\21\b\23\t\25\n"+
		"\3\2\3\6\2C\\aac|\u0082\u0101\u0081\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\r\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\3\27"+
		"\3\2\2\2\5\31\3\2\2\2\7\33\3\2\2\2\t\36\3\2\2\2\13\66\3\2\2\2\rH\3\2\2"+
		"\2\17J\3\2\2\2\21L\3\2\2\2\23Z\3\2\2\2\25j\3\2\2\2\27\30\7*\2\2\30\4\3"+
		"\2\2\2\31\32\7+\2\2\32\6\3\2\2\2\33\34\7=\2\2\34\b\3\2\2\2\35\37\7/\2"+
		"\2\36\35\3\2\2\2\36\37\3\2\2\2\37\64\3\2\2\2 \"\7\60\2\2!#\5\13\6\2\""+
		"!\3\2\2\2#$\3\2\2\2$\"\3\2\2\2$%\3\2\2\2%\65\3\2\2\2&(\5\13\6\2\'&\3\2"+
		"\2\2()\3\2\2\2)\'\3\2\2\2)*\3\2\2\2*\62\3\2\2\2+/\7\60\2\2,.\5\13\6\2"+
		"-,\3\2\2\2.\61\3\2\2\2/-\3\2\2\2/\60\3\2\2\2\60\63\3\2\2\2\61/\3\2\2\2"+
		"\62+\3\2\2\2\62\63\3\2\2\2\63\65\3\2\2\2\64 \3\2\2\2\64\'\3\2\2\2\65\n"+
		"\3\2\2\2\66\67\4\62;\2\67\f\3\2\2\28=\5\17\b\29<\5\17\b\2:<\5\13\6\2;"+
		"9\3\2\2\2;:\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>I\3\2\2\2?=\3\2\2\2"+
		"@E\5\13\6\2AD\5\17\b\2BD\5\13\6\2CA\3\2\2\2CB\3\2\2\2DG\3\2\2\2EC\3\2"+
		"\2\2EF\3\2\2\2FI\3\2\2\2GE\3\2\2\2H8\3\2\2\2H@\3\2\2\2I\16\3\2\2\2JK\t"+
		"\2\2\2K\20\3\2\2\2LM\7\61\2\2MN\7,\2\2NR\3\2\2\2OQ\13\2\2\2PO\3\2\2\2"+
		"QT\3\2\2\2RS\3\2\2\2RP\3\2\2\2SU\3\2\2\2TR\3\2\2\2UV\7,\2\2VW\7\61\2\2"+
		"WX\3\2\2\2XY\b\t\2\2Y\22\3\2\2\2Z[\7\61\2\2[\\\7\61\2\2\\`\3\2\2\2]_\13"+
		"\2\2\2^]\3\2\2\2_b\3\2\2\2`a\3\2\2\2`^\3\2\2\2ad\3\2\2\2b`\3\2\2\2ce\7"+
		"\17\2\2dc\3\2\2\2de\3\2\2\2ef\3\2\2\2fg\7\f\2\2gh\3\2\2\2hi\b\n\2\2i\24"+
		"\3\2\2\2jn\7%\2\2km\13\2\2\2lk\3\2\2\2mp\3\2\2\2no\3\2\2\2nl\3\2\2\2o"+
		"q\3\2\2\2pn\3\2\2\2qr\7\f\2\2rs\3\2\2\2st\b\13\2\2t\26\3\2\2\2\22\2\36"+
		"$)/\62\64;=CEHR`dn\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}