package org.spongycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Set;
import org.spongycastle.asn1.BEROctetStringGenerator;
import org.spongycastle.asn1.BERSet;
import org.spongycastle.asn1.DERSet;
import org.spongycastle.asn1.DERTaggedObject;
import org.spongycastle.asn1.cms.ContentInfo;
import org.spongycastle.asn1.cms.IssuerAndSerialNumber;
import org.spongycastle.asn1.x509.CertificateList;
import org.spongycastle.asn1.x509.TBSCertificateStructure;
import org.spongycastle.asn1.x509.X509CertificateStructure;
import org.spongycastle.cert.X509AttributeCertificateHolder;
import org.spongycastle.cert.X509CRLHolder;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.operator.DigestCalculator;
import org.spongycastle.util.Store;
import org.spongycastle.util.io.Streams;
import org.spongycastle.util.io.TeeInputStream;
import org.spongycastle.util.io.TeeOutputStream;

class CMSUtils
{
    static ContentInfo readContentInfo(
        byte[] input)
        throws CMSException
    {
        // enforce limit checking as from a byte array
        return readContentInfo(new ASN1InputStream(input));
    }

    static ContentInfo readContentInfo(
        InputStream input)
        throws CMSException
    {
        // enforce some limit checking
        return readContentInfo(new ASN1InputStream(input));
    } 

    static List getCertificatesFromStore(CertStore certStore)
        throws CertStoreException, CMSException
    {
        List certs = new ArrayList();

        try
        {
            for (Iterator it = certStore.getCertificates(null).iterator(); it.hasNext();)
            {
                X509Certificate c = (X509Certificate)it.next();

                certs.add(X509CertificateStructure.getInstance(
                                                       ASN1Primitive.fromByteArray(c.getEncoded())));
            }

            return certs;
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("error processing certs", e);
        }
        catch (IOException e)
        {
            throw new CMSException("error processing certs", e);
        }
        catch (CertificateEncodingException e)
        {
            throw new CMSException("error encoding certs", e);
        }
    }

    static List getCertificatesFromStore(Store certStore)
        throws CMSException
    {
        List certs = new ArrayList();

        try
        {
            for (Iterator it = certStore.getMatches(null).iterator(); it.hasNext();)
            {
                X509CertificateHolder c = (X509CertificateHolder)it.next();

                certs.add(c.toASN1Structure());
            }

            return certs;
        }
        catch (ClassCastException e)
        {
            throw new CMSException("error processing certs", e);
        }
    }

    static List getAttributeCertificatesFromStore(Store attrStore)
        throws CMSException
    {
        List certs = new ArrayList();

        try
        {
            for (Iterator it = attrStore.getMatches(null).iterator(); it.hasNext();)
            {
                X509AttributeCertificateHolder attrCert = (X509AttributeCertificateHolder)it.next();

                certs.add(new DERTaggedObject(false, 2, attrCert.toASN1Structure()));
            }

            return certs;
        }
        catch (ClassCastException e)
        {
            throw new CMSException("error processing certs", e);
        }
    }

    static List getCRLsFromStore(CertStore certStore)
        throws CertStoreException, CMSException
    {
        List crls = new ArrayList();

        try
        {
            for (Iterator it = certStore.getCRLs(null).iterator(); it.hasNext();)
            {
                X509CRL c = (X509CRL)it.next();

                crls.add(CertificateList.getInstance(ASN1Primitive.fromByteArray(c.getEncoded())));
            }

            return crls;
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("error processing crls", e);
        }
        catch (IOException e)
        {
            throw new CMSException("error processing crls", e);
        }
        catch (CRLException e)
        {
            throw new CMSException("error encoding crls", e);
        }
    }

    static List getCRLsFromStore(Store crlStore)
        throws CMSException
    {
        List certs = new ArrayList();

        try
        {
            for (Iterator it = crlStore.getMatches(null).iterator(); it.hasNext();)
            {
                X509CRLHolder c = (X509CRLHolder)it.next();

                certs.add(c.toASN1Structure());
            }

            return certs;
        }
        catch (ClassCastException e)
        {
            throw new CMSException("error processing certs", e);
        }
    }

    static ASN1Set createBerSetFromList(List derObjects)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        for (Iterator it = derObjects.iterator(); it.hasNext();)
        {
            v.add((ASN1Encodable)it.next());
        }

        return new BERSet(v);
    }

    static ASN1Set createDerSetFromList(List derObjects)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        for (Iterator it = derObjects.iterator(); it.hasNext();)
        {
            v.add((ASN1Encodable)it.next());
        }

        return new DERSet(v);
    }

    static OutputStream createBEROctetOutputStream(OutputStream s,
            int tagNo, boolean isExplicit, int bufferSize) throws IOException
    {
        BEROctetStringGenerator octGen = new BEROctetStringGenerator(s, tagNo, isExplicit);

        if (bufferSize != 0)
        {
            return octGen.getOctetOutputStream(new byte[bufferSize]);
        }

        return octGen.getOctetOutputStream();
    }

    static TBSCertificateStructure getTBSCertificateStructure(
        X509Certificate cert)
    {
        try
        {
            return TBSCertificateStructure.getInstance(
                ASN1Primitive.fromByteArray(cert.getTBSCertificate()));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(
                "can't extract TBS structure from this cert");
        }
    }

    static IssuerAndSerialNumber getIssuerAndSerialNumber(X509Certificate cert)
    {
        TBSCertificateStructure tbsCert = getTBSCertificateStructure(cert);
        return new IssuerAndSerialNumber(tbsCert.getIssuer(), tbsCert.getSerialNumber().getValue());
    }

    private static ContentInfo readContentInfo(
        ASN1InputStream in)
        throws CMSException
    {
        try
        {
            return ContentInfo.getInstance(in.readObject());
        }
        catch (IOException e)
        {
            throw new CMSException("IOException reading content.", e);
        }
        catch (ClassCastException e)
        {
            throw new CMSException("Malformed content.", e);
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("Malformed content.", e);
        }
    }
    
    public static byte[] streamToByteArray(
        InputStream in) 
        throws IOException
    {
        return Streams.readAll(in);
    }

    public static byte[] streamToByteArray(
        InputStream in,
        int         limit)
        throws IOException
    {
        return Streams.readAllLimited(in, limit);
    }

    public static Provider getProvider(String providerName)
        throws NoSuchProviderException
    {
        if (providerName != null)
        {
            Provider prov = Security.getProvider(providerName);

            if (prov != null)
            {
                return prov;
            }

            throw new NoSuchProviderException("provider " + providerName + " not found.");
        }

        return null; 
    }

    static InputStream attachDigestsToInputStream(Collection digests, InputStream s)
    {
        InputStream result = s;
        Iterator it = digests.iterator();
        while (it.hasNext())
        {
            DigestCalculator digest = (DigestCalculator)it.next();
            result = new TeeInputStream(result, digest.getOutputStream());
        }
        return result;
    }

    static OutputStream attachSignersToOutputStream(Collection signers, OutputStream s)
    {
        OutputStream result = s;
        Iterator it = signers.iterator();
        while (it.hasNext())
        {
            SignerInfoGenerator signerGen = (SignerInfoGenerator)it.next();
            result = getSafeTeeOutputStream(result, signerGen.getCalculatingOutputStream());
        }
        return result;
    }

    static OutputStream getSafeOutputStream(OutputStream s)
    {
        return s == null ? new NullOutputStream() : s;
    }

    static OutputStream getSafeTeeOutputStream(OutputStream s1,
            OutputStream s2)
    {
        return s1 == null ? getSafeOutputStream(s2)
                : s2 == null ? getSafeOutputStream(s1) : new TeeOutputStream(
                        s1, s2);
    }
}
