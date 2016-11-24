// Generated from SplittingTree.g4 by ANTLR 4.4
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
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__2=1, T__1=2, T__0=3, NUMBER=4, ID=5, COMMENT=6, LINE_COMMENT=7, PREPROC=8, 
		WS=9;
	public static final String[] tokenNames = {
		"<INVALID>", "'('", "')'", "';'", "NUMBER", "ID", "COMMENT", "LINE_COMMENT", 
		"PREPROC", "WS"
	};
	public static final int
		RULE_splitting_tree = 0, RULE_subtree = 1, RULE_state = 2, RULE_root = 3, 
		RULE_input = 4, RULE_output = 5;
	public static final String[] ruleNames = {
		"splitting_tree", "subtree", "state", "root", "input", "output"
	};

	@Override
	public String getGrammarFileName() { return "SplittingTree.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

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
		public List<SubtreeContext> subtree() {
			return getRuleContexts(SubtreeContext.class);
		}
		public StateContext state(int i) {
			return getRuleContext(StateContext.class,i);
		}
		public InputContext input() {
			return getRuleContext(InputContext.class,0);
		}
		public SubtreeContext subtree(int i) {
			return getRuleContext(SubtreeContext.class,i);
		}
		public List<StateContext> state() {
			return getRuleContexts(StateContext.class);
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
			setState(36);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(12); input();
				setState(13); match(T__2);
				setState(19);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NUMBER || _la==ID) {
					{
					{
					setState(14); subtree();
					setState(15); match(T__0);
					}
					}
					setState(21);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(22); match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(24); input();
				setState(25); match(T__2);
				setState(31);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NUMBER || _la==ID) {
					{
					{
					setState(26); state();
					setState(27); match(T__0);
					}
					}
					setState(33);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(34); match(T__1);
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

	public static class SubtreeContext extends ParserRuleContext {
		public List<SubtreeContext> subtree() {
			return getRuleContexts(SubtreeContext.class);
		}
		public InputContext input() {
			return getRuleContext(InputContext.class,0);
		}
		public SubtreeContext subtree(int i) {
			return getRuleContext(SubtreeContext.class,i);
		}
		public OutputContext output() {
			return getRuleContext(OutputContext.class,0);
		}
		public StateContext state() {
			return getRuleContext(StateContext.class,0);
		}
		public SubtreeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subtree; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterSubtree(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitSubtree(this);
		}
	}

	public final SubtreeContext subtree() throws RecognitionException {
		SubtreeContext _localctx = new SubtreeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_subtree);
		int _la;
		try {
			setState(65);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(38); output();
				setState(39); match(T__2);
				setState(41);
				_la = _input.LA(1);
				if (_la==NUMBER || _la==ID) {
					{
					setState(40); state();
					}
				}

				setState(43); match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(45); output();
				setState(46); match(T__2);
				setState(47); subtree();
				setState(48); match(T__1);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(50); output();
				setState(51); match(T__2);
				setState(52); input();
				setState(53); match(T__2);
				setState(59);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NUMBER || _la==ID) {
					{
					{
					setState(54); subtree();
					setState(55); match(T__0);
					}
					}
					setState(61);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(62); match(T__1);
				setState(63); match(T__1);
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

	public static class StateContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SplittingTreeParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(SplittingTreeParser.NUMBER, 0); }
		public StateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_state; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).enterState(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SplittingTreeListener ) ((SplittingTreeListener)listener).exitState(this);
		}
	}

	public final StateContext state() throws RecognitionException {
		StateContext _localctx = new StateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_state);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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
		enterRule(_localctx, 6, RULE_root);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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
		enterRule(_localctx, 8, RULE_input);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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
		enterRule(_localctx, 10, RULE_output);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			_la = _input.LA(1);
			if ( !(_la==NUMBER || _la==ID) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\13N\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\2\3\2\3\2\7\2\24\n\2\f\2"+
		"\16\2\27\13\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\7\2 \n\2\f\2\16\2#\13\2\3\2"+
		"\3\2\5\2\'\n\2\3\3\3\3\3\3\5\3,\n\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\7\3<\n\3\f\3\16\3?\13\3\3\3\3\3\3\3\5\3D\n\3\3"+
		"\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\2\2\b\2\4\6\b\n\f\2\3\3\2\6\7N\2&\3"+
		"\2\2\2\4C\3\2\2\2\6E\3\2\2\2\bG\3\2\2\2\nI\3\2\2\2\fK\3\2\2\2\16\17\5"+
		"\n\6\2\17\25\7\3\2\2\20\21\5\4\3\2\21\22\7\5\2\2\22\24\3\2\2\2\23\20\3"+
		"\2\2\2\24\27\3\2\2\2\25\23\3\2\2\2\25\26\3\2\2\2\26\30\3\2\2\2\27\25\3"+
		"\2\2\2\30\31\7\4\2\2\31\'\3\2\2\2\32\33\5\n\6\2\33!\7\3\2\2\34\35\5\6"+
		"\4\2\35\36\7\5\2\2\36 \3\2\2\2\37\34\3\2\2\2 #\3\2\2\2!\37\3\2\2\2!\""+
		"\3\2\2\2\"$\3\2\2\2#!\3\2\2\2$%\7\4\2\2%\'\3\2\2\2&\16\3\2\2\2&\32\3\2"+
		"\2\2\'\3\3\2\2\2()\5\f\7\2)+\7\3\2\2*,\5\6\4\2+*\3\2\2\2+,\3\2\2\2,-\3"+
		"\2\2\2-.\7\4\2\2.D\3\2\2\2/\60\5\f\7\2\60\61\7\3\2\2\61\62\5\4\3\2\62"+
		"\63\7\4\2\2\63D\3\2\2\2\64\65\5\f\7\2\65\66\7\3\2\2\66\67\5\n\6\2\67="+
		"\7\3\2\289\5\4\3\29:\7\5\2\2:<\3\2\2\2;8\3\2\2\2<?\3\2\2\2=;\3\2\2\2="+
		">\3\2\2\2>@\3\2\2\2?=\3\2\2\2@A\7\4\2\2AB\7\4\2\2BD\3\2\2\2C(\3\2\2\2"+
		"C/\3\2\2\2C\64\3\2\2\2D\5\3\2\2\2EF\t\2\2\2F\7\3\2\2\2GH\t\2\2\2H\t\3"+
		"\2\2\2IJ\t\2\2\2J\13\3\2\2\2KL\t\2\2\2L\r\3\2\2\2\b\25!&+=C";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}