package org.spongycastle.pkcs.bc;

import org.spongycastle.asn1.DERNull;
import org.spongycastle.asn1.oiw.OIWObjectIdentifiers;
import org.spongycastle.asn1.pkcs.PKCS12PBEParams;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.crypto.ExtendedDigest;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.operator.MacCalculator;
import org.spongycastle.pkcs.PKCS12MacCalculatorBuilder;
import org.spongycastle.pkcs.PKCS12MacCalculatorBuilderProvider;

public class BcPKCS12MacCalculatorBuilderProviderBuilder
    implements PKCS12MacCalculatorBuilderProvider
{
    private ExtendedDigest digest;
    private AlgorithmIdentifier digestAlgorithmIdentifier;

    public BcPKCS12MacCalculatorBuilderProviderBuilder()
    {
        this(new SHA1Digest(), new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE));
    }

    public BcPKCS12MacCalculatorBuilderProviderBuilder(ExtendedDigest digest, AlgorithmIdentifier algorithmIdentifier)
    {
        this.digest = digest;
        this.digestAlgorithmIdentifier = algorithmIdentifier;
    }

    public PKCS12MacCalculatorBuilder get(final AlgorithmIdentifier algorithmIdentifier)
    {
        return new PKCS12MacCalculatorBuilder()
        {
            public MacCalculator build(final char[] password)
            {
                PKCS12PBEParams pbeParams = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());

                return PKCS12PBEUtils.createMacCalculator(digestAlgorithmIdentifier.getAlgorithm(), digest, pbeParams, password);
            }

            public AlgorithmIdentifier getDigestAlgorithmIdentifier()
            {
                return digestAlgorithmIdentifier;
            }
        };
    }
}
