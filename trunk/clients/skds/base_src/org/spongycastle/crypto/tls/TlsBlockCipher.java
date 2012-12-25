package org.spongycastle.crypto.tls;

import java.io.IOException;
import java.security.SecureRandom;

import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.util.Arrays;

/**
 * A generic TLS 1.0 / SSLv3 block cipher.
 * This can be used for AES or 3DES for example.
 */
public class TlsBlockCipher implements TlsCipher
{
    protected TlsClientContext context;

    protected BlockCipher encryptCipher;
    protected BlockCipher decryptCipher;

    protected TlsMac writeMac;
    protected TlsMac readMac;

	public TlsMac getWriteMac()
	{
		return writeMac;
	}

	public TlsMac getReadMac()
	{
		return readMac;
	}

    public TlsBlockCipher(TlsClientContext context, BlockCipher encryptCipher,
        BlockCipher decryptCipher, Digest writeDigest, Digest readDigest, int cipherKeySize)
    {
        this.context = context;
        this.encryptCipher = encryptCipher;
        this.decryptCipher = decryptCipher;

        int key_block_size = (2 * cipherKeySize) + writeDigest.getDigestSize()
            + readDigest.getDigestSize() + encryptCipher.getBlockSize()
            + decryptCipher.getBlockSize();

        byte[] key_block = TlsUtils.calculateKeyBlock(context, key_block_size);

        int offset = 0;

        // Init MACs
        writeMac = new TlsMac(context, writeDigest, key_block, offset, writeDigest.getDigestSize());
        offset += writeDigest.getDigestSize();
        readMac = new TlsMac(context, readDigest, key_block, offset, readDigest.getDigestSize());
        offset += readDigest.getDigestSize();

        // Init Ciphers
        this.initCipher(true, encryptCipher, key_block, cipherKeySize, offset, offset
            + (cipherKeySize * 2));
        offset += cipherKeySize;
        this.initCipher(false, decryptCipher, key_block, cipherKeySize, offset, offset
            + cipherKeySize + encryptCipher.getBlockSize());

    }

    protected void initCipher(boolean forEncryption, BlockCipher cipher, byte[] key_block,
        int key_size, int key_offset, int iv_offset)
    {
        KeyParameter key_parameter = new KeyParameter(key_block, key_offset, key_size);
        ParametersWithIV parameters_with_iv = new ParametersWithIV(key_parameter, key_block,
            iv_offset, cipher.getBlockSize());
        cipher.init(forEncryption, parameters_with_iv);
    }

    public byte[] encodePlaintext(short type, byte[] plaintext, int offset, int len)
    {
        int blocksize = encryptCipher.getBlockSize();
        int minPaddingSize = blocksize - ((len + writeMac.getSize() + 1) % blocksize);
        int paddingSize = minPaddingSize;

        boolean isTls = context.getServerVersion().getFullVersion() >= ProtocolVersion.TLSv10.getFullVersion();

        if (isTls)
        {
            // Add a random number of extra blocks worth of padding
            int maxExtraPadBlocks = (255 - minPaddingSize) / blocksize;
            int actualExtraPadBlocks = chooseExtraPadBlocks(context.getSecureRandom(), maxExtraPadBlocks);
            paddingSize += (actualExtraPadBlocks * blocksize);
        }

        int totalsize = len + writeMac.getSize() + paddingSize + 1;
        byte[] outbuf = new byte[totalsize];
        System.arraycopy(plaintext, offset, outbuf, 0, len);
        byte[] mac = writeMac.calculateMac(type, plaintext, offset, len);
        System.arraycopy(mac, 0, outbuf, len, mac.length);
        int paddoffset = len + mac.length;
        for (int i = 0; i <= paddingSize; i++)
        {
            outbuf[i + paddoffset] = (byte)paddingSize;
        }
        for (int i = 0; i < totalsize; i += blocksize)
        {
            encryptCipher.processBlock(outbuf, i, outbuf, i);
        }
        return outbuf;
    }

    public byte[] decodeCiphertext(short type, byte[] ciphertext, int offset, int len)
        throws IOException
    {
        // TODO TLS 1.1 (RFC 4346) introduces an explicit IV

        int minLength = readMac.getSize() + 1;
        int blocksize = decryptCipher.getBlockSize();
        boolean decrypterror = false;

        /*
         * ciphertext must be at least (macsize + 1) bytes long
         */
        if (len < minLength)
        {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }

        /*
         * ciphertext must be a multiple of blocksize
         */
        if (len % blocksize != 0)
        {
            throw new TlsFatalAlert(AlertDescription.decryption_failed);
        }

        /*
         * Decrypt all the ciphertext using the blockcipher
         */
        for (int i = 0; i < len; i += blocksize)
        {
            decryptCipher.processBlock(ciphertext, i + offset, ciphertext, i + offset);
        }

        /*
         * Check if padding is correct
         */
        int lastByteOffset = offset + len - 1;

        byte paddingsizebyte = ciphertext[lastByteOffset];

        // Note: interpret as unsigned byte
        int paddingsize = paddingsizebyte & 0xff;

        boolean isTls = context.getServerVersion().getFullVersion() >= ProtocolVersion.TLSv10.getFullVersion();

        int maxPaddingSize = len - minLength;
        if (!isTls)
        {
            maxPaddingSize = Math.min(maxPaddingSize, blocksize);
        }

        if (paddingsize > maxPaddingSize)
        {
            decrypterror = true;
            paddingsize = 0;
        }
        else if (isTls)
        {
            /*
             * Now, check all the padding-bytes (constant-time comparison).
             * (Skipped for SSLv3, where the padding may be anything)
             */
            byte diff = 0;
            for (int i = lastByteOffset - paddingsize; i < lastByteOffset; ++i)
            {
                diff |= (ciphertext[i] ^ paddingsizebyte);
            }
            if (diff != 0)
            {
                /* Wrong padding */
                decrypterror = true;
                paddingsize = 0;
            }
        }

        /*
         * We now don't care if padding verification has failed or not, we will calculate
         * the mac to give an attacker no kind of timing profile he can use to find out if
         * mac verification failed or padding verification failed.
         */
        int plaintextlength = len - minLength - paddingsize;
        byte[] calculatedMac = readMac.calculateMac(type, ciphertext, offset, plaintextlength);

        /*
         * Check all bytes in the mac (constant-time comparison).
         */
        byte[] decryptedMac = new byte[calculatedMac.length];
        System.arraycopy(ciphertext, offset + plaintextlength, decryptedMac, 0,
            calculatedMac.length);

        if (!Arrays.constantTimeAreEqual(calculatedMac, decryptedMac))
        {
            decrypterror = true;
        }

        /*
         * Now, it is safe to fail.
         */
        if (decrypterror)
        {
            throw new TlsFatalAlert(AlertDescription.bad_record_mac);
        }

        byte[] plaintext = new byte[plaintextlength];
        System.arraycopy(ciphertext, offset, plaintext, 0, plaintextlength);
        return plaintext;
    }

    protected int chooseExtraPadBlocks(SecureRandom r, int max)
    {
//        return r.nextInt(max + 1);

        int x = r.nextInt();
        int n = lowestBitSet(x);
        return Math.min(n, max);
    }

    protected int lowestBitSet(int x)
    {
        if (x == 0)
        {
            return 32;
        }

        int n = 0;
        while ((x & 1) == 0)
        {
            ++n;
            x >>= 1;
        }
        return n;
    }
}
