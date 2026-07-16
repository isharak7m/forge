import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import { Zap, Mail, Lock, ArrowRight, Activity, TrendingUp, Brain, Shield } from 'lucide-react';
export default function LoginPage() {
  useEffect(() => { document.title = 'Sign In - Forge'; }, []);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuthStore();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await authApi.login({ email, password });
      if (res.success) {
        login(res.data.token, res.data.user);
        toast.success('Welcome back!');
        navigate('/dashboard');
      } else {
        toast.error(res.message || 'Login failed');
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const features = [
    { icon: Activity, label: 'Smart Analytics', desc: 'Real-time macro & volume tracking' },
    { icon: TrendingUp, label: 'AI Predictions', desc: 'Forecast progress using ML models' },
    { icon: Brain, label: 'Neural Insights', desc: 'Personalized fitness intelligence' },
    { icon: Shield, label: 'Plateau Detection', desc: 'Break stagnation with AI guidance' },
  ];

  return (
    <div className="min-h-screen flex" style={{ background: 'var(--bg)' }}>
      {/* Left panel */}
      <motion.div
        initial={{ opacity: 0, x: -20 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.4 }}
        className="hidden lg:flex flex-col justify-between w-[420px] flex-shrink-0 p-10 relative overflow-hidden"
        style={{ background: 'rgba(255, 255, 255, 0.92)', borderRight: '1px solid var(--border)' }}
      >
        <div className="relative z-10">
          <div className="flex items-center gap-2.5 mb-12">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: 'var(--primary)' }}>
              <Zap size={16} className="text-white" />
            </div>
            <span className="text-lg font-semibold text-white" style={{ letterSpacing: '-0.02em' }}>Forge</span>
          </div>

          <h2 className="text-2xl font-bold leading-tight mb-3" style={{ color: 'var(--text)', letterSpacing: '-0.02em' }}>
            Your AI-Powered<br />
            <span className="gradient-text">Fitness Intelligence</span>
          </h2>
          <p className="text-sm mb-10" style={{ color: 'var(--text-muted)' }}>
            Stop guessing. Start optimizing. Transform raw fitness data into actionable intelligence.
          </p>

          <div className="flex flex-col gap-2.5">
            {features.map(({ icon: Icon, label, desc }, i) => (
              <motion.div
                key={label}
                initial={{ opacity: 0, x: -8 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 + i * 0.05 }}
                className="flex items-center gap-3 p-3 rounded-lg"
                style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)' }}
              >
                <div className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0" style={{ background: 'hsla(var(--hue-primary), 55%, 50%, 0.08)' }}>
                  <Icon size={15} style={{ color: 'var(--primary-light)' }} />
                </div>
                <div>
                  <p className="text-sm font-medium text-white">{label}</p>
                  <p className="text-xs" style={{ color: 'var(--text-muted)' }}>{desc}</p>
                </div>
              </motion.div>
            ))}
          </div>
        </div>

        <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
          &copy; 2025 Forge &middot; All rights reserved
        </p>
      </motion.div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-6">
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="w-full max-w-sm"
        >
          <div className="flex items-center gap-2.5 mb-8 lg:hidden">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: 'var(--primary)' }}>
              <Zap size={16} className="text-white" />
            </div>
            <span className="text-lg font-semibold text-white">Forge</span>
          </div>

          <h1 className="text-xl font-bold mb-1" style={{ color: 'var(--text)' }}>Welcome back</h1>
          <p className="text-sm mb-6" style={{ color: 'var(--text-muted)' }}>Sign in to access your fitness dashboard</p>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label className="label">Email</label>
              <div className="relative">
                <Mail size={14} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: 'var(--text-muted)' }} />
                <input type="email" className="w-full rounded-lg text-sm input" style={{ padding: '0.625rem 0.875rem 0.625rem 2.25rem' }} placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)} required />
              </div>
            </div>
            <div>
              <label className="label">Password</label>
              <div className="relative">
                <Lock size={14} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: 'var(--text-muted)' }} />
                <input type="password" className="w-full rounded-lg text-sm input" style={{ padding: '0.625rem 0.875rem 0.625rem 2.25rem' }} placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;" value={password} onChange={e => setPassword(e.target.value)} required />
              </div>
            </div>
            <motion.button
              whileHover={{ scale: loading ? 1 : 1.01 }}
              whileTap={{ scale: loading ? 1 : 0.99 }}
              type="submit"
              disabled={loading}
              className="mt-1 w-full py-2.5 rounded-lg text-sm font-medium flex items-center justify-center gap-2 btn-primary"
            >
              {loading ? <div className="w-4 h-4 rounded-full border-2 border-white border-t-transparent animate-spin" /> : <><span>Sign in</span><ArrowRight size={15} /></>}
            </motion.button>
            </form>

          <div className="mt-6 pt-5" style={{ borderTop: '1px solid var(--border)' }}>
            <p className="text-center text-sm" style={{ color: 'var(--text-muted)' }}>
              Don&apos;t have an account?{' '}
              <Link to="/register" className="font-medium" style={{ color: 'var(--primary-light)' }}>Create account</Link>
            </p>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
