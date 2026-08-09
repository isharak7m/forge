import { useState, useEffect, useMemo } from 'react';
import { analyticsApi } from '../api/analytics';
import { DailyDashboard } from '../types';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { Activity, Flame, Utensils, Moon, Dumbbell, Droplets, TrendingUp, Zap } from 'lucide-react';
import { RadialBarChart, RadialBar, ResponsiveContainer, PolarAngleAxis } from 'recharts';
import { motion, AnimatePresence } from 'framer-motion';

function calculateTDEE(user: {
  age?: number;
  gender?: string;
  heightCm?: number;
  currentWeightKg?: number;
  activityLevel?: string;
}): number {
  if (!user.age || !user.heightCm || !user.currentWeightKg) return 2500;
  const bmr =
    user.gender === 'female'
      ? 10 * user.currentWeightKg + 6.25 * user.heightCm - 5 * user.age - 161
      : 10 * user.currentWeightKg + 6.25 * user.heightCm - 5 * user.age + 5;
  const factors: Record<string, number> = {
    sedentary: 1.2,
    light: 1.375,
    moderate: 1.55,
    active: 1.725,
    extra_active: 1.9,
  };
  return Math.round(bmr * (factors[user.activityLevel || ''] || 1.55));
}

interface StatCardProps {
  icon: React.ElementType;
  label: string;
  value: string;
  unit: string;
  color: string;
  bg: string;
  sub?: string;
}

function StatCard({ icon: Icon, label, value, unit, color, bg, sub }: StatCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ y: -2 }}
      transition={{ duration: 0.25 }}
      className="flex items-center gap-3"
      style={{
        background: 'var(--bg-card)',
        border: '1px solid var(--border)',
        borderRadius: '12px',
        padding: '16px 18px',
        transition: 'all 0.2s ease',
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = 'var(--border-hover)';
        (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-lg)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = 'var(--border)';
        (e.currentTarget as HTMLElement).style.boxShadow = 'none';
      }}
    >
      <div
        style={{
          width: '40px',
          height: '40px',
          borderRadius: '10px',
          background: bg,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
        }}
      >
        <Icon size={18} style={{ color }} />
      </div>
      <div className="flex-1 min-w-0">
        <p
          style={{
            fontSize: '11px',
            color: 'var(--text-muted)',
            marginBottom: '1px',
            fontWeight: 500,
          }}
        >
          {label}
        </p>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: '3px' }}>
          <span
            style={{
              fontSize: '20px',
              fontWeight: 600,
              color: 'var(--text)',
              letterSpacing: '-0.02em',
            }}
          >
            {value}
          </span>
          <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{unit}</span>
        </div>
        {sub && (
          <p style={{ fontSize: '10px', color: 'var(--text-muted)', marginTop: '1px' }}>{sub}</p>
        )}
      </div>
    </motion.div>
  );
}

function MacroBar({
  label,
  current,
  target,
  color,
}: {
  label: string;
  current: number;
  target: number;
  color: string;
}) {
  const pct = Math.min(100, (current / target) * 100);
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
        <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{label}</span>
        <span style={{ fontSize: '12px', color, fontWeight: 600 }}>
          {current.toFixed(0)}g{' '}
          <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>/ {target}g</span>
        </span>
      </div>
      <div
        style={{
          height: '4px',
          background: 'rgba(255,255,255,0.04)',
          borderRadius: '99px',
          overflow: 'hidden',
        }}
      >
        <motion.div
          initial={{ width: 0 }}
          animate={{ width: `${pct}%` }}
          transition={{ duration: 0.8, ease: 'easeOut', delay: 0.2 }}
          style={{ height: '100%', background: color, borderRadius: '99px' }}
        />
      </div>
    </div>
  );
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

  useEffect(() => {
    document.title = 'Dashboard - Forge';
  }, []);
  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await analyticsApi.getDailyDashboard(today);
      if (res.success) setData(res.data);
    } catch {
      toast.error('Failed to load dashboard');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="skeleton" style={{ height: '76px', borderRadius: '12px' }} />
          ))}
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="skeleton" style={{ height: '260px', borderRadius: '14px' }} />
          <div
            className="skeleton md:col-span-2"
            style={{ height: '260px', borderRadius: '14px' }}
          />
        </div>
      </div>
    );
  }

  if (!data) return null;

  const netCalories = data.caloriesConsumed - data.caloriesBurned;
  const calPct = Math.min(100, (data.caloriesConsumed / targetCalories) * 100);

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={today}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}
      >
        {/* Welcome strip */}
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border)',
            borderRadius: '14px',
            padding: '14px 20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div
              style={{
                width: '34px',
                height: '34px',
                borderRadius: '10px',
                background: 'var(--primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 600,
                color: 'white',
                fontSize: '14px',
              }}
            >
              {user?.name?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div>
              <p style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text)' }}>
                Good{' '}
                {new Date().getHours() < 12
                  ? 'morning'
                  : new Date().getHours() < 17
                    ? 'afternoon'
                    : 'evening'}
                , {user?.name?.split(' ')[0]}!
              </p>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                {format(new Date(), 'EEEE, MMMM do')}
              </p>
            </div>
          </div>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              fontSize: '12px',
              color: 'var(--text-muted)',
            }}
          >
            <Zap size={14} style={{ color: 'var(--warm)' }} />
            <span>Goal: {user?.fitnessGoal?.replace('_', ' ') || 'Active'}</span>
          </div>
        </div>

        {/* Stats grid */}
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          <StatCard
            icon={Flame}
            label="Calories"
            value={data.caloriesConsumed.toFixed(0)}
            unit="kcal"
            color="var(--primary)"
            bg="hsla(var(--hue-primary), 55%, 50%, 0.08)"
            sub={`${calPct.toFixed(0)}% of ${targetCalories}`}
          />
          <StatCard
            icon={TrendingUp}
            label="Burned"
            value={data.caloriesBurned.toFixed(0)}
            unit="kcal"
            color="#ef4444"
            bg="rgba(239,68,68,0.08)"
            sub={`Net: ${netCalories.toFixed(0)} kcal`}
          />
          <StatCard
            icon={Dumbbell}
            label="Workouts"
            value={String(data.workoutsCompleted)}
            unit="sessions"
            color="var(--accent)"
            bg="rgba(16,185,129,0.08)"
          />
          <StatCard
            icon={Moon}
            label="Sleep"
            value={data.sleepHours.toFixed(1)}
            unit="hrs"
            color="var(--secondary)"
            bg="hsla(var(--hue-secondary), 50%, 50%, 0.08)"
          />
          <StatCard
            icon={Droplets}
            label="Water"
            value={data.waterLiters.toFixed(1)}
            unit="L"
            color="#38bdf8"
            bg="rgba(56,189,248,0.08)"
          />
        </div>

        {/* Middle row */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Calorie gauge */}
          <div
            className="flex flex-col items-center"
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border)',
              borderRadius: '14px',
              padding: '20px',
            }}
          >
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                width: '100%',
                marginBottom: '14px',
              }}
            >
              <h3 style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text)' }}>
                Calorie Goal
              </h3>
              <span
                style={{
                  fontSize: '10px',
                  color: 'var(--text-muted)',
                  background: 'rgba(255,255,255,0.03)',
                  padding: '2px 8px',
                  borderRadius: '99px',
                  border: '1px solid var(--border)',
                }}
              >
                {targetCalories} target
              </span>
            </div>
            <div className="flex gap-6 justify-center w-full">
              {[
                {
                  label: 'Consumed',
                  value: data.caloriesConsumed,
                  max: targetCalories,
                  color: 'var(--primary)',
                },
                {
                  label: 'Burned',
                  value: data.caloriesBurned,
                  max: targetCalories,
                  color: '#ef4444',
                },
              ].map(({ label, value, max, color }) => (
                <div key={label} style={{ position: 'relative', width: '110px', height: '110px' }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <RadialBarChart
                      cx="50%"
                      cy="50%"
                      innerRadius="70%"
                      outerRadius="100%"
                      barSize={8}
                      data={[{ name: label, value: Math.min(value, max), fill: color }]}
                      startAngle={210}
                      endAngle={-30}
                    >
                      <PolarAngleAxis
                        type="number"
                        domain={[0, max]}
                        angleAxisId={0}
                        tick={false}
                      />
                      <RadialBar
                        background={{ fill: 'rgba(255,255,255,0.03)' }}
                        dataKey="value"
                        cornerRadius={8}
                      />
                    </RadialBarChart>
                  </ResponsiveContainer>
                  <div
                    style={{
                      position: 'absolute',
                      inset: 0,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <span style={{ fontSize: '15px', fontWeight: 600, color }}>
                      {value.toFixed(0)}
                    </span>
                    <span style={{ fontSize: '9px', color: 'var(--text-muted)' }}>
                      {label.toLowerCase()}
                    </span>
                  </div>
                </div>
              ))}
            </div>
            <div
              style={{
                marginTop: '14px',
                width: '100%',
                padding: '8px 12px',
                borderRadius: '8px',
                background: netCalories > 0 ? 'rgba(239,68,68,0.06)' : 'rgba(16,185,129,0.06)',
                border: `1px solid ${netCalories > 0 ? 'rgba(239,68,68,0.1)' : 'rgba(16,185,129,0.1)'}`,
                textAlign: 'center',
              }}
            >
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 500,
                  color: netCalories > 0 ? '#f87171' : '#34d399',
                }}
              >
                {netCalories > 0
                  ? `+${netCalories.toFixed(0)} kcal surplus`
                  : `${Math.abs(netCalories).toFixed(0)} kcal deficit`}
              </span>
            </div>
          </div>

          {/* Macros */}
          <div
            className="md:col-span-2"
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border)',
              borderRadius: '14px',
              padding: '20px',
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '20px',
              }}
            >
              <h3
                style={{
                  fontSize: '13px',
                  fontWeight: 500,
                  color: 'var(--text)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                }}
              >
                <Utensils size={14} style={{ color: 'var(--text-muted)' }} /> Macros
              </h3>
              <span
                style={{
                  fontSize: '10px',
                  color: 'var(--accent)',
                  fontWeight: 500,
                  background: 'rgba(16,185,129,0.06)',
                  padding: '2px 8px',
                  borderRadius: '99px',
                  border: '1px solid rgba(16,185,129,0.12)',
                }}
              >
                Today
              </span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <MacroBar
                label="Protein"
                current={data.nutritionSummary.totalProtein}
                target={proteinTarget}
                color="var(--accent)"
              />
              <MacroBar
                label="Carbs"
                current={data.nutritionSummary.totalCarbs}
                target={carbTarget}
                color="var(--warm)"
              />
              <MacroBar
                label="Fat"
                current={data.nutritionSummary.totalFat}
                target={fatTarget}
                color="var(--secondary)"
              />
            </div>
            <div className="grid grid-cols-3 gap-3 mt-5">
              {[
                {
                  label: 'Protein',
                  val: data.nutritionSummary.totalProtein,
                  color: 'var(--accent)',
                },
                { label: 'Carbs', val: data.nutritionSummary.totalCarbs, color: 'var(--warm)' },
                { label: 'Fat', val: data.nutritionSummary.totalFat, color: 'var(--secondary)' },
              ].map(({ label, val, color }) => (
                <div
                  key={label}
                  style={{
                    textAlign: 'center',
                    padding: '10px',
                    borderRadius: '8px',
                    background: 'rgba(255,255,255,0.02)',
                    border: '1px solid var(--border)',
                  }}
                >
                  <div style={{ fontSize: '18px', fontWeight: 600, color }}>
                    {val.toFixed(0)}
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 400 }}>
                      g
                    </span>
                  </div>
                  <div style={{ fontSize: '10px', color: 'var(--text-muted)', marginTop: '1px' }}>
                    {label}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Workouts */}
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border)',
            borderRadius: '14px',
            padding: '20px',
          }}
        >
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: '14px',
            }}
          >
            <h3
              style={{
                fontSize: '13px',
                fontWeight: 500,
                color: 'var(--text)',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
              }}
            >
              <Activity size={14} style={{ color: 'var(--text-muted)' }} /> Today's Workouts
            </h3>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
              {data.workouts.length} session{data.workouts.length !== 1 ? 's' : ''}
            </span>
          </div>
          {data.workouts.length === 0 ? (
            <div
              style={{
                padding: '32px',
                textAlign: 'center',
                color: 'var(--text-muted)',
                border: '1px dashed rgba(255,255,255,0.04)',
                borderRadius: '10px',
              }}
            >
              <Dumbbell size={24} style={{ margin: '0 auto 8px', opacity: 0.3 }} />
              <p style={{ fontSize: '13px' }}>No workouts logged today</p>
              <p style={{ fontSize: '12px', marginTop: '2px', color: 'var(--text-muted)' }}>
                Head to Workouts to log your session
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {data.workouts.map((w, i) => (
                <div
                  key={w.id}
                  style={{
                    padding: '14px',
                    borderRadius: '10px',
                    background: 'rgba(255,255,255,0.02)',
                    border: '1px solid var(--border)',
                  }}
                >
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      marginBottom: '6px',
                    }}
                  >
                    <h4 style={{ fontWeight: 500, color: 'var(--primary-light)' }}>{w.name}</h4>
                    <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                      {w.durationMinutes} min
                    </span>
                  </div>
                  <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '8px' }}>
                    {w.exercises.length} exercises &middot; {w.totalVolume.toFixed(0)} kg volume
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                    {w.exercises.slice(0, 3).map((e) => (
                      <span
                        key={e.id}
                        style={{
                          fontSize: '11px',
                          padding: '2px 8px',
                          borderRadius: '5px',
                          background: 'rgba(255,255,255,0.03)',
                          color: 'var(--text-secondary)',
                        }}
                      >
                        {e.exerciseName}
                      </span>
                    ))}
                    {w.exercises.length > 3 && (
                      <span
                        style={{
                          fontSize: '11px',
                          padding: '2px 8px',
                          borderRadius: '5px',
                          background: 'rgba(255,255,255,0.03)',
                          color: 'var(--text-muted)',
                        }}
                      >
                        +{w.exercises.length - 3} more
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </motion.div>
    </AnimatePresence>
  );
}
