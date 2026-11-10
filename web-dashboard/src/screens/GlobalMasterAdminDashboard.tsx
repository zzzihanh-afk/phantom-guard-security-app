import React, { useState } from 'react';
import { 
  Crown, 
  Users, 
  Smartphone, 
  Wifi, 
  AlertTriangle, 
  ShieldCheck, 
  Search, 
  CheckCircle2, 
  Ban, 
  Activity, 
  PieChart, 
  FileText,
  Settings,
  Cpu
} from 'lucide-react';
import { AdminUser } from '../types';

export const GlobalMasterAdminDashboard: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [users, setUsers] = useState<AdminUser[]>([
    { id: 'usr_001', name: 'Tanzimul', email: 'tanzimul@phantomguard.security', devicesCount: 3, status: 'ACTIVE', registeredAt: '10 Nov, 2026' },
    { id: 'usr_002', name: 'Alex Rivera', email: 'alex.r@phantomguard.security', devicesCount: 5, status: 'ACTIVE', registeredAt: '09 Nov, 2026' },
    { id: 'usr_003', name: 'Sara Chen', email: 'sara.c@phantomguard.security', devicesCount: 2, status: 'ACTIVE', registeredAt: '08 Nov, 2026' },
    { id: 'usr_004', name: 'Malicious Suspicious', email: 'hacker@shady.net', devicesCount: 1, status: 'SUSPENDED', registeredAt: '07 Nov, 2026' }
  ]);

  const toggleUserStatus = (id: string) => {
    setUsers(users.map(u => {
      if (u.id === id) {
        return { ...u, status: u.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE' };
      }
      return u;
    }));
  };

  const filteredUsers = users.filter(u => 
    u.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    u.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-8">
      {/* Master Admin Header Banner */}
      <div className="cyber-card p-6 sm:p-8 bg-gradient-to-r from-[#1E1B4B] via-[#0F1A36] to-[#0D3325] border border-[#EAB308]/50 shadow-xl">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-5">
            <div className="w-16 h-16 rounded-2xl bg-[#EAB308]/20 border border-[#EAB308] flex items-center justify-center text-[#EAB308] shadow-[0_0_25px_rgba(234,179,8,0.3)]">
              <Crown className="w-9 h-9" />
            </div>
            <div>
              <div className="flex items-center space-x-3">
                <h2 className="text-2xl font-bold text-white">Global Master Admin</h2>
                <span className="px-3 py-0.5 rounded-full text-xs font-bold bg-[#EAB308]/20 text-[#EAB308] border border-[#EAB308]/40">
                  ROOT SYSTEM CONTROLLER
                </span>
              </div>
              <p className="text-sm text-[#94A3B8] mt-1">
                Sole Platform Creator Master Console • Global Telemetry & Fleet Oversight
              </p>
            </div>
          </div>

          {/* Quick Metrics 4-Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 w-full sm:w-auto">
            <div className="cyber-card p-4 text-center min-w-[110px]">
              <div className="text-2xl font-black text-[#1D72FE]">125</div>
              <div className="text-xs text-[#94A3B8]">Total Admins</div>
            </div>
            <div className="cyber-card p-4 text-center min-w-[110px]">
              <div className="text-2xl font-black text-[#05D182]">842</div>
              <div className="text-xs text-[#94A3B8]">Total Devices</div>
            </div>
            <div className="cyber-card p-4 text-center min-w-[110px]">
              <div className="text-2xl font-black text-[#A855F7]">98%</div>
              <div className="text-xs text-[#94A3B8]">Online Rate</div>
            </div>
            <div className="cyber-card p-4 text-center min-w-[110px]">
              <div className="text-2xl font-black text-[#F59E0B]">12</div>
              <div className="text-xs text-[#94A3B8]">Alerts</div>
            </div>
          </div>
        </div>
      </div>

      {/* Platform Analytics & OEM Compatibility Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* OEM Fleet Compatibility Breakdown */}
        <div className="cyber-card p-6 space-y-4">
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <Cpu className="w-5 h-5 text-[#00D2FF]" />
            OEM Fleet Distribution
          </h3>

          <div className="space-y-3 pt-2">
            <div>
              <div className="flex justify-between text-xs font-semibold mb-1">
                <span>Xiaomi / POCO / Redmi (MIUI/HyperOS)</span>
                <span className="text-[#00D2FF]">34% (286 devices)</span>
              </div>
              <div className="w-full bg-[#070B19] h-2 rounded-full overflow-hidden">
                <div className="bg-[#00D2FF] h-full rounded-full w-[34%]" />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-semibold mb-1">
                <span>Samsung (One UI & Knox)</span>
                <span className="text-[#1D72FE]">28% (235 devices)</span>
              </div>
              <div className="w-full bg-[#070B19] h-2 rounded-full overflow-hidden">
                <div className="bg-[#1D72FE] h-full rounded-full w-[28%]" />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-semibold mb-1">
                <span>OPPO / Realme / OnePlus</span>
                <span className="text-[#05D182]">18% (151 devices)</span>
              </div>
              <div className="w-full bg-[#070B19] h-2 rounded-full overflow-hidden">
                <div className="bg-[#05D182] h-full rounded-full w-[18%]" />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-semibold mb-1">
                <span>Google Pixel (Stock Android DPC)</span>
                <span className="text-[#A855F7]">12% (101 devices)</span>
              </div>
              <div className="w-full bg-[#070B19] h-2 rounded-full overflow-hidden">
                <div className="bg-[#A855F7] h-full rounded-full w-[12%]" />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-semibold mb-1">
                <span>Transsion / Vivo / Motorola / Others</span>
                <span className="text-[#F59E0B]">8% (69 devices)</span>
              </div>
              <div className="w-full bg-[#070B19] h-2 rounded-full overflow-hidden">
                <div className="bg-[#F59E0B] h-full rounded-full w-[8%]" />
              </div>
            </div>
          </div>

          <div className="pt-4 border-t border-[#1D3265] flex items-center justify-between text-xs text-[#94A3B8]">
            <span>Deep Protection (Device Owner):</span>
            <span className="text-[#05D182] font-bold">538 devices (64%)</span>
          </div>
        </div>

        {/* Realtime Telemetry Graph */}
        <div className="lg:col-span-2 cyber-card p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <Activity className="w-5 h-5 text-[#05D182]" />
              Platform Telemetry Curve (Past 24 Hours)
            </h3>
            <span className="text-xs text-[#94A3B8]">Heartbeat & Command Latency &lt; 280ms</span>
          </div>

          <div className="h-56 relative flex items-end justify-between px-4 pt-6 pb-2 border-b border-[#1D3265]">
            {/* Telemetry Bar simulation */}
            {[65, 78, 82, 90, 85, 94, 98, 92, 88, 95, 99, 98].map((val, idx) => (
              <div key={idx} className="flex flex-col items-center gap-2 group flex-1">
                <div 
                  style={{ height: `${val * 1.6}px` }} 
                  className="w-4/5 max-w-[28px] rounded-t-md bg-gradient-to-t from-[#1D72FE] to-[#00D2FF] group-hover:to-[#05D182] transition-all"
                />
                <span className="text-[10px] text-[#64748B]">{idx * 2}:00</span>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-between text-xs text-[#94A3B8]">
            <span>Active Connections: 825 / 842</span>
            <span>Zero Data Leakage IDOR Audited • Firestore Rules Strict</span>
          </div>
        </div>
      </div>

      {/* Registered Admin Management Table */}
      <div className="cyber-card p-6 space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <Users className="w-5 h-5 text-[#1D72FE]" />
              Registered Admins Management
            </h3>
            <p className="text-xs text-[#94A3B8] mt-0.5">
              Review accounts, enrolled device quotas, and enforce suspension where justified
            </p>
          </div>

          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-3 text-[#64748B]" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search user or email..."
              className="bg-[#070B19] border border-[#1D3265] rounded-xl pl-9 pr-4 py-2 text-sm text-white placeholder-[#64748B] focus:outline-none focus:border-[#00D2FF] w-full sm:w-64"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-[#94A3B8]">
            <thead className="bg-[#070B19] text-xs text-[#94A3B8] uppercase border-b border-[#1D3265]">
              <tr>
                <th className="px-4 py-3">Admin Name</th>
                <th className="px-4 py-3">Email Address</th>
                <th className="px-4 py-3">Enrolled Devices</th>
                <th className="px-4 py-3">Registered Date</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1D3265]">
              {filteredUsers.map((u) => (
                <tr key={u.id} className="hover:bg-[#132247]/50 transition-colors">
                  <td className="px-4 py-3 font-bold text-white">{u.name}</td>
                  <td className="px-4 py-3">{u.email}</td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-0.5 rounded-full text-xs bg-[#1D72FE]/15 text-[#00D2FF] border border-[#1D72FE]/30 font-semibold">
                      {u.devicesCount} devices
                    </span>
                  </td>
                  <td className="px-4 py-3">{u.registeredAt}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                      u.status === 'ACTIVE' 
                        ? 'bg-[#05D182]/15 text-[#05D182] border border-[#05D182]/40' 
                        : 'bg-[#EF4444]/15 text-[#EF4444] border border-[#EF4444]/40'
                    }`}>
                      {u.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      onClick={() => toggleUserStatus(u.id)}
                      className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all ${
                        u.status === 'ACTIVE'
                          ? 'bg-[#EF4444]/20 text-[#EF4444] hover:bg-[#EF4444]/40 border border-[#EF4444]/40'
                          : 'bg-[#05D182]/20 text-[#05D182] hover:bg-[#05D182]/40 border border-[#05D182]/40'
                      }`}
                    >
                      {u.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
