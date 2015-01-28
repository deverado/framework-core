package de.deverado.framework.core;

import javax.annotation.ParametersAreNonnullByDefault;

import java.nio.ByteBuffer;

/**
 * Byte buffers are useful to avoid copying (direct buffers) and they don't combine multiple non-contiguous areas. That
 * could be achieved with ByteSource from Guava or InputStreams. To pass on a part of a buffer use
 * slice().limit(newEnd).
 */
@ParametersAreNonnullByDefault
public class ByteBuffers {

    /**
     * Please use ByteBuffer for performance - gets bytes between position and
     * limit. Tries to reuse the buffers backing array.
     * 
     * @param buffer
     * @return
     */
    public static byte[] getBytesReusingInternalArray(ByteBuffer buffer) {
        if (buffer.hasArray() && buffer.arrayOffset() == 0
                && buffer.remaining() == buffer.array().length) {
            return buffer.array();
        }
        return bufferReadableBytesToNewArray(buffer);

    }

    /**
     * Always allocates a new array to return.
     * 
     * @param buffer
     * @return
     */
    public static byte[] bufferReadableBytesToNewArray(ByteBuffer buffer) {
        byte[] retval = new byte[buffer.remaining()];
        buffer.get(retval);
        return retval;
    }
}
