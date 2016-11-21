// Generated from SplittingTree.g4 by ANTLR 4.5.3
package tools.antlr4.SplittingTree;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SplittingTreeParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, NUMBER=4, ID=5, COMMENT=6, LINE_COMMENT=7, PREPROC=8, 
		WS=9;
	public static final int
		RULE_splitting_tree = 0, RULE_branch = 1, RULE_subsplitting_tree = 2, 
		RULE_leaf = 3, RULE_root = 4, RULE_input = 5, RULE_output = 6;
	public static final String[] ruleNames = {
		"splitting_tree", "branch", "subsplitting_tree", "leaf", "root", "input", 
		"output"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "';'", "')'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, "NUMBER", "ID", "COMMENT", "LINE_COMMENT", "PREPROC", 
		"WS"
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

	@Override
	public String getGrammarFileName() { return "SplittingTree.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SplittingTreeParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class Splitting_treeContext extends ParserRuleContext {
		public RootContext root() {
			return getRuleContext(RootContext.class,0);
		}
		public List<BranchContext> branch() {
			return getRuleContexts(BranchContext.class);
		}
		public BranchContext branch(int i) {
			return getRuleContext(BranchContext.class,i);
		}
		public List<LeafContext> leaf() {
			return getRuleContexts(LeafContext.class);
		}
		public LeafContext leaf(int i) {
			return getRuleContext(LeafContext.class,i);
		}
		public Splitting_treeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_splitting_tree; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterSplitting_tree(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitSplitting_tree(this);
		}
	}

	public final Splitting_treeContext splitting_tree() throws RecognitionException {
		Splitting_treeContext _localctx = new Splitting_treeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_splitting_tree);
		int _la;
		try {
			setState(31);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(14);
				root();
				setState(15);
				match(T__0);
				setState(16);
				branch();
				setState(17);
				match(T__1);
				setState(18);
				branch();
				setState(19);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(21);
				root();
				setState(22);
				match(T__0);
				setState(26);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NUMBER || _la==ID) {
					{
					{
					setState(23);
					leaf();
					}
					}
					setState(28);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(29);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BranchContext extends ParserRuleContext {
		public OutputContext output() {
			return getRuleContext(OutputContext.class,0);
		}
		public List<LeafContext> leaf() {
			return getRuleContexts(LeafContext.class);
		}
		public LeafContext leaf(int i) {
			return getRuleContext(LeafContext.class,i);
		}
		public Subsplitting_treeContext subsplitting_tree() {
			return getRuleContext(Subsplitting_treeContext.class,0);
		}
		public BranchContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_branch; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterBranch(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitBranch(this);
		}
	}

	public final BranchContext branch() throws RecognitionException {
		BranchContext _localctx = new BranchContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_branch);
		int _la;
		try {
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(33);
				output();
				setState(34);
				match(T__0);
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NUMBER || _la==ID) {
					{
					{
					setState(35);
					leaf();
					}
					}
					setState(40);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(41);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(43);
				output();
				setState(44);
				match(T__0);
				setState(45);
				subsplitting_tree();
				setState(46);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Subsplitting_treeContext extends ParserRuleContext {
		public InputContext input() {
			return getRuleContext(InputContext.class,0);
		}
		public List<BranchContext> branch() {
			return getRuleContexts(BranchContext.class);
		}
		public BranchContext branch(int i) {
			return getRuleContext(BranchContext.class,i);
		}
		public Subsplitting_treeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subsplitting_tree; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterSubsplitting_tree(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitSubsplitting_tree(this);
		}
	}

	public final Subsplitting_treeContext subsplitting_tree() throws RecognitionException {
		Subsplitting_treeContext _localctx = new Subsplitting_treeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_subsplitting_tree);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			input();
			setState(51);
			match(T__0);
			setState(52);
			branch();
			setState(53);
			match(T__1);
			setState(54);
			branch();
			setState(55);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LeafContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SplittingTreeParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(SplittingTreeParser.NUMBER, 0); }
		public LeafContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_leaf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterLeaf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitLeaf(this);
		}
	}

	public final LeafContext leaf() throws RecognitionException {
		LeafContext _localctx = new LeafContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_leaf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RootContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SplittingTreeParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(SplittingTreeParser.NUMBER, 0); }
		public RootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_root; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitRoot(this);
		}
	}

	public final RootContext root() throws RecognitionException {
		RootContext _localctx = new RootContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_root);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InputContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SplittingTreeParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(SplittingTreeParser.NUMBER, 0); }
		public InputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_input; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitInput(this);
		}
	}

	public final InputContext input() throws RecognitionException {
		InputContext _localctx = new InputContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_input);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OutputContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SplittingTreeParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(SplittingTreeParser.NUMBER, 0); }
		public OutputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_output; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterOutput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitOutput(this);
		}
	}

	public final OutputContext output() throws RecognitionException {
		OutputContext _localctx = new OutputContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_output);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\13D\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\2\3\2\3\2\7\2\33\n\2\f\2\16\2\36\13\2\3\2\3\2\5\2\"\n\2\3\3\3\3\3\3"+
		"\7\3\'\n\3\f\3\16\3*\13\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\63\n\3\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\2\2\t\2\4\6"+
		"\b\n\f\16\2\3\3\2\6\7@\2!\3\2\2\2\4\62\3\2\2\2\6\64\3\2\2\2\b;\3\2\2\2"+
		"\n=\3\2\2\2\f?\3\2\2\2\16A\3\2\2\2\20\21\5\n\6\2\21\22\7\3\2\2\22\23\5"+
		"\4\3\2\23\24\7\4\2\2\24\25\5\4\3\2\25\26\7\5\2\2\26\"\3\2\2\2\27\30\5"+
		"\n\6\2\30\34\7\3\2\2\31\33\5\b\5\2\32\31\3\2\2\2\33\36\3\2\2\2\34\32\3"+
		"\2\2\2\34\35\3\2\2\2\35\37\3\2\2\2\36\34\3\2\2\2\37 \7\5\2\2 \"\3\2\2"+
		"\2!\20\3\2\2\2!\27\3\2\2\2\"\3\3\2\2\2#$\5\16\b\2$(\7\3\2\2%\'\5\b\5\2"+
		"&%\3\2\2\2\'*\3\2\2\2(&\3\2\2\2()\3\2\2\2)+\3\2\2\2*(\3\2\2\2+,\7\5\2"+
		"\2,\63\3\2\2\2-.\5\16\b\2./\7\3\2\2/\60\5\6\4\2\60\61\7\5\2\2\61\63\3"+
		"\2\2\2\62#\3\2\2\2\62-\3\2\2\2\63\5\3\2\2\2\64\65\5\f\7\2\65\66\7\3\2"+
		"\2\66\67\5\4\3\2\678\7\4\2\289\5\4\3\29:\7\5\2\2:\7\3\2\2\2;<\t\2\2\2"+
		"<\t\3\2\2\2=>\t\2\2\2>\13\3\2\2\2?@\t\2\2\2@\r\3\2\2\2AB\t\2\2\2B\17\3"+
		"\2\2\2\6\34!(\62";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}