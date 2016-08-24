package tools.antlr4;// Generated from DotMealy.g4 by ANTLR 4.5.3
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DotMealyParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		STRICT=10, GRAPH=11, DIGRAPH=12, NODE=13, EDGE=14, SUBGRAPH=15, NUMBER=16, 
		ID=17, HTML_STRING=18, COMMENT=19, LINE_COMMENT=20, PREPROC=21, WS=22;
	public static final int
		RULE_graph = 0, RULE_mealy_list = 1, RULE_mealy_trans = 2, RULE_mealy_attrs = 3, 
		RULE_label_name = 4, RULE_edge = 5, RULE_state = 6, RULE_edgeop = 7, RULE_trans = 8, 
		RULE_value = 9, RULE_input = 10, RULE_output = 11;
	public static final String[] ruleNames = {
		"graph", "mealy_list", "mealy_trans", "mealy_attrs", "label_name", "edge", 
		"state", "edgeop", "trans", "value", "input", "output"
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

	@Override
	public String getGrammarFileName() { return "DotMealy.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DotMealyParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class GraphContext extends ParserRuleContext {
		public Mealy_listContext mealy_list() {
			return getRuleContext(Mealy_listContext.class,0);
		}
		public TerminalNode GRAPH() { return getToken(DotMealyParser.GRAPH, 0); }
		public TerminalNode DIGRAPH() { return getToken(DotMealyParser.DIGRAPH, 0); }
		public TerminalNode STRICT() { return getToken(DotMealyParser.STRICT, 0); }
		public TerminalNode ID() { return getToken(DotMealyParser.ID, 0); }
		public GraphContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graph; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterGraph(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitGraph(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitGraph(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GraphContext graph() throws RecognitionException {
		GraphContext _localctx = new GraphContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_graph);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			_la = _input.LA(1);
			if (_la==STRICT) {
				{
				setState(24);
				match(STRICT);
				}
			}

			setState(27);
			_la = _input.LA(1);
			if ( !(_la==GRAPH || _la==DIGRAPH) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			setState(29);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(28);
				match(ID);
				}
			}

			setState(31);
			match(T__0);
			setState(32);
			mealy_list();
			setState(33);
			match(T__1);
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

	public static class Mealy_listContext extends ParserRuleContext {
		public List<Mealy_transContext> mealy_trans() {
			return getRuleContexts(Mealy_transContext.class);
		}
		public Mealy_transContext mealy_trans(int i) {
			return getRuleContext(Mealy_transContext.class,i);
		}
		public Mealy_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mealy_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterMealy_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitMealy_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitMealy_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Mealy_listContext mealy_list() throws RecognitionException {
		Mealy_listContext _localctx = new Mealy_listContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_mealy_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << NUMBER) | (1L << ID))) != 0)) {
				{
				{
				setState(35);
				mealy_trans();
				setState(37);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(36);
					match(T__2);
					}
					break;
				}
				}
				}
				setState(43);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class Mealy_transContext extends ParserRuleContext {
		public EdgeContext edge() {
			return getRuleContext(EdgeContext.class,0);
		}
		public List<Mealy_attrsContext> mealy_attrs() {
			return getRuleContexts(Mealy_attrsContext.class);
		}
		public Mealy_attrsContext mealy_attrs(int i) {
			return getRuleContext(Mealy_attrsContext.class,i);
		}
		public Label_nameContext label_name() {
			return getRuleContext(Label_nameContext.class,0);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public StateContext state() {
			return getRuleContext(StateContext.class,0);
		}
		public Mealy_transContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mealy_trans; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterMealy_trans(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitMealy_trans(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitMealy_trans(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Mealy_transContext mealy_trans() throws RecognitionException {
		Mealy_transContext _localctx = new Mealy_transContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_mealy_trans);
		int _la;
		try {
			int _alt;
			setState(82);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(44);
				edge();
				setState(49); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(45);
						match(T__3);
						setState(46);
						mealy_attrs();
						setState(47);
						match(T__4);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(51); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(53);
				label_name();
				setState(54);
				match(T__5);
				setState(62);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(55);
					value();
					}
					break;
				case 2:
					{
					setState(59);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__6) {
						{
						{
						setState(56);
						match(T__6);
						}
						}
						setState(61);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(64);
				state();
				setState(71);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(65);
						match(T__3);
						setState(66);
						mealy_attrs();
						setState(67);
						match(T__4);
						}
						} 
					}
					setState(73);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(74);
				state();
				{
				setState(75);
				match(T__5);
				setState(79);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(76);
						value();
						}
						} 
					}
					setState(81);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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

	public static class Mealy_attrsContext extends ParserRuleContext {
		public Label_nameContext label_name() {
			return getRuleContext(Label_nameContext.class,0);
		}
		public List<InputContext> input() {
			return getRuleContexts(InputContext.class);
		}
		public InputContext input(int i) {
			return getRuleContext(InputContext.class,i);
		}
		public List<OutputContext> output() {
			return getRuleContexts(OutputContext.class);
		}
		public OutputContext output(int i) {
			return getRuleContext(OutputContext.class,i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public Mealy_attrsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mealy_attrs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterMealy_attrs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitMealy_attrs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitMealy_attrs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Mealy_attrsContext mealy_attrs() throws RecognitionException {
		Mealy_attrsContext _localctx = new Mealy_attrsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_mealy_attrs);
		int _la;
		try {
			setState(125);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(84);
				label_name();
				setState(103);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(85);
					match(T__5);
					setState(87);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
					case 1:
						{
						setState(86);
						match(T__6);
						}
						break;
					}
					setState(95);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==NUMBER || _la==ID) {
						{
						{
						setState(89);
						input();
						setState(90);
						match(T__7);
						setState(91);
						output();
						}
						}
						setState(97);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(99);
					_la = _input.LA(1);
					if (_la==T__6) {
						{
						setState(98);
						match(T__6);
						}
					}

					}
					}
					setState(105);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(106);
				label_name();
				setState(122);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(107);
					match(T__5);
					setState(109);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
					case 1:
						{
						setState(108);
						match(T__6);
						}
						break;
					}
					setState(114);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << ID) | (1L << HTML_STRING))) != 0)) {
						{
						{
						setState(111);
						value();
						}
						}
						setState(116);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(118);
					_la = _input.LA(1);
					if (_la==T__6) {
						{
						setState(117);
						match(T__6);
						}
					}

					}
					}
					setState(124);
					_errHandler.sync(this);
					_la = _input.LA(1);
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

	public static class Label_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DotMealyParser.ID, 0); }
		public Label_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_label_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterLabel_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitLabel_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitLabel_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Label_nameContext label_name() throws RecognitionException {
		Label_nameContext _localctx = new Label_nameContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_label_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(ID);
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

	public static class EdgeContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(DotMealyParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(DotMealyParser.ID, i);
		}
		public List<TerminalNode> NUMBER() { return getTokens(DotMealyParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(DotMealyParser.NUMBER, i);
		}
		public EdgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitEdge(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitEdge(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EdgeContext edge() throws RecognitionException {
		EdgeContext _localctx = new EdgeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_edge);
		int _la;
		try {
			int _alt;
			setState(162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ID) {
					{
					{
					setState(130);
					match(ID);
					setState(131);
					match(T__8);
					setState(135);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(132);
							match(ID);
							}
							} 
						}
						setState(137);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
					}
					}
					}
					setState(142);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(144);
				_la = _input.LA(1);
				if (_la==T__2) {
					{
					setState(143);
					match(T__2);
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(156);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NUMBER) {
					{
					{
					setState(146);
					match(NUMBER);
					setState(147);
					match(T__8);
					setState(151);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(148);
							match(NUMBER);
							}
							} 
						}
						setState(153);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
					}
					}
					}
					setState(158);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(160);
				_la = _input.LA(1);
				if (_la==T__2) {
					{
					setState(159);
					match(T__2);
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
		public TerminalNode ID() { return getToken(DotMealyParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(DotMealyParser.NUMBER, 0); }
		public StateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_state; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterState(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitState(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitState(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StateContext state() throws RecognitionException {
		StateContext _localctx = new StateContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_state);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
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

	public static class EdgeopContext extends ParserRuleContext {
		public EdgeopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edgeop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterEdgeop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitEdgeop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitEdgeop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EdgeopContext edgeop() throws RecognitionException {
		EdgeopContext _localctx = new EdgeopContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_edgeop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
			match(T__8);
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

	public static class TransContext extends ParserRuleContext {
		public EdgeContext edge() {
			return getRuleContext(EdgeContext.class,0);
		}
		public List<Mealy_attrsContext> mealy_attrs() {
			return getRuleContexts(Mealy_attrsContext.class);
		}
		public Mealy_attrsContext mealy_attrs(int i) {
			return getRuleContext(Mealy_attrsContext.class,i);
		}
		public TransContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trans; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterTrans(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitTrans(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitTrans(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransContext trans() throws RecognitionException {
		TransContext _localctx = new TransContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_trans);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			edge();
			setState(175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(169);
				match(T__3);
				setState(170);
				mealy_attrs();
				setState(171);
				match(T__4);
				}
				}
				setState(177);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DotMealyParser.ID, 0); }
		public TerminalNode HTML_STRING() { return getToken(DotMealyParser.HTML_STRING, 0); }
		public TerminalNode NUMBER() { return getToken(DotMealyParser.NUMBER, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << ID) | (1L << HTML_STRING))) != 0)) ) {
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
		public TerminalNode ID() { return getToken(DotMealyParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(DotMealyParser.NUMBER, 0); }
		public InputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_input; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputContext input() throws RecognitionException {
		InputContext _localctx = new InputContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_input);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(180);
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
		public TerminalNode ID() { return getToken(DotMealyParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(DotMealyParser.NUMBER, 0); }
		public OutputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_output; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).enterOutput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DotMealyListener ) ((DotMealyListener)listener).exitOutput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DotMealyVisitor ) return ((DotMealyVisitor<? extends T>)visitor).visitOutput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OutputContext output() throws RecognitionException {
		OutputContext _localctx = new OutputContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_output);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\30\u00bb\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\3\2\5\2\34\n\2\3\2\3\2\5\2 \n\2\3\2\3\2\3\2\3\2"+
		"\3\3\3\3\5\3(\n\3\7\3*\n\3\f\3\16\3-\13\3\3\4\3\4\3\4\3\4\3\4\6\4\64\n"+
		"\4\r\4\16\4\65\3\4\3\4\3\4\3\4\7\4<\n\4\f\4\16\4?\13\4\5\4A\n\4\3\4\3"+
		"\4\3\4\3\4\3\4\7\4H\n\4\f\4\16\4K\13\4\3\4\3\4\3\4\7\4P\n\4\f\4\16\4S"+
		"\13\4\5\4U\n\4\3\5\3\5\3\5\5\5Z\n\5\3\5\3\5\3\5\3\5\7\5`\n\5\f\5\16\5"+
		"c\13\5\3\5\5\5f\n\5\7\5h\n\5\f\5\16\5k\13\5\3\5\3\5\3\5\5\5p\n\5\3\5\7"+
		"\5s\n\5\f\5\16\5v\13\5\3\5\5\5y\n\5\7\5{\n\5\f\5\16\5~\13\5\5\5\u0080"+
		"\n\5\3\6\3\6\3\7\3\7\3\7\3\7\7\7\u0088\n\7\f\7\16\7\u008b\13\7\7\7\u008d"+
		"\n\7\f\7\16\7\u0090\13\7\3\7\5\7\u0093\n\7\3\7\3\7\3\7\7\7\u0098\n\7\f"+
		"\7\16\7\u009b\13\7\7\7\u009d\n\7\f\7\16\7\u00a0\13\7\3\7\5\7\u00a3\n\7"+
		"\5\7\u00a5\n\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\n\3\n\7\n\u00b0\n\n\f\n\16"+
		"\n\u00b3\13\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\2\2\16\2\4\6\b\n\f\16\20\22"+
		"\24\26\30\2\5\3\2\r\16\3\2\22\23\3\2\22\24\u00cc\2\33\3\2\2\2\4+\3\2\2"+
		"\2\6T\3\2\2\2\b\177\3\2\2\2\n\u0081\3\2\2\2\f\u00a4\3\2\2\2\16\u00a6\3"+
		"\2\2\2\20\u00a8\3\2\2\2\22\u00aa\3\2\2\2\24\u00b4\3\2\2\2\26\u00b6\3\2"+
		"\2\2\30\u00b8\3\2\2\2\32\34\7\f\2\2\33\32\3\2\2\2\33\34\3\2\2\2\34\35"+
		"\3\2\2\2\35\37\t\2\2\2\36 \7\23\2\2\37\36\3\2\2\2\37 \3\2\2\2 !\3\2\2"+
		"\2!\"\7\3\2\2\"#\5\4\3\2#$\7\4\2\2$\3\3\2\2\2%\'\5\6\4\2&(\7\5\2\2\'&"+
		"\3\2\2\2\'(\3\2\2\2(*\3\2\2\2)%\3\2\2\2*-\3\2\2\2+)\3\2\2\2+,\3\2\2\2"+
		",\5\3\2\2\2-+\3\2\2\2.\63\5\f\7\2/\60\7\6\2\2\60\61\5\b\5\2\61\62\7\7"+
		"\2\2\62\64\3\2\2\2\63/\3\2\2\2\64\65\3\2\2\2\65\63\3\2\2\2\65\66\3\2\2"+
		"\2\66U\3\2\2\2\678\5\n\6\28@\7\b\2\29A\5\24\13\2:<\7\t\2\2;:\3\2\2\2<"+
		"?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>A\3\2\2\2?=\3\2\2\2@9\3\2\2\2@=\3\2\2\2"+
		"AU\3\2\2\2BI\5\16\b\2CD\7\6\2\2DE\5\b\5\2EF\7\7\2\2FH\3\2\2\2GC\3\2\2"+
		"\2HK\3\2\2\2IG\3\2\2\2IJ\3\2\2\2JU\3\2\2\2KI\3\2\2\2LM\5\16\b\2MQ\7\b"+
		"\2\2NP\5\24\13\2ON\3\2\2\2PS\3\2\2\2QO\3\2\2\2QR\3\2\2\2RU\3\2\2\2SQ\3"+
		"\2\2\2T.\3\2\2\2T\67\3\2\2\2TB\3\2\2\2TL\3\2\2\2U\7\3\2\2\2Vi\5\n\6\2"+
		"WY\7\b\2\2XZ\7\t\2\2YX\3\2\2\2YZ\3\2\2\2Za\3\2\2\2[\\\5\26\f\2\\]\7\n"+
		"\2\2]^\5\30\r\2^`\3\2\2\2_[\3\2\2\2`c\3\2\2\2a_\3\2\2\2ab\3\2\2\2be\3"+
		"\2\2\2ca\3\2\2\2df\7\t\2\2ed\3\2\2\2ef\3\2\2\2fh\3\2\2\2gW\3\2\2\2hk\3"+
		"\2\2\2ig\3\2\2\2ij\3\2\2\2j\u0080\3\2\2\2ki\3\2\2\2l|\5\n\6\2mo\7\b\2"+
		"\2np\7\t\2\2on\3\2\2\2op\3\2\2\2pt\3\2\2\2qs\5\24\13\2rq\3\2\2\2sv\3\2"+
		"\2\2tr\3\2\2\2tu\3\2\2\2ux\3\2\2\2vt\3\2\2\2wy\7\t\2\2xw\3\2\2\2xy\3\2"+
		"\2\2y{\3\2\2\2zm\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\u0080\3\2\2\2"+
		"~|\3\2\2\2\177V\3\2\2\2\177l\3\2\2\2\u0080\t\3\2\2\2\u0081\u0082\7\23"+
		"\2\2\u0082\13\3\2\2\2\u0083\u00a5\3\2\2\2\u0084\u0085\7\23\2\2\u0085\u0089"+
		"\7\13\2\2\u0086\u0088\7\23\2\2\u0087\u0086\3\2\2\2\u0088\u008b\3\2\2\2"+
		"\u0089\u0087\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008d\3\2\2\2\u008b\u0089"+
		"\3\2\2\2\u008c\u0084\3\2\2\2\u008d\u0090\3\2\2\2\u008e\u008c\3\2\2\2\u008e"+
		"\u008f\3\2\2\2\u008f\u0092\3\2\2\2\u0090\u008e\3\2\2\2\u0091\u0093\7\5"+
		"\2\2\u0092\u0091\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u00a5\3\2\2\2\u0094"+
		"\u0095\7\22\2\2\u0095\u0099\7\13\2\2\u0096\u0098\7\22\2\2\u0097\u0096"+
		"\3\2\2\2\u0098\u009b\3\2\2\2\u0099\u0097\3\2\2\2\u0099\u009a\3\2\2\2\u009a"+
		"\u009d\3\2\2\2\u009b\u0099\3\2\2\2\u009c\u0094\3\2\2\2\u009d\u00a0\3\2"+
		"\2\2\u009e\u009c\3\2\2\2\u009e\u009f\3\2\2\2\u009f\u00a2\3\2\2\2\u00a0"+
		"\u009e\3\2\2\2\u00a1\u00a3\7\5\2\2\u00a2\u00a1\3\2\2\2\u00a2\u00a3\3\2"+
		"\2\2\u00a3\u00a5\3\2\2\2\u00a4\u0083\3\2\2\2\u00a4\u008e\3\2\2\2\u00a4"+
		"\u009e\3\2\2\2\u00a5\r\3\2\2\2\u00a6\u00a7\t\3\2\2\u00a7\17\3\2\2\2\u00a8"+
		"\u00a9\7\13\2\2\u00a9\21\3\2\2\2\u00aa\u00b1\5\f\7\2\u00ab\u00ac\7\6\2"+
		"\2\u00ac\u00ad\5\b\5\2\u00ad\u00ae\7\7\2\2\u00ae\u00b0\3\2\2\2\u00af\u00ab"+
		"\3\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2"+
		"\23\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4\u00b5\t\4\2\2\u00b5\25\3\2\2\2\u00b6"+
		"\u00b7\t\3\2\2\u00b7\27\3\2\2\2\u00b8\u00b9\t\3\2\2\u00b9\31\3\2\2\2\35"+
		"\33\37\'+\65=@IQTYaeiotx|\177\u0089\u008e\u0092\u0099\u009e\u00a2\u00a4"+
		"\u00b1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}