package org.lorislab.p6.process.stream.events;

import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target({TYPE, FIELD})
public @interface EventServiceType {

    ProcessTokenType value();

    final class Literal extends AnnotationLiteral<EventServiceType> implements EventServiceType {

        public static Literal create(ProcessTokenType value) {
            return new Literal(value);
        }

        private static final long serialVersionUID = 1L;

        private ProcessTokenType value;

        public Literal(ProcessTokenType value) {
            this.value = value;
        }
        @Override
        public ProcessTokenType value() {
            return value;
        }
    }
}
