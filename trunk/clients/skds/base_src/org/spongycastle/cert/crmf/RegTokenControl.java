package org.spongycastle.cert.crmf;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.DERUTF8String;
import org.spongycastle.asn1.crmf.CRMFObjectIdentifiers;

/**
 * Carrier for a registration token control.
 */
public class RegTokenControl
    implements Control
{
    private static final ASN1ObjectIdentifier type = CRMFObjectIdentifiers.id_regCtrl_regToken;

    private final DERUTF8String token;

    /**
     * Basic constructor - build from a UTF-8 string representing the token.
     *
     * @param token UTF-8 string representing the token.
     */
    public RegTokenControl(DERUTF8String token)
    {
        this.token = token;
    }

    /**
     * Basic constructor - build from a string representing the token.
     *
     * @param token string representing the token.
     */
    public RegTokenControl(String token)
    {
        this.token = new DERUTF8String(token);
    }

    /**
     * Return the type of this control.
     *
     * @return CRMFObjectIdentifiers.id_regCtrl_regToken
     */
    public ASN1ObjectIdentifier getType()
    {
        return type;
    }

    /**
     * Return the token associated with this control (a UTF8String).
     *
     * @return a UTF8String.
     */
    public ASN1Encodable getValue()
    {
        return token;
    }
}
