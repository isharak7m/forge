import { useState, useEffect, useMemo } from 'react';
import { analyticsApi } from '../api/analytics';
import { DailyDashboard } from '../types';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { Activity, Flame, Utensils, Moon, Dumbbell, Droplets } from 'lucide-react';
import { RadialBarChart, RadialBar, ResponsiveContainer, PolarAngleAxis } from 'recharts';

function calculateTDEE(user: { age?: number; gender?: string; heightCm?: number; currentWeightKg?: number; activityLevel?: string }): number {
  if (!user.age || !user.heightCm || !user.currentWeightKg) return 2500;
  const bmr = user.gender === 'female'
    ? 10 * user.currentWeightKg + 6.25 * user.heightCm - 5 * user.age - 161
    : 10 * user.currentWeightKg + 6.25 * user.heightCm - 5 * user.age + 5;
  const factors: Record<string, number> = { sedentary: 1.2, light: 1.375, moderate: 1.55, active: 1.725, extra_active: 1.9 };
  return Math.round(bmr * (factors[user.activityLevel || ''] || 1.55));
}

export default function DashboardPage() {
  const { user } = useAuthStore();
  const [data, setData] = useState<DailyDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const today = format(new Date(), 'yyyy-MM-dd');
  const targetCalories = useMemo(() => calculateTDEE(user || {}), [user]);
  const proteinTarget = Math.round((user?.currentWeightKg || 80) * 2);
  const fatTarget = Math.round((targetCalories * 0.25) / 9);
  const carbTarget = Math.round((targetCalories - proteinTarget * 4 - fatTarget * 9) / 4);

  useEffect(() => { document.title = 'Dashboard - FitMind'; }, []);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await analyticsApi.getDailyDashboard(today);
      if (res.success) {
        setData(res.data);
      }
    } catch (error) {
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="card-glass h-32 skeleton"></div>
        ))}
        <div className="card-glass h-80 skeleton md:col-span-2"></div>
        <div className="card-glass h-80 skeleton md:col-span-2"></div>
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="flex flex-col gap-6">
      {/* Header Stats */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
        <div className="card-glass flex items-center p-6 gap-4">
          <div className="p-3 bg-[rgba(59,130,246,0.1)] rounded-lg">
            <Flame className="text-blue-400" size={24} />
          </div>
          <div>
            <p className="text-sm text-secondary">Calories Intake</p>
            <h3 className="text-2xl font-bold">{data.caloriesConsumed.toFixed(0)} <span className="text-sm text-secondary font-normal">kcal</span></h3>
          </div>
        </div>
        
        <div className="card-glass flex items-center p-6 gap-4">
          <div className="p-3 bg-[rgba(239,68,68,0.1)] rounded-lg">
            <Flame className="text-red-400" size={24} />
          </div>
          <div>
            <p className="text-sm text-secondary">Calories Burned</p>
            <h3 className="text-2xl font-bold">{data.caloriesBurned.toFixed(0)} <span className="text-sm text-secondary font-normal">kcal</span></h3>
          </div>
        </div>

        <div className="card-glass flex items-center p-6 gap-4">
          <div className="p-3 bg-[rgba(16,185,129,0.1)] rounded-lg">
            <Dumbbell className="text-green-400" size={24} />
          </div>
          <div>
            <p className="text-sm text-secondary">Workouts</p>
            <h3 className="text-2xl font-bold">{data.workoutsCompleted} <span className="text-sm text-secondary font-normal">sessions</span></h3>
          </div>
        </div>

        <div className="card-glass flex items-center p-6 gap-4">
          <div className="p-3 bg-[rgba(139,92,246,0.1)] rounded-lg">
            <Moon className="text-purple-400" size={24} />
          </div>
          <div>
            <p className="text-sm text-secondary">Sleep</p>
            <h3 className="text-2xl font-bold">{data.sleepHours.toFixed(1)} <span className="text-sm text-secondary font-normal">hours</span></h3>
          </div>
        </div>

        <div className="card-glass flex items-center p-6 gap-4">
          <div className="p-3 bg-[rgba(56,189,248,0.1)] rounded-lg">
            <Droplets className="text-sky-400" size={24} />
          </div>
          <div>
            <p className="text-sm text-secondary">Hydration</p>
            <h3 className="text-2xl font-bold">{data.waterLiters.toFixed(1)} <span className="text-sm text-secondary font-normal">L</span></h3>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Calorie Gauges */}
        <div className="card-glass col-span-1 flex flex-col items-center justify-center min-h-[300px]">
          <h3 className="font-semibold w-full text-left mb-4">Daily Calorie Goal</h3>
          <div className="flex gap-6 justify-center w-full">
            <div className="relative w-40 h-40">
              <ResponsiveContainer width="100%" height="100%">
                <RadialBarChart cx="50%" cy="50%" innerRadius="70%" outerRadius="100%" barSize={12} data={[{ name: 'Consumed', value: Math.min(data.caloriesConsumed, targetCalories), fill: '#3b82f6' }]} startAngle={210} endAngle={-30}>
                  <PolarAngleAxis type="number" domain={[0, targetCalories]} angleAxisId={0} tick={false} />
                  <RadialBar background={{ fill: 'rgba(255,255,255,0.05)' }} dataKey="value" cornerRadius={10} />
                </RadialBarChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-xl font-bold text-blue-400">{data.caloriesConsumed.toFixed(0)}</span>
                <span className="text-xs text-secondary">consumed</span>
              </div>
            </div>
            <div className="relative w-40 h-40">
              <ResponsiveContainer width="100%" height="100%">
                <RadialBarChart cx="50%" cy="50%" innerRadius="70%" outerRadius="100%" barSize={12} data={[{ name: 'Burned', value: Math.min(data.caloriesBurned, targetCalories), fill: '#ef4444' }]} startAngle={210} endAngle={-30}>
                  <PolarAngleAxis type="number" domain={[0, targetCalories]} angleAxisId={0} tick={false} />
                  <RadialBar background={{ fill: 'rgba(255,255,255,0.05)' }} dataKey="value" cornerRadius={10} />
                </RadialBarChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-xl font-bold text-red-400">{data.caloriesBurned.toFixed(0)}</span>
                <span className="text-xs text-secondary">burned</span>
              </div>
            </div>
          </div>
          <div className="w-full flex justify-between mt-4 text-sm text-secondary px-4">
             <span>0</span>
             <span>{targetCalories} kcal</span>
          </div>
        </div>

        {/* Nutrition Macros */}
        <div className="card-glass col-span-2">
           <div className="flex justify-between items-center mb-6">
             <h3 className="font-semibold flex items-center gap-2"><Utensils size={18}/> Macros Progress</h3>
             <span className="badge badge-green">On Track</span>
           </div>
           
           <div className="flex flex-col gap-6">
              <div>
                 <div className="flex justify-between text-sm mb-2">
                    <span>Protein</span>
                     <span className="text-green-400">{data.nutritionSummary.totalProtein.toFixed(0)}g / {proteinTarget}g</span>
                  </div>
                  <div className="h-2 bg-secondary rounded-full overflow-hidden">
                     <div className="h-full bg-green-400 rounded-full" style={{ width: `${Math.min(100, (data.nutritionSummary.totalProtein / proteinTarget) * 100)}%` }}></div>
                 </div>
              </div>
              
              <div>
                 <div className="flex justify-between text-sm mb-2">
                    <span>Carbs</span>
                     <span className="text-orange-400">{data.nutritionSummary.totalCarbs.toFixed(0)}g / {carbTarget}g</span>
                  </div>
                  <div className="h-2 bg-secondary rounded-full overflow-hidden">
                     <div className="h-full bg-orange-400 rounded-full" style={{ width: `${Math.min(100, (data.nutritionSummary.totalCarbs / carbTarget) * 100)}%` }}></div>
                 </div>
              </div>

              <div>
                 <div className="flex justify-between text-sm mb-2">
                    <span>Fat</span>
                     <span className="text-purple-400">{data.nutritionSummary.totalFat.toFixed(0)}g / {fatTarget}g</span>
                  </div>
                  <div className="h-2 bg-secondary rounded-full overflow-hidden">
                     <div className="h-full bg-purple-400 rounded-full" style={{ width: `${Math.min(100, (data.nutritionSummary.totalFat / fatTarget) * 100)}%` }}></div>
                 </div>
              </div>
           </div>
        </div>
      </div>
      
      {/* Workouts */}
      <div className="card-glass">
         <h3 className="font-semibold mb-4 flex items-center gap-2"><Activity size={18}/> Today's Workouts</h3>
         {data.workouts.length === 0 ? (
            <div className="text-center py-8 text-secondary border border-dashed border-[rgba(48,54,61,0.8)] rounded-lg">
               No workouts logged today.
            </div>
         ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
               {data.workouts.map(w => (
                 <div key={w.id} className="p-4 bg-[rgba(0,0,0,0.2)] rounded-lg border border-[rgba(255,255,255,0.05)]">
                   <div className="flex justify-between mb-2">
                      <h4 className="font-semibold text-blue-400">{w.name}</h4>
                      <span className="text-sm text-secondary">{w.durationMinutes} min</span>
                   </div>
                   <p className="text-sm text-secondary mb-3">{w.exercises.length} exercises • {w.totalVolume.toFixed(0)} kg volume</p>
                   <div className="flex flex-wrap gap-2">
                     {w.exercises.slice(0, 3).map(e => (
                        <span key={e.id} className="text-xs bg-secondary px-2 py-1 rounded">{e.exerciseName}</span>
                     ))}
                     {w.exercises.length > 3 && <span className="text-xs bg-secondary px-2 py-1 rounded">+{w.exercises.length - 3} more</span>}
                   </div>
                 </div>
               ))}
            </div>
         )}
      </div>
    </div>
  );
}
