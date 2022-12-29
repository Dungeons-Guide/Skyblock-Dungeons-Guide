/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.launcher.loader;

import kr.syeyoung.dungeonsguide.launcher.branch.Update;
import kr.syeyoung.dungeonsguide.launcher.exceptions.InvalidSignatureException;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SignatureValidator {
    private static final String dungeonsGuideMasterSigningKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "\n" +
            "mQINBGIKe1gBEACq9zKF93cupt/ymFnpxYJdhB1wXhOXDix0+LoclBVOTiyckdF5\n" +
            "QyKkNpRCYai9Pc111pNaPjHVKZyAyWjMbJpb6f1ar2Fhw1OBqchgwE3RIGUtPyan\n" +
            "KtrNhTf7U4uA2KsQGeIlpnURy+eQxubY+yrmjMGhP6QIu/o7Ci5lNfKnCCp/6I0S\n" +
            "yojmgZ7kbx1lTpFF6giqX/fCEbB3lOOT1oqj2bWUhELz9/AezeA895IEv+TDb7Tx\n" +
            "GUSM3/buDvrBeeSV+TTQ82y1JtNO0wyAS0HC86QbchQrsShBawpH2b+CqxG5y66h\n" +
            "b1X11kvEnjyOOiMtfsJsuqQeTdlJzvrDn9F28mU/J9XAsEH1bW831wSJLkOSp6uU\n" +
            "aSLtx+Bo/Em/Pfb8XTeF1CMo6cpoWEsLS4XrjN4J+ybHQArj2j0ujHDzXb0iOsGG\n" +
            "d7sPa4Q5g3a6+Liwd9K8nMt5JxRwqmEA3Uvz37pecPbj6HB8rvYzkdE+4wNFzZR6\n" +
            "lIhAPM73IYnXCWt7+hFws8G+q0dnElf847R/2YbKnCbk9njC9et6NmbjTk2Hn67Q\n" +
            "qYlEqee1MxfZz6x2MYeDQY6sF3b7bQS5juXB/FNzxPL1HtvcnXNfp4EdFXzTt2S4\n" +
            "gVlGRO7Y/kXWW8kpeSnXlnH+cmprmaVIX/bUlRl606PFEXCs6g51RITxXQARAQAB\n" +
            "tENzeWV5b3VuZyAoRHVuZ2VvbnMgR3VpZGUgQmluYXJ5IFNpZ25hdHVyZSBLZXkp\n" +
            "IDxjeW91bmcwNkBuYXZlci5jb20+iQJOBBMBCAA4FiEEZLh+2i0+aBePKaep3Tuu\n" +
            "MijcSs0FAmIKe1gCGwMFCwkIBwIGFQoJCAsCBBYCAwECHgECF4AACgkQ3TuuMijc\n" +
            "Ss02LxAAoHLAM72GyIqoKiIdc80+6zwHTGHvbH6uuIn0t2m1SO/ayN1kxUvXaYBQ\n" +
            "edi9URHolr0KR6WLx5hxLDlLgujwN+x/ZyGvB1KX75b+st0EOdnOMdGl+ATuZ8bY\n" +
            "n6iF8UIAxHP4iz7hEJKsqnALJWm2c6N+kkFZQPSRQLQNMCH2QVFDTSjJoNfgdKPh\n" +
            "szpQsolQ4AA9a5hI/3RZkG06lMnb0EpIuLMHUo72Fkiur12vP3SYk8rL4DzGhhko\n" +
            "6s/5bj/Prns/z4cJUUTuwoHf20BrlEefiKlC/5yIdlUteUa+Dwtxa8yxGfwBIsoq\n" +
            "hdlBPz94s5gSq86GbjTppqVy9DmB1AXs5izbPCJCa/rphgfVpUaR73gHwrXIRFJJ\n" +
            "hkE7LlM762hK1IZbD9ICA9aCiiVExnS4jpUJfayOMTIfGOQ19o/1DiNtxfAzQ4p5\n" +
            "THGE25MSNz1FI8F0u21l1w3MD1wRw4AibUI3cdSbRCJ02d5wtivjRg8+erW67cn8\n" +
            "n4/lUgHFCickvVZ0rBM6xUNsOgt5JQDIw4V+sL2fPIYzXhm/gchxLedmDakHyYH+\n" +
            "PJEMOSEad8MRQ6LejDzqkPUFY9dUYUaB9jSg8VZ+OGy6j72JjX98M6z8BNcc1kMn\n" +
            "jL7XiqRGwVzajcGKERa9XdQdzVf5j2iZgWFoBie9ygo3LOJFOZm5Ag0EYgp7WAEQ\n" +
            "APAa6LjaHA9N7n0W2/FrWLf38w7BOSmQB627gllQUnjRm6oBFT5doDxl2WTM7WXf\n" +
            "DoO+m0j3pWGVTz+P3DDHC/OUWqa8pDrYqSqNGxcBlkiT31eINb4cd5nFR8ikUdJd\n" +
            "XbNig4fOzzr/JgmkQgJ679OV7BCrnQ4rlYDwjeTMfGekv9/37om3CWrwUn0+j0bM\n" +
            "JiJQVFyzY7d6eD8urImFYNipRe8aTAwBUdocpdaxx2K8sJwjdGleekQmPJakkwec\n" +
            "ZCrIaJ/qznRttHu790MPU6h+/FqH5pLtQSGLJ8wcyLCl5ox8TasjUu1r0eysURuG\n" +
            "NxMsGztEhnM6SM6JZLAk1PJdDSOqVPrYV/TrdaKUpdgy1wSbmeYkFqr849HOEz46\n" +
            "wu4efrzfKyoLw+v8zeD+FpbiQUWOGSudVXqL6simGeHeFJQ6gGP+Bp8w9H3ARndw\n" +
            "OoRtjXYxoQuG7ER9KUB6k1jPyNmUwe4Pd2YNQS8XJfGQ+5cqAZ743KJejo2pTnrP\n" +
            "mT0NWAuAxllxXImP3yM5Qtc+FLwaBO6moK5C/0wKZ1Ieojnwc8BQzK4fkMo/dt9j\n" +
            "dFf3GaL1hishEDGnPo59nX7msWC/eQgukhNieUqSZwET6KobZAp7CwIP8Z+P4MhC\n" +
            "Hzo4aPq3ukXc43SaZK+p8EkMQ6TY0bBBOsFlUEUESlOTABEBAAGJAjYEGAEIACAW\n" +
            "IQRkuH7aLT5oF48pp6ndO64yKNxKzQUCYgp7WAIbDAAKCRDdO64yKNxKzbCLD/9Z\n" +
            "TPSzB/Pwd9q8pSBhBnfKyfgB98FefRSuqurJVjYtgfzIcnSIEHzd8ft6BO6cj6qV\n" +
            "T006acPRwffnbzDuJyfPJf9l/wRnyh/63/3tKESWJJfGEspXTBfIdef85E4hAo8J\n" +
            "QuS7DsMm3djcAYgg1DiYidTZdv4qpr4ADgecH4iYX5FViWl5qZeHThZM6E4HPw5h\n" +
            "bLbbjdwOwNR/fUlClQz11wiDdEA3dyc4BQL8rox9wTvPHRKgQVTEkAoyEzm6IgQS\n" +
            "LLK+RL5Zd2AMy20SbCGQiiKM3nhQ/K6ykGZVcunRoz5nS+/yTTQNFQ2FuhO+S9Vk\n" +
            "a9dELsxKxwpVcX3SeQ4sfu6scGufrJx4KzpXttx4IOwmuUVzHSOY6vZwnW9DArbM\n" +
            "vCslVRkjrqSIoUSiEBGo5RQWmQmhUpSTT9k3AsGBYtXvD6AhNQvQbQz6JwbqXiRs\n" +
            "KGA9qgt7uGU1c19z5AfXzHAMRgzgNZJexUiJl1oC5bpe+lpvNAIL/5u4SOywDoo/\n" +
            "DP5Q9owbDSp4Anc4CU2y7exhCl/Suy0jtbnaVHGfSnKSokazHl8wLQC5qSiaMJBX\n" +
            "zGGzky0d0PNpiYbu/FxsgSF9u0vbafSN5vlMMNU/WH6sjasga0KN4tYGMVV/1L0r\n" +
            "MA7pIBUIqxqgfJIHWswS9VPsd+2NZxKUxlG+bl+wNA==\n" +
            "=3u3j\n" +
            "-----END PGP PUBLIC KEY BLOCK-----\n";


    private static PGPPublicKeyRingCollection publicKeyRingCollection;
    private static Exception loadingException;
    static {
        InputStream in = new ByteArrayInputStream(dungeonsGuideMasterSigningKey.getBytes());
        try {
            publicKeyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in), new JcaKeyFingerprintCalculator());
        } catch (IOException | PGPException e) {
            loadingException = e;
        }
    }

    public static void validateVersion1Signature(Update update, byte[] payload, byte[] signature) {
        if (publicKeyRingCollection == null) {
            throw new InvalidSignatureException(update, loadingException);
        }
        try {
            InputStream sigin = PGPUtil.getDecoderStream(new ByteArrayInputStream(signature));
            JcaPGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory(sigin);
            PGPSignatureList p3 = (PGPSignatureList) pgpObjectFactory.nextObject();
            PGPSignature signature1 = p3.get(0);
            PGPPublicKey publicKey = publicKeyRingCollection.getPublicKey(signature1.getKeyID());
            signature1.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);

            signature1.update(payload);

            boolean truth = signature1.verify();
            if (!truth) throw new InvalidSignatureException(update, "DG SIGNATURE FORGED");
        }catch (IOException | PGPException e) {
            throw new InvalidSignatureException(update, e);
        }
    }


}
