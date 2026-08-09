import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { motion, AnimatePresence } from 'framer-motion';
import { Zap, User, Mail, Lock, ArrowRight, ArrowLeft, Check } from 'lucide-react';
export default function RegisterPage() {
  useEffect(() => {
    document.title = 'Create Account - Forge';
  }, []);
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuthStore();

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    age: '',
    gender: 'MALE',
    heightCm: '',
    currentWeightKg: '',
    goalWeightKg: '',
    activityLevel: 'MODERATELY_ACTIVE',
    fitnessGoal: 'FAT_LOSS',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (step === 1) {
      setStep(2);
      return;
    }
    setLoading(true);
    try {
      const payload = {
        ...formData,
        age: parseInt(formData.age),
        heightCm: parseFloat(formData.heightCm),
        currentWeightKg: parseFloat(formData.currentWeightKg),
        goalWeightKg: parseFloat(formData.goalWeightKg),
      };
      const res = await authApi.register(payload);
      if (res.success) {
        login(res.data.token, res.data.user);
        toast.success('Welcome to Forge!');
        navigate('/dashboard');
      } else toast.error(res.message || 'Registration failed');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{ background: 'var(--bg)' }}
    >
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="w-full max-w-md"
      >
        <div className="text-center mb-6">
          <div className="inline-flex items-center gap-2.5 mb-4">
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center"
              style={{ background: 'var(--primary)' }}
            >
              <Zap size={16} className="text-white" />
            </div>
            <span className="text-lg font-semibold text-white">Forge</span>
          </div>
          <h1 className="text-xl font-bold mb-1" style={{ color: 'var(--text)' }}>
            {step === 1 ? 'Create your account' : 'Set up your profile'}
          </h1>
          <p className="text-sm" style={{ color: 'var(--text-muted)' }}>
            {step === 1
              ? 'Start your AI-powered fitness journey'
              : 'Help our AI understand your baseline'}
          </p>
        </div>

        {/* Progress bar */}
        <div className="flex gap-2 mb-6">
          {[1, 2].map((s) => (
            <div
              key={s}
              className="flex-1 h-1 rounded-full overflow-hidden"
              style={{ background: 'rgba(255,255,255,0.04)' }}
            >
              <motion.div
                className="h-full rounded-full"
                style={{ background: 'var(--primary)' }}
                initial={{ width: '0%' }}
                animate={{ width: step >= s ? '100%' : '0%' }}
                transition={{ duration: 0.3 }}
              />
            </div>
          ))}
        </div>

        {/* Step labels */}
        <div className="flex justify-between mb-6 text-xs" style={{ color: 'var(--text-muted)' }}>
          <span
            style={{
              color: step >= 1 ? 'var(--text-secondary)' : 'var(--text-muted)',
              fontWeight: step >= 1 ? 500 : 400,
            }}
          >
            Account Details
          </span>
          <span
            style={{
              color: step >= 2 ? 'var(--text-secondary)' : 'var(--text-muted)',
              fontWeight: step >= 2 ? 500 : 400,
            }}
          >
            Fitness Profile
          </span>
        </div>

        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border)',
            borderRadius: '14px',
            padding: '1.5rem',
          }}
        >
          <form onSubmit={handleSubmit}>
            <AnimatePresence mode="wait">
              {step === 1 && (
                <motion.div
                  key="step1"
                  initial={{ opacity: 0, x: -12 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -12 }}
                  className="flex flex-col gap-4"
                >
                  <div>
                    <label className="label">Full Name</label>
                    <div className="relative">
                      <User
                        size={14}
                        className="absolute left-3 top-1/2 -translate-y-1/2"
                        style={{ color: 'var(--text-muted)' }}
                      />
                      <input
                        type="text"
                        name="name"
                        required
                        value={formData.name}
                        onChange={handleChange}
                        className="input"
                        style={{ paddingLeft: '34px' }}
                        placeholder="John Doe"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="label">Email</label>
                    <div className="relative">
                      <Mail
                        size={14}
                        className="absolute left-3 top-1/2 -translate-y-1/2"
                        style={{ color: 'var(--text-muted)' }}
                      />
                      <input
                        type="email"
                        name="email"
                        required
                        value={formData.email}
                        onChange={handleChange}
                        className="input"
                        style={{ paddingLeft: '34px' }}
                        placeholder="you@example.com"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="label">Password</label>
                    <div className="relative">
                      <Lock
                        size={14}
                        className="absolute left-3 top-1/2 -translate-y-1/2"
                        style={{ color: 'var(--text-muted)' }}
                      />
                      <input
                        type="password"
                        name="password"
                        required
                        minLength={6}
                        value={formData.password}
                        onChange={handleChange}
                        className="input"
                        style={{ paddingLeft: '34px' }}
                        placeholder="Min. 6 characters"
                      />
                    </div>
                  </div>
                  <motion.button
                    type="submit"
                    whileHover={{ scale: 1.01 }}
                    whileTap={{ scale: 0.99 }}
                    className="mt-1 w-full py-2.5 rounded-lg text-sm font-medium flex items-center justify-center gap-2 btn-primary"
                  >
                    Next <ArrowRight size={15} />
                  </motion.button>
                </motion.div>
              )}
              {step === 2 && (
                <motion.div
                  key="step2"
                  initial={{ opacity: 0, x: 12 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: 12 }}
                  className="flex flex-col gap-4"
                >
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="label">Age</label>
                      <input
                        type="number"
                        name="age"
                        required
                        value={formData.age}
                        onChange={handleChange}
                        className="input"
                        placeholder="25"
                      />
                    </div>
                    <div>
                      <label className="label">Gender</label>
                      <select
                        name="gender"
                        value={formData.gender}
                        onChange={handleChange}
                        className="input"
                      >
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                      </select>
                    </div>
                    <div>
                      <label className="label">Height (cm)</label>
                      <input
                        type="number"
                        name="heightCm"
                        required
                        step="0.1"
                        value={formData.heightCm}
                        onChange={handleChange}
                        className="input"
                        placeholder="175"
                      />
                    </div>
                    <div>
                      <label className="label">Current Weight</label>
                      <input
                        type="number"
                        name="currentWeightKg"
                        required
                        step="0.1"
                        value={formData.currentWeightKg}
                        onChange={handleChange}
                        className="input"
                        placeholder="75 kg"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="label">Goal Weight</label>
                    <input
                      type="number"
                      name="goalWeightKg"
                      required
                      step="0.1"
                      value={formData.goalWeightKg}
                      onChange={handleChange}
                      className="input"
                      placeholder="70 kg"
                    />
                  </div>
                  <div>
                    <label className="label">Activity Level</label>
                    <select
                      name="activityLevel"
                      value={formData.activityLevel}
                      onChange={handleChange}
                      className="input"
                    >
                      <option value="SEDENTARY">Sedentary</option>
                      <option value="LIGHTLY_ACTIVE">Lightly Active</option>
                      <option value="MODERATELY_ACTIVE">Moderately Active</option>
                      <option value="VERY_ACTIVE">Very Active</option>
                      <option value="EXTRA_ACTIVE">Extra Active</option>
                    </select>
                  </div>
                  <div>
                    <label className="label">Fitness Goal</label>
                    <select
                      name="fitnessGoal"
                      value={formData.fitnessGoal}
                      onChange={handleChange}
                      className="input"
                    >
                      <option value="FAT_LOSS">Fat Loss</option>
                      <option value="MUSCLE_GAIN">Muscle Gain</option>
                      <option value="MAINTENANCE">Maintenance</option>
                      <option value="ENDURANCE">Endurance</option>
                    </select>
                  </div>
                  <div className="flex gap-2 mt-1">
                    <motion.button
                      type="button"
                      onClick={() => setStep(1)}
                      whileHover={{ scale: 1.01 }}
                      whileTap={{ scale: 0.99 }}
                      className="flex-1 py-2.5 rounded-lg text-sm font-medium btn-secondary"
                    >
                      <ArrowLeft size={14} /> Back
                    </motion.button>
                    <motion.button
                      type="submit"
                      disabled={loading}
                      whileHover={{ scale: loading ? 1 : 1.01 }}
                      whileTap={{ scale: loading ? 1 : 0.99 }}
                      className="flex-[2] py-2.5 rounded-lg text-sm font-medium btn-primary"
                    >
                      {loading ? (
                        <div className="w-4 h-4 rounded-full border-2 border-white border-t-transparent animate-spin" />
                      ) : (
                        <>
                          <Check size={14} /> Create Account
                        </>
                      )}
                    </motion.button>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </form>
        </div>

        {step === 1 && (
          <p className="text-center mt-5 text-sm" style={{ color: 'var(--text-muted)' }}>
            Already have an account?{' '}
            <Link to="/login" className="font-medium" style={{ color: 'var(--primary-light)' }}>
              Sign in
            </Link>
          </p>
        )}
      </motion.div>
    </div>
  );
}
