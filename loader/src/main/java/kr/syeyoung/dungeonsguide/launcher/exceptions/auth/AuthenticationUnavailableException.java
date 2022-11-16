package kr.syeyoung.dungeonsguide.launcher.exceptions.auth;

public class AuthenticationUnavailableException extends RuntimeException {
    public AuthenticationUnavailableException(Throwable cause){
        super(cause);
    }

    public AuthenticationUnavailableException(String s) {
        super(s);
    }
    public AuthenticationUnavailableException() {}
}
