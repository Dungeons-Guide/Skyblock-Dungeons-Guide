package kr.syeyoung.dungeonsguide.launcher.loader;

import kr.syeyoung.dungeonsguide.launcher.branch.Update;
import kr.syeyoung.dungeonsguide.launcher.exceptions.InvalidSignatureException;
import org.apache.commons.codec.binary.Base64;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SignatureValidator {
    private static PublicKey dgPublicKey;
    private static PublicKey getDGPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (dgPublicKey != null) return dgPublicKey;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxO89qtwG67jNucQ9Y44c" +
                "IUs/B+5BeJPs7G+RG2gfs4/2+tzF/c1FLDc33M7yKw8aKk99vsBUY9Oo8gxxiEPB" +
                "JitP/qfon2THp94oM77ZTpHlmFoqbZMcKGZVI8yfvEL4laTM8Hw+qh5poQwtpEbK" +
                "Xo47AkxygxJasUnykER2+aSTZ6kWU2D4xiNtFA6lzqN+/oA+NaYfPS0amAvyVlHR" +
                "n/8IuGkxb5RrlqVssQstFnxsJuv88qdGSEqlcKq2tLeg9hb8eCnl2OFzvXmgbVER" +
                "0JaV+4Z02fVG1IlR3Xo1mSit7yIU6++3usRCjx2yfXpnGGJUW5pe6YETjNew3ax+" +
                "FAZ4GePWCdmS7FvBnbbABKo5pE06ZTfDUTCjQlAJQiUgoF6ntMJvQAXPu48Vr8q/" +
                "mTcuZWVnI6CDgyE7nNq3WNoq3397sBzxRohMxuqzl3T19zkfPKF05iV2Ju1HQMW5" +
                "I119bYrmVD240aGESZc20Sx/9g1BFpNzQbM5PGUlWJ0dhLjl2ge4ip2hHciY3OEY" +
                "p2Qy2k+xEdenpKdL+WMRimCQoO9gWe2Tp4NmP5dppDXZgPjXqjZpnGs0Uxs+fXqW" +
                "cwlg3MbX3rFl9so/fhVf4p9oXZK3ve7z5D6XSSDRYECvsKIa08WAxJ/U6n204E/4" +
                "xUF+3ZgFPdzZGn2PU7SsnOsCAwEAAQ=="));
        return dgPublicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static void validateVersion1Signature(Update update, byte[] payload, byte[] signature) {
        try {
            Signature sign = Signature.getInstance("SHA512withRSA");
            sign.initVerify(getDGPublicKey());
            sign.update(payload);
            boolean truth = sign.verify(signature);
            if (!truth) throw new InvalidSignatureException(update, "DG SIGNATURE FORGED");
        }catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            throw new InvalidSignatureException(update, e);
        }
    }


}
