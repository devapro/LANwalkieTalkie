package pro.devapp.walkietalkiek

interface Session {
//    interface Listener {
//        /**
//         * Called by framework when some data is available.
//         * Executed serially in a one thread, but not necessary always the same.
//         * Position in the byte buffer can be greater than 0,
//         * limit can be less than capacity.
//         * @param data the data received from the related socket
//         */
//        fun onDataReceived(data: RetainableByteBuffer?)
//
//        /**
//         * Called by framework when underlying socket channel
//         * is closed and all income data is processed.
//         */
//        fun onConnectionClosed()
//    }
//
//    /**
//     * @return Collider instance the session is linked with.
//     */
//    fun getCollider(): Collider?
//
//    /**
//     * @return local socket address of the session.
//     */
//    fun getLocalAddress(): SocketAddress?
//
//    /**
//     * @return remote socket address of the session.
//     */
//    fun getRemoteAddress(): SocketAddress?
//
//    /**
//     * Schedules data to be sent to the underlying socket channel.
//     * Retains the data buffer, but buffer remains unchanged
//     * (even it's attributes like a position, limit etc)
//     * so the buffer can be reused to send the same data
//     * to the different sessions.
//     * @param data byte buffer with data to send
//     * @return value greater than 0 if byte buffer is retained by the framework,
//     * (data will be sent as soon as possible), or less than 0 if the session is closed.
//     */
//    fun sendData(data: ByteBuffer?): Int
//    fun sendData(data: RetainableByteBuffer?): Int
//
//    /**
//     * Method makes an attempt to write data synchronously to the underlying socket channel.
//     * It can happen if it is the single thread calling the *sendData* or *sendDataSync*.
//     * Otherwise data will sent as *sendData* would be called.
//     * @param data byte buffer with data to send
//     * @return 0 if data has been written to the socket and byte buffer can be reused,
//     * greater than 0 if byte buffer is retained by the framework, will be sent as soon as possible,
//     * less than 0 if session is closed.
//     */
//    fun sendDataSync(data: ByteBuffer?): Int
//
//    /**
//     * Method to be used to close the session.
//     * Works asynchronously so connection will not be closed immediately
//     * after function return. Outgoing data scheduled but not sent yet
//     * will be sent. Any data already read from the socket at the moment
//     * but not processed yet will be processed. *onConnectionClosed*
//     * will be called after all received data will be processed.
//     * All further *sendData*, *sendDataAsync* and
//     * *closeConnection* calls will return -1.
//     * @return less than 0 if session already has been closed,
//     * otherwise amount of data waiting to be sent.
//     */
//    fun closeConnection(): Int
//
//    /**
//     * Replaces the current session listener with a new one.
//     * Supposed to be called only from the <tt>onDataReceived()</tt> callback.
//     * Calling it not from the <tt>onDataReceived</tt> callback will result
//     * in undefined behaviour.
//     * @param newListener the new listener to be used for the session
//     * @return the previous listener was used to the session
//     */
//    fun replaceListener(newListener: Listener?): Listener?
//
//    fun accelerate(shMem: ShMem?, message: ByteBuffer?): Int
}