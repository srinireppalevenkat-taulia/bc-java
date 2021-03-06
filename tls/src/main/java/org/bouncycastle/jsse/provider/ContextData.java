package org.bouncycastle.jsse.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.X509ExtendedKeyManager;

import org.bouncycastle.jsse.BCX509ExtendedTrustManager;
import org.bouncycastle.jsse.java.security.BCAlgorithmConstraints;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.tls.SignatureScheme;
import org.bouncycastle.tls.crypto.TlsCrypto;

final class ContextData
{
    // TODO Support jdk.tls.signatureSchemes, a mooted feature in SunJSSE (see JDK-8229720)

    private static final int[] DEFAULT_ACTIVE = {
//        SignatureScheme.ed25519,
//        SignatureScheme.ed448,
        SignatureScheme.ecdsa_secp256r1_sha256,
        SignatureScheme.ecdsa_secp384r1_sha384,
        SignatureScheme.ecdsa_secp521r1_sha512,
//        SignatureScheme.rsa_pss_rsae_sha256,
//        SignatureScheme.rsa_pss_rsae_sha384,
//        SignatureScheme.rsa_pss_rsae_sha512,
//        SignatureScheme.rsa_pss_pss_sha256,
//        SignatureScheme.rsa_pss_pss_sha384,
//        SignatureScheme.rsa_pss_pss_sha512,
        SignatureScheme.rsa_pkcs1_sha256,
        SignatureScheme.rsa_pkcs1_sha384,
        SignatureScheme.rsa_pkcs1_sha512,
        SignatureSchemeInfo.historical_dsa_sha256,
        SignatureSchemeInfo.historical_ecdsa_sha224,
        SignatureSchemeInfo.historical_rsa_sha224,
        SignatureSchemeInfo.historical_dsa_sha224,
        SignatureScheme.ecdsa_sha1,
        SignatureScheme.rsa_pkcs1_sha1,
        SignatureSchemeInfo.historical_dsa_sha1,
        SignatureSchemeInfo.historical_rsa_md5,
    };

    private static void addSignatureScheme(TlsCrypto crypto, Map<Integer, SignatureSchemeInfo> ss, int signatureScheme,
        String name, String jcaSignatureAlgorithm, String keyAlgorithm)
    {
        boolean enabled = crypto.hasSignatureScheme(signatureScheme);

        SignatureSchemeInfo signatureSchemeInfo = new SignatureSchemeInfo(signatureScheme, name, jcaSignatureAlgorithm,
            keyAlgorithm, enabled);

        if (null != ss.put(signatureScheme, signatureSchemeInfo))
        {
            throw new IllegalStateException("Duplicate entries for SignatureSchemeInfo");
        }
    }

    private static void addSignatureScheme(TlsCrypto crypto, Map<Integer, SignatureSchemeInfo> ss, int signatureScheme,
        String jcaSignatureAlgorithm, String keyAlgorithm)
    {
        String name = SignatureScheme.getName(signatureScheme);

        addSignatureScheme(crypto, ss, signatureScheme, name, jcaSignatureAlgorithm, keyAlgorithm);
    }

    private static void addSignatureSchemeHistorical(TlsCrypto crypto, Map<Integer, SignatureSchemeInfo> ss, int signatureScheme,
        String name, String jcaSignatureAlgorithm, String keyAlgorithm)
    {
        // TODO[tls13] Historical schemes can no longer be used
        addSignatureScheme(crypto, ss, signatureScheme, name, jcaSignatureAlgorithm, keyAlgorithm);
    }

    private static void addSignatureSchemeLegacy(TlsCrypto crypto, Map<Integer, SignatureSchemeInfo> ss, int signatureScheme,
        String jcaSignatureAlgorithm, String keyAlgorithm)
    {
        // TODO[tls13] Legacy schemes can still be used for certificate signatures only
        addSignatureScheme(crypto, ss, signatureScheme, jcaSignatureAlgorithm, keyAlgorithm);
    }

    private static Map<Integer, SignatureSchemeInfo> createSignatureSchemesMap(TlsCrypto crypto)
    {
        Map<Integer, SignatureSchemeInfo> ss = new TreeMap<Integer, SignatureSchemeInfo>();

        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pkcs1_sha256, "SHA256withRSA", "RSA");
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pkcs1_sha384, "SHA384withRSA", "RSA");
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pkcs1_sha512, "SHA512withRSA", "RSA");

        // TODO[tls13] Constrain to the specific NamedGroup (only) for TLS 1.3+
        addSignatureScheme(crypto, ss, SignatureScheme.ecdsa_secp256r1_sha256, "SHA256withECDSA", "EC");
        addSignatureScheme(crypto, ss, SignatureScheme.ecdsa_secp384r1_sha384, "SHA384withECDSA", "EC");
        addSignatureScheme(crypto, ss, SignatureScheme.ecdsa_secp521r1_sha512, "SHA512withECDSA", "EC");

        // TODO Ideally would use signature algorithm names like "SHA256withRSAandMGF1" instead of "RSASSA-PSS"
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pss_rsae_sha256, "RSASSA-PSS", "RSA");
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pss_rsae_sha384, "RSASSA-PSS", "RSA");
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pss_rsae_sha512, "RSASSA-PSS", "RSA");

        addSignatureScheme(crypto, ss, SignatureScheme.ed25519, "ed25519", "ed25519");
        addSignatureScheme(crypto, ss, SignatureScheme.ed448, "ed448", "ed448");

        // TODO Ideally would use signature algorithm names like "SHA256withRSAandMGF1" instead of "RSASSA-PSS"
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pss_pss_sha256, "RSASSA-PSS", "RSASSA-PSS");
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pss_pss_sha384, "RSASSA-PSS", "RSASSA-PSS");
        addSignatureScheme(crypto, ss, SignatureScheme.rsa_pss_pss_sha512, "RSASSA-PSS", "RSASSA-PSS");

        /*
         * Legacy algorithms: "These values refer solely to signatures which appear in certificates
         * (see Section 4.4.2.2) and are not defined for use in signed TLS handshake messages,
         * although they MAY appear in "signature_algorithms" and "signature_algorithms_cert" for
         * backward compatibility with TLS 1.2."
         */
        addSignatureSchemeLegacy(crypto, ss, SignatureScheme.rsa_pkcs1_sha1, "SHA1withRSA", "RSA");
        addSignatureSchemeLegacy(crypto, ss, SignatureScheme.ecdsa_sha1, "SHA1withECDSA", "EC");

        /*
         * Historical algorithms (for SignatureAndHashAlgorithm values): TLS 1.2 and earlier only.
         */
        addSignatureSchemeHistorical(crypto, ss, SignatureSchemeInfo.historical_rsa_md5, "rsa_md5", "MD5withRSA",
            "RSA");
        addSignatureSchemeHistorical(crypto, ss, SignatureSchemeInfo.historical_rsa_sha224, "rsa_sha224",
            "SHA224withRSA", "RSA");

        addSignatureSchemeHistorical(crypto, ss, SignatureSchemeInfo.historical_dsa_sha1, "dsa_sha1", "SHA1withDSA",
            "DSA");
        addSignatureSchemeHistorical(crypto, ss, SignatureSchemeInfo.historical_dsa_sha224, "dsa_sha224",
            "SHA224withDSA", "DSA");
        addSignatureSchemeHistorical(crypto, ss, SignatureSchemeInfo.historical_dsa_sha256, "dsa_sha256",
            "SHA256withDSA", "DSA");

        addSignatureSchemeHistorical(crypto, ss, SignatureSchemeInfo.historical_ecdsa_sha224, "ecdsa_sha224",
            "SHA224withECDSA", "EC");

        return Collections.unmodifiableMap(ss);
    }

    private final ProvSSLContextSpi context;
    private final TlsCrypto crypto;
    private final X509ExtendedKeyManager x509KeyManager;
    private final BCX509ExtendedTrustManager x509TrustManager;
    private final ProvSSLSessionContext clientSessionContext;
    private final ProvSSLSessionContext serverSessionContext;

    private final Map<Integer, SignatureSchemeInfo> signatureSchemesMap;

    ContextData(ProvSSLContextSpi context, TlsCrypto crypto, X509ExtendedKeyManager x509KeyManager,
        BCX509ExtendedTrustManager x509TrustManager)
    {
        this.context = context;
        this.crypto = crypto;
        this.x509KeyManager = x509KeyManager;
        this.x509TrustManager = x509TrustManager;
        this.clientSessionContext = new ProvSSLSessionContext(this);
        this.serverSessionContext = new ProvSSLSessionContext(this);

        this.signatureSchemesMap = createSignatureSchemesMap(crypto);
    }

    List<SignatureSchemeInfo> getActiveSignatureSchemes(ProvSSLParameters sslParameters,
        ProtocolVersion[] activeProtocolVersions)
    {
        // TODO[tls13] SignatureSchemeInfo instances need to know their valid versions for sigAlgs/sigAlgsCert
        if (!ProtocolVersion.contains(activeProtocolVersions, ProtocolVersion.TLSv12))
        {
            return null;
        }

        BCAlgorithmConstraints algorithmConstraints = sslParameters.getAlgorithmConstraints();

        int count = DEFAULT_ACTIVE.length;
        ArrayList<SignatureSchemeInfo> result = new ArrayList<SignatureSchemeInfo>(count);
        for (int i = 0; i < count; ++i)
        {
            int signatureScheme = DEFAULT_ACTIVE[i];

            SignatureSchemeInfo signatureSchemeInfo = signatureSchemesMap.get(signatureScheme);
            if (null == signatureSchemeInfo)
            {
                continue;
            }
            if (!signatureSchemeInfo.isEnabled())
            {
                continue;
            }

            // TODO[tls13] Exclude SignatureSchemeInfo instances based on their individual valid version ranges

            if (!signatureSchemeInfo.isPermittedBy(algorithmConstraints))
            {
                continue;
            }

            result.add(signatureSchemeInfo);
        }
        if (result.isEmpty())
        {
            return null;
        }
        result.trimToSize();
        return Collections.unmodifiableList(result);
    }

    ProvSSLContextSpi getContext()
    {
        return context;
    }

    TlsCrypto getCrypto()
    {
        return crypto;
    }

    ProvSSLSessionContext getClientSessionContext()
    {
        return clientSessionContext;
    }

    ProvSSLSessionContext getServerSessionContext()
    {
        return serverSessionContext;
    }

    Vector<SignatureAndHashAlgorithm> getSignatureAndHashAlgorithms(List<SignatureSchemeInfo> signatureSchemeInfos)
    {
        if (null == signatureSchemeInfos || signatureSchemeInfos.isEmpty())
        {
            return null;
        }

        int count = signatureSchemeInfos.size();
        Vector<SignatureAndHashAlgorithm> result = new Vector<SignatureAndHashAlgorithm>(count);
        for (SignatureSchemeInfo signatureSchemeInfo : signatureSchemeInfos)
        {
            if (null != signatureSchemeInfo)
            {
                SignatureAndHashAlgorithm sigAndHashAlg = signatureSchemeInfo.getSignatureAndHashAlgorithm();

                result.add(sigAndHashAlg);
            }
        }
        if (result.isEmpty())
        {
            return null;
        }
        result.trimToSize();
        return result;
    }

    List<SignatureSchemeInfo> getSignatureSchemes(Vector<SignatureAndHashAlgorithm> sigAndHashAlgs)
    {
        if (null == sigAndHashAlgs || sigAndHashAlgs.isEmpty())
        {
            return null;
        }

        int count = sigAndHashAlgs.size();
        ArrayList<SignatureSchemeInfo> result = new ArrayList<SignatureSchemeInfo>(count);
        for (int i = 0; i < count; ++i)
        {
            SignatureAndHashAlgorithm sigAndHashAlg = sigAndHashAlgs.elementAt(i);
            if (null != sigAndHashAlg)
            {
                int signatureScheme = SignatureSchemeInfo.getSignatureScheme(sigAndHashAlg);

                SignatureSchemeInfo signatureSchemeInfo = signatureSchemesMap.get(signatureScheme);
                if (null != signatureSchemeInfo)
                {
                    result.add(signatureSchemeInfo);
                }
            }
        }
        if (result.isEmpty())
        {
            return null;
        }
        result.trimToSize();
        return Collections.unmodifiableList(result);
    }

    X509ExtendedKeyManager getX509KeyManager()
    {
        return x509KeyManager;
    }

    BCX509ExtendedTrustManager getX509TrustManager()
    {
        return x509TrustManager;
    }
}
