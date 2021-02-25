Auth Token (formerly, Android Token)
=============
Auth Token, (formerly Android Token) is an Android applicaion used to generate one time passwords (OTP). The application is open source and written in Java. The application supports both HOTP (event tokens, http://tools.ietf.org/html/rfc4226) and TOTP tokens (time tokens, http://tools.ietf.org/html/draft-mraihi-totp-timebased-00).

The application supports provisioning tokens using 
- [KeyUriFormat](https://github.com/google/google-authenticator/wiki/Key-Uri-Format) 
- QR codes
- Manual creation.

The application can optionally be protected with a PIN to stop unauthorised access to the software tokens.

Tokens can be exported as a QR code or by manually copying the seed to the clipboard.

Screen Shots
------------
![Main View](https://github.com/markmcavoy/androidtoken/blob/wiki/mainlist.png)
![Manually adding a new token](https://github.com/markmcavoy/androidtoken/blob/wiki/add_token.png)
![Settings page](https://github.com/markmcavoy/androidtoken/blob/wiki/settings.png)
