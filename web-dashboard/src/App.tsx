import React, { useState, useEffect } from 'react';
import { 
  Shield, 
  Smartphone, 
  Lock, 
  Crown, 
  Bell, 
  Settings, 
  LogOut, 
  Plus, 
  RefreshCw,
  ExternalLink,
  Database,
  CheckCircle2,
  X
} from 'lucide-react';
import { RegisteredAdminDashboard } from './screens/RegisteredAdminDashboard';
import { GlobalMasterAdminDashboard } from './screens/GlobalMasterAdminDashboard';
import { Device, SecurityAlert, ChatMessage, UserRole } from './types';
import { 
  subscribeToDevices, 
  subscribeToAlerts, 
  subscribeToRecoveryChat, 
  dispatchRemoteCommand, 
  pushChatMessage,
  DEFAULT_FIREBASE_CONFIG 
} from './firebase';

export const App: React.FC = () => {
  const [role, setRole] = useState<UserRole>('REGISTERED_ADMIN');
  const [showDbModal, setShowDbModal] = useState(false);
  const [firebaseProjectId, setFirebaseProjectId] = useState(DEFAULT_FIREBASE_CONFIG.projectId);
  const [firebaseApiKey, setFirebaseApiKey] = useState(DEFAULT_FIREBASE_CONFIG.apiKey);

  const [devices, setDevices] = useState<Device[]>([
    {
      id: 'dev_redmi_13',
      name: 'Redmi Note 13',
      manufacturer: 'Xiaomi',
      model: '23124RA7EO',
      androidVersion: '14',
      apiLevel: 34,
      batteryLevel: 85,
      isCharging: false,
      isOnline: true,
      protectionTier: 'DEEP_PROTECTION',
      lastSeen: 'Just now',
      location: 'Dhaka, Bangladesh',
      latitude: 23.8103,
      longitude: 90.4125
    },
    {
      id: 'dev_samsung_a54',
      name: 'Samsung A54',
      manufacturer: 'Samsung',
      model: 'SM-A546B',
      androidVersion: '14',
      apiLevel: 34,
      batteryLevel: 72,
      isCharging: true,
      isOnline: true,
      protectionTier: 'STANDARD_PROTECTION',
      lastSeen: '5 min ago',
      location: 'Dhaka, Bangladesh',
      latitude: 23.7925,
      longitude: 90.4078
    },
    {
      id: 'dev_realme_c67',
      name: 'Realme C67',
      manufacturer: 'Realme',
      model: 'RMX3890',
      androidVersion: '13',
      apiLevel: 33,
      batteryLevel: 45,
      isCharging: false,
      isOnline: false,
      protectionTier: 'STANDARD_PROTECTION',
      lastSeen: '2h ago',
      location: 'Dhaka, Bangladesh',
      latitude: 23.7771,
      longitude: 90.3994
    }
  ]);

  const [alerts, setAlerts] = useState<SecurityAlert[]>([
    {
      id: 'alt_001',
      deviceId: 'dev_redmi_13',
      deviceName: 'Redmi Note 13',
      title: 'SIM Change Detected',
      message: 'New SIM detected on Redmi Note 13',
      severity: 'CRITICAL',
      timestamp: '10 Nov, 2026 • 09:24 AM'
    },
    {
      id: 'alt_002',
      deviceId: 'dev_realme_c67',
      deviceName: 'Realme C67',
      title: 'Device Offline',
      message: 'Device is offline for more than 1 hour',
      severity: 'HIGH',
      timestamp: '10 Nov, 2026 • 08:15 AM'
    },
    {
      id: 'alt_003',
      deviceId: 'dev_redmi_13',
      deviceName: 'Redmi Note 13',
      title: 'Protection Active',
      message: 'All systems are normal',
      severity: 'INFO',
      timestamp: '10 Nov, 2026 • 07:50 AM'
    }
  ]);

  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([
    {
      id: 'msg_001',
      sender: 'Owner',
      isOwner: true,
      text: 'Hello! This is the owner of this device. If you have found this phone, please contact me. Thank you!',
      timestamp: '10:14 AM'
    },
    {
      id: 'msg_002',
      sender: 'Finder',
      isOwner: false,
      text: 'Sure, I will return it. 👍',
      timestamp: '10:15 AM'
    }
  ]);

  // Hook up Real-time Firebase Firestore subscriptions
  useEffect(() => {
    const unsubDevices = subscribeToDevices((firestoreDevices) => {
      if (firestoreDevices.length > 0) {
        setDevices(firestoreDevices);
      }
    });

    const unsubAlerts = subscribeToAlerts((firestoreAlerts) => {
      if (firestoreAlerts.length > 0) {
        setAlerts(firestoreAlerts);
      }
    });

    const unsubChat = subscribeToRecoveryChat((firestoreChat) => {
      if (firestoreChat.length > 0) {
        setChatMessages(firestoreChat);
      }
    });

    return () => {
      unsubDevices();
      unsubAlerts();
      unsubChat();
    };
  }, []);

  const handleSendMessage = async (text: string) => {
    const newMsg: ChatMessage = {
      id: 'msg_' + Date.now(),
      sender: 'Owner',
      isOwner: true,
      text,
      timestamp: 'Just now'
    };
    setChatMessages(prev => [...prev, newMsg]);
    await pushChatMessage(newMsg);
  };

  const handleExecuteCommand = async (deviceId: string, command: string) => {
    console.log(`Dispatched ${command} to ${deviceId}`);
    await dispatchRemoteCommand(deviceId, command);
  };

  const saveDatabaseConfig = () => {
    localStorage.setItem('pg_firebase_project_id', firebaseProjectId);
    localStorage.setItem('pg_firebase_api_key', firebaseApiKey);
    setShowDbModal(false);
    window.location.reload();
  };

  return (
    <div className="min-h-screen bg-[#070B19] text-white flex flex-col">
      {/* Top Cyber Navigation Bar */}
      <header className="sticky top-0 z-50 bg-[#0F1A36]/90 backdrop-blur-md border-b border-[#1D3265] px-4 sm:px-8 py-3.5">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          {/* Logo & Tagline */}
          <div className="flex items-center space-x-3.5">
            <div className="w-10 h-10 rounded-xl bg-[#15264F] border border-[#00D2FF] flex items-center justify-center text-[#00D2FF] glow-cyan">
              <Shield className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-black text-xl tracking-wider text-white">PHANTOM</span>
                <span className="font-black text-xl tracking-wider text-[#00D2FF]">GUARD</span>
              </div>
              <div className="text-[10px] tracking-widest text-[#94A3B8] font-bold">
                LOCK • TRACK • PROTECT
              </div>
            </div>
          </div>

          {/* Right Role Switcher & Database Status controls */}
          <div className="flex items-center space-x-3">
            {/* Database Connect Status Pill */}
            <button
              onClick={() => setShowDbModal(true)}
              className="px-3 py-1.5 rounded-xl bg-[#0F1A36] border border-[#05D182]/50 hover:border-[#05D182] flex items-center space-x-2 transition-all text-xs font-semibold"
            >
              <div className="w-2 h-2 rounded-full bg-[#05D182] animate-pulse" />
              <Database className="w-3.5 h-3.5 text-[#05D182]" />
              <span className="hidden md:inline text-white">Cloud Firestore</span>
            </button>

            {/* Role Switcher Pill */}
            <div className="bg-[#070B19] p-1 rounded-xl border border-[#1D3265] flex items-center">
              <button
                onClick={() => setRole('REGISTERED_ADMIN')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center space-x-1.5 ${
                  role === 'REGISTERED_ADMIN'
                    ? 'bg-[#1D72FE] text-white shadow-md'
                    : 'text-[#94A3B8] hover:text-white'
                }`}
              >
                <Smartphone className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Registered Admin</span>
              </button>

              <button
                onClick={() => setRole('GLOBAL_MASTER_ADMIN')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center space-x-1.5 ${
                  role === 'GLOBAL_MASTER_ADMIN'
                    ? 'bg-[#EAB308] text-black shadow-md'
                    : 'text-[#94A3B8] hover:text-white'
                }`}
              >
                <Crown className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Global Master Admin</span>
              </button>
            </div>

            {/* Profile Avatar */}
            <div className="w-9 h-9 rounded-xl bg-[#15264F] border border-[#1D3265] flex items-center justify-center text-[#00D2FF]">
              <span className="text-xs font-bold">TZ</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Viewport */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-8">
        {role === 'REGISTERED_ADMIN' ? (
          <RegisteredAdminDashboard
            devices={devices}
            alerts={alerts}
            chatMessages={chatMessages}
            onSendMessage={handleSendMessage}
            onExecuteCommand={handleExecuteCommand}
          />
        ) : (
          <GlobalMasterAdminDashboard />
        )}
      </main>

      {/* Database Connection Modal */}
      {showDbModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4">
          <div className="cyber-card p-6 sm:p-8 max-w-lg w-full space-y-6 bg-[#0F1A36] border-[#00D2FF] glow-cyan">
            <div className="flex items-center justify-between pb-4 border-b border-[#1D3265]">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-xl bg-[#05D182]/20 border border-[#05D182] flex items-center justify-center text-[#05D182]">
                  <Database className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-white">Cloud Firestore Database</h3>
                  <div className="flex items-center space-x-1.5 text-xs text-[#05D182] font-semibold">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Real-time Sync Active</span>
                  </div>
                </div>
              </div>
              <button 
                onClick={() => setShowDbModal(false)}
                className="text-[#94A3B8] hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4 text-sm text-[#94A3B8]">
              <p>
                Phantom Guard is configured for **Cloud Firestore (Firebase Spark Free Tier)**. All remote commands, device heartbeats, and security alerts are synced in real-time.
              </p>

              <div>
                <label className="block text-xs font-semibold text-white mb-1.5">
                  Firebase Project ID
                </label>
                <input
                  type="text"
                  value={firebaseProjectId}
                  onChange={(e) => setFirebaseProjectId(e.target.value)}
                  className="w-full bg-[#070B19] border border-[#1D3265] rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-[#00D2FF]"
                  placeholder="e.g. phantom-guard-security"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-white mb-1.5">
                  Web API Key (Optional Override)
                </label>
                <input
                  type="text"
                  value={firebaseApiKey}
                  onChange={(e) => setFirebaseApiKey(e.target.value)}
                  className="w-full bg-[#070B19] border border-[#1D3265] rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-[#00D2FF]"
                  placeholder="AIzaSy..."
                />
              </div>

              <div className="p-3.5 rounded-xl bg-[#070B19] border border-[#1D3265] text-xs space-y-1">
                <div className="text-white font-semibold">Linked Collections:</div>
                <div className="text-[#00D2FF] font-mono">/devices • /remoteCommands • /securityAlerts • /recoveryConversations</div>
              </div>
            </div>

            <div className="flex items-center justify-end space-x-3 pt-4 border-t border-[#1D3265]">
              <button
                onClick={() => setShowDbModal(false)}
                className="px-4 py-2 rounded-xl text-sm text-[#94A3B8] hover:text-white"
              >
                Close
              </button>
              <button
                onClick={saveDatabaseConfig}
                className="px-5 py-2 rounded-xl bg-[#1D72FE] hover:bg-[#00D2FF] text-white text-sm font-bold transition-all"
              >
                Save & Connect
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="border-t border-[#1D3265] bg-[#070B19] py-6 px-4 text-center text-xs text-[#64748B]">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
          <div>
            <span className="font-bold text-[#94A3B8]">PHANTOM GUARD</span> • Universal Android Anti-Theft & Device Management Platform
          </div>
          <div className="text-[#00D2FF] font-semibold">
            100% Free • No Ads • No Subscriptions • Cloud Firestore Synchronized
          </div>
        </div>
      </footer>
    </div>
  );
};

export default App;
