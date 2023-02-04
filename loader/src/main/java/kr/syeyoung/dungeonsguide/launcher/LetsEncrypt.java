/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;

// Smh minecraft default launcher letsencrypt
public class LetsEncrypt {
    public static SSLSocketFactory LETS_ENCRYPT;

    static {
        try {
            LETS_ENCRYPT = letsEncryptAddedFactory();
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException |
                 KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static SSLSocketFactory letsEncryptAddedFactory() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
        String keyStoreLocation = System.getProperty( "javax.net.ssl.trustStore", Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts").toString());
        String keyStorePassword = System.getProperty( "javax.net.ssl.trustStorePassword", "" ); // You might ask, "THE DEFAULT PASSWORD IS changeit". But in fact, just loading keystore does not require a key!! https://stackoverflow.com/a/42363257
        String keyStoreType = System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());

        char[] charArr = keyStorePassword.isEmpty() ? null : keyStorePassword.toCharArray();

        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        try (InputStream readStream = Files.newInputStream(Paths.get(keyStoreLocation))) {
            keyStore.load(readStream, charArr);
        }

        if (keyStore.getCertificate("ISRGRootX1") == null) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream caInput = LetsEncrypt.class.getResourceAsStream("/isrgrootx1.der")) {
                Certificate crt = cf.generateCertificate(caInput);
                keyStore.setCertificateEntry("ISRGRootX1", crt);
            }
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext context = SSLContext.getInstance( "TLS" );
        context.init( null, tmf.getTrustManagers(), null);
        return context.getSocketFactory();
    }
}
