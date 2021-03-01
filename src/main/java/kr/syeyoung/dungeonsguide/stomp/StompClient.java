package kr.syeyoung.dungeonsguide.stomp;

import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import sun.security.ssl.SSLSocketFactoryImpl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class StompClient extends WebSocketClient implements StompInterface {
    private SSLSocketFactory getSocketfactory() throws NoSuchAlgorithmException, KeyManagementException, CertificateException, KeyStoreException, IOException {
        X509Certificate a = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(("-----BEGIN CERTIFICATE-----\n" +
                        "MIIEZTCCA02gAwIBAgIQQAF1BIMUpMghjISpDBbN3zANBgkqhkiG9w0BAQsFADA/\n" +
                        "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n" +
                        "DkRTVCBSb290IENBIFgzMB4XDTIwMTAwNzE5MjE0MFoXDTIxMDkyOTE5MjE0MFow\n" +
                        "MjELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUxldCdzIEVuY3J5cHQxCzAJBgNVBAMT\n" +
                        "AlIzMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuwIVKMz2oJTTDxLs\n" +
                        "jVWSw/iC8ZmmekKIp10mqrUrucVMsa+Oa/l1yKPXD0eUFFU1V4yeqKI5GfWCPEKp\n" +
                        "Tm71O8Mu243AsFzzWTjn7c9p8FoLG77AlCQlh/o3cbMT5xys4Zvv2+Q7RVJFlqnB\n" +
                        "U840yFLuta7tj95gcOKlVKu2bQ6XpUA0ayvTvGbrZjR8+muLj1cpmfgwF126cm/7\n" +
                        "gcWt0oZYPRfH5wm78Sv3htzB2nFd1EbjzK0lwYi8YGd1ZrPxGPeiXOZT/zqItkel\n" +
                        "/xMY6pgJdz+dU/nPAeX1pnAXFK9jpP+Zs5Od3FOnBv5IhR2haa4ldbsTzFID9e1R\n" +
                        "oYvbFQIDAQABo4IBaDCCAWQwEgYDVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8E\n" +
                        "BAMCAYYwSwYIKwYBBQUHAQEEPzA9MDsGCCsGAQUFBzAChi9odHRwOi8vYXBwcy5p\n" +
                        "ZGVudHJ1c3QuY29tL3Jvb3RzL2RzdHJvb3RjYXgzLnA3YzAfBgNVHSMEGDAWgBTE\n" +
                        "p7Gkeyxx+tvhS5B1/8QVYIWJEDBUBgNVHSAETTBLMAgGBmeBDAECATA/BgsrBgEE\n" +
                        "AYLfEwEBATAwMC4GCCsGAQUFBwIBFiJodHRwOi8vY3BzLnJvb3QteDEubGV0c2Vu\n" +
                        "Y3J5cHQub3JnMDwGA1UdHwQ1MDMwMaAvoC2GK2h0dHA6Ly9jcmwuaWRlbnRydXN0\n" +
                        "LmNvbS9EU1RST09UQ0FYM0NSTC5jcmwwHQYDVR0OBBYEFBQusxe3WFbLrlAJQOYf\n" +
                        "r52LFMLGMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0B\n" +
                        "AQsFAAOCAQEA2UzgyfWEiDcx27sT4rP8i2tiEmxYt0l+PAK3qB8oYevO4C5z70kH\n" +
                        "ejWEHx2taPDY/laBL21/WKZuNTYQHHPD5b1tXgHXbnL7KqC401dk5VvCadTQsvd8\n" +
                        "S8MXjohyc9z9/G2948kLjmE6Flh9dDYrVYA9x2O+hEPGOaEOa1eePynBgPayvUfL\n" +
                        "qjBstzLhWVQLGAkXXmNs+5ZnPBxzDJOLxhF2JIbeQAcH5H0tZrUlo5ZYyOqA7s9p\n" +
                        "O5b85o3AM/OJ+CktFBQtfvBhcJVd9wvlwPsk+uyOy2HI7mNxKKgsBTt375teA2Tw\n" +
                        "UdHkhVNcsAKX1H7GNNLOEADksd86wuoXvg==\n" +
                        "-----END CERTIFICATE-----").getBytes()));

        KeyStore b = KeyStore.getInstance(KeyStore.getDefaultType());
        b.load(null, null);
        b.setCertificateEntry(Integer.toString(1), a);

        TrustManagerFactory c = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        c.init(b);

        SSLContext d = SSLContext.getInstance("TLSv1.2");
        d.init(null, c.getTrustManagers(), null);
        return d.getSocketFactory();
    }
    public StompClient(URI serverUri, final String token, CloseListener closeListener) throws Exception {
        super(serverUri);
        this.closeListener = closeListener;
        addHeader("Authorization", token);
        setSocketFactory(getSocketfactory());

        connectBlocking();
        while(this.stompClientStatus == StompClientStatus.CONNECTING);
    }
    private CloseListener closeListener;

    @Getter
    private volatile StompClientStatus stompClientStatus = StompClientStatus.CONNECTING;

    @Getter
    private StompPayload errorPayload;

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send(new StompPayload().method(StompHeader.CONNECT)
                .header("accept-version","1.2")
                .header("host",uri.getHost()).getBuilt()
        );
    }

    @Override
    public void onMessage(String message) {
        try {
            StompPayload payload = StompPayload.parse(message);
            if (payload.method() == StompHeader.CONNECTED) {
                stompClientStatus = StompClientStatus.CONNECTED;
            } else if (payload.method() == StompHeader.ERROR) {
                errorPayload = payload;
                stompClientStatus = StompClientStatus.ERROR;
                this.close();
            } else if (payload.method() == StompHeader.MESSAGE) {
                // mesage
                StompSubscription stompSubscription = stompSubscriptionMap.get(Integer.parseInt(payload.headers().get("subscription")));
                try {
                    stompSubscription.getStompMessageHandler().handle(this, payload);
                    if (stompSubscription.getAckMode() != StompSubscription.AckMode.AUTO) {
                        send(new StompPayload().method(StompHeader.ACK)
                                .header("id",payload.headers().get("ack")).getBuilt()
                        );
                    }
                } catch (Exception e) {
                    send(new StompPayload().method(StompHeader.NACK)
                            .header("id",payload.headers().get("ack")).getBuilt()
                    );
                    e.printStackTrace();
                }
            } else if (payload.method() == StompHeader.RECEIPT) {
                String receipt_id = payload.headers().get("receipt-id");
                StompPayload payload1 = receiptMap.remove(Integer.parseInt(receipt_id));
                if (payload1.method() == StompHeader.DISCONNECT) {
                    stompClientStatus = StompClientStatus.DISCONNECTED;
                    close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        closeListener.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {

    }

    private Map<Integer, StompSubscription> stompSubscriptionMap = new HashMap<Integer, StompSubscription>();
    private Map<Integer, StompPayload> receiptMap = new HashMap<Integer, StompPayload>();

    private int idIncrement = 0;

    @Override
    public void send(StompPayload payload) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        payload.method(StompHeader.SEND);
        if (payload.headers().get("receipt") != null)
            receiptMap.put(Integer.parseInt(payload.headers().get("receipt")), payload);
        send(payload);
    }

    @Override
    public void subscribe(StompSubscription stompSubscription) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        stompSubscription.setId(++idIncrement);

        send(new StompPayload().method(StompHeader.SUBSCRIBE)
                .header("id",String.valueOf(stompSubscription.getId()))
                .header("destination", stompSubscription.getDestination())
                .header("ack", stompSubscription.getAckMode().getValue()).getBuilt()
        );

        stompSubscriptionMap.put(stompSubscription.getId(), stompSubscription);
    }

    @Override
    public void unsubscribe(StompSubscription stompSubscription) {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        send(new StompPayload().method(StompHeader.UNSUBSCRIBE)
                .header("id",String.valueOf(stompSubscription.getId())).getBuilt()
        );
        stompSubscriptionMap.remove(stompSubscription.getId());
    }

    @Override
    public void disconnect() {
        if (stompClientStatus != StompClientStatus.CONNECTED) throw new IllegalStateException("not connected");
        StompPayload stompPayload;
        stompClientStatus =StompClientStatus.DISCONNECTING;
        send((stompPayload = new StompPayload().method(StompHeader.DISCONNECT)
                .header("receipt", String.valueOf(++idIncrement)))
                .getBuilt()
        );
        receiptMap.put(idIncrement, stompPayload);
    }
}
