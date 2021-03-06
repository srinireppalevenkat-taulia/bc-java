package org.bouncycastle.jsse.provider;

import java.util.List;

import org.bouncycastle.jsse.BCSNIServerName;
import org.bouncycastle.tls.ProtocolVersion;
import org.bouncycastle.tls.SecurityParameters;

class ProvSSLSessionHandshake
    extends ProvSSLSessionBase
{
    protected final SecurityParameters securityParameters;
    protected final JsseSecurityParameters jsseSecurityParameters;

    ProvSSLSessionHandshake(ProvSSLSessionContext sslSessionContext, String peerHost, int peerPort,
        SecurityParameters securityParameters, JsseSecurityParameters jsseSecurityParameters)
    {
        super(sslSessionContext, peerHost, peerPort);

        this.securityParameters = securityParameters;
        this.jsseSecurityParameters = jsseSecurityParameters;
    }

    String getApplicationProtocol()
    {
        return JsseUtils.getApplicationProtocol(securityParameters);
    }

    @Override
    protected int getCipherSuiteTLS()
    {
        return securityParameters.getCipherSuite();
    }

    @Override
    protected byte[] getIDArray()
    {
        return securityParameters.getSessionID();
    }

    @Override
    protected JsseSecurityParameters getJsseSecurityParameters()
    {
        return jsseSecurityParameters;
    }

    @Override
    protected JsseSessionParameters getJsseSessionParameters()
    {
        return null;
    }

    @Override
    protected org.bouncycastle.tls.Certificate getLocalCertificateTLS()
    {
        return securityParameters.getLocalCertificate();
    }

    @Override
    public String[] getLocalSupportedSignatureAlgorithms()
    {
        return SignatureSchemeInfo.getJcaSignatureAlgorithms(jsseSecurityParameters.localSigSchemesCert);
    }

    @Override
    protected org.bouncycastle.tls.Certificate getPeerCertificateTLS()
    {
        return securityParameters.getPeerCertificate();
    }

    @Override
    public String[] getPeerSupportedSignatureAlgorithms()
    {
        return SignatureSchemeInfo.getJcaSignatureAlgorithms(jsseSecurityParameters.peerSigSchemesCert);
    }

    @Override
    protected ProtocolVersion getProtocolTLS()
    {
        return securityParameters.getNegotiatedVersion();
    }

    @Override
    public List<BCSNIServerName> getRequestedServerNames()
    {
        return JsseUtils.convertSNIServerNames(securityParameters.getClientServerNames());
    }
}
