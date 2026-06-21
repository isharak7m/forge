import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { Brain, Mail, Lock, ArrowRight, Activity, TrendingUp } from 'lucide-react';

export default function LoginPage() {
  useEffect(() => { document.title = 'Login - FitMind'; }, []);
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
      toast.error(error.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-primary p-4 md:p-8">
      <div className="w-full max-w-5xl grid md:grid-cols-2 gap-8 items-center">
        
        {/* Left Side: Branding */}
        <div className="hidden md:flex flex-col gap-6 p-8 animate-fade-in">
          <div className="flex items-center gap-3 mb-8">
            <div className="p-3 bg-gradient-blue rounded-xl shadow-glow">
              <Brain size={40} className="text-white" />
            </div>
            <h1 className="text-4xl font-bold text-transparent bg-clip-text bg-gradient-blue">FitMind</h1>
          </div>
          
          <h2 className="text-3xl font-bold leading-tight">
            AI-Powered Fitness <br/> Intelligence
          </h2>
          <p className="text-secondary text-lg">
            Stop tracking. Start predicting. FitMind analyzes your habits to eliminate plateaus and optimize results.
          </p>
          
          <div className="grid grid-cols-2 gap-4 mt-8">
             <div className="card-glass p-4">
                <Activity className="text-blue-400 mb-2" />
                <h3 className="font-semibold mb-1">Smart Analytics</h3>
                <p className="text-sm text-secondary">Real-time macro and volume tracking.</p>
             </div>
             <div className="card-glass p-4">
                <TrendingUp className="text-green-400 mb-2" />
                <h3 className="font-semibold mb-1">AI Predictions</h3>
                <p className="text-sm text-secondary">Forecast your progress using ML.</p>
             </div>
          </div>
        </div>

        {/* Right Side: Form */}
        <div className="card-glass p-8 w-full max-w-md mx-auto relative overflow-hidden animate-fade-in">
          {/* Decorative glow */}
          <div className="absolute -top-20 -right-20 w-64 h-64 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20"></div>
          <div className="absolute -bottom-20 -left-20 w-64 h-64 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20"></div>
          
          <div className="relative z-10">
            <div className="md:hidden flex items-center justify-center gap-2 mb-8">
              <Brain size={28} className="text-blue-500" />
              <h1 className="text-2xl font-bold">FitMind</h1>
            </div>

            <h2 className="text-2xl font-bold mb-2">Welcome Back</h2>
            <p className="text-secondary mb-8">Enter your credentials to access your dashboard.</p>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <div className="form-group mb-0">
                <label className="label">Email Address</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-muted" size={18} />
                  <input
                    type="email"
                    className="input pl-10"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>
              
              <div className="form-group mb-2">
                <label className="label">Password</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-muted" size={18} />
                  <input
                    type="password"
                    className="input pl-10"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </div>

              <button 
                type="submit" 
                className="btn btn-primary w-full mt-4"
                disabled={loading}
              >
                {loading ? 'Authenticating...' : (
                  <>Sign In <ArrowRight size={18} /></>
                )}
              </button>
            </form>

            <p className="text-center mt-8 text-sm text-secondary">
              Don't have an account?{' '}
              <Link to="/register" className="text-blue-400 hover:text-blue-300 font-medium">
                Create one now
              </Link>
            </p>
          </div>
        </div>

      </div>
    </div>
  );
}
