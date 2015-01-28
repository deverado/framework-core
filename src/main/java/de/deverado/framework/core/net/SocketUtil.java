package de.deverado.framework.core.net;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

@ParametersAreNonnullByDefault
public class SocketUtil {

    private static final Logger log = LoggerFactory.getLogger(SocketUtil.class);

    public static ListenableFuture<Boolean> canConnectTcp(
            ListeningExecutorService exec, final String host, final int port,
            final int timeoutMs) {

        return exec.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {

                try {
                    InetSocketAddress socketAddress = new InetSocketAddress(
                            host, port);
                    Socket socket = new Socket();
                    try {
                        socket.connect(socketAddress, timeoutMs);
                        log.trace("Can connect to {}:{}", host, port);
                        return true;
                    } finally {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            log.debug(
                                    "Couldn't close socket after server lifeness check",
                                    e);
                        }
                    }

                } catch (Exception e) {
                    log.debug("Cannot connect to {}:{}, msg:", host, port);
                }
                return false;

            }
        });
    }
}
