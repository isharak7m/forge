import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { Brain, User, Mail, Lock, ArrowRight, ArrowLeft } from 'lucide-react';

export default function RegisterPage() {
  useEffect(() => { document.title = 'Register - FitMind'; }, []);
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
    fitnessGoal: 'FAT_LOSS'
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
        goalWeightKg: parseFloat(formData.goalWeightKg)
      };
      const res = await authApi.register(payload);
      if (res.success) {
        login(res.data.token, res.data.user);
        toast.success('Registration successful!');
        navigate('/dashboard');
      } else {
        toast.error(res.message || 'Registration failed');
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-primary p-4">
      <div className="card-glass p-8 w-full max-w-lg relative overflow-hidden animate-fade-in">
        <div className="absolute top-0 left-0 w-full h-1 bg-secondary">
           <div className={`h-full bg-gradient-blue transition-all duration-300 ${step === 1 ? 'w-1/2' : 'w-full'}`}></div>
        </div>

        <div className="flex justify-center mb-6 mt-4">
          <div className="p-3 bg-gradient-blue rounded-xl shadow-glow">
             <Brain size={32} className="text-white" />
          </div>
        </div>
        
        <h2 className="text-2xl font-bold text-center mb-2">
          {step === 1 ? 'Create Account' : 'Fitness Profile'}
        </h2>
        <p className="text-secondary text-center mb-8">
          {step === 1 ? 'Start your AI fitness journey.' : 'Help the AI understand your baseline.'}
        </p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {step === 1 && (
            <div className="animate-fade-in">
              <div className="form-group">
                <label className="label">Full Name</label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 text-muted" size={18} />
                  <input type="text" name="name" className="input pl-10" required value={formData.name} onChange={handleChange} />
                </div>
              </div>
              <div className="form-group">
                <label className="label">Email</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-muted" size={18} />
                  <input type="email" name="email" className="input pl-10" required value={formData.email} onChange={handleChange} />
                </div>
              </div>
              <div className="form-group">
                <label className="label">Password</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-muted" size={18} />
                  <input type="password" name="password" className="input pl-10" required minLength={6} value={formData.password} onChange={handleChange} />
                </div>
              </div>
              
              <button type="submit" className="btn btn-primary w-full mt-4">
                Next Step <ArrowRight size={18} />
              </button>
            </div>
          )}

          {step === 2 && (
            <div className="animate-fade-in">
              <div className="grid grid-cols-2 gap-4">
                <div className="form-group mb-0">
                  <label className="label">Age</label>
                  <input type="number" name="age" className="input" required value={formData.age} onChange={handleChange} />
                </div>
                <div className="form-group mb-0">
                  <label className="label">Gender</label>
                  <select name="gender" className="input" value={formData.gender} onChange={handleChange}>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                  </select>
                </div>
                <div className="form-group mb-0">
                  <label className="label">Height (cm)</label>
                  <input type="number" name="heightCm" className="input" required step="0.1" value={formData.heightCm} onChange={handleChange} />
                </div>
                <div className="form-group mb-0">
                  <label className="label">Current Weight (kg)</label>
                  <input type="number" name="currentWeightKg" className="input" required step="0.1" value={formData.currentWeightKg} onChange={handleChange} />
                </div>
              </div>
              
              <div className="form-group mt-4">
                <label className="label">Goal Weight (kg)</label>
                <input type="number" name="goalWeightKg" className="input" required step="0.1" value={formData.goalWeightKg} onChange={handleChange} />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="form-group mb-0">
                  <label className="label">Activity Level</label>
                  <select name="activityLevel" className="input" value={formData.activityLevel} onChange={handleChange}>
                    <option value="SEDENTARY">Sedentary</option>
                    <option value="LIGHTLY_ACTIVE">Lightly Active</option>
                    <option value="MODERATELY_ACTIVE">Moderately Active</option>
                    <option value="VERY_ACTIVE">Very Active</option>
                    <option value="EXTRA_ACTIVE">Extra Active</option>
                  </select>
                </div>
                <div className="form-group mb-0">
                  <label className="label">Goal</label>
                  <select name="fitnessGoal" className="input" value={formData.fitnessGoal} onChange={handleChange}>
                    <option value="FAT_LOSS">Fat Loss</option>
                    <option value="MUSCLE_GAIN">Muscle Gain</option>
                    <option value="MAINTENANCE">Maintenance</option>
                    <option value="ENDURANCE">Endurance</option>
                  </select>
                </div>
              </div>

              <div className="flex gap-4 mt-6">
                <button type="button" onClick={() => setStep(1)} className="btn btn-secondary flex-1">
                  <ArrowLeft size={18} /> Back
                </button>
                <button type="submit" disabled={loading} className="btn btn-primary flex-1">
                  {loading ? 'Creating...' : 'Complete Registration'}
                </button>
              </div>
            </div>
          )}
        </form>

        {step === 1 && (
          <p className="text-center mt-6 text-sm text-secondary">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-400 hover:text-blue-300 font-medium">
              Sign In
            </Link>
          </p>
        )}
      </div>
    </div>
  );
}
