package org.spongycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.NoSuchProviderException;
import java.security.Provider;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1Set;
import org.spongycastle.asn1.cms.AttributeTable;
import org.spongycastle.asn1.cms.ContentInfo;
import org.spongycastle.asn1.cms.EncryptedContentInfo;
import org.spongycastle.asn1.cms.EnvelopedData;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.cms.jcajce.JceAlgorithmIdentifierConverter;

/**
 * containing class for an CMS Enveloped Data object
 * <p>
 * Example of use - assuming the first recipient matches the private key we have.
 * <pre>
 *      CMSEnvelopedData     ed = new CMSEnvelopedData(inputStream);
 *
 *      RecipientInformationStore  recipients = ed.getRecipientInfos();
 *
 *      Collection  c = recipients.getRecipients();
 *      Iterator    it = c.iterator();
 *
 *      if (it.hasNext())
 *      {
 *          RecipientInformation   recipient = (RecipientInformation)it.next();
 *
 *          byte[] recData = recipient.getContent(new JceKeyTransEnvelopedRecipient(privateKey).setProvider("SC"));
 *
 *          processData(recData);
 *      }
 *  </pre>
 */
public class CMSEnvelopedData
{
    RecipientInformationStore   recipientInfoStore;
    ContentInfo                 contentInfo;

    private AlgorithmIdentifier    encAlg;
    private ASN1Set                unprotectedAttributes;
    private OriginatorInformation  originatorInfo;

    public CMSEnvelopedData(
        byte[]    envelopedData)
        throws CMSException
    {
        this(CMSUtils.readContentInfo(envelopedData));
    }

    public CMSEnvelopedData(
        InputStream    envelopedData)
        throws CMSException
    {
        this(CMSUtils.readContentInfo(envelopedData));
    }

    /**
     * Construct a CMSEnvelopedData object from a content info object.
     *
     * @param contentInfo the contentInfo containing the CMS EnvelopedData object.
     * @throws CMSException in the case where malformed content is encountered.
     */
    public CMSEnvelopedData(
        ContentInfo contentInfo)
        throws CMSException
    {
        this.contentInfo = contentInfo;

        try
        {
            EnvelopedData  envData = EnvelopedData.getInstance(contentInfo.getContent());

            if (envData.getOriginatorInfo() != null)
            {
                originatorInfo = new OriginatorInformation(envData.getOriginatorInfo());
            }

            //
            // read the recipients
            //
            ASN1Set recipientInfos = envData.getRecipientInfos();

            //
            // read the encrypted content info
            //
            EncryptedContentInfo encInfo = envData.getEncryptedContentInfo();
            this.encAlg = encInfo.getContentEncryptionAlgorithm();
            CMSReadable readable = new CMSProcessableByteArray(encInfo.getEncryptedContent().getOctets());
            CMSSecureReadable secureReadable = new CMSEnvelopedHelper.CMSEnvelopedSecureReadable(
                this.encAlg, readable);

            //
            // build the RecipientInformationStore
            //
            this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(
                recipientInfos, this.encAlg, secureReadable);

            this.unprotectedAttributes = envData.getUnprotectedAttrs();
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

    private byte[] encodeObj(
        ASN1Encodable obj)
        throws IOException
    {
        if (obj != null)
        {
            return obj.toASN1Primitive().getEncoded();
        }

        return null;
    }

    /**
     * Return the originator information associated with this message if present.
     *
     * @return OriginatorInformation, null if not present.
     */
    public OriginatorInformation getOriginatorInfo()
    {
        return originatorInfo;
    }

    /**
     * Return the content encryption algorithm details for the data in this object.
     *
     * @return AlgorithmIdentifier representing the content encryption algorithm.
     */
    public AlgorithmIdentifier getContentEncryptionAlgorithm()
    {
        return encAlg;
    }

    /**
     * return the object identifier for the content encryption algorithm.
     */
    public String getEncryptionAlgOID()
    {
        return encAlg.getAlgorithm().getId();
    }

    /**
     * return the ASN.1 encoded encryption algorithm parameters, or null if
     * there aren't any.
     */
    public byte[] getEncryptionAlgParams()
    {
        try
        {
            return encodeObj(encAlg.getParameters());
        }
        catch (Exception e)
        {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    /**
     * Return an AlgorithmParameters object giving the encryption parameters
     * used to encrypt the message content.
     *
     * @param provider the provider to generate the parameters for.
     * @return the parameters object, null if there is not one.
     * @throws CMSException if the algorithm cannot be found, or the parameters can't be parsed.
     * @throws NoSuchProviderException if the provider cannot be found.
     * @deprecated use getContentEncryptionAlgorithm and JceAlgorithmIdentifierConverter().
     */
    public AlgorithmParameters getEncryptionAlgorithmParameters(
        String  provider)
    throws CMSException, NoSuchProviderException
    {
        return new JceAlgorithmIdentifierConverter().setProvider(provider).getAlgorithmParameters(encAlg);
    }

    /**
     * Return an AlgorithmParameters object giving the encryption parameters
     * used to encrypt the message content.
     *
     * @param provider the provider to generate the parameters for.
     * @return the parameters object, null if there is not one.
     * @throws CMSException if the algorithm cannot be found, or the parameters can't be parsed.
     * @deprecated use getContentEncryptionAlgorithm and JceAlgorithmIdentifierConverter().
     */
    public AlgorithmParameters getEncryptionAlgorithmParameters(
        Provider provider)
    throws CMSException
    {
        return new JceAlgorithmIdentifierConverter().setProvider(provider).getAlgorithmParameters(encAlg);
    }

    /**
     * return a store of the intended recipients for this message
     */
    public RecipientInformationStore getRecipientInfos()
    {
        return recipientInfoStore;
    }

    /**
     * return the ContentInfo
     * @deprecated use toASN1Structure()
     */
    public ContentInfo getContentInfo()
    {
        return contentInfo;
    }

    /**
     * return the ContentInfo
     */
    public ContentInfo toASN1Structure()
    {
        return contentInfo;
    }

    /**
     * return a table of the unprotected attributes indexed by
     * the OID of the attribute.
     */
    public AttributeTable getUnprotectedAttributes()
    {
        if (unprotectedAttributes == null)
        {
            return null;
        }

        return new AttributeTable(unprotectedAttributes);
    }

    /**
     * return the ASN.1 encoded representation of this object.
     */
    public byte[] getEncoded()
        throws IOException
    {
        return contentInfo.getEncoded();
    }
}
