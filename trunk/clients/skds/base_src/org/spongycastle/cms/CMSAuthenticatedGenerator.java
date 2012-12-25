package org.spongycastle.cms;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.spongycastle.asn1.DERObjectIdentifier;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;

public class CMSAuthenticatedGenerator
    extends CMSEnvelopedGenerator
{
    protected CMSAttributeTableGenerator authGen;
    protected CMSAttributeTableGenerator unauthGen;

    /**
     * base constructor
     */
    public CMSAuthenticatedGenerator()
    {
    }

    /**
     * constructor allowing specific source of randomness
     *
     * @param rand instance of SecureRandom to use
     */
    public CMSAuthenticatedGenerator(
        SecureRandom rand)
    {
        super(rand);
    }

    public void setAuthenticatedAttributeGenerator(CMSAttributeTableGenerator authGen)
    {
        this.authGen = authGen;
    }

    public void setUnauthenticatedAttributeGenerator(CMSAttributeTableGenerator unauthGen)
    {
        this.unauthGen = unauthGen;
    }

    protected Map getBaseParameters(DERObjectIdentifier contentType, AlgorithmIdentifier digAlgId, byte[] hash)
    {
        Map param = new HashMap();
        param.put(CMSAttributeTableGenerator.CONTENT_TYPE, contentType);
        param.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, digAlgId);
        param.put(CMSAttributeTableGenerator.DIGEST,  hash.clone());
        return param;
    }
}
