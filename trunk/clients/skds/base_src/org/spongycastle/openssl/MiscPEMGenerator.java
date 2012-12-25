package org.spongycastle.openssl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERInteger;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.cms.ContentInfo;
import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.asn1.x509.DSAParameter;
import org.spongycastle.jce.PKCS10CertificationRequest;
import org.spongycastle.util.Strings;
import org.spongycastle.util.io.pem.PemGenerationException;
import org.spongycastle.util.io.pem.PemHeader;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemObjectGenerator;
import org.spongycastle.x509.X509AttributeCertificate;
import org.spongycastle.x509.X509V2AttributeCertificate;

/**
 * PEM generator for the original set of PEM objects used in Open SSL.
 */
public class MiscPEMGenerator
    implements PemObjectGenerator
{
    private static final byte[] hexEncodingTable =
    {
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', (byte)'6', (byte)'7',
        (byte)'8', (byte)'9', (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F'
    };

    private Object obj;
    private String algorithm;
    private char[] password;
    private SecureRandom random;
    private Provider provider;

    public MiscPEMGenerator(Object o)
    {
        this.obj = o;
    }

    public MiscPEMGenerator(
        Object       obj,
        String       algorithm,
        char[]       password,
        SecureRandom random,
        Provider     provider)
    {
        this.obj = obj;
        this.algorithm = algorithm;
        this.password = password;
        this.random = random;
        this.provider = provider;
    }

    public MiscPEMGenerator(
        Object       obj,
        String       algorithm,
        char[]       password,
        SecureRandom random,
        String       provider)
        throws NoSuchProviderException
    {
        this.obj = obj;
        this.algorithm = algorithm;
        this.password = password;
        this.random = random;

        if (provider != null)
        {
            this.provider = Security.getProvider(provider);
            if (this.provider == null)
            {
                throw new NoSuchProviderException("cannot find provider: " + provider);
            }
        }
    }

    private PemObject createPemObject(Object o)
        throws IOException
    {
        String  type;
        byte[]  encoding;

        if (o instanceof PemObject)
        {
            return (PemObject)o;
        }
        if (o instanceof PemObjectGenerator)
        {
            return ((PemObjectGenerator)o).generate();
        }
        if (o instanceof X509Certificate)
        {
            type = "CERTIFICATE";
            try
            {
                encoding = ((X509Certificate)o).getEncoded();
            }
            catch (CertificateEncodingException e)
            {
                throw new PemGenerationException("Cannot encode object: " + e.toString());
            }
        }
        else if (o instanceof X509CRL)
        {
            type = "X509 CRL";
            try
            {
                encoding = ((X509CRL)o).getEncoded();
            }
            catch (CRLException e)
            {
                throw new PemGenerationException("Cannot encode object: " + e.toString());
            }
        }
        else if (o instanceof KeyPair)
        {
            return createPemObject(((KeyPair)o).getPrivate());
        }
        else if (o instanceof PrivateKey)
        {
            PrivateKeyInfo info = new PrivateKeyInfo(
                (ASN1Sequence) ASN1Primitive.fromByteArray(((Key)o).getEncoded()));

            if (o instanceof RSAPrivateKey)
            {
                type = "RSA PRIVATE KEY";

                encoding = info.parsePrivateKey().toASN1Primitive().getEncoded();
            }
            else if (o instanceof DSAPrivateKey)
            {
                type = "DSA PRIVATE KEY";

                DSAParameter p = DSAParameter.getInstance(info.getPrivateKeyAlgorithm().getParameters());
                ASN1EncodableVector v = new ASN1EncodableVector();

                v.add(new DERInteger(0));
                v.add(new DERInteger(p.getP()));
                v.add(new DERInteger(p.getQ()));
                v.add(new DERInteger(p.getG()));

                BigInteger x = ((DSAPrivateKey)o).getX();
                BigInteger y = p.getG().modPow(x, p.getP());

                v.add(new DERInteger(y));
                v.add(new DERInteger(x));

                encoding = new DERSequence(v).getEncoded();
            }
            else if (((PrivateKey)o).getAlgorithm().equals("ECDSA"))
            {
                type = "EC PRIVATE KEY";

                encoding = info.parsePrivateKey().toASN1Primitive().getEncoded();
            }
            else
            {
                throw new IOException("Cannot identify private key");
            }
        }
        else if (o instanceof PublicKey)
        {
            type = "PUBLIC KEY";

            encoding = ((PublicKey)o).getEncoded();
        }
        else if (o instanceof X509AttributeCertificate)
        {
            type = "ATTRIBUTE CERTIFICATE";
            encoding = ((X509V2AttributeCertificate)o).getEncoded();
        }
        else if (o instanceof PKCS10CertificationRequest)
        {
            type = "CERTIFICATE REQUEST";
            encoding = ((PKCS10CertificationRequest)o).getEncoded();
        }
        else if (o instanceof ContentInfo)
        {
            type = "PKCS7";
            encoding = ((ContentInfo)o).getEncoded();
        }
        else
        {
            throw new PemGenerationException("unknown object passed - can't encode.");
        }

        return new PemObject(type, encoding);
    }

    private String getHexEncoded(byte[] bytes)
        throws IOException
    {
        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i != bytes.length; i++)
        {
            int    v = bytes[i] & 0xff;

            chars[2 * i] = (char)(hexEncodingTable[(v >>> 4)]);
            chars[2 * i + 1]  = (char)(hexEncodingTable[v & 0xf]);
        }

        return new String(chars);
    }

    private PemObject createPemObject(
        Object       obj,
        String       algorithm,
        char[]       password,
        SecureRandom random)
        throws IOException
    {
        if (obj instanceof KeyPair)
        {
            return createPemObject(((KeyPair)obj).getPrivate(), algorithm, password, random);
        }

        String type = null;
        byte[] keyData = null;

        if (obj instanceof RSAPrivateCrtKey)
        {
            type = "RSA PRIVATE KEY";

            RSAPrivateCrtKey k = (RSAPrivateCrtKey)obj;

            org.spongycastle.asn1.pkcs.RSAPrivateKey keyStruct = new org.spongycastle.asn1.pkcs.RSAPrivateKey(
                k.getModulus(),
                k.getPublicExponent(),
                k.getPrivateExponent(),
                k.getPrimeP(),
                k.getPrimeQ(),
                k.getPrimeExponentP(),
                k.getPrimeExponentQ(),
                k.getCrtCoefficient());

            // convert to bytearray
            keyData = keyStruct.getEncoded();
        }
        else if (obj instanceof DSAPrivateKey)
        {
            type = "DSA PRIVATE KEY";

            DSAPrivateKey       k = (DSAPrivateKey)obj;
            DSAParams p = k.getParams();
            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(new DERInteger(0));
            v.add(new DERInteger(p.getP()));
            v.add(new DERInteger(p.getQ()));
            v.add(new DERInteger(p.getG()));

            BigInteger x = k.getX();
            BigInteger y = p.getG().modPow(x, p.getP());

            v.add(new DERInteger(y));
            v.add(new DERInteger(x));

            keyData = new DERSequence(v).getEncoded();
        }
        else if (obj instanceof PrivateKey && "ECDSA".equals(((PrivateKey)obj).getAlgorithm()))
        {
            type = "EC PRIVATE KEY";

            PrivateKeyInfo      privInfo = PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(((PrivateKey)obj).getEncoded()));

            keyData = privInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        }

        if (type == null || keyData == null)
        {
            // TODO Support other types?
            throw new IllegalArgumentException("Object type not supported: " + obj.getClass().getName());
        }

        String dekAlgName = Strings.toUpperCase(algorithm);

        // Note: For backward compatibility
        if (dekAlgName.equals("DESEDE"))
        {
            dekAlgName = "DES-EDE3-CBC";
        }

        int ivLength = dekAlgName.startsWith("AES-") ? 16 : 8;

        byte[] iv = new byte[ivLength];
        random.nextBytes(iv);

        byte[] encData = PEMUtilities.crypt(true, provider, keyData, password, dekAlgName, iv);

        List headers = new ArrayList(2);

        headers.add(new PemHeader("Proc-Type", "4,ENCRYPTED"));
        headers.add(new PemHeader("DEK-Info", dekAlgName + "," + getHexEncoded(iv)));

        return new PemObject(type, headers, encData);
    }

    public PemObject generate()
        throws PemGenerationException
    {
        try
        {
            if (algorithm != null)
            {
                return createPemObject(obj, algorithm, password, random);
            }

            return createPemObject(obj);
        }
        catch (IOException e)
        {
            throw new PemGenerationException("encoding exception: " + e.getMessage(), e);
        }
    }
}
