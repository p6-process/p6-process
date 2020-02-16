package org.lorislab.p6.process.stream.response;

import org.lorislab.p6.process.dao.model.enums.ProcessTokenResponse;

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
public @interface EventResponseServiceType {

    ProcessTokenResponse value();

    final class Literal extends AnnotationLiteral<EventResponseServiceType> implements EventResponseServiceType {

        public static Literal create(ProcessTokenResponse value) {
            return new Literal(value);
        }

        private static final long serialVersionUID = 1L;

        private ProcessTokenResponse value;

        public Literal(ProcessTokenResponse value) {
            this.value = value;
        }
        @Override
        public ProcessTokenResponse value() {
            return value;
        }
    }
}
