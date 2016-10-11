package io.airlift.configuration;

import java.io.IOException;
import java.net.Socket;

public interface SocketConfigurator
{
    void apply(Socket socket)
            throws IOException;
}
