package kr.syeyoung.dungeonsguide.launcher.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;

@Data
@AllArgsConstructor
public class DGResponse<T> {
    private final int responseCode;
    private final String status;
    private final T data;
    private final String errorMessage;
    private final String qrCode;
}

