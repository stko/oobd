package org.spongycastle.asn1.x509;

import org.spongycastle.asn1.ASN1ObjectIdentifier;


/**
 * CertPolicyId, used in the CertificatePolicies and PolicyMappings
 * X509V3 Extensions.
 *
 * <pre>
 *     CertPolicyId ::= OBJECT IDENTIFIER
 * </pre>
 */
public class CertPolicyId extends ASN1ObjectIdentifier 
{
   public CertPolicyId (String id) 
   {
     super(id);
   }
}
