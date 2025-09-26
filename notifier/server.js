const express = require("express");
const bodyParser = require("body-parser");
const admin = require("firebase-admin");

const app = express();
app.use(bodyParser.json());

admin.initializeApp({
  credential: admin.credential.cert(require("./serviceAccountKey.json")),
});

app.post("/sendInvite", async (req, res) => {
    console.log("POST /sendInvite", req.body);

  const {inviteId, receiverEmail, receiverName, senderName, allianceName, senderEmail } = req.body;
  try {
    const snap = await admin.firestore()
      .collection("users")
      .where("email", "==", receiverEmail)
      .limit(1)
      .get();

    if (snap.empty) {
      return res.status(404).send("User not found");
    }

    const userDoc = snap.docs[0];
    const fcmToken = userDoc.get("fcmToken");
    if (!fcmToken) {
      return res.status(400).send("No FCM token");
    }

    const message = {
      token: fcmToken,
      notification: {
        title: "Alliance invitation",
        body: `${senderName} invited you to ${allianceName}`,
      },
      data: {
        type: "invite",
        inviteId: inviteId,
        receiverEmail: receiverEmail,
        receiverName: receiverName,
        allianceName: allianceName,
        senderName: senderName,
        senderEmail: senderEmail,
      },
    };

    await admin.messaging().send(message);
    res.send("Notification sent");
  } catch (err) {
    console.error(err);
    res.status(500).send(err.message);
  }
});

app.post("/sendAccept", async (req, res) => {
  const { receiverEmail, accepterName, allianceName } = req.body;
  try {
    const snap = await admin.firestore()
      .collection("users")
      .where("email", "==", receiverEmail)
      .limit(1)
      .get();

    if (snap.empty) return res.status(404).send("Sender not found");

    const userDoc = snap.docs[0];
    const fcmToken = userDoc.get("fcmToken");
    if (!fcmToken) return res.status(400).send("No FCM token");

    const message = {
      token: fcmToken,
      notification: {
        title: "Invite accepted",
        body: `${accepterName} accepted your alliance invitation to ${allianceName}`,
      },
      data: {
        type: "inviteAccepted",
        allianceName,
        accepterName,
      },
    };

    await admin.messaging().send(message);
    res.send("Acceptance notification sent");
  } catch (err) {
    console.error(err);
    res.status(500).send(err.message);
  }
});

app.post("/sendAllianceMessage", async (req, res) => {
  const { allianceName, senderEmail, senderName, messageText } = req.body;

  if (!allianceName || !senderEmail || !messageText) {
    return res.status(400).send("Missing required fields");
  }

  try {
    const snap = await admin.firestore()
      .collection("users")
      .where("alliance", "==", allianceName)
      .get();

    if (snap.empty) {
      return res.status(404).send("No members found");
    }

    const tokens = [];
    snap.forEach(doc => {
      const userEmail = doc.get("email");
      const token = doc.get("fcmToken");
      if (token && userEmail !== senderEmail) {
        tokens.push(token);
      }
    });

    if (tokens.length === 0) {
      return res.status(400).send("No valid FCM tokens");
    }

    await Promise.all(tokens.map(token =>
      admin.messaging().send({
        token,
        notification: {
          title: `New message in ${allianceName}`,
          body: `${senderName}: ${messageText}`,
        },
        data: {
          type: "allianceMessage",
          allianceName,
          senderEmail,
          senderName,
          text: messageText,
        },
      })
    ));

    console.log(`Alliance message sent to ${tokens.length} members`);
    res.send(`Sent to ${tokens.length} members`);
  } catch (err) {
    console.error(err);
    res.status(500).send(err.message);
  }
});



app.listen(3000, () => console.log("Server running on port 3000"));
