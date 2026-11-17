package com.open_f.fu.shared.realtime;

import java.io.IOException;
import java.util.ArrayDeque;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

/** Una sola escritura en curso y una cola acotada por conexión. */
final class WebSocketSender {
    private static final int MAX_MESSAGES = 128;
    private static final int MAX_CHARS = 128 * 1024;
    private final Session session;
    private final ArrayDeque<String> pending = new ArrayDeque<>();
    private boolean sending;
    private boolean closed;
    private int chars;

    WebSocketSender(Session session) {
        this.session = session;
        session.getAsyncRemote().setSendTimeout(5_000);
    }

    void offer(String text) {
        boolean overflow;
        String first = null;
        synchronized (this) {
            if (closed || !session.isOpen()) return;
            overflow = text.length() > MAX_CHARS || pending.size() >= MAX_MESSAGES
                    || chars > MAX_CHARS - text.length();
            if (!overflow) {
                pending.addLast(text);
                chars += text.length();
                if (!sending) {
                    sending = true;
                    first = pending.getFirst();
                }
            }
        }
        if (overflow) {
            fail();
        } else if (first != null) {
            send(first);
        }
    }

    private void send(String text) {
        try {
            session.getAsyncRemote().sendText(text, result -> {
                if (!result.isOK()) {
                    fail();
                    return;
                }
                String next;
                synchronized (this) {
                    if (closed) return;
                    chars -= pending.removeFirst().length();
                    next = pending.peekFirst();
                    if (next == null) sending = false;
                }
                if (next != null) send(next);
            });
        } catch (RuntimeException error) {
            fail();
        }
    }

    synchronized void clear() {
        closed = true;
        pending.clear();
        chars = 0;
    }

    private void fail() {
        clear();
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY,
                    "No se pudo entregar el evento; vuelve a conectar"));
        } catch (IOException ignored) {
            // El endpoint o el barrido periódico eliminan la sesión cerrada.
        }
    }
}
