package de.deverado.framework.core;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@ParametersAreNonnullByDefault
public class Strings2 {

    public static boolean equalsIgnoreCaseEnglishLowercasing(@Nullable String a, @Nullable  String b) {
        if (a == b) {
            return true;
        }
        if (a != null) {
            return Objects.equal(toLowerCaseEnglish(a), toLowerCaseEnglish(b));
        }
        return false;
    }

    public static String toLowerCaseEnglish(@Nullable String a) {
        return a == null ? null : a.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Use {@link StringUtils#isBlank(CharSequence)} instead!
     */
    public static boolean isNullOrWhitespace(@Nullable CharSequence s) {
        return StringUtils.isBlank(s);
    }

    public static String noSlashAtStart(@Nullable String in) {
        return StringUtils.removeStart(in, "/");
    }

    public static String slashAtStart(@Nullable String in) {
        return StringUtils.prependIfMissing(in, "/");
    }

    public static String maxLength(@Nullable String in, int maxLen) {
        if (in == null) {
            return null;
        }
        if (in.length() > maxLen) {
            return in.substring(0, maxLen);
        }
        return in;
    }

    /**
     * Strings will be length = maxLen + 3.
     *
     */
    public static String maxLengthAddDotsAfterCut(@Nullable String in, int maxLen) {
        if (in == null) {
            return null;
        }
        if (in.length() > maxLen) {
            return "" + in.substring(0, maxLen) + "...";
        }
        return in;
    }

    public static byte[] toUTF8ByteArray(@Nonnull CharSequence val) {
        ByteBuffer encoded = StandardCharsets.UTF_8
                .encode(CharBuffer.wrap(val));
        if (encoded.remaining() == encoded.array().length) {
            return encoded.array();
        }
        byte[] retval = new byte[encoded.remaining()];
        encoded.get(retval);
        return retval;
    }
}
