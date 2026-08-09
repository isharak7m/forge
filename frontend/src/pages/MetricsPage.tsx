import { useState, useEffect } from 'react';
import { metricApi, SleepEntry, WaterEntry } from '../api/metrics';
import toast from 'react-hot-toast';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Scale,
  Moon,
  Droplets,
  TrendingUp,
  TrendingDown,
  Target,
  BarChart3,
  Activity,
} from 'lucide-react';
import { api } from '../api/axios';
import { ApiResponse, User } from '../types';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Line,
  ComposedChart,
  Legend,
  Cell,
} from 'recharts';

export default function MetricsPage() {
  const [user, setUser] = useState<User | null>(null);
  const [sleepHistory, setSleepHistory] = useState<SleepEntry[]>([]);
  const [waterHistory, setWaterHistory] = useState<WaterEntry[]>([]);
  const [sleepAvg, setSleepAvg] = useState(0);
  const [waterAvg, setWaterAvg] = useState(0);
  const [sleepQualityAvg, setSleepQualityAvg] = useState(0);

  const [showWeightForm, setShowWeightForm] = useState(false);
  const [weightInput, setWeightInput] = useState('');
  const [showSleepForm, setShowSleepForm] = useState(false);
  const [sleepInput, setSleepInput] = useState({
    durationHours: '',
    date: new Date().toISOString().split('T')[0],
    qualityScore: '',
  });
  const [showWaterForm, setShowWaterForm] = useState(false);
  const [waterInput, setWaterInput] = useState({
    amountMl: '',
    date: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    document.title = 'Metrics - Forge';
    loadMetrics();
  }, []);

  const loadMetrics = async () => {
    try {
      const [u, sleepRes, waterRes] = await Promise.all([
        api.get<ApiResponse<User>>('/users/me').then((r) => r.data.data),
        metricApi.getSleepHistory(14),
        metricApi.getWaterHistory(14),
      ]);
      setUser(u);
      setSleepHistory(sleepRes.data);
      setWaterHistory(waterRes.data);
      if (sleepRes.data.length > 0) {
        const avgH = sleepRes.data.reduce((s, e) => s + e.durationHours, 0) / sleepRes.data.length;
        const avgQ =
          sleepRes.data.reduce((s, e) => s + (e.qualityScore || 0), 0) / sleepRes.data.length;
        setSleepAvg(Math.round(avgH * 10) / 10);
        setSleepQualityAvg(Math.round(avgQ * 10) / 10);
      }
      if (waterRes.data.length > 0) {
        const avgW = waterRes.data.reduce((s, e) => s + e.amountMl, 0) / waterRes.data.length;
        setWaterAvg(Math.round(avgW));
      }
    } catch {
      toast.error('Failed to load metrics');
    }
  };

  const handleWeightSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseFloat(weightInput);
    if (isNaN(val) || val < 30 || val > 300) {
      toast.error('Enter a valid weight (30-300 kg)');
      return;
    }
    try {
      const res = await api.put<ApiResponse<User>>('/users/me', { currentWeightKg: val });
      setUser(res.data.data);
      toast.success('Weight updated!');
      setShowWeightForm(false);
      setWeightInput('');
    } catch {
      toast.error('Failed to update weight');
    }
  };

  const handleSleepSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const h = parseFloat(sleepInput.durationHours);
    if (isNaN(h) || h < 0 || h > 24) {
      toast.error('Enter valid hours (0-24)');
      return;
    }
    try {
      await metricApi.recordSleep({
        date: sleepInput.date,
        durationHours: h,
        qualityScore: sleepInput.qualityScore ? parseInt(sleepInput.qualityScore) : undefined,
      });
      toast.success('Sleep logged!');
      setShowSleepForm(false);
      setSleepInput({
        durationHours: '',
        date: new Date().toISOString().split('T')[0],
        qualityScore: '',
      });
      loadMetrics();
    } catch {
      toast.error('Failed to log sleep');
    }
  };

  const handleWaterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const ml = parseFloat(waterInput.amountMl);
    if (isNaN(ml) || ml < 0 || ml > 15000) {
      toast.error('Enter valid amount (0-15000 ml)');
      return;
    }
    try {
      await metricApi.recordWater({ date: waterInput.date, amountMl: ml });
      toast.success('Water logged!');
      setShowWaterForm(false);
      setWaterInput({ amountMl: '', date: new Date().toISOString().split('T')[0] });
      loadMetrics();
    } catch {
      toast.error('Failed to log water');
    }
  };

  const goalProgress =
    user && user.goalWeightKg && user.currentWeightKg
      ? Math.min(
          100,
          Math.round(
            (Math.abs(
              user.currentWeightKg -
                (user.goalWeightKg > user.currentWeightKg
                  ? user.currentWeightKg * 1.1
                  : user.currentWeightKg * 0.9),
            ) /
              Math.abs(
                user.goalWeightKg -
                  (user.goalWeightKg > user.currentWeightKg
                    ? user.currentWeightKg * 1.1
                    : user.currentWeightKg * 0.9),
              )) *
              100,
          ),
        )
      : 0;

  const sleepChartData = (() => {
    const map = new Map<string, { date: string; hours: number; quality: number }>();
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const key = d.toISOString().split('T')[0];
      map.set(key, { date: key, hours: 0, quality: 0 });
    }
    sleepHistory
      .filter((s) => {
        const diff = (Date.now() - new Date(s.date).getTime()) / 86400000;
        return diff >= 0 && diff < 7;
      })
      .forEach((s) => {
        if (map.has(s.date)) {
          map.set(s.date, { date: s.date, hours: s.durationHours, quality: s.qualityScore || 0 });
        }
      });
    return Array.from(map.values());
  })();

  const waterChartData = (() => {
    const map = new Map<string, { date: string; ml: number }>();
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const key = d.toISOString().split('T')[0];
      map.set(key, { date: key, ml: 0 });
    }
    waterHistory
      .filter((w) => {
        const diff = (Date.now() - new Date(w.date).getTime()) / 86400000;
        return diff >= 0 && diff < 7;
      })
      .forEach((w) => {
        if (map.has(w.date)) {
          map.set(w.date, { date: w.date, ml: w.amountMl });
        }
      });
    return Array.from(map.values());
  })();

  const formatDate = (d: string) => {
    const dt = new Date(d);
    return dt.toLocaleDateString('en', { weekday: 'short' });
  };

  const weightDiff =
    user && user.currentWeightKg && user.goalWeightKg
      ? (user.currentWeightKg - user.goalWeightKg).toFixed(1)
      : '0';

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h2
          className="text-xl font-bold"
          style={{ color: 'var(--text)', letterSpacing: '-0.02em' }}
        >
          Health Metrics
        </h2>
        <span
          className="text-xs px-2.5 py-1 rounded-full"
          style={{ background: 'rgba(6, 214, 160, .1)', color: 'var(--primary)' }}
        >
          {sleepHistory.length + waterHistory.length} records
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* ═══════ WEIGHT CARD ═══════ */}
        <div className="card">
          <div className="flex justify-between items-center mb-4">
            <h3
              className="text-sm font-medium flex items-center gap-2"
              style={{ color: 'var(--text)' }}
            >
              <Scale size={16} style={{ color: 'var(--primary)' }} /> Weight
            </h3>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="btn btn-primary"
              style={{ padding: '4px 12px', fontSize: '12px' }}
              onClick={() => setShowWeightForm(!showWeightForm)}
            >
              Update
            </motion.button>
          </div>

          <AnimatePresence>
            {showWeightForm && (
              <motion.form
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                onSubmit={handleWeightSubmit}
                className="flex gap-2 items-end mb-3"
              >
                <div className="flex-1">
                  <label className="label">New weight (kg)</label>
                  <input
                    type="number"
                    step="0.1"
                    className="input"
                    required
                    value={weightInput}
                    onChange={(e) => setWeightInput(e.target.value)}
                  />
                </div>
                <motion.button
                  type="submit"
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className="btn btn-primary"
                  style={{ padding: '7px 14px', fontSize: '12px' }}
                >
                  Save
                </motion.button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  style={{ padding: '7px 10px', fontSize: '12px' }}
                  onClick={() => setShowWeightForm(false)}
                >
                  X
                </button>
              </motion.form>
            )}
          </AnimatePresence>

          <div className="flex items-end gap-3 mb-3">
            <div>
              <span className="text-2xl font-bold" style={{ color: 'var(--text)' }}>
                {user?.currentWeightKg ?? '—'}
              </span>
              <span className="text-sm ml-1" style={{ color: 'var(--text-muted)' }}>
                kg
              </span>
            </div>
            {user?.goalWeightKg && (
              <div
                className="flex items-center gap-1 text-xs mb-1"
                style={{ color: 'var(--text-muted)' }}
              >
                <Target size={12} />
                <span>Goal {user.goalWeightKg} kg</span>
              </div>
            )}
          </div>

          {user?.currentWeightKg && user?.goalWeightKg && (
            <>
              <div className="flex items-center gap-1.5 mb-2">
                {parseFloat(weightDiff) > 0 ? (
                  <TrendingUp size={14} style={{ color: '#f97316' }} />
                ) : (
                  <TrendingDown size={14} style={{ color: '#22c55e' }} />
                )}
                <span
                  className="text-xs"
                  style={{ color: parseFloat(weightDiff) > 0 ? '#f97316' : '#22c55e' }}
                >
                  {parseFloat(weightDiff) > 0
                    ? `${weightDiff} kg above goal`
                    : `${Math.abs(parseFloat(weightDiff))} kg below goal`}
                </span>
              </div>
              <div
                className="w-full h-2 rounded-full overflow-hidden"
                style={{ background: 'rgba(255,255,255,0.04)' }}
              >
                <motion.div
                  className="h-full rounded-full"
                  style={{
                    background:
                      Math.abs(parseFloat(weightDiff)) < 2
                        ? 'var(--primary)'
                        : parseFloat(weightDiff) > 0
                          ? '#f97316'
                          : '#22c55e',
                  }}
                  initial={{ width: 0 }}
                  animate={{
                    width: `${Math.min(100, Math.abs(parseFloat(weightDiff) / 5) * 100)}%`,
                  }}
                  transition={{ duration: 0.6 }}
                />
              </div>
              <p className="text-xs mt-1.5" style={{ color: 'var(--text-muted)' }}>
                {user.heightCm
                  ? `BMI ${(user.currentWeightKg / (user.heightCm / 100) ** 2).toFixed(1)}`
                  : ''}
              </p>
            </>
          )}
        </div>

        {/* ═══════ SLEEP CARD ═══════ */}
        <div className="card lg:col-span-2">
          <div className="flex justify-between items-center mb-4">
            <h3
              className="text-sm font-medium flex items-center gap-2"
              style={{ color: 'var(--text)' }}
            >
              <Moon size={16} style={{ color: 'var(--secondary)' }} /> Sleep
              <span className="text-xs ml-1" style={{ color: 'var(--text-muted)' }}>
                — {sleepAvg}h avg · Quality {sleepQualityAvg}/10
              </span>
            </h3>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="btn btn-primary"
              style={{ padding: '4px 12px', fontSize: '12px' }}
              onClick={() => setShowSleepForm(!showSleepForm)}
            >
              Log
            </motion.button>
          </div>

          <AnimatePresence>
            {showSleepForm && (
              <motion.form
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                onSubmit={handleSleepSubmit}
                className="flex flex-wrap gap-2 items-end mb-3"
              >
                <div>
                  <label className="label">Date</label>
                  <input
                    type="date"
                    className="input"
                    value={sleepInput.date}
                    onChange={(e) => setSleepInput({ ...sleepInput, date: e.target.value })}
                  />
                </div>
                <div>
                  <label className="label">Hours</label>
                  <input
                    type="number"
                    step="0.5"
                    min="0"
                    max="24"
                    className="input"
                    required
                    value={sleepInput.durationHours}
                    onChange={(e) =>
                      setSleepInput({ ...sleepInput, durationHours: e.target.value })
                    }
                    style={{ width: 80 }}
                  />
                </div>
                <div>
                  <label className="label">Quality (1-10)</label>
                  <input
                    type="number"
                    min="1"
                    max="10"
                    className="input"
                    value={sleepInput.qualityScore}
                    onChange={(e) => setSleepInput({ ...sleepInput, qualityScore: e.target.value })}
                    style={{ width: 80 }}
                  />
                </div>
                <motion.button
                  type="submit"
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className="btn btn-primary"
                  style={{ padding: '6px 12px', fontSize: '12px' }}
                >
                  Save
                </motion.button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  style={{ padding: '6px 10px', fontSize: '12px' }}
                  onClick={() => setShowSleepForm(false)}
                >
                  X
                </button>
              </motion.form>
            )}
          </AnimatePresence>

          <div style={{ height: 140 }}>
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart
                data={sleepChartData}
                margin={{ top: 4, right: 4, left: -16, bottom: 0 }}
              >
                <XAxis
                  dataKey="date"
                  tickFormatter={formatDate}
                  tick={{ fontSize: 10, fill: 'var(--text-muted)' }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  yAxisId="left"
                  domain={[0, 10]}
                  tick={{ fontSize: 10, fill: 'var(--text-muted)' }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis yAxisId="right" orientation="right" domain={[0, 10]} hide />
                <Tooltip
                  contentStyle={{
                    background: '#FFFFFF',
                    border: '1px solid var(--border)',
                    borderRadius: 8,
                    fontSize: 12,
                  }}
                  labelFormatter={(d: string) =>
                    new Date(d).toLocaleDateString('en', {
                      weekday: 'long',
                      month: 'short',
                      day: 'numeric',
                    })
                  }
                />
                <Bar
                  yAxisId="left"
                  dataKey="hours"
                  fill="var(--secondary)"
                  radius={[3, 3, 0, 0]}
                  name="Hours"
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="quality"
                  stroke="var(--primary-light)"
                  strokeWidth={2}
                  dot={{ r: 3, fill: 'var(--primary-light)' }}
                  name="Quality"
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* ═══════ WATER CARD ═══════ */}
        <div className="card lg:col-span-2">
          <div className="flex justify-between items-center mb-4">
            <h3
              className="text-sm font-medium flex items-center gap-2"
              style={{ color: 'var(--text)' }}
            >
              <Droplets size={16} style={{ color: '#38bdf8' }} /> Water
              <span className="text-xs ml-1" style={{ color: 'var(--text-muted)' }}>
                — {(waterAvg / 1000).toFixed(1)}L avg / day
              </span>
            </h3>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="btn btn-primary"
              style={{ padding: '4px 12px', fontSize: '12px' }}
              onClick={() => setShowWaterForm(!showWaterForm)}
            >
              Log
            </motion.button>
          </div>

          <AnimatePresence>
            {showWaterForm && (
              <motion.form
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                onSubmit={handleWaterSubmit}
                className="flex flex-wrap gap-2 items-end mb-3"
              >
                <div>
                  <label className="label">Date</label>
                  <input
                    type="date"
                    className="input"
                    value={waterInput.date}
                    onChange={(e) => setWaterInput({ ...waterInput, date: e.target.value })}
                  />
                </div>
                <div>
                  <label className="label">Amount (ml)</label>
                  <input
                    type="number"
                    min="0"
                    className="input"
                    required
                    value={waterInput.amountMl}
                    onChange={(e) => setWaterInput({ ...waterInput, amountMl: e.target.value })}
                    style={{ width: 120 }}
                  />
                </div>
                <motion.button
                  type="submit"
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className="btn btn-primary"
                  style={{ padding: '6px 12px', fontSize: '12px' }}
                >
                  Save
                </motion.button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  style={{ padding: '6px 10px', fontSize: '12px' }}
                  onClick={() => setShowWaterForm(false)}
                >
                  X
                </button>
              </motion.form>
            )}
          </AnimatePresence>

          <div style={{ height: 140 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={waterChartData} margin={{ top: 4, right: 4, left: -16, bottom: 0 }}>
                <XAxis
                  dataKey="date"
                  tickFormatter={formatDate}
                  tick={{ fontSize: 10, fill: 'var(--text-muted)' }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  domain={[0, 'auto']}
                  tick={{ fontSize: 10, fill: 'var(--text-muted)' }}
                  axisLine={false}
                  tickLine={false}
                  tickFormatter={(v: number) => `${(v / 1000).toFixed(1)}L`}
                />
                <Tooltip
                  contentStyle={{
                    background: '#FFFFFF',
                    border: '1px solid var(--border)',
                    borderRadius: 8,
                    fontSize: 12,
                  }}
                  labelFormatter={(d: string) =>
                    new Date(d).toLocaleDateString('en', {
                      weekday: 'long',
                      month: 'short',
                      day: 'numeric',
                    })
                  }
                  formatter={(value: number) => [`${(value / 1000).toFixed(1)}L`, 'Water']}
                />
                <Bar dataKey="ml" fill="#38bdf8" radius={[3, 3, 0, 0]} name="Water">
                  {waterChartData.map((entry, i) => (
                    <Cell
                      key={i}
                      fill={entry.ml >= 2500 ? '#22c55e' : entry.ml >= 1500 ? '#38bdf8' : '#f97316'}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* ═══════ NUTRITION CARD ═══════ */}
        <div className="card">
          <h3
            className="text-sm font-medium flex items-center gap-2 mb-3"
            style={{ color: 'var(--text)' }}
          >
            <Activity size={16} style={{ color: 'var(--primary)' }} /> Quick Stats
          </h3>
          <div className="flex flex-col gap-2.5">
            <div
              className="flex justify-between items-center px-3 py-2 rounded-lg"
              style={{ background: 'rgba(255,255,255,0.02)' }}
            >
              <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                Height
              </span>
              <span className="text-sm font-medium" style={{ color: 'var(--text)' }}>
                {user?.heightCm ?? '—'} cm
              </span>
            </div>
            <div
              className="flex justify-between items-center px-3 py-2 rounded-lg"
              style={{ background: 'rgba(255,255,255,0.02)' }}
            >
              <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                Goal
              </span>
              <span className="text-sm font-medium" style={{ color: 'var(--text)' }}>
                {user?.fitnessGoal?.replace('_', ' ') ?? '—'}
              </span>
            </div>
            <div
              className="flex justify-between items-center px-3 py-2 rounded-lg"
              style={{ background: 'rgba(255,255,255,0.02)' }}
            >
              <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                Activity
              </span>
              <span className="text-sm font-medium" style={{ color: 'var(--text)' }}>
                {user?.activityLevel?.replace('_', ' ') ?? '—'}
              </span>
            </div>
            <div
              className="flex justify-between items-center px-3 py-2 rounded-lg"
              style={{ background: 'rgba(255,255,255,0.02)' }}
            >
              <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                Age
              </span>
              <span className="text-sm font-medium" style={{ color: 'var(--text)' }}>
                {user?.age ?? '—'}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
