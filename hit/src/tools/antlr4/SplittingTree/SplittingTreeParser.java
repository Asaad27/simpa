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
		T__2=1, T__1=2, T__0=3, NUMBER=4, ID=5, COMMENT=6, LINE_COMMENT=7, PREPROC=8;
	public static final String[] tokenNames = {
		"<INVALID>", "'('", "')'", "';'", "NUMBER", "ID", "COMMENT", "LINE_COMMENT", 
		"PREPROC"
	};
	public static final int
		RULE_splitting_tree = 0, RULE_state = 1, RULE_input = 2, RULE_output = 3;
	public static final String[] ruleNames = {
		"splitting_tree", "state", "input", "output"
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
		public Splitting_treeContext splitting_tree(int i) {
			return getRuleContext(Splitting_treeContext.class,i);
		}
		public InputContext input() {
			return getRuleContext(InputContext.class,0);
		}
		public OutputContext output(int i) {
			return getRuleContext(OutputContext.class,i);
		}
		public List<Splitting_treeContext> splitting_tree() {
			return getRuleContexts(Splitting_treeContext.class);
		}
		public List<OutputContext> output() {
			return getRuleContexts(OutputContext.class);
		}
		public StateContext state() {
			return getRuleContext(StateContext.class,0);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SplittingTreeVisitor ) return ((SplittingTreeVisitor<? extends T>)visitor).visitSplitting_tree(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Splitting_treeContext splitting_tree() throws RecognitionException {
		Splitting_treeContext _localctx = new Splitting_treeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_splitting_tree);
		int _la;
		try {
			setState(26);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(8); input();
				setState(9); match(T__2);
				setState(18);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NUMBER || _la==ID) {
					{
					{
					setState(10); output();
					setState(11); match(T__2);
					setState(12); splitting_tree();
					setState(13); match(T__1);
					setState(14); match(T__0);
					}
					}
					setState(20);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(21); match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(24);
				_la = _input.LA(1);
				if (_la==NUMBER || _la==ID) {
					{
					setState(23); state();
					}
				}

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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SplittingTreeVisitor ) return ((SplittingTreeVisitor<? extends T>)visitor).visitState(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StateContext state() throws RecognitionException {
		StateContext _localctx = new StateContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_state);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SplittingTreeVisitor ) return ((SplittingTreeVisitor<? extends T>)visitor).visitInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputContext input() throws RecognitionException {
		InputContext _localctx = new InputContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_input);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SplittingTreeVisitor ) return ((SplittingTreeVisitor<? extends T>)visitor).visitOutput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OutputContext output() throws RecognitionException {
		OutputContext _localctx = new OutputContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_output);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\n%\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\7\2\23\n\2\f\2\16\2"+
		"\26\13\2\3\2\3\2\3\2\5\2\33\n\2\5\2\35\n\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5"+
		"\2\2\6\2\4\6\b\2\3\3\2\6\7#\2\34\3\2\2\2\4\36\3\2\2\2\6 \3\2\2\2\b\"\3"+
		"\2\2\2\n\13\5\6\4\2\13\24\7\3\2\2\f\r\5\b\5\2\r\16\7\3\2\2\16\17\5\2\2"+
		"\2\17\20\7\4\2\2\20\21\7\5\2\2\21\23\3\2\2\2\22\f\3\2\2\2\23\26\3\2\2"+
		"\2\24\22\3\2\2\2\24\25\3\2\2\2\25\27\3\2\2\2\26\24\3\2\2\2\27\30\7\4\2"+
		"\2\30\35\3\2\2\2\31\33\5\4\3\2\32\31\3\2\2\2\32\33\3\2\2\2\33\35\3\2\2"+
		"\2\34\n\3\2\2\2\34\32\3\2\2\2\35\3\3\2\2\2\36\37\t\2\2\2\37\5\3\2\2\2"+
		" !\t\2\2\2!\7\3\2\2\2\"#\t\2\2\2#\t\3\2\2\2\5\24\32\34";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}