import { useState, useEffect } from 'react';
import { workoutApi } from '../api/workouts';
import { WorkoutSession, PersonalRecord } from '../types';
import toast from 'react-hot-toast';
import { format, subDays } from 'date-fns';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Dumbbell, Trophy, X, Brain, Trash2 } from 'lucide-react';

interface SetEntry { weightKg: number; reps: number; rpe: string; }
interface WorkoutPrediction {
  exerciseName: string; current1RM: number;
  predicted30Days1RM: number; predicted60Days1RM: number; predicted90Days1RM: number;
  trend: string; confidence: string; methodology: string; keyFactors: string[];
}

export default function WorkoutsPage() {
  const [sessions, setSessions] = useState<WorkoutSession[]>([]);
  const [prs, setPrs] = useState<PersonalRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [predictions, setPredictions] = useState<Record<string, WorkoutPrediction>>({});
  const [predicting, setPredicting] = useState(false);
  const [showSessionForm, setShowSessionForm] = useState(false);
  const [sessionForm, setSessionForm] = useState({ name: '', date: format(new Date(), 'yyyy-MM-dd') });
  const [addingExerciseForSession, setAddingExerciseForSession] = useState<number | null>(null);
  const [category, setCategory] = useState<'STRENGTH' | 'CARDIO'>('STRENGTH');
  const [exerciseForm, setExerciseForm] = useState({
    exerciseName: '', setEntries: [{ weightKg: 0, reps: 10, rpe: '' }] as SetEntry[], duration: 0, zone: '', notes: ''
  });

  useEffect(() => { document.title = 'Workouts - Forge'; }, []);
  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const to = format(new Date(), 'yyyy-MM-dd');
      const from = format(subDays(new Date(), 30), 'yyyy-MM-dd');
      const [sessRes, prsRes] = await Promise.all([workoutApi.getSessions(from, to), workoutApi.getPRs()]);
      if (sessRes.success) setSessions(sessRes.data);
      if (prsRes.success) { setPrs(prsRes.data); loadPredictions(prsRes.data); }
    } catch { toast.error('Failed to load workouts'); }
    finally { setLoading(false); }
  };

  const loadPredictions = async (records: PersonalRecord[]) => {
    setPredicting(true);
    const results: Record<string, WorkoutPrediction> = {};
    await Promise.all(records.map(async pr => {
      try { const res = await workoutApi.predict1RM(pr.exerciseName); if (res.success) results[pr.exerciseName] = res.data; } catch {}
    }));
    setPredictions(results);
    setPredicting(false);
  };

  const handlePredictOne = async (exerciseName: string) => {
    try { const res = await workoutApi.predict1RM(exerciseName); if (res.success) setPredictions(prev => ({ ...prev, [exerciseName]: res.data })); } catch { toast.error('Prediction failed'); }
  };

  const addSetRow = () => setExerciseForm(prev => ({ ...prev, setEntries: [...prev.setEntries, { weightKg: 0, reps: prev.setEntries[0]?.reps || 10, rpe: '' }] }));
  const removeSetRow = (idx: number) => { if (exerciseForm.setEntries.length <= 1) return; setExerciseForm(prev => ({ ...prev, setEntries: prev.setEntries.filter((_, i) => i !== idx) })); };
  const updateSetRow = (idx: number, field: keyof SetEntry, value: string) => {
    setExerciseForm(prev => {
      const entries = [...prev.setEntries];
      entries[idx] = { ...entries[idx], [field]: field === 'rpe' ? value : parseFloat(value) || 0 };
      return { ...prev, setEntries: entries };
    });
  };

  const handleAddExercise = async (sessionId: number, e: React.FormEvent) => {
    e.preventDefault();
    if (!exerciseForm.exerciseName.trim()) { toast.error('Enter exercise name'); return; }
    let payload: any = { exerciseName: exerciseForm.exerciseName.trim(), category, notes: exerciseForm.notes };
    if (category === 'STRENGTH') {
      const filledSets = exerciseForm.setEntries.filter(s => s.weightKg > 0 && s.reps > 0);
      if (filledSets.length === 0) { toast.error('Add at least one set with weight and reps'); return; }
      const totalSets = filledSets.length;
      const avgReps = Math.round(filledSets.reduce((s, x) => s + x.reps, 0) / totalSets);
      const maxWeight = Math.max(...filledSets.map(s => s.weightKg));
      const rpeVals = filledSets.map(s => parseInt(s.rpe) || 0).filter(v => v > 0);
      const avgRpe = rpeVals.length > 0 ? Math.round(rpeVals.reduce((a, b) => a + b, 0) / rpeVals.length) : null;
      payload = { ...payload, sets: totalSets, reps: avgReps, weightKg: maxWeight, rpe: avgRpe };
    } else {
      if (!exerciseForm.duration || exerciseForm.duration <= 0) { toast.error('Enter duration in minutes'); return; }
      if (!exerciseForm.zone) { toast.error('Select a heart rate zone'); return; }
      payload = { ...payload, duration: exerciseForm.duration, zone: exerciseForm.zone };
    }
    try { const res = await workoutApi.addExercise(sessionId, payload); if (res.success) { toast.success('Exercise logged!'); loadData(); } } catch { toast.error('Failed to log exercise'); loadData(); }
    setAddingExerciseForSession(null);
    setExerciseForm({ exerciseName: '', setEntries: [{ weightKg: 0, reps: 10, rpe: '' }], duration: 0, zone: '', notes: '' });
  };

  const handleCreateSession = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await workoutApi.createSession({ name: sessionForm.name, date: sessionForm.date, durationMinutes: 45 });
      if (res.success) { toast.success('Session created!'); setShowSessionForm(false); setSessionForm({ name: '', date: format(new Date(), 'yyyy-MM-dd') }); loadData(); }
    } catch { toast.error('Failed to create session'); }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <div className="flex justify-between items-center">
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 600, color: 'var(--text)', letterSpacing: '-0.01em' }}>Training Log</h2>
          <p className="text-sm" style={{ color: 'var(--text-muted)' }}>Last 30 days &middot; {sessions.length} sessions</p>
        </div>
        <motion.button whileHover={{ scale: 1.01 }} whileTap={{ scale: 0.99 }} onClick={() => setShowSessionForm(true)} className="btn btn-primary">
          <Plus size={15} /> New Session
        </motion.button>
      </div>

      <AnimatePresence>
        {showSessionForm && (
          <motion.div initial={{ opacity: 0, y: -8, scale: 0.99 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, y: -8, scale: 0.99 }} className="card">
            <div className="flex justify-between items-center mb-3">
              <h3 className="text-sm font-medium" style={{ color: 'var(--text)' }}>New Workout Session</h3>
              <button onClick={() => setShowSessionForm(false)} style={{ color: 'var(--text-muted)', padding: '4px', cursor: 'pointer', background: 'none', border: 'none' }}><X size={16} /></button>
            </div>
            <form onSubmit={handleCreateSession} className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label className="label">Session Name</label>
                <input type="text" required placeholder="e.g. Push Day" className="input" value={sessionForm.name} onChange={e => setSessionForm({ ...sessionForm, name: e.target.value })} />
              </div>
              <div>
                <label className="label">Date</label>
                <input type="date" required className="input" value={sessionForm.date} onChange={e => setSessionForm({ ...sessionForm, date: e.target.value })} />
              </div>
              <div className="md:col-span-2 flex justify-end gap-2 mt-1">
                <button type="button" onClick={() => setShowSessionForm(false)} className="btn btn-secondary" style={{ padding: '8px 14px', fontSize: '13px' }}>Cancel</button>
                <button type="submit" className="btn btn-primary" style={{ padding: '8px 14px', fontSize: '13px' }}>Start Session</button>
              </div>
            </form>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="md:col-span-2 flex flex-col gap-3">
          <h3 className="text-xs font-medium flex items-center gap-1.5" style={{ color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
            <Dumbbell size={13} /> Recent Sessions
          </h3>
          {loading ? (
            <div className="skeleton" style={{ height: '120px', borderRadius: '14px' }} />
          ) : sessions.length === 0 ? (
            <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
              <Dumbbell size={28} style={{ margin: '0 auto 10px', opacity: 0.3, color: 'var(--text-muted)' }} />
              <p className="text-sm" style={{ color: 'var(--text-muted)' }}>No sessions in the last 30 days</p>
              <p className="text-xs mt-1" style={{ color: 'var(--text-muted)' }}>Click "New Session" to get started</p>
            </div>
          ) : (
            <AnimatePresence>
              {sessions.map((session, si) => (
                <motion.div key={session.id} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: si * 0.04 }} className="card" style={{ padding: 0, overflow: 'hidden' }}>
                  <div style={{ padding: '12px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)' }}>
                    <div>
                      <h4 className="text-sm font-medium" style={{ color: 'var(--primary-light)' }}>{session.name}</h4>
                      <span className="text-xs" style={{ color: 'var(--text-muted)' }}>{session.date} &middot; {session.durationMinutes || '-'} min</span>
                    </div>
                    <span className="badge badge-purple">{session.totalVolume.toFixed(0)} kg vol.</span>
                  </div>
                  <div style={{ padding: '12px 16px' }}>
                    {session.exercises && session.exercises.length > 0 ? (
                      <table style={{ width: '100%', fontSize: '13px', borderCollapse: 'collapse' }}>
                        <thead>
                          <tr style={{ color: 'var(--text-muted)', fontSize: '11px', borderBottom: '1px solid var(--border)' }}>
                            <th style={{ textAlign: 'left', paddingBottom: '6px', fontWeight: 500 }}>Exercise</th>
                            <th style={{ textAlign: 'left', paddingBottom: '6px', fontWeight: 500 }}>Details</th>
                            <th style={{ textAlign: 'left', paddingBottom: '6px', fontWeight: 500 }}>Load</th>
                            <th style={{ textAlign: 'left', paddingBottom: '6px', fontWeight: 500 }}>1RM</th>
                          </tr>
                        </thead>
                        <tbody>
                          {session.exercises.map(ex => {
                            const isStrength = ex.category === 'STRENGTH';
                            const epley1RM = isStrength && ex.reps > 0 ? ex.weightKg * (1 + ex.reps / 30) : ex.weightKg;
                            return (
                              <tr key={ex.id} style={{ borderBottom: '1px solid var(--border)' }}>
                                <td style={{ padding: '7px 0', color: 'var(--text)' }}>{ex.exerciseName} <span className="text-xs" style={{ color: 'var(--text-muted)' }}>({ex.category})</span></td>
                                <td style={{ padding: '7px 0', color: 'var(--text-secondary)' }}>{isStrength ? `${ex.sets} x ${ex.reps}` : `${ex.duration}min Z${ex.zone?.replace('ZONE_', '')}`}</td>
                                <td style={{ padding: '7px 0', color: 'var(--warm)', fontWeight: 600 }}>{isStrength ? `${ex.weightKg} kg` : '-'}</td>
                                <td style={{ padding: '7px 0', color: 'var(--primary-light)', fontWeight: 500 }}>{isStrength ? `${epley1RM.toFixed(0)} kg` : '-'}</td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    ) : (
                      <p className="text-xs" style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No exercises logged yet</p>
                    )}
                    <motion.button whileHover={{ x: 2 }} onClick={() => setAddingExerciseForSession(addingExerciseForSession === session.id ? null : session.id)}
                      style={{ marginTop: '10px', display: 'flex', alignItems: 'center', gap: '5px', fontSize: '12px', color: 'var(--primary-light)', background: 'hsla(var(--hue-primary), 55%, 50%, 0.06)', border: '1px solid hsla(var(--hue-primary), 55%, 50%, 0.12)', padding: '5px 10px', borderRadius: '7px', cursor: 'pointer' }}>
                      <Plus size={12} /> Add Exercise
                    </motion.button>

                    <AnimatePresence>
                      {addingExerciseForSession === session.id && (
                        <motion.form initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} onSubmit={e => handleAddExercise(session.id, e)} style={{ marginTop: '10px', padding: '12px', background: 'rgba(0,0,0,0.15)', borderRadius: '10px', overflow: 'hidden' }}>
                          <div className="mb-3">
                            <label className="label">Exercise Name</label>
                            <input type="text" className="input" placeholder="e.g. Bench Press" value={exerciseForm.exerciseName} onChange={e => setExerciseForm(prev => ({ ...prev, exerciseName: e.target.value }))} />
                          </div>
                          <div className="mb-3">
                            <label className="label">Category</label>
                            <div style={{ display: 'flex', gap: '6px' }}>
                              {(['STRENGTH', 'CARDIO'] as const).map(c => (
                                <button key={c} type="button" onClick={() => setCategory(c)}
                                  style={{ flex: 1, padding: '6px 10px', borderRadius: '7px', fontSize: '12px', cursor: 'pointer', border: category === c ? '1px solid var(--border-active)' : '1px solid var(--border)', background: category === c ? 'hsla(var(--hue-primary), 55%, 50%, 0.08)' : 'rgba(255,255,255,0.02)', color: category === c ? 'var(--primary-light)' : 'var(--text-muted)' }}>
                                  {c === 'STRENGTH' ? 'Strength' : 'Cardio'}
                                </button>
                              ))}
                            </div>
                          </div>
                          {category === 'STRENGTH' ? (
                            <>
                              <label className="label">Sets</label>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: '5px', marginBottom: '10px' }}>
                                {exerciseForm.setEntries.map((entry, idx) => (
                                  <div key={idx} style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', minWidth: '36px' }}>Set {idx + 1}</span>
                                    <input type="number" step={0.5} min={0} className="input" style={{ padding: '6px 8px', fontSize: '12px' }} placeholder="kg" value={entry.weightKg || ''} onChange={e => updateSetRow(idx, 'weightKg', e.target.value)} />
                                    <span className="text-xs" style={{ color: 'var(--text-muted)' }}>x</span>
                                    <input type="number" min={1} className="input" style={{ padding: '6px 8px', fontSize: '12px' }} placeholder="reps" value={entry.reps || ''} onChange={e => updateSetRow(idx, 'reps', e.target.value)} />
                                    <input type="number" min={1} max={10} step={0.5} className="input" style={{ padding: '6px 8px', fontSize: '12px', width: '60px' }} placeholder="RPE" value={entry.rpe} onChange={e => updateSetRow(idx, 'rpe', e.target.value)} />
                                    {exerciseForm.setEntries.length > 1 && (
                                      <button type="button" onClick={() => removeSetRow(idx)} style={{ color: '#ef4444', background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}><Trash2 size={13} /></button>
                                    )}
                                  </div>
                                ))}
                              </div>
                              <button type="button" onClick={addSetRow} style={{ fontSize: '12px', color: 'var(--accent)', background: 'rgba(16,185,129,0.06)', border: '1px dashed rgba(16,185,129,0.15)', padding: '5px 10px', borderRadius: '7px', cursor: 'pointer', width: '100%', marginBottom: '10px' }}>
                                <Plus size={12} style={{ display: 'inline', marginRight: '3px' }} /> Add Set
                              </button>
                            </>
                          ) : (
                            <div style={{ display: 'flex', gap: '8px', marginBottom: '10px' }}>
                              <div style={{ flex: 1 }}>
                                <label className="label">Duration (min)</label>
                                <input type="number" min={1} className="input" placeholder="30" value={exerciseForm.duration || ''} onChange={e => setExerciseForm(prev => ({ ...prev, duration: parseInt(e.target.value) || 0 }))} />
                              </div>
                              <div style={{ flex: 1 }}>
                                <label className="label">HR Zone</label>
                                <select className="input" value={exerciseForm.zone} onChange={e => setExerciseForm(prev => ({ ...prev, zone: e.target.value }))}>
                                  <option value="">Select</option>
                                  <option value="ZONE_1">Zone 1</option>
                                  <option value="ZONE_2">Zone 2</option>
                                  <option value="ZONE_3">Zone 3</option>
                                  <option value="ZONE_4">Zone 4</option>
                                  <option value="ZONE_5">Zone 5</option>
                                </select>
                              </div>
                            </div>
                          )}
                          <div className="flex justify-end gap-2">
                            <button type="button" onClick={() => setAddingExerciseForSession(null)} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '12px', borderRadius: '7px' }}>Cancel</button>
                            <button type="submit" className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '12px', borderRadius: '7px' }}>Log Exercise</button>
                          </div>
                        </motion.form>
                      )}
                    </AnimatePresence>
                  </div>
                </motion.div>
              ))}
            </AnimatePresence>
          )}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <h3 className="text-xs font-medium flex items-center gap-1.5" style={{ color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
            <Trophy size={13} style={{ color: 'var(--warm)' }} /> Records & Predictions
          </h3>
          <div className="card" style={{ padding: '16px' }}>
            {loading || predicting ? (
              <div className="skeleton" style={{ height: '100px', borderRadius: '8px' }} />
            ) : prs.length === 0 ? (
              <p className="text-sm" style={{ color: 'var(--text-muted)' }}>Log workouts to generate PRs and predictions.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {prs.map((pr, i) => {
                  const pred = predictions[pr.exerciseName];
                  return (
                    <div key={i} style={{ paddingBottom: '10px', borderBottom: i < prs.length - 1 ? '1px solid var(--border)' : 'none' }}>
                      <p className="text-sm font-medium" style={{ color: 'var(--text)', marginBottom: '4px' }}>{pr.exerciseName}</p>
                      <div className="flex justify-between items-center mb-1">
                        <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--warm)' }}>
                          {pr.weightKg} kg <span className="text-xs" style={{ color: 'var(--text-muted)', fontWeight: 400 }}>x {pr.reps}</span>
                        </span>
                        <span className="text-xs" style={{ color: 'var(--text-muted)' }}>1RM ~{pr.estimatedOneRepMax.toFixed(0)}</span>
                      </div>
                      {pred ? (
                        <div className="flex gap-1.5 flex-wrap">
                          <span className="badge badge-green">+30d: {pred.predicted30Days1RM} kg</span>
                          <span className="badge badge-green">+60d: {pred.predicted60Days1RM} kg</span>
                          <span className="badge badge-green">+90d: {pred.predicted90Days1RM} kg</span>
                        </div>
                      ) : (
                        <button onClick={() => handlePredictOne(pr.exerciseName)} style={{ fontSize: '11px', padding: '2px 8px', borderRadius: '6px', background: 'hsla(var(--hue-primary), 55%, 50%, 0.08)', color: 'var(--primary-light)', border: '1px solid hsla(var(--hue-primary), 55%, 50%, 0.12)', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
                          <Brain size={10} /> Predict
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {Object.keys(predictions).length > 0 && (
            <div className="card" style={{ padding: '16px', borderColor: 'var(--border-active)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '5px', marginBottom: '10px', fontSize: '12px', color: 'var(--text-muted)' }}>
                <Brain size={12} style={{ color: 'var(--primary-light)' }} /> AI Strength Forecast
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {Object.entries(predictions).map(([name, p]) => {
                  const color = p.trend === 'GAINING' ? 'var(--accent)' : p.trend === 'LOSING' ? '#ef4444' : 'var(--warm)';
                  return (
                    <div key={name} style={{ fontSize: '12px', padding: '8px', borderRadius: '8px', background: 'rgba(255,255,255,0.02)' }}>
                      <div className="flex justify-between mb-1">
                        <span style={{ color: 'var(--text)', fontWeight: 500 }}>{name}</span>
                        <span style={{ color }}>{p.current1RM} kg &rarr; {p.predicted90Days1RM} kg</span>
                      </div>
                      <div style={{ width: '100%', height: '3px', background: 'rgba(255,255,255,0.03)', borderRadius: '99px', overflow: 'hidden' }}>
                        <motion.div initial={{ width: 0 }} animate={{ width: `${Math.min(100, (p.predicted90Days1RM / p.current1RM) * 100)}%` }} style={{ height: '100%', background: color, borderRadius: '99px', transition: 'width 0.6s ease' }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
