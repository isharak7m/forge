import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Activity, PieChart, TrendingUp, Settings, LogOut, LayoutDashboard, Zap, User } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { motion, AnimatePresence } from 'framer-motion';

const Layout = ({ children }: { children: React.ReactNode }) => {
  const location = useLocation();
  const { user, logout } = useAuthStore();

  const pageTitleMap: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/nutrition': 'Nutrition',
    '/workouts': 'Workouts',
    '/metrics': 'Metrics',
    '/profile': 'Profile',
  };
  const pageTitle = pageTitleMap[location.pathname] || 'Forge';

  const links = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/nutrition', icon: PieChart, label: 'Nutrition' },
    { to: '/workouts', icon: Activity, label: 'Workouts' },
    { to: '/metrics', icon: TrendingUp, label: 'Metrics' },
    { to: '/profile', icon: Settings, label: 'Profile' },
  ];

  return (
    <div className="flex min-h-screen" style={{ background: 'var(--bg)' }}>
      {/* Sidebar */}
      <motion.nav
        initial={{ x: -60, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.4 }}
        className="w-56 flex-col hidden md:flex fixed h-full z-10"
        style={{
          background: 'rgba(255, 255, 255, 0.92)',
          backdropFilter: 'blur(20px)',
          borderRight: '1px solid var(--border)',
        }}
      >
        <div className="p-5 pb-4">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: 'var(--primary)' }}>
              <Zap size={16} className="text-white" />
            </div>
            <span className="text-base font-semibold text-white" style={{ letterSpacing: '-0.02em' }}>Forge</span>
          </div>
        </div>

        <div className="flex-1 px-2.5 flex flex-col gap-0.5">
          {links.map((link) => {
            const Icon = link.icon;
            const isActive = location.pathname === link.to;
            return (
              <Link key={link.to} to={link.to}>
                <div
                  className="flex items-center gap-2.5 px-3 py-2 rounded-lg transition-all"
                  style={{
                    background: isActive ? 'hsla(var(--hue-primary), 60%, 50%, 0.08)' : 'transparent',
                    color: isActive ? 'var(--primary-light)' : 'var(--text-muted)',
                  }}
                >
                  <Icon size={16} />
                  <span style={{ fontSize: '0.8125rem', fontWeight: isActive ? 500 : 400 }}>{link.label}</span>
                </div>
              </Link>
            );
          })}
        </div>

        <div className="p-3 m-2.5 rounded-xl" style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)' }}>
          <div className="flex items-center gap-2.5 mb-2.5">
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-semibold text-white flex-shrink-0"
              style={{ background: 'var(--primary)' }}
            >
              {user?.name?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm text-white truncate" style={{ fontWeight: 500 }}>{user?.name}</p>
              <p className="text-xs truncate" style={{ color: 'var(--text-muted)' }}>{user?.email}</p>
            </div>
          </div>
          <button
            onClick={logout}
            className="w-full flex items-center justify-center gap-1.5 px-3 py-1.5 text-xs rounded-lg transition-all"
            style={{ color: '#f87171', background: 'rgba(239,68,68,0.06)', border: '1px solid rgba(239,68,68,0.08)' }}
          >
            <LogOut size={12} />
            Sign Out
          </button>
        </div>
      </motion.nav>

      {/* Main */}
      <div className="flex-1 md:ml-56 flex flex-col min-h-screen">
        <header
          className="h-12 flex items-center px-6 sticky top-0 z-10"
          style={{
            background: 'rgba(255, 255, 255, 0.85)',
            backdropFilter: 'blur(12px)',
            borderBottom: '1px solid var(--border)',
          }}
        >
          <h2 style={{ fontSize: '0.9375rem', fontWeight: 600, color: 'var(--text)', letterSpacing: '-0.01em' }}>
            {pageTitle}
          </h2>
          <div className="ml-auto flex items-center gap-2">
            <div className="w-1.5 h-1.5 rounded-full" style={{ background: 'var(--accent)' }} />
            <span className="text-xs" style={{ color: 'var(--text-muted)' }}>Live</span>
          </div>
        </header>

        <main className="flex-1 px-4 md:px-6 py-4 md:py-5 max-w-7xl w-full mx-auto" style={{ paddingBottom: 'calc(5.5rem + var(--safe-area-bottom, 0px))' }}>
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.2 }}
            >
              {children}
            </motion.div>
          </AnimatePresence>
        </main>
      </div>

      {/* Mobile Bottom Nav */}
      <motion.nav
        initial={{ y: 40 }}
        animate={{ y: 0 }}
        className="md:hidden fixed bottom-0 left-0 right-0 flex justify-around items-center z-20"
        style={{
          background: 'rgba(255, 255, 255, 0.94)',
          backdropFilter: 'blur(16px)',
          borderTop: '1px solid var(--border)',
          paddingBottom: 'var(--safe-area-bottom, 0px)',
          paddingTop: '0.375rem',
          paddingLeft: '0.25rem',
          paddingRight: '0.25rem',
        }}
      >
        {links.map((link) => {
          const Icon = link.icon;
          const isActive = location.pathname === link.to;
          return (
            <Link key={link.to} to={link.to} className="flex flex-col items-center gap-0.5 px-2 py-1 rounded-lg flex-1"
              style={{ color: isActive ? 'var(--primary-light)' : 'var(--text-muted)' }}
            >
              <Icon size={20} />
              <span className="text-[10px]" style={{ fontWeight: 500 }}>{link.label}</span>
            </Link>
          );
        })}
      </motion.nav>
    </div>
  );
};

export default Layout;
