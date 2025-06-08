# Push to Talk for local network

### Motivation

This app is proof of concept for push-to-talk application for local networks, and example of usage of different Android APIs and libraries.

### Description
This app is a simple push-to-talk application for local networks, allowing users to communicate with each other without the need for an internet connection (only local WIFI network).

The app works on the local network without server.
Each app instance has a server and client.
App find other devices through NsdManager.

Apps contains example of usage of:
- NsdManager
- MediaRecorder
- AudioRecord
- AudioTrack
- Foreground service

App architecture is based on MVI.
UI is implemented with Jetpack Compose.

### App structure

- `app` - main application package
  - `core` - core architecture components
  - `core-ui` - reusable UI components
  - `service-network` - classes for network communication
  - `service-voice` - classes for recording and playing
  - `features-XXX` - App UI features
    - `ptt` - Ptt
    - `chat` - chat
    - `settings` - settings screen

### Ideas and pull requests

If you have any ideas or suggestions for improvements, feel free to create a pull request or open an issue.

### Screenshots

<table>
    <tr>
        <td><img src="https://raw.githubusercontent.com/devapro/LANwalkieTalkie/refs/heads/master/portrait_chart.jpeg" alt="App screen" width="300"/></td>
        <td><img src="https://raw.githubusercontent.com/devapro/LANwalkieTalkie/refs/heads/master/landscape_chart.jpeg" alt="App screen" width="400"/></td>
    </tr>
</table>
