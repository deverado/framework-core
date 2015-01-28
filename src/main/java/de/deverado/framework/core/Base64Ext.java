package de.deverado.framework.core;/*
 * Copyright Georg Koester 2012. All rights reserved.
 */

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guava has BaseEncoding.base64 which also provides streaming encoding via ByteSource etc.
 */
@ParametersAreNonnullByDefault
public class Base64Ext {

    public static String encodeBase64WithChunking(byte[] toEnc) {
        return new Base64().encodeAsString(toEnc);
    }

    public static byte[] decodeBase64(String toDec) {
        return new Base64().decode(toDec);
    }

    public static byte[] decodeBase64(byte[] toDec) {
        return new Base64().decode(toDec);
    }

    /**
     * Untested, just a note.
     */
    public static byte[] decodeBase64(byte[] toDec, int offset, int len) {
        try {
            return BaseEncoding.base64().decodingSource(
                    ByteSource.wrap(toDec).slice(offset, len).asCharSource(Charsets.UTF_8)).read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeBase64NoChunksUrlSafe(byte[] toEnc) {
        // not the fastest way - could use caching. But uses
        // big buffer internally and has lots of copying making
        // it certainly a target for replacement if speed is needed
        return new Base64(0 // no chunking, see BaseN
                , null, true).encodeAsString(toEnc);
    }

    public static byte[] decodeBase64UrlSafe(String toDec) {
        return new Base64(0 // no chunking, see BaseN
                , null, true).decode(toDec);
    }

    public static final Pattern BASE64_URLSAFE_PATTERN = Pattern
            .compile("[a-zA-Z0-9_\\-]*");

    public static boolean isBase64UrlSafeCompliantString(@Nullable String s) {
        Matcher matcher = BASE64_URLSAFE_PATTERN.matcher(s);
        return matcher != null && matcher.matches();
    }
}
