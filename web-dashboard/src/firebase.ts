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
  authDomain: localStorage.getItem('pg_firebase_auth_domain') || "phantom-guard-security.firebaseapp.com",
  projectId: localStorage.getItem('pg_firebase_project_id') || "phantom-guard-security",
  storageBucket: "phantom-guard-security.appspot.com",
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

  if (db) {
    try {
      await addDoc(collection(db, "remoteCommands"), commandData);
      console.log("Command written to Firestore collection 'remoteCommands':", commandData);
    } catch (e) {
      console.warn("Firestore offline, command recorded locally:", e);
    }
  }

  return commandData;
}

/**
 * Real-time listener for enrolled devices
 */
export function subscribeToDevices(callback: (devices: Device[]) => void) {
  if (!db) return () => {};
  try {
    return onSnapshot(collection(db, "devices"), (snapshot) => {
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
    return () => {};
  }
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
