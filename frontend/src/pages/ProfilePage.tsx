import { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import { userApi } from '../api/user';
import { User } from '../types';
import toast from 'react-hot-toast';
import { User as UserIcon, Save } from 'lucide-react';

export default function ProfilePage() {
  const { user, setUser } = useAuthStore();
  const [loading, setLoading] = useState(false);

  useEffect(() => { document.title = 'Profile - FitMind'; }, []);

  const [formData, setFormData] = useState<Partial<User>>({
    name: user?.name || '',
    age: user?.age || 0,
    heightCm: user?.heightCm || 0,
    currentWeightKg: user?.currentWeightKg || 0,
    goalWeightKg: user?.goalWeightKg || 0,
    gender: user?.gender || 'male',
    activityLevel: user?.activityLevel || 'moderate',
    fitnessGoal: user?.fitnessGoal || 'FAT_LOSS'
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await userApi.updateProfile({
         ...formData,
         age: Number(formData.age),
         heightCm: Number(formData.heightCm),
         currentWeightKg: Number(formData.currentWeightKg),
         goalWeightKg: Number(formData.goalWeightKg)
      });
      if (res.success) {
        toast.success('Profile updated successfully');
        setUser(res.data);
      }
    } catch (e) {
      toast.error('Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  if (!user) return null;

  return (
    <div className="max-w-3xl mx-auto flex flex-col gap-6">
      <div className="card-glass flex items-center gap-6 p-8">
        <div className="w-24 h-24 rounded-full bg-gradient-blue flex items-center justify-center text-4xl font-bold text-white shadow-glow">
          {user.name.charAt(0)}
        </div>
        <div>
          <h2 className="text-3xl font-bold mb-1">{user.name}</h2>
          <p className="text-secondary mb-2">{user.email}</p>
          <div className="flex gap-2">
            <span className="badge badge-blue">Member since {new Date(user.createdAt).getFullYear()}</span>
            <span className="badge badge-purple">{user.role}</span>
          </div>
        </div>
      </div>

      <div className="card-glass p-8">
        <h3 className="font-semibold mb-6 flex items-center gap-2 border-b border-border-color pb-4">
          <UserIcon size={18} className="text-blue-400"/> Profile Settings
        </h3>
        
        <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="form-group mb-0">
            <label className="label">Full Name</label>
            <input type="text" name="name" className="input" value={formData.name} onChange={handleChange} required />
          </div>
          
          <div className="form-group mb-0">
            <label className="label">Age</label>
            <input type="number" name="age" className="input" value={formData.age} onChange={handleChange} required />
          </div>
          
          <div className="form-group mb-0">
            <label className="label">Height (cm)</label>
            <input type="number" name="heightCm" className="input" value={formData.heightCm} onChange={handleChange} required />
          </div>
          
          <div className="form-group mb-0">
            <label className="label">Current Weight (kg)</label>
            <input type="number" name="currentWeightKg" className="input" value={formData.currentWeightKg} onChange={handleChange} required />
          </div>

          <div className="form-group mb-0">
            <label className="label">Goal Weight (kg)</label>
            <input type="number" name="goalWeightKg" className="input" value={formData.goalWeightKg} onChange={handleChange} required />
          </div>

          <div className="form-group mb-0">
            <label className="label">Gender</label>
            <select name="gender" className="input" value={formData.gender} onChange={handleChange}>
              <option value="male">Male</option>
              <option value="female">Female</option>
            </select>
          </div>

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
            <label className="label">Fitness Goal</label>
            <select name="fitnessGoal" className="input" value={formData.fitnessGoal} onChange={handleChange}>
              <option value="FAT_LOSS">Fat Loss</option>
              <option value="MUSCLE_GAIN">Muscle Gain</option>
              <option value="MAINTENANCE">Maintenance</option>
              <option value="ENDURANCE">Endurance</option>
            </select>
          </div>

          <div className="md:col-span-2 pt-4 border-t border-border-color flex justify-end">
             <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Saving...' : <><Save size={18}/> Save Changes</>}
             </button>
          </div>
        </form>
      </div>
    </div>
  );
}
