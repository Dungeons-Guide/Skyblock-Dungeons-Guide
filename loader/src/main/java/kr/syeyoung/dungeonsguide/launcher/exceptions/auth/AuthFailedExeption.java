package kr.syeyoung.dungeonsguide.launcher.exceptions.auth;

public class AuthFailedExeption extends AuthenticationUnavailableException {
    public AuthFailedExeption(Throwable cause) {
        super(cause);
    }
}
