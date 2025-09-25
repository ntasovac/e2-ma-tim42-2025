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


app.listen(3000, () => console.log("Server running on port 3000"));
