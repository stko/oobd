package org.spongycastle.pkcs;


import java.io.IOException;
import java.io.OutputStream;

import org.spongycastle.asn1.pkcs.MacData;
import org.spongycastle.asn1.pkcs.PKCS12PBEParams;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.DigestInfo;
import org.spongycastle.operator.MacCalculator;

class MacDataGenerator
{
    private PKCS12MacCalculatorBuilder builder;

    MacDataGenerator(PKCS12MacCalculatorBuilder builder)
    {
        this.builder = builder;
    }

    public MacData build(char[] password, byte[] data)
        throws PKCSException
    {
        MacCalculator     macCalculator = builder.build(password);

        AlgorithmIdentifier algId = macCalculator.getAlgorithmIdentifier();

        OutputStream out = macCalculator.getOutputStream();

        try
        {
            out.write(data);

            out.close();
        }
        catch (IOException e)
        {
            throw new PKCSException("unable to process data: " + e.getMessage(), e);
        }

        DigestInfo dInfo = new DigestInfo(builder.getDigestAlgorithmIdentifier(), macCalculator.getMac());
        PKCS12PBEParams params = PKCS12PBEParams.getInstance(algId.getParameters());

        return new MacData(dInfo, params.getIV(), params.getIterations().intValue());
    }
}
