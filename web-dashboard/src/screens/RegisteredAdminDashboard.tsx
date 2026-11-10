import React, { useState } from 'react';
import { 
  Shield, 
  Smartphone, 
  Lock, 
  Bell, 
  AlertTriangle, 
  MapPin, 
  Send, 
  Trash2, 
  RotateCcw, 
  CheckCircle2, 
  Battery, 
  BatteryCharging, 
  Wifi, 
  WifiOff, 
  MessageSquare, 
  Award,
  Zap,
  Activity
} from 'lucide-react';
import { Device, SecurityAlert, ChatMessage } from '../types';

interface Props {
  devices: Device[];
  alerts: SecurityAlert[];
  chatMessages: ChatMessage[];
  onSendMessage: (text: string) => void;
  onExecuteCommand: (deviceId: string, command: string) => void;
}

export const RegisteredAdminDashboard: React.FC<Props> = ({
  devices,
  alerts,
  chatMessages,
  onSendMessage,
  onExecuteCommand
}) => {
  const [selectedDevice, setSelectedDevice] = useState<Device>(devices[0] || null);
  const [inputText, setInputText] = useState('');
  const [lastCommandMessage, setLastCommandMessage] = useState<string | null>(null);
  const [isAlarmActive, setIsAlarmActive] = useState(false);

  const handleCommand = (cmd: string) => {
    if (!selectedDevice) return;
    onExecuteCommand(selectedDevice.id, cmd);
    if (cmd === 'ALARM') {
      setIsAlarmActive(!isAlarmActive);
      setLastCommandMessage(`Command dispatched: ${!isAlarmActive ? 'SIREN TRIGGERED (Max Volume)' : 'SIREN STOPPED'} on ${selectedDevice.name}`);
    } else {
      setLastCommandMessage(`Cryptographically signed command '${cmd}' delivered to ${selectedDevice.name}`);
    }
    setTimeout(() => setLastCommandMessage(null), 5000);
  };

  return (
    <div className="space-y-8">
      {/* Platform Security Banner */}
      <div className="relative overflow-hidden rounded-2xl p-6 sm:p-8 bg-gradient-to-r from-[#0D3325] to-[#0F1A36] border border-[#10B981]/50 shadow-lg">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-5">
            <div className="w-16 h-16 rounded-2xl bg-[#05D182]/20 border border-[#05D182] flex items-center justify-center text-[#05D182] shadow-[0_0_20px_rgba(5,209,130,0.3)]">
              <Shield className="w-9 h-9" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h2 className="text-2xl font-bold text-white">All Devices Secure</h2>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-[#05D182]/20 text-[#05D182] border border-[#05D182]/40">
                  DEEP ACTIVE
                </span>
              </div>
              <p className="text-sm text-[#05D182] font-semibold mt-0.5">
                Excellent Protection • Universal Anti-Theft Surveillance Online
              </p>
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 w-full sm:w-auto">
            <div className="cyber-card p-4 text-center min-w-[100px]">
              <div className="text-xl font-bold text-white">{devices.length}</div>
              <div className="text-xs text-[#94A3B8]">Devices</div>
            </div>
            <div className="cyber-card p-4 text-center min-w-[100px]">
              <div className="text-xl font-bold text-[#05D182]">100%</div>
              <div className="text-xs text-[#94A3B8]">Protected</div>
            </div>
            <div className="cyber-card p-4 text-center min-w-[100px]">
              <div className="text-xl font-bold text-[#00D2FF]">{devices.filter(d => d.isOnline).length}</div>
              <div className="text-xs text-[#94A3B8]">Online</div>
            </div>
            <div className="cyber-card p-4 text-center min-w-[100px]">
              <div className="text-xl font-bold text-[#F59E0B]">{alerts.length}</div>
              <div className="text-xs text-[#94A3B8]">Alerts</div>
            </div>
          </div>
        </div>
      </div>

      {lastCommandMessage && (
        <div className="p-4 rounded-xl bg-[#15264F] border border-[#00D2FF] text-[#00D2FF] flex items-center space-x-3 glow-cyan animate-pulse">
          <Zap className="w-5 h-5 flex-shrink-0" />
          <span className="text-sm font-semibold">{lastCommandMessage}</span>
        </div>
      )}

      {/* Main Grid: Devices List & Command Hub */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Device Selector */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <Smartphone className="w-5 h-5 text-[#00D2FF]" />
              Enrolled Devices
            </h3>
            <span className="text-xs text-[#94A3B8]">{devices.length} registered</span>
          </div>

          <div className="space-y-3">
            {devices.map((device) => {
              const isSelected = selectedDevice?.id === device.id;
              return (
                <div
                  key={device.id}
                  onClick={() => setSelectedDevice(device)}
                  className={`cyber-card p-4 cursor-pointer transition-all ${
                    isSelected 
                      ? 'border-[#00D2FF] bg-[#15264F] glow-cyan' 
                      : 'hover:border-[#1D72FE] hover:bg-[#132247]'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="w-12 h-12 rounded-xl bg-[#070B19] border border-[#1D3265] flex items-center justify-center text-[#00D2FF]">
                        <Smartphone className="w-6 h-6" />
                      </div>
                      <div>
                        <h4 className="font-bold text-white text-base">{device.name}</h4>
                        <div className="flex items-center space-x-2 mt-1">
                          <span className={`w-2 h-2 rounded-full ${device.isOnline ? 'bg-[#05D182]' : 'bg-[#F59E0B]'}`} />
                          <span className={`text-xs ${device.isOnline ? 'text-[#05D182]' : 'text-[#F59E0B]'}`}>
                            {device.isOnline ? 'Online' : 'Offline'}
                          </span>
                          <span className="text-xs text-[#64748B]">•</span>
                          <div className="flex items-center text-xs text-[#94A3B8]">
                            <Battery className="w-3.5 h-3.5 mr-1" />
                            {device.batteryLevel}%
                          </div>
                        </div>
                      </div>
                    </div>

                    <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#05D182]/15 text-[#05D182] border border-[#05D182]/30">
                      {device.protectionTier === 'DEEP_PROTECTION' ? 'Deep Protection' : 'Standard'}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Center Column: Remote Control Center */}
        <div className="lg:col-span-2 space-y-6">
          {selectedDevice ? (
            <div className="cyber-card p-6 sm:p-8 space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-[#1D3265] gap-4">
                <div>
                  <h3 className="text-xl font-bold text-white flex items-center gap-2">
                    <Lock className="w-5 h-5 text-[#00D2FF]" />
                    Remote Control — {selectedDevice.name}
                  </h3>
                  <p className="text-xs text-[#94A3B8] mt-1">
                    Manufacturer: {selectedDevice.manufacturer} • Android {selectedDevice.androidVersion} (API {selectedDevice.apiLevel})
                  </p>
                </div>
                <div className="flex items-center space-x-3">
                  <span className="px-3 py-1 rounded-full text-xs font-semibold bg-[#1D72FE]/20 text-[#00D2FF] border border-[#1D72FE]/50">
                    ECDSA P-256 SIGNED
                  </span>
                </div>
              </div>

              {/* 2x3 Action Commands Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                <button
                  onClick={() => handleCommand('LOCK')}
                  className="p-4 rounded-xl bg-[#0F1A36] border border-[#1D3265] hover:border-[#00D2FF] hover:bg-[#15264F] transition-all text-center group"
                >
                  <Lock className="w-7 h-7 mx-auto text-[#00D2FF] group-hover:scale-110 transition-transform mb-2" />
                  <div className="text-sm font-bold text-white">Lock Device</div>
                  <div className="text-[11px] text-[#94A3B8] mt-0.5">Force Hardware Lock</div>
                </button>

                <button
                  onClick={() => handleCommand('ALARM')}
                  className={`p-4 rounded-xl border transition-all text-center group ${
                    isAlarmActive
                      ? 'bg-[#EF4444]/20 border-[#EF4444] glow-red'
                      : 'bg-[#0F1A36] border-[#1D3265] hover:border-[#F59E0B] hover:bg-[#15264F]'
                  }`}
                >
                  <Bell className="w-7 h-7 mx-auto text-[#F59E0B] group-hover:scale-110 transition-transform mb-2" />
                  <div className="text-sm font-bold text-white">{isAlarmActive ? 'Stop Siren' : 'Siren Alarm'}</div>
                  <div className="text-[11px] text-[#94A3B8] mt-0.5">Max Volume Override</div>
                </button>

                <button
                  onClick={() => handleCommand('STOLEN_MODE')}
                  className="p-4 rounded-xl bg-[#0F1A36] border border-[#1D3265] hover:border-[#05D182] hover:bg-[#15264F] transition-all text-center group"
                >
                  <Shield className="w-7 h-7 mx-auto text-[#05D182] group-hover:scale-110 transition-transform mb-2" />
                  <div className="text-sm font-bold text-white">Stolen Mode</div>
                  <div className="text-[11px] text-[#94A3B8] mt-0.5">Rapid GPS & Lock</div>
                </button>

                <button
                  onClick={() => handleCommand('LOCATE')}
                  className="p-4 rounded-xl bg-[#0F1A36] border border-[#1D3265] hover:border-[#00D2FF] hover:bg-[#15264F] transition-all text-center group"
                >
                  <MapPin className="w-7 h-7 mx-auto text-[#00D2FF] group-hover:scale-110 transition-transform mb-2" />
                  <div className="text-sm font-bold text-white">Locate Now</div>
                  <div className="text-[11px] text-[#94A3B8] mt-0.5">GPS Pinpoint Fix</div>
                </button>

                <button
                  onClick={() => handleCommand('SEND_MESSAGE')}
                  className="p-4 rounded-xl bg-[#0F1A36] border border-[#1D3265] hover:border-[#1D72FE] hover:bg-[#15264F] transition-all text-center group"
                >
                  <MessageSquare className="w-7 h-7 mx-auto text-[#1D72FE] group-hover:scale-110 transition-transform mb-2" />
                  <div className="text-sm font-bold text-white">Display Message</div>
                  <div className="text-[11px] text-[#94A3B8] mt-0.5">Show Screen Banner</div>
                </button>

                <button
                  onClick={() => handleCommand('WIPE_DATA')}
                  className="p-4 rounded-xl bg-[#0F1A36] border border-[#EF4444]/40 hover:border-[#EF4444] hover:bg-[#EF4444]/15 transition-all text-center group"
                >
                  <Trash2 className="w-7 h-7 mx-auto text-[#EF4444] group-hover:scale-110 transition-transform mb-2" />
                  <div className="text-sm font-bold text-[#EF4444]">Wipe Device</div>
                  <div className="text-[11px] text-[#94A3B8] mt-0.5">Enterprise Reset</div>
                </button>
              </div>

              {/* Emergency Lockdown Action */}
              <div className="pt-2">
                <button
                  onClick={() => handleCommand('EMERGENCY_LOCKDOWN')}
                  className="w-full py-4 px-6 rounded-xl bg-gradient-to-r from-[#DC2626] to-[#EF4444] hover:from-[#B91C1C] hover:to-[#DC2626] text-white font-bold text-base shadow-lg glow-red transition-all flex items-center justify-center space-x-2"
                >
                  <AlertTriangle className="w-5 h-5" />
                  <span>EMERGENCY LOCKDOWN (LOCKTASK KIOSK)</span>
                </button>
              </div>
            </div>
          ) : (
            <div className="cyber-card p-12 text-center text-[#94A3B8]">
              Select a device to view remote telemetry and dispatch authenticated commands.
            </div>
          )}
        </div>
      </div>

      {/* Two-Column Bottom Row: Live GPS Map & Recovery Chat */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Live GPS Map Simulation */}
        <div className="cyber-card p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <MapPin className="w-5 h-5 text-[#00D2FF]" />
              Live Device Location
            </h3>
            <span className="text-xs text-[#05D182] font-semibold">● Realtime GPS Tracked</span>
          </div>

          <div className="relative h-64 rounded-xl overflow-hidden bg-[#070B19] border border-[#1D3265] flex items-center justify-center">
            {/* Grid background lines */}
            <div className="absolute inset-0 bg-[radial-gradient(#1D3265_1px,transparent_1px)] [background-size:16px_16px] opacity-40" />

            {/* Concentric radar rings */}
            <div className="absolute w-44 h-44 rounded-full border border-[#00D2FF]/20 animate-ping" />
            <div className="absolute w-28 h-28 rounded-full border border-[#00D2FF]/40" />
            
            {/* Pin Target */}
            <div className="relative z-10 flex flex-col items-center">
              <div className="w-10 h-10 rounded-full bg-[#EF4444] text-white flex items-center justify-center shadow-[0_0_20px_rgba(239,68,68,0.6)]">
                <MapPin className="w-6 h-6" />
              </div>
              <div className="mt-2 px-3 py-1 rounded-full bg-[#0F1A36] border border-[#00D2FF] text-xs font-bold text-white shadow-md">
                {selectedDevice?.name || "Redmi Note 13"}
              </div>
            </div>

            <div className="absolute bottom-3 left-3 bg-[#0F1A36]/90 backdrop-blur px-3 py-1.5 rounded-lg border border-[#1D3265] text-xs text-[#94A3B8]">
              {selectedDevice?.location || "Dhaka, Bangladesh"} (Lat: {selectedDevice?.latitude || 23.8103}, Lng: {selectedDevice?.longitude || 90.4125})
            </div>
          </div>
        </div>

        {/* Recovery Negotiation Center */}
        <div className="cyber-card p-6 space-y-4 flex flex-col">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-[#00D2FF]" />
              Recovery Negotiation Center
            </h3>
            <div className="flex items-center space-x-1 text-xs text-[#EAB308] bg-[#EAB308]/15 px-2.5 py-1 rounded-full border border-[#EAB308]/30">
              <Award className="w-3.5 h-3.5" />
              <span>Reward ৳5,000</span>
            </div>
          </div>

          <div className="flex-1 space-y-3 overflow-y-auto max-h-56 pr-2">
            {chatMessages.map((msg) => (
              <div
                key={msg.id}
                className={`flex flex-col ${msg.isOwner ? 'items-end' : 'items-start'}`}
              >
                <div
                  className={`max-w-[85%] rounded-2xl px-4 py-2.5 text-sm ${
                    msg.isOwner
                      ? 'bg-[#1D72FE] text-white rounded-br-xs'
                      : 'bg-[#15264F] text-white border border-[#1D3265] rounded-bl-xs'
                  }`}
                >
                  {msg.text}
                </div>
                <span className="text-[10px] text-[#64748B] mt-1 px-1">{msg.timestamp}</span>
              </div>
            ))}
          </div>

          <form
            onSubmit={(e) => {
              e.preventDefault();
              if (inputText.trim()) {
                onSendMessage(inputText);
                setInputText('');
              }
            }}
            className="flex items-center space-x-2 pt-2 border-t border-[#1D3265]"
          >
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Type message to device finder..."
              className="flex-1 bg-[#070B19] border border-[#1D3265] rounded-xl px-4 py-2.5 text-sm text-white placeholder-[#64748B] focus:outline-none focus:border-[#00D2FF]"
            />
            <button
              type="submit"
              className="px-4 py-2.5 rounded-xl bg-[#1D72FE] hover:bg-[#00D2FF] text-white font-semibold text-sm transition-all"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
