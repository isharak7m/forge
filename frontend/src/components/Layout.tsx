import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Activity, Brain, PieChart, TrendingUp, Settings, LogOut, LayoutDashboard } from 'lucide-react';
import { useAuthStore } from '../store/authStore';

const Layout = ({ children }: { children: React.ReactNode }) => {
  const location = useLocation();
  const { user, logout } = useAuthStore();

  const pageTitleMap: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/nutrition': 'Nutrition Tracker',
    '/workouts': 'Workout Log',
    '/metrics': 'Body Metrics',
    '/ai': 'AI Fitness Hub',
    '/profile': 'Profile Settings',
  };
  const pageTitle = pageTitleMap[location.pathname] || 'FitMind';

  const links = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/nutrition', icon: PieChart, label: 'Nutrition' },
    { to: '/workouts', icon: Activity, label: 'Workouts' },
    { to: '/metrics', icon: TrendingUp, label: 'Metrics' },
    { to: '/ai', icon: Brain, label: 'AI Hub' },
    { to: '/profile', icon: Settings, label: 'Profile' },
  ];

  return (
    <div className="flex min-h-screen bg-primary">
      {/* Sidebar */}
      <div className="w-64 bg-[#161b22] border-r border-[rgba(48,54,61,0.8)] flex-col hidden md:flex fixed h-full z-10">
        <div className="p-6 flex items-center gap-3">
          <div className="p-2 bg-gradient-blue rounded-lg">
            <Brain size={24} className="text-white" />
          </div>
          <h1 className="text-xl font-bold text-transparent bg-clip-text bg-gradient-blue">FitMind</h1>
        </div>

        <nav className="flex-1 px-4 py-6 flex flex-col gap-2">
          {links.map((link) => {
            const Icon = link.icon;
            const isActive = location.pathname === link.to;
            return (
              <Link
                key={link.to}
                to={link.to}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all ${
                  isActive 
                    ? 'bg-[rgba(59,130,246,0.1)] text-blue-400 border border-[rgba(59,130,246,0.2)]' 
                    : 'text-secondary hover:bg-[rgba(255,255,255,0.05)] hover:text-primary'
                }`}
              >
                <Icon size={20} className={isActive ? 'text-blue-400' : ''} />
                <span className="font-medium">{link.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-[rgba(48,54,61,0.8)]">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-full bg-gradient-green flex items-center justify-center font-bold text-white">
              {user?.name?.charAt(0) || 'U'}
            </div>
            <div className="flex-1 overflow-hidden">
              <p className="text-sm font-medium text-primary truncate">{user?.name}</p>
              <p className="text-xs text-secondary truncate">{user?.email}</p>
            </div>
          </div>
          <button 
            onClick={logout}
            className="w-full flex items-center justify-center gap-2 px-4 py-2 text-sm text-red-400 hover:bg-[rgba(239,68,68,0.1)] rounded-lg transition-all"
          >
            <LogOut size={16} />
            Logout
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 md:ml-64 flex flex-col min-h-screen">
        <header className="h-16 border-b border-[rgba(48,54,61,0.8)] bg-[rgba(22,27,34,0.8)] backdrop-blur-md sticky top-0 z-10 flex items-center px-6">
           <h2 className="text-lg font-semibold text-primary capitalize">
             {pageTitle}
           </h2>
        </header>
        <main className="flex-1 p-6 pb-24 md:pb-6 max-w-7xl w-full mx-auto animate-fade-in">
          {children}
        </main>
      </div>

      {/* Mobile Bottom Nav */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-secondary border-t border-[rgba(48,54,61,0.8)] flex justify-around items-center py-2 z-20">
        {links.map((link) => {
          const Icon = link.icon;
          const isActive = location.pathname === link.to;
          return (
            <Link
              key={link.to}
              to={link.to}
              className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition-all ${
                isActive ? 'text-blue-400' : 'text-secondary hover:text-primary'
              }`}
            >
              <Icon size={20} />
              <span className="text-xs font-medium">{link.label}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );
};

export default Layout;
