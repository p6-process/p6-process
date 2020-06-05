package org.lorislab.p6.process.reactive;

import io.quarkus.qute.*;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Process expression helper. The implementation is using the Qute engine.
 */
public class ProcessExpressionHelper {

    private static final Logger log = LoggerFactory.getLogger(ProcessExpressionHelper.class);

    static final Pattern INTEGER_LITERAL_PATTERN = Pattern.compile("[-+]?\\d{1,10}");
    static final Pattern LONG_LITERAL_PATTERN = Pattern.compile("[-+]?\\d{1,19}(L|l)");
    static final Pattern DOUBLE_LITERAL_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+(d|D)");
    static final Pattern FLOAT_LITERAL_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+(f|F)");

    /**
     * The Qute engine instance
     */
    static final Engine ENGINE = Engine.builder()
            .addSectionHelpers(new IfSectionHelper.Factory())
            .addDefaultValueResolvers()
            .build();

    private ProcessExpressionHelper() {
        // empty constructor
    }

    /**
     * Evaluate the if expression and return boolean value.
     *
     * @param expression the expression.
     * @param data       the context data.
     * @return {@code true} if the expression is validate to true.
     */
    public static boolean ifExpression(String expression, Map<String, Object> data) {
        String tmp = null;
        try {
            log.debug("If expression: {} data: {}", expression, data);
            String input = expression;
            if (!input.trim().startsWith("{#if")) {
                input = "{#if " + input + "}true{/}";
            }
            Template template = ENGINE.parse(input);
            tmp = template.render(data);
            return Boolean.parseBoolean(tmp);
        } catch (Exception ex) {
            log.error("Error execute boolean expression {} with data {} result {}", expression, tmp, data);
            throw ex;
        }
    }

    /**
     * Evaluate the expression and return the value of it.
     *
     * @param expression the expression.
     * @param data       the context data.
     * @return the corresponding value.
     */
    public static Object valueExpression(String expression, Object data) {
        String tmp = null;
        try {
            Template template = ENGINE.parse("{" + expression + "}");
            tmp = template.data(data).render();
            return getLiteralValue(tmp);
//            Set<Expression> exp = template.getExpressions();
//
//            CompletableFuture<Object> literal = exp.iterator().next().getLiteralValue();
//            if (literal == null) {
//                return tmp;
//            }
//            return literal.get();
        } catch (Exception ex) {
            log.error("Error execute expression {} with data {} result {}", expression, tmp, data);
            return ex;
        }
    }

    static Object getLiteralValue(String literal) {
        if (literal == null || literal.isEmpty()) {
            return Results.Result.NOT_FOUND;
        }
        Object value = Results.Result.NOT_FOUND;
        if (isStringLiteralSeparator(literal.charAt(0))) {
            value = literal.substring(1, literal.length() - 1);
        } else if (literal.equals("true")) {
            value = Boolean.TRUE;
        } else if (literal.equals("false")) {
            value = Boolean.FALSE;
        } else if (literal.equals("null")) {
            value = null;
        } else {
            char firstChar = literal.charAt(0);
            if (Character.isDigit(firstChar) || firstChar == '-' || firstChar == '+') {
                if (INTEGER_LITERAL_PATTERN.matcher(literal).matches()) {
                    try {
                        value = Integer.parseInt(literal);
                    } catch (NumberFormatException e) {
                        log.warn("Unable to parse integer literal: " + literal, e);
                    }
                } else if (LONG_LITERAL_PATTERN.matcher(literal).matches()) {
                    try {
                        value = Long
                                .parseLong(literal.substring(0, literal.length() - 1));
                    } catch (NumberFormatException e) {
                        log.warn("Unable to parse long literal: " + literal, e);
                    }
                } else if (DOUBLE_LITERAL_PATTERN.matcher(literal).matches()) {
                    try {
                        value = Double
                                .parseDouble(literal.substring(0, literal.length() - 1));
                    } catch (NumberFormatException e) {
                        log.warn("Unable to parse double literal: " + literal, e);
                    }
                } else if (FLOAT_LITERAL_PATTERN.matcher(literal).matches()) {
                    try {
                        value = Float
                                .parseFloat(literal.substring(0, literal.length() - 1));
                    } catch (NumberFormatException e) {
                        log.warn("Unable to parse float literal: " + literal, e);
                    }
                }
            }
        }
        if (value == Results.Result.NOT_FOUND) {
            return literal;
        }
        return value;
    }

    static boolean isStringLiteralSeparator(char character) {
        return character == '"' || character == '\'';
    }
}
