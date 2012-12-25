package org.spongycastle.jce.interfaces;

import java.util.Enumeration;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.DERObjectIdentifier;

/**
 * allow us to set attributes on objects that can go into a PKCS12 store.
 */
public interface PKCS12BagAttributeCarrier
{
    void setBagAttribute(
        ASN1ObjectIdentifier oid,
        ASN1Encodable attribute);

    ASN1Encodable getBagAttribute(
        DERObjectIdentifier oid);

    Enumeration getBagAttributeKeys();
}
