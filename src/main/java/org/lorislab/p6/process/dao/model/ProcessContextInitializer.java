package org.lorislab.p6.process.dao.model;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = {ProcessInstanceModel.class, ProcessTokenModel.class}, schemaPackageName = "p6_process")
interface ProcessContextInitializer extends SerializationContextInitializer {

}
