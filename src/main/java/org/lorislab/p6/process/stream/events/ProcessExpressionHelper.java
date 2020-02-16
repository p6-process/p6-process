package org.lorislab.p6.process.stream.events;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Expression;
import io.quarkus.qute.IfSectionHelper;
import io.quarkus.qute.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Process expression helper. The implementation is using the Qute engine.
 */
public class ProcessExpressionHelper {

    private static final Logger log = LoggerFactory.getLogger(ProcessExpressionHelper.class);

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
    public static boolean ifExpression(String expression, Object data) {
        String tmp = null;
        try {
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
            tmp = template.render(data);
            CompletableFuture<Object> literal = Expression.literal(tmp).literal;
            if (literal == null) {
                return tmp;
            }
            return literal.get();
        } catch (Exception ex) {
            log.error("Error execute expression {} with data {} result {}", expression, tmp, data);
            return ex;
        }
    }
}
