## Social Distancing Tracker

Proof of concept (PoC) has been created to check whether an android smartphone can detect another nearby device for social distancing. The application continuously searches for any nearby devices in the background and stores the log information in the local storage. Track logs can be accessed anytime by launching the app in the foreground.

Android 4.3 (API level 18) introduces built-in platform support for Bluetooth Low Energy (BLE) in the central role and provides APIs that apps can use to discover devices, query for services, and transmit information. The application continuously advertises bluetooth low energy signals, which will be detected by any nearby android devices. The advertising and device recognition happens entirely in the background without any user intervention. The distance will be calculated by the signal strength at the moment. This is achieved by using the library Android beacon Library. The library internal uses bluetooth low energy API.

On detecting any nearby devices, the application stores the device's unique ID, timestamp, signal strength into the local database. The data can be accessed anytime by the app user. The data can also be synchronized to a remote server on active internet connection or upon user request (Not implemented yet)
