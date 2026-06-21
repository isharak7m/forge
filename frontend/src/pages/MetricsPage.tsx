import { useState, useEffect } from 'react';
import { metricApi } from '../api/metrics';
import { TrendPoint } from '../types';
import toast from 'react-hot-toast';
import { Activity, Scale, HeartPulse } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export default function MetricsPage() {
  const [weightTrend, setWeightTrend] = useState<TrendPoint[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    weightKg: '',
    sleepHours: '',
    recoveryScore: '',
    waterLiters: ''
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await metricApi.getWeightTrend();
      if (res.success) {
        // Format dates for chart
        const formatted = res.data.map(p => ({
           ...p,
           dateStr: new Date(p.date).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
        })).reverse(); // Oldest to newest
        setWeightTrend(formatted);
      }
    } catch (e) {
      toast.error('Failed to load metrics');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await metricApi.recordMetric({
        weightKg: formData.weightKg ? parseFloat(formData.weightKg) : null,
        sleepHours: formData.sleepHours ? parseFloat(formData.sleepHours) : null,
        recoveryScore: formData.recoveryScore ? parseInt(formData.recoveryScore) : null,
        waterLiters: formData.waterLiters ? parseFloat(formData.waterLiters) : null,
      });
      if (res.success) {
        toast.success('Metrics recorded!');
        setShowForm(false);
        setFormData({ weightKg: '', sleepHours: '', recoveryScore: '', waterLiters: '' });
        loadData();
      }
    } catch (e) {
      toast.error('Failed to save metrics');
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">Body Metrics</h2>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          <Scale size={18} /> Log Metrics
        </button>
      </div>

      {showForm && (
        <div className="card-glass animate-fade-in">
          <h3 className="font-semibold mb-4">Record Today's Metrics</h3>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="form-group mb-0">
              <label className="label">Weight (kg)</label>
              <input type="number" step="0.1" className="input" value={formData.weightKg} onChange={e => setFormData({...formData, weightKg: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Sleep (hours)</label>
              <input type="number" step="0.5" className="input" value={formData.sleepHours} onChange={e => setFormData({...formData, sleepHours: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Recovery Score (1-100)</label>
              <input type="number" min="1" max="100" className="input" value={formData.recoveryScore} onChange={e => setFormData({...formData, recoveryScore: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Water (L)</label>
              <input type="number" step="0.1" className="input" value={formData.waterLiters} onChange={e => setFormData({...formData, waterLiters: e.target.value})} />
            </div>
            <div className="md:col-span-4 flex justify-end gap-2 mt-2">
              <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary">Save Metrics</button>
            </div>
          </form>
        </div>
      )}

      <div className="card-glass">
        <h3 className="font-semibold mb-6 flex items-center gap-2">
          <Activity className="text-blue-400"/> Weight Trend
        </h3>
        {loading ? (
           <div className="h-80 skeleton"></div>
        ) : weightTrend.length < 2 ? (
           <div className="h-80 flex items-center justify-center text-secondary border border-dashed border-border-color rounded-lg">
             Not enough weight data to show a trend. Log at least two entries.
           </div>
        ) : (
           <div className="h-80 w-full">
             <ResponsiveContainer width="100%" height="100%">
               <AreaChart data={weightTrend} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                 <defs>
                   <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                     <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                     <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                   </linearGradient>
                 </defs>
                 <XAxis dataKey="dateStr" stroke="#8b949e" tick={{fill: '#8b949e', fontSize: 12}} />
                 <YAxis domain={['auto', 'auto']} stroke="#8b949e" tick={{fill: '#8b949e', fontSize: 12}} />
                 <CartesianGrid strokeDasharray="3 3" stroke="#30363d" vertical={false} />
                 <Tooltip 
                   contentStyle={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '8px' }}
                   itemStyle={{ color: '#f0f6fc' }}
                 />
                 <Area type="monotone" dataKey="value" stroke="#3b82f6" strokeWidth={3} fillOpacity={1} fill="url(#colorValue)" />
               </AreaChart>
             </ResponsiveContainer>
           </div>
        )}
      </div>
    </div>
  );
}
