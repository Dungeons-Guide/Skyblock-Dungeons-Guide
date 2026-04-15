package kr.syeyoung.modapi;

import java.util.UUID;

// why not share with authapi?? I'm pretty sure i'll make breaking changes to authapi and it's pain to handle mismatching dependency versions.
public interface AuthService {
    /**
     * Always Always Always make sure serverId incorporates both random value from
     * * the client <--- very important
     * * the server's pubkey <-- important on serverside
     * Client's secret must be encrypted with server's pubkey to prevent attacks
     *
     * If this method is called with unverified values, it may hijack user's account
     *
     * Here, I list some of wrong implementations that call this method
     * 1. Using server provided value solely
     * -> You're letting server control entirity of serverId, which means the server owner can join any other server as the user's account
     *
     * 2. Using client generated random value solely
     * *** THIS IS COMMON IMPLEMENTATION ON MANY MODS. IF YOU CARE ABOUT CLIENT LEGITIMACY, PLEASE DO NOT DO THIS ***
     * -> Server has no way of telling if the client to it is really the user
     * -> Which makes attacks like
     *    User -> Any malicious minecraft server -> Any other auth server (2)
     * -> Possible. Malicious minecraft server can just send derivied value to (2).
     *
     *
     * Also when you're super cautious...
     * 3. Using server provided value AND client generated random value, but client random value is not encrypted with server's pubkey
     * -> Server also has no way of telling if the client to it is really the user
     * -> Why?
     *    User -> Any malicious auth server (1) -> Any other auth server (2)
     *    (1) can just relay (2)'s random value to the client, and relay client's response to (2)
     *    Since (1) is doing mitm, it obtains dg jwt.
     *
     * 4. Using server provided value AND client generated random value, client random value is encrypted, but the server's response is not encrypted with client's random value
     * -> Same with 3, middle man can just relay and look at response.
     *
     * Note attacks 3, 4 is blocked if server uses https, and client verifies tls cert thoroughly.
     *
     *
     * Here's one correct way to implement this method (assuming your isp is malicious and you do not use tls cert)
     * 1. Server provides its pubkey to the client
     * 2. Client generates random value, encrypts it with server's pubkey, and sends it to the server
     * 3. Client also sends derivied value (random val + server pub key) to mojang auth
     * 4. Server decrypts client's random value, and sends derived value to mojang auth
     * 5. Mojang auth checks if the derived value is correct, and responds accordingly
     * 6. Server sends back the result to the client, encrypted with client's random value
     * 7. Middle man has no way of knowing the jwt
     *
     * -> This prevents attacks in 1,2,3
     *
     * Here's another correct way to call this method
     * -> do 3, but use https.
     *
     * */
    void mojangAuth(String serverId);

    UUID getCurrentPlayerUUID();
    String getCurrentPlayerUsername();
}
