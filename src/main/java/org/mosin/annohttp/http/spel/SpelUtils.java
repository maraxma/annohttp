package org.mosin.annohttp.http.spel;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

public class SpelUtils {

    private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();
    private static final String SPEL_ARG_NAME_PREFIX = "arg";

    public static StandardEvaluationContext prepareSpelContext(Object[] args) {
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String argName = SPEL_ARG_NAME_PREFIX + i;
                ctx.setVariable(argName, args[i]);
            }
        }
        return ctx;
    }

    public static <T> T executeSpel(String spel, EvaluationContext ctx, Class<T> clazz) {
        try {
            Expression exp = SPEL_PARSER.parseExpression(spel);
            return exp.getValue(ctx, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Illegal SpEL expression: " + spel, e);
        }
    }

    public static void main(String[] args) {
        EvaluationContext context = new StandardEvaluationContext();
        Object o = executeSpel("new Object[]{ {\"name\": \"mara\", \"age\": 21} }", context, Object.class);
        if (o instanceof Object[]) {
            System.out.println("List");
            Object[] array = (Object[]) o;
            System.out.println(array[0].getClass());
        } else if (o instanceof Map) {
            System.out.println("Map");
        } else {
            System.out.println(o.getClass());
        }
    }
}
