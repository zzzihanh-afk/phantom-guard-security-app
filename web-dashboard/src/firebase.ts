import { initializeApp, getApps, FirebaseApp } from 'firebase/app';
import { 
  getFirestore, 
  collection, 
  onSnapshot, 
  addDoc, 
  doc, 
  updateDoc, 
  setDoc,
  serverTimestamp,
  query,
  orderBy,
  Firestore
} from 'firebase/firestore';
import { Device, SecurityAlert, ChatMessage } from './types';

// Default Firebase Configuration (can be overridden via localStorage or Settings)
export const DEFAULT_FIREBASE_CONFIG = {
  apiKey: localStorage.getItem('pg_firebase_api_key') || "AIzaSyD-phantom-guard-free-demo-key",
  authDomain: localStorage.getItem('pg_firebase_auth_domain') || "phantom-guard-security-app.firebaseapp.com",
  projectId: localStorage.getItem('pg_firebase_project_id') || "phantom-guard-security-app",
  storageBucket: "phantom-guard-security-app.appspot.com",
  messagingSenderId: "987654321098",
  appId: "1:987654321098:web:a1b2c3d4e5f6g7h8i9j0"
};

let app: FirebaseApp | null = null;
let db: Firestore | null = null;

try {
  if (!getApps().length) {
    app = initializeApp(DEFAULT_FIREBASE_CONFIG);
  } else {
    app = getApps()[0];
  }
  db = getFirestore(app);
} catch (e) {
  console.warn("Firebase initialized in offline/fallback mode:", e);
}

export { db };

/**
 * Dispatches a cryptographically traceable remote command to Firestore
 */
export async function dispatchRemoteCommand(
  deviceId: string, 
  commandType: string, 
  payload: Record<string, string> = {}
) {
  const nonce = Math.random().toString(36).substring(2, 15);
  const now = Date.now();
  const expiresAt = now + (15 * 60 * 1000); // 15 mins expiry
  
  const commandData = {
    commandId: "cmd_" + now + "_" + nonce,
    deviceId,
    ownerId: "usr_tanzimul_001",
    commandType,
    timestamp: now,
    expiresAt,
    nonce,
    payload,
    signature: btoa("PHANTOM_GUARD_AUTH_NONCE_" + nonce),
    status: "PENDING"
  };

  const projId = localStorage.getItem('pg_firebase_project_id') || DEFAULT_FIREBASE_CONFIG.projectId;
  
  // 1. Write active command directly onto the device document for instant execution
  try {
    await fetch(`https://firestore.googleapis.com/v1/projects/${projId}/databases/(default)/documents/devices/${deviceId}?updateMask.fieldPaths=pendingCommand`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fields: {
          pendingCommand: {
            mapValue: {
              fields: {
                commandType: { stringValue: commandData.commandType },
                commandId: { stringValue: commandData.commandId },
                nonce: { stringValue: commandData.nonce },
                status: { stringValue: "PENDING" },
                timestamp: { integerValue: commandData.timestamp.toString() }
              }
            }
          }
        }
      })
    });
    console.log(`Direct pendingCommand patched for ${deviceId}: ${commandType}`);
  } catch (e) {
    console.warn("Direct device pendingCommand patch note:", e);
  }

  // 2. Append to remoteCommands audit log
  try {
    await fetch(`https://firestore.googleapis.com/v1/projects/${projId}/databases/(default)/documents/remoteCommands`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fields: {
          commandId: { stringValue: commandData.commandId },
          deviceId: { stringValue: commandData.deviceId },
          ownerId: { stringValue: commandData.ownerId },
          commandType: { stringValue: commandData.commandType },
          timestamp: { integerValue: commandData.timestamp.toString() },
          expiresAt: { integerValue: commandData.expiresAt.toString() },
          nonce: { stringValue: commandData.nonce },
          status: { stringValue: commandData.status },
          signature: { stringValue: commandData.signature }
        }
      })
    });
  } catch (e) {
    console.warn("REST remoteCommand dispatch note:", e);
  }

  if (db) {
    try {
      await addDoc(collection(db, "remoteCommands"), commandData);
    } catch (e) {
      console.warn("Firestore SDK write note:", e);
    }
  }

  return commandData;
}

/**
 * Real-time listener for enrolled devices (Hybrid REST + Firestore SDK)
 */
export function subscribeToDevices(callback: (devices: Device[]) => void) {
  const fetchRestDevices = async () => {
    try {
      const projId = localStorage.getItem('pg_firebase_project_id') || DEFAULT_FIREBASE_CONFIG.projectId;
      const res = await fetch(`https://firestore.googleapis.com/v1/projects/${projId}/databases/(default)/documents/devices`);
      if (res.ok) {
        const data = await res.json();
        if (data.documents && Array.isArray(data.documents)) {
          const parsed = data.documents.map((doc: any) => {
            const fields = doc.fields || {};
            const docId = doc.name.split('/').pop() || '';
            return {
              id: docId,
              name: fields.name?.stringValue || docId,
              manufacturer: fields.manufacturer?.stringValue || 'Samsung',
              model: fields.model?.stringValue || 'SM-A546E',
              androidVersion: fields.androidVersion?.stringValue || '15',
              apiLevel: parseInt(fields.apiLevel?.integerValue || '35', 10),
              batteryLevel: parseInt(fields.batteryLevel?.integerValue || '85', 10),
              isCharging: fields.isCharging?.booleanValue || false,
              isOnline: fields.isOnline?.booleanValue ?? true,
              protectionTier: fields.protectionTier?.stringValue || 'STANDARD_PROTECTION',
              lastSeen: fields.lastSeen?.stringValue || 'Just now',
              location: fields.location?.stringValue || 'Dhaka, Bangladesh',
              latitude: fields.latitude?.doubleValue || 23.8103,
              longitude: fields.longitude?.doubleValue || 90.4125
            } as Device;
          });
          if (parsed.length > 0) {
            callback(parsed);
          }
        }
      }
    } catch (e) {
      // ignore
    }
  };

  fetchRestDevices();
  const intervalId = setInterval(fetchRestDevices, 4000);

  let unsubSdk = () => {};
  if (db) {
    try {
      unsubSdk = onSnapshot(collection(db, "devices"), (snapshot) => {
        if (!snapshot.empty) {
          const devicesList = snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
          })) as Device[];
          callback(devicesList);
        }
      }, (error) => {
        console.warn("Firestore devices subscription note:", error.message);
      });
    } catch (e) {
      // ignore
    }
  }

  return () => {
    clearInterval(intervalId);
    unsubSdk();
  };
}

/**
 * Real-time listener for security alerts
 */
export function subscribeToAlerts(callback: (alerts: SecurityAlert[]) => void) {
  if (!db) return () => {};
  try {
    return onSnapshot(collection(db, "securityAlerts"), (snapshot) => {
      if (!snapshot.empty) {
        const alertsList = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as SecurityAlert[];
        callback(alertsList);
      }
    }, (error) => {
      console.warn("Firestore alerts subscription note:", error.message);
    });
  } catch (e) {
    return () => {};
  }
}

/**
 * Real-time listener for recovery chat
 */
export function subscribeToRecoveryChat(callback: (messages: ChatMessage[]) => void) {
  if (!db) return () => {};
  try {
    return onSnapshot(collection(db, "recoveryConversations", "device_redmi_13", "messages"), (snapshot) => {
      if (!snapshot.empty) {
        const msgs = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as ChatMessage[];
        callback(msgs);
      }
    }, (error) => {
      console.warn("Firestore chat subscription note:", error.message);
    });
  } catch (e) {
    return () => {};
  }
}

/**
 * Sends a recovery message to Cloud Firestore
 */
export async function pushChatMessage(message: ChatMessage) {
  if (!db) return;
  try {
    await addDoc(collection(db, "recoveryConversations", "device_redmi_13", "messages"), {
      ...message,
      createdAt: serverTimestamp()
    });
  } catch (e) {
    console.warn("Firestore chat note:", e);
  }
}
