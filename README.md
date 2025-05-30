# Push-to-Talk App. Local Network Communication.

Discover instant, server-free communication with our Push-to-Talk App! Designed for local networks, this app transforms your device into both a server and a client, enabling direct, real-time voice and data exchange without relying on external servers.

Key Features
Server-Free Connectivity: Each app instance acts as its own server.
Effortless Device Discovery: App used NsdManager, the app automatically detects and connects to devices in local network.
Data Transfer: App used SocketChannel (java.nio.channels) for reliable, low-latency data transmission.

<img src="https://github.com/devapro/LANwalkieTalkie/raw/master/screen.jpg" width="230" />

## TODO:

- ping (check connection by timer)
- send only if has active client
- add limit to send queue
- using Compose for UI
- added talk button to foreground notification
- switch between audio outputs
- settings
- sounds (for new messages, for start play audio)
