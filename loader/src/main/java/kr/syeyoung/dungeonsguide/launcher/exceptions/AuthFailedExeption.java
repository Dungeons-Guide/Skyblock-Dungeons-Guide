package kr.syeyoung.dungeonsguide.launcher.exceptions;

public class AuthFailedExeption extends AuthenticationUnavailableException {
    public AuthFailedExeption(Throwable cause) {
        super(cause);
    }
}
