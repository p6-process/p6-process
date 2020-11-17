package org.lorislab.p6.process.reactive;

import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ProcessInstanceRepository;
import org.lorislab.p6.process.deployment.DeploymentService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class ProcessService {

    @Inject
    DeploymentService deploymentService;


    @Inject
    ProcessInstanceRepository processInstanceRepository;

//    public Uni<String> find(String id) {
//
//        String tmpId = "/p6/pi/queue/" + id + "/lock";
//        ByteSequence lockId = ByteSequence.from(tmpId.getBytes());
//
//        String tmpKey = "/p6/pi/data/" + id + "/json";
//        ByteSequence key = ByteSequence.from(tmpKey.getBytes());
//
//
//        return Uni.createFrom().completionStage(lease.grant(10L))
//                .onItem().produceUni(r ->
//                    Uni.createFrom().completionStage(client.txn()
//                    .If(
//                            new Cmp(key, Cmp.Op.GREATER, CmpTarget.version(0)),
//                            new Cmp(lockId, Cmp.Op.EQUAL, CmpTarget.version(0))
//                    )
//                    .Then(
//                            Op.put(lockId, ByteSequence.from("true".getBytes()), PutOption.newBuilder().withLeaseId(r.getID()).build())
//                    ).commit())
//
//        )
//                .map(x -> {
//                    System.out.println("FIND2 " + x);
//                    return "OK";
//                });
//
//    }

//    public Uni<String> find2(String id) {
//        ByteSequence kk = ByteSequence.from(id.getBytes());
//        String tmp = "/p6/pi/lock/" + id;
//        ByteSequence key = ByteSequence.from(tmp.getBytes());
//
////        GetResponse r = Uni.createFrom().completionStage(client.get(key, GetOption.DEFAULT))
////                .await().indefinitely();
//        GetOption option = GetOption.newBuilder()
//                .withRange(ByteSequence.from(("/p6/pi/lock0").getBytes())).build();
//        return Uni.createFrom().completionStage(client.get(key, option))
//                .onItem().produceUni(r -> {
//                    System.out.println("### " + r);
//                    System.out.println("### " + r.getCount());
//                    System.out.println("### " + r.getKvs());
//                if (r.getCount() > 0) {
//                    System.out.println("LOCK EXISTS! " + tmp);
//                    return Uni.createFrom().nullItem();
//                }
//                return lock(key);
//
//        });
//        return Uni.createFrom().completionStage(lease.grant(60L))
//                .onItem().produceUni(x -> {
//                    System.out.println("ID " + x.getID());
//                    return Uni.createFrom().completionStage(lock.lock(key, x.getID()))
//                            .ifNoItem().after(Duration.ofMillis(500L))
//                            .recoverWithUni(() ->
//                                    Uni.createFrom().completionStage(lease.revoke(x.getID()))
//                                    .map(rt -> RESPONSE)
//                            )
//                            .onItem().ifNotNull().apply(r -> {
//                                System.out.println("### " + r.getKey().toString(StandardCharsets.UTF_8));
//                                return "OK";
//                            });
//        });

//        String tmp = "/p6/pi";
//        GetOption option = GetOption.newBuilder()
//                .withRange(ByteSequence.from((tmp + "0").getBytes()))
//                .withSerializable(true)
//                .build();
//        return Uni.createFrom().completionStage(client.get(ByteSequence.from(tmp.getBytes()), option))
//                .map(x -> {
//                    System.out.println("GET " + x);
//                    return "OK";
//                });
//    }

//    private Uni<String> lock(ByteSequence key) {
//        return Uni.createFrom().completionStage(lease.grant(60L))
//                .onItem().produceUni(x -> {
//            System.out.println("ID " + x.getID());
//            return Uni.createFrom().completionStage(lock.lock(key, x.getID()))
//                    .ifNoItem().after(Duration.ofMillis(500L))
//                    .recoverWithUni(() ->
//                            Uni.createFrom().completionStage(lease.revoke(x.getID()))
//                                    .map(rt -> RESPONSE)
//                    )
//                    .onItem().ifNotNull().apply(r -> {
//                        System.out.println("### " + r.getKey().toString(StandardCharsets.UTF_8));
//                        return "OK";
//                    });
//        });
//    }

//      public Uni<Long> createRequest(JsonObject data) {
//          return processQueueDAO.create(data);
//      }

//    public Uni<ProcessInstance> startProcess(StartProcessRequestDTO request) {
//
//        ProcessInstance pi = createProcessInstance(request);
//        if (pi == null) {
//            return Uni.createFrom().nullItem();
//        }
//        return Uni.createFrom().nullItem();
////        return client.xadd(List.of("stream1", "*", request.id, UUID.randomUUID().toString()))
////                .onItem().transform(x -> pi);
//    }

//    private ProcessInstance createProcessInstance(StartProcessRequestDTO request) {
//
//        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(request.processId, request.processVersion);
//        if (pd == null) {
//            log.error("No process definition found for the {}/{}/{}", request.id, request.processId, request.processVersion);
//            return null;
//        }
//
//        ProcessInstance pi = new ProcessInstance();
//        pi.id = UUID.randomUUID().toString();
//        pi.status = ProcessInstance.Status.CREATED;
//        pi.processId = request.processId;
//        pi.processVersion = request.processVersion;
//        if (request.data != null) {
//            pi.data.putAll(request.data);
//        }
//        log.info("Create ProcessInstance {}", pi);
//        return pi;
//    }

}
