package pro.devapp.walkietalkiek

import java.net.InetSocketAddress
import java.nio.ByteOrder

abstract class SessionEmitter(val addr: InetSocketAddress) {
    private val useDirectBuffers = -1
    private val byteOrder: ByteOrder? = null

    private val reuseAddr = false
    private val tcpNoDelay = true

    /* Use collider global settings by default */

    /* Use collider global settings by default */
    private val socketRecvBufSize = 0
    private val socketSendBufSize = 0
    private val forwardReadMaxSize = 0
    private val inputQueueBlockSize = 0

    /* -1 - use collider global value,
         *  0 - disable message join,
         */

    /* -1 - use collider global value,
         *  0 - disable message join,
         */
    private val joinMessageMaxSize = -1

    /**
     * Called by framework to create session listener instance.
     * See <tt>Acceptor.createSessionListener</tt> and
     * <tt>Connector.createSessionListener</tt> for detailed description.
     * @param session session the listener will be used for
     * @return A listener object for the given session
     */

}