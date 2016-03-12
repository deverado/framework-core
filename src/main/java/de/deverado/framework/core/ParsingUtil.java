package de.deverado.framework.core;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This makes reading config parsing logic so much easier.
 */
@ParametersAreNonnullByDefault
public class ParsingUtil {

    private static final Logger log = LoggerFactory.getLogger(ParsingUtil.class);

    /**
     * Accepts true,>0 as true values.
     *
     */
    public static boolean parseAsBoolean(@Nullable String s) {
        boolean retval = false;
        if (!Strings.isNullOrEmpty(s)) {
            s = s.trim();
            if (Strings2.equalsIgnoreCaseEnglishLowercasing("yes", s)) {
                retval = true;
            } else {
                try {
                    retval = Integer.parseInt(s) > 0;
                } catch (NumberFormatException e) {
                    retval = Boolean.parseBoolean(s);
                }
            }
        }
        return retval;
    }

    public static Long parseAsLong(@Nullable String s, @Nullable Long defaultVal) {
        try {
            return Long.parseLong(s.trim());
        } catch (RuntimeException nfe) {
            if (defaultVal == null) {
                throw nfe;
            }
            return defaultVal;
        }
    }

    public static Long parseAsEnglishLong(@Nullable String s, @Nullable Long defaultVal) {
        try {
            return parseAsEnglishLong(s);
        } catch (NumberFormatException nfe) {
            if (defaultVal == null) {
                throw nfe;
            }
            return defaultVal;
        }
    }

    public static long parseAsEnglishLong(String s) {

        s = makeEnglishLongOrIntParseable(s);
        if (StringUtils.isBlank(s)) {
            throw new NumberFormatException("null");
        }
        return Long.parseLong(s);
    }

    private static String makeEnglishLongOrIntParseable(String s) {
        return Strings.nullToEmpty(s).replace(",", "").trim();
    }

    public static int parseAsInt(@Nullable String s, @Nullable Integer defaultVal) {
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException nfe) {
            if (defaultVal == null) {
                throw nfe;
            }
            return defaultVal;
        }
    }

    public static int parseAsEnglishInt(@Nullable String s, @Nullable Integer defaultVal) {
        try {
            return parseAsEnglishInt(s);
        } catch (NumberFormatException nfe) {
            if (defaultVal == null) {
                throw nfe;
            }
            return defaultVal;
        }
    }

    public static int parseAsEnglishInt(String s) {

        s = makeEnglishLongOrIntParseable(s);
        if (StringUtils.isBlank(s)) {
            throw new NumberFormatException("null");
        }
        return Integer.parseInt(s);
    }

    public static Number parseAsEnglishNumber(String s)
            throws NumberFormatException {
        try {
            return NumberFormat.getInstance(Locale.ENGLISH).parse(s);
        } catch (ParseException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    public static Date dateIn(long distance, TimeUnit unit) {
        Date now = new Date();
        return new Date(now.getTime() + unit.toMillis(distance));
    }

}
