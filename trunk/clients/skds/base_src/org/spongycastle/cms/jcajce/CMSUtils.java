package org.spongycastle.cms.jcajce;

import java.security.Provider;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.cms.IssuerAndSerialNumber;
import org.spongycastle.asn1.x509.Certificate;
import org.spongycastle.asn1.x509.TBSCertificateStructure;
import org.spongycastle.asn1.x509.X509Extension;

class CMSUtils
{
    static TBSCertificateStructure getTBSCertificateStructure(
        X509Certificate cert)
        throws CertificateEncodingException
    {
            return TBSCertificateStructure.getInstance(cert.getTBSCertificate());
    }

    static IssuerAndSerialNumber getIssuerAndSerialNumber(X509Certificate cert)
        throws CertificateEncodingException
    {
        Certificate certStruct = Certificate.getInstance(cert.getEncoded());

        return new IssuerAndSerialNumber(certStruct.getIssuer(), cert.getSerialNumber());
    }


    static byte[] getSubjectKeyId(X509Certificate cert)
    {
        byte[] ext = cert.getExtensionValue(X509Extension.subjectKeyIdentifier.getId());

        if (ext != null)
        {
            return ASN1OctetString.getInstance(ASN1OctetString.getInstance(ext).getOctets()).getOctets();
        }
        else
        {
            return null;
        }
    }

    static EnvelopedDataHelper createContentHelper(Provider provider)
    {
        if (provider != null)
        {
            return new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        }
        else
        {
            return new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
        }
    }

    static EnvelopedDataHelper createContentHelper(String providerName)
    {
        if (providerName != null)
        {
            return new EnvelopedDataHelper(new NamedJcaJceExtHelper(providerName));
        }
        else
        {
            return new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
        }
    }

}