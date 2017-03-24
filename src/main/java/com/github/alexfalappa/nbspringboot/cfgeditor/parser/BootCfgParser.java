package com.github.alexfalappa.nbspringboot.cfgeditor.parser;

import java.util.Properties;

import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.support.StringBuilderVar;
import org.parboiled.support.ValueStack;
import org.parboiled.support.Var;

/**
 * Spring Boot configuration properties parser based on Parboiled library.
 * <p>
 * This parser implements a grammar accepting the Java Properties format (with some minor exceptions) and adding dot separated keys, array
 * notation ({@code array[index]=value}.
 * <p>
 * Differences with base Java Properties syntax:
 * <ul>
 * <li>values must be explicitly separated from keys by a <tt>=</tt> (equal sign) or <tt>:</tt> (colon), first occurring whitespace as
 * separator is not supported.
 * </ul>
 *
 * @author Alessandro Falappa
 */
//@BuildParseTree
public class BootCfgParser extends BaseParser<String> {

    private Properties parsedProps = new Properties();

    public Properties getParsedProps() {
        return parsedProps;
    }

    Action<String> actionStoreProp = new Action<String>() {
        @Override
        public boolean run(Context<java.lang.String> context) {
            final ValueStack<java.lang.String> stack = context.getValueStack();
            int size = stack.size();
            switch (size) {
                case 0:
                    System.out.println("Empty stack");
                    break;
                case 1:
                    parsedProps.setProperty(stack.pop(), "");
                    break;
                case 2:
                    parsedProps.setProperty(stack.pop(1), stack.pop());
                    break;
                default:
                    System.out.println("More than 2 values on the stack");
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            return "StorePropsAction";
        }

    };

    Rule cfgProps() {
        return Sequence(
                ZeroOrMore(
                        FirstOf(
                                kvPair(),
                                comment(),
                                whitespace(),
                                eolChar()
                        )
                ),
                EOI
        );
    }

    Rule kvPair() {
        StringBuilderVar sbvValue = new StringBuilderVar();
        return Sequence(
                FirstOf(
                        Sequence(
                                key(),
                                Optional(whitespace()),
                                separator(),
                                Optional(whitespace()),
                                Sequence(value(sbvValue), push(sbvValue.getString()))
                        ),
                        key()
                ),
                actionStoreProp
        );
    }

    Rule comment() {
        return Sequence(commentStart(), ZeroOrMore(notEolChar()));
    }

    Rule key() {
        StringBuilderVar sbvKey = new StringBuilderVar();
        return Sequence(
                literal(sbvKey),
                ZeroOrMore(
                        Sequence(
                                Ch('.'), sbvKey.append('.'),
                                literal(sbvKey)
                        )
                ),
                Optional(arrayIndex(sbvKey)),
                push(sbvKey.getString())
        );
    }

    Rule value(StringBuilderVar sbv) {
        return ZeroOrMore(
                FirstOf(
                        Sequence(
                                escapedEolChar(),
                                Optional(whitespace()),
                                Sequence(notEolWhitespace(), sbv.append(matchedChar()))
                        ),
                        encodedUnicode(sbv),
                        literalOrSpace(sbv)
                //                        Sequence(notEolChar(), sbv.append(matchedChar()))
                )
        );
    }

    Rule literal(StringBuilderVar sbv) {
        return OneOrMore(
                FirstOf(
                        new JavaIdPartMatcher(sbv),
                        encodedSpecialChar(sbv),
                        encodedTab(sbv),
                        encodedLinefeed(sbv),
                        encodedUnicode(sbv)
                )
        );
    }

    Rule literalOrSpace(StringBuilderVar sbv) {
        return OneOrMore(
                FirstOf(
                        new JavaIdPartMatcher(sbv),
                        Sequence(AnyOf(" \t\f"), sbv.append(matchedChar())),
                        encodedSpecialChar(sbv),
                        encodedTab(sbv),
                        encodedLinefeed(sbv),
                        encodedUnicode(sbv),
                        malformedEscape(sbv)
                )
        );
    }

    Rule arrayIndex(StringBuilderVar sbv) {
        return Sequence(
                Sequence(Ch('['), sbv.append('[')),
                Sequence(integer(), sbv.append(match())),
                Sequence(Ch(']'), sbv.append(']'))
        );
    }

    Rule whitespace() {
        return OneOrMore(AnyOf(" \t\f"));
    }

    Rule eolChar() {
        return FirstOf('\n', "\r\n", '\r');
    }

    Rule notEolChar() {
        return NoneOf("\r\n");
    }

    Rule notEolWhitespace() {
        return NoneOf(" \t\f\r\n");
    }

    Rule separator() {
        return AnyOf("=:");
    }

    Rule commentStart() {
        return AnyOf("#!");
    }

    Rule encodedSpecialChar(StringBuilderVar sbv) {
        return Sequence(Ch('\\'), AnyOf(" \\=:#!"), sbv.append(matchedChar()));
    }

    Rule encodedLinefeed(StringBuilderVar sbv) {
        return Sequence(Ch('\\'), Ch('n'), sbv.append('\n'));
    }

    Rule encodedTab(StringBuilderVar sbv) {
        return Sequence(Ch('\\'), Ch('t'), sbv.append('\t'));
    }

    Rule encodedUnicode(StringBuilderVar sbv) {
        return Sequence(
                Ch('\\'),
                Ch('u'),
                Sequence(hexDigit(), hexDigit(), hexDigit(), hexDigit()), sbv.append(uniToStr(match()))
        );
    }

    Rule escapedEolChar() {
        return Sequence(Ch('\\'), eolChar());
    }

    Rule malformedEscape(StringBuilderVar sbv) {
        return Sequence(Ch('\\'), NoneOf("ntu \t\f\r\n=:#!\\"), sbv.append(matchedChar()));
    }

    Rule integer() {
        return FirstOf('0', Sequence(CharRange('1', '9'), ZeroOrMore(digit())));
    }

    Rule hexDigit() {
        return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), CharRange('0', '9'));
    }

    Rule digit() {
        return CharRange('0', '9');
    }

    boolean debug(Var v) {
        System.out.println(String.valueOf(v.get()));
        return true;
    }

    String uniToStr(String str) {
        int codePoint = Integer.parseInt(str, 16);
        return new String(Character.toChars(codePoint));
    }

}
