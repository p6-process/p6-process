package org.lorislab.p6.process.events;

import org.lorislab.p6.process.model.ProcessToken;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target({TYPE, FIELD})
public @interface EventType {

    ProcessToken.Type value();

    final class Literal extends AnnotationLiteral<EventType> implements EventType {

        public static Literal create(ProcessToken.Type value) {
            return new Literal(value);
        }

        private static final long serialVersionUID = 1L;

        private ProcessToken.Type value;

        public Literal(ProcessToken.Type value) {
            this.value = value;
        }
        @Override
        public ProcessToken.Type value() {
            return value;
        }
    }

}
