package com.aliakkoyun.mysocket.socket;

import com.aliakkoyun.mysocket.model.Message;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketModule {

    private final SocketIOServer socketIOServer;

    public SocketModule(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
        socketIOServer.addConnectListener(onConnected());
        socketIOServer.addDisconnectListener(onDisconnected());
        socketIOServer.addEventListener("send_message", Message.class, onMessageRecived());
    }


    private DataListener<Message> onMessageRecived(){
        return (senderClient, data, ackSender) -> {
            log.info(data.getContent());
            log.info(String.format("%s -> %s", senderClient.getSessionId().toString(), data.getContent()));
//            senderClient.getNamespace().getBroadcastOperations().sendEvent("get_message",data.getContent());
//            ÜSTTEKİ KULLANIM MESAJIN BİZE GERİ DÖNMESİNE SEBEP OLUR

            String room = senderClient.getHandshakeData().getSingleUrlParam("room");
            senderClient.getNamespace().getRoomOperations(room).getClients().forEach(
                    x -> {
                        if(!x.getSessionId().equals(senderClient.getSessionId())){
                            x.sendEvent("get_message", data.getContent());
                        }
                    }
            );

            /*
            senderClient.getNamespace().getAllClients().forEach(
                    x -> {
                        if(!x.getSessionId().equals(senderClient.getSessionId())){
                             x.sendEvent("get_message", data.getContent());
                        }
                    }
            );
            */

        };
    }
    private ConnectListener onConnected(){
        return client -> {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            client.joinRoom(room);
            client.getNamespace().getRoomOperations(room)
                    .sendEvent("get_message", String.format("%s connected to-> %s",client.getSessionId(),room));
            log.info(String.format("SocketID: %s connected.", client.getSessionId().toString()));
        };
    }
    private DisconnectListener onDisconnected(){
        return client -> {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            client.getNamespace().getRoomOperations(room)
                            .sendEvent("get_message", String.format("%s disconnected to -> %s", client.getSessionId(), room));
            log.info(String.format("SocketID: %s disconnected.",client.getSessionId().toString()));
        };
    }

}
