package com.marichtech.artyy;

public class Encryption {
 /**
    // Create an initialization vector to use for encrypting and decrypting the same data.  Store this with the encrypted data.
    byte[] initializationVector = null;
    try {

        initializationVector = CRDCrypt.generateInitializationVector();

    } catch (
    CRDCryptException e) {

        Log.e(TAG, "onCreate: failed to create initialization vector", e);
    }

    /////Encrypton
    String myKey = "TheThirtyTwoByteKeyForEncryption";

    String dataToEncrypt = "This is the data to encrypt with AES256 encryption.";

    byte[] encrypted = null;
try {

        encrypted = CRDCrypt.aes256Encrypt(myKey, dataToEncrypt.getBytes("UTF-8"), initializationVector);

    } catch (UnsupportedEncodingException e) {

        Log.e(TAG, "onCreate: failed to encode data to encrypt", e);

    } catch (CRDCryptException e) {

        Log.e(TAG, "onCreate: failed to encrypt data", e);
    }


//Decrypt
byte[] decrypted = null;
try {

        decrypted = CRDCrypt.aes256Decrypt(myKey, encrypted, initializationVector);

    } catch (CRDCryptException e) {

        Log.e(TAG, "onCreate: failed to decrypt data", e);
    }


  */
}
