package com.trackingpath.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {
  private static final Logger log = LoggerFactory.getLogger(LiveWebSocketHandler.class);

  private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
  private final ObjectMapper mapper;

  public LiveWebSocketHandler(ObjectMapper mapper) {
    // prefer injecting a shared ObjectMapper (thread-safe after configuration)
    this.mapper = mapper;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    sessions.add(session);
    log.info("WS connected: id={} uri={}", session.getId(), session.getUri());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    log.warn("WS transport error (id={}): {}", session.getId(), exception.toString());
    safeClose(session, CloseStatus.SERVER_ERROR);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    sessions.remove(session);
    log.info("WS closed: id={} status={}", session.getId(), status);
  }

  /** Broadcast a POJO as JSON to all open sessions. */
  public void broadcast(Object payload) {
    try {
      String json = mapper.writeValueAsString(payload);
      broadcastJson(json);
    } catch (IOException e) {
      log.error("Failed to serialize WS payload", e);
    }
  }

  /** Broadcast a prebuilt JSON string to all open sessions. */
  public void broadcastJson(String json) {
    TextMessage msg = new TextMessage(json);
    Iterator<WebSocketSession> it = sessions.iterator();
    while (it.hasNext()) {
      WebSocketSession s = it.next();
      if (!s.isOpen()) { it.remove(); continue; }
      try {
        s.sendMessage(msg);
      } catch (Exception ex) {
        log.warn("WS send failed; dropping session id={}", s.getId(), ex);
        safeClose(s, CloseStatus.PROTOCOL_ERROR);
        it.remove();
      }
    }
  }

  private void safeClose(WebSocketSession s, CloseStatus status) {
    try { if (s.isOpen()) s.close(status); } catch (Exception ignore) {}
    sessions.remove(s);
  }

  /** (Optional) how many clients are connected */
  public int sessionCount() { return sessions.size(); }
}
