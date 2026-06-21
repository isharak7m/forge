import { useState, useEffect } from 'react';
import { workoutApi } from '../api/workouts';
import { WorkoutSession, PersonalRecord } from '../types';
import toast from 'react-hot-toast';
import { format, subDays } from 'date-fns';
import { Plus, Dumbbell, Trophy } from 'lucide-react';

export default function WorkoutsPage() {
  const [sessions, setSessions] = useState<WorkoutSession[]>([]);
  const [prs, setPrs] = useState<PersonalRecord[]>([]);
  const [loading, setLoading] = useState(true);

  const [showSessionForm, setShowSessionForm] = useState(false);
  const [sessionForm, setSessionForm] = useState({ name: '', durationMinutes: '', date: format(new Date(), 'yyyy-MM-dd') });
  const [addingExerciseForSession, setAddingExerciseForSession] = useState<number | null>(null);
  const [exerciseSearch, setExerciseSearch] = useState('');
  const [exerciseResults, setExerciseResults] = useState<any[]>([]);
  const [exerciseSearching, setExerciseSearching] = useState(false);
  const [exerciseForm, setExerciseForm] = useState({ exerciseName: '', sets: 3, reps: 10, weightKg: 0, rpe: '' });

  useEffect(() => { document.title = 'Workouts - FitMind'; }, []);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const to = format(new Date(), 'yyyy-MM-dd');
      const from = format(subDays(new Date(), 30), 'yyyy-MM-dd');
      
      const [sessRes, prsRes] = await Promise.all([
        workoutApi.getSessions(from, to),
        workoutApi.getPRs()
      ]);

      if (sessRes.success) setSessions(sessRes.data);
      if (prsRes.success) setPrs(prsRes.data);
    } catch (e) {
      toast.error('Failed to load workouts');
    } finally {
      setLoading(false);
    }
  };

  const handleExerciseSearch = async (query: string) => {
    setExerciseSearch(query);
    if (query.length < 3) { setExerciseResults([]); return; }
    setExerciseSearching(true);
    try {
      const res = await fetch(`https://wger.de/api/v2/exercise/search/?term=${encodeURIComponent(query)}&language=english&format=json`);
      const data = await res.json();
      setExerciseResults(data.suggestions || []);
    } catch { setExerciseResults([]); }
    finally { setExerciseSearching(false); }
  };

  const selectExercise = (name: string) => {
    setExerciseForm(prev => ({ ...prev, exerciseName: name }));
    setExerciseSearch('');
    setExerciseResults([]);
  };

  const handleAddExercise = async (sessionId: number, e: React.FormEvent) => {
    e.preventDefault();
    if (!exerciseForm.exerciseName) return;
    const payload = { ...exerciseForm, rpe: exerciseForm.rpe ? parseFloat(exerciseForm.rpe) : null };
    const optimistic = { id: -Date.now(), exerciseName: exerciseForm.exerciseName, category: '', sets: Number(exerciseForm.sets), reps: Number(exerciseForm.reps), weightKg: Number(exerciseForm.weightKg), rpe: payload.rpe || 0, notes: '', volume: Number(exerciseForm.sets) * Number(exerciseForm.reps) * Number(exerciseForm.weightKg) };
    setSessions(prev => prev.map(s => s.id === sessionId ? { ...s, exercises: [...s.exercises, optimistic], totalVolume: s.totalVolume + optimistic.volume } : s));
    try {
      const res = await workoutApi.addExercise(sessionId, payload);
      if (res.success) toast.success('Exercise added!');
      loadData();
    } catch { toast.error('Failed to add exercise'); loadData(); }
    setAddingExerciseForSession(null);
    setExerciseForm({ exerciseName: '', sets: 3, reps: 10, weightKg: 0, rpe: '' });
  };

  const handleCreateSession = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await workoutApi.createSession({
        name: sessionForm.name,
        durationMinutes: parseInt(sessionForm.durationMinutes),
        date: sessionForm.date
      });
      if (res.success) {
        toast.success('Session created!');
        setShowSessionForm(false);
        setSessionForm({ name: '', durationMinutes: '', date: format(new Date(), 'yyyy-MM-dd') });
        loadData();
      }
    } catch (e) {
      toast.error('Failed to create session');
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">Training Log</h2>
        <button className="btn btn-primary" onClick={() => setShowSessionForm(true)}>
          <Plus size={18} /> New Workout
        </button>
      </div>

      {showSessionForm && (
        <div className="card-glass animate-fade-in">
          <h3 className="font-semibold mb-4">Create Workout Session</h3>
          <form onSubmit={handleCreateSession} className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="form-group mb-0">
              <label className="label">Date</label>
              <input type="date" className="input" required value={sessionForm.date} onChange={e => setSessionForm({...sessionForm, date: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Workout Name</label>
              <input type="text" className="input" placeholder="e.g. Upper Body Power" required value={sessionForm.name} onChange={e => setSessionForm({...sessionForm, name: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Duration (min)</label>
              <input type="number" className="input" required value={sessionForm.durationMinutes} onChange={e => setSessionForm({...sessionForm, durationMinutes: e.target.value})} />
            </div>
            <div className="md:col-span-3 flex justify-end gap-2 mt-2">
              <button type="button" className="btn btn-ghost" onClick={() => setShowSessionForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary">Start Session</button>
            </div>
          </form>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 flex flex-col gap-4">
          <h3 className="font-semibold flex items-center gap-2"><Dumbbell size={18}/> Recent Sessions</h3>
          {loading ? (
            <div className="card-glass h-32 skeleton"></div>
          ) : sessions.length === 0 ? (
            <div className="card-glass text-center py-8 text-secondary">No sessions logged recently.</div>
          ) : (
            sessions.map(session => (
              <div key={session.id} className="card-glass p-0 overflow-hidden">
                <div className="bg-secondary p-4 flex justify-between items-center border-b border-border-color">
                  <div>
                    <h4 className="font-bold text-lg text-blue-400">{session.name}</h4>
                    <span className="text-sm text-secondary">{session.date} • {session.durationMinutes} mins</span>
                  </div>
                  <div className="text-right">
                    <span className="badge badge-purple mb-1">{session.totalVolume} kg volume</span>
                  </div>
                </div>
                <div className="p-4">
                  {session.exercises && session.exercises.length > 0 ? (
                    <table className="w-full text-sm text-left">
                      <thead className="text-secondary border-b border-border-color">
                        <tr>
                          <th className="pb-2 font-medium">Exercise</th>
                          <th className="pb-2 font-medium">Sets x Reps</th>
                          <th className="pb-2 font-medium">Weight</th>
                          <th className="pb-2 font-medium">RPE</th>
                        </tr>
                      </thead>
                      <tbody>
                        {session.exercises.map(ex => (
                          <tr key={ex.id} className="border-b border-[rgba(255,255,255,0.05)] last:border-0">
                            <td className="py-2">{ex.exerciseName}</td>
                            <td className="py-2">{ex.sets} x {ex.reps}</td>
                            <td className="py-2">{ex.weightKg} kg</td>
                            <td className="py-2">{ex.rpe ? ex.rpe : '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p className="text-sm text-secondary italic">No exercises logged in this session yet.</p>
                  )}
                  <button className="btn btn-ghost text-sm mt-3" onClick={() => setAddingExerciseForSession(addingExerciseForSession === session.id ? null : session.id)}>
                    <Plus size={16} /> Add Exercise
                  </button>
                  {addingExerciseForSession === session.id && (
                    <form onSubmit={(e) => handleAddExercise(session.id, e)} className="mt-3 p-3 bg-[rgba(0,0,0,0.2)] rounded-lg grid grid-cols-1 md:grid-cols-6 gap-3">
                      <div className="form-group mb-0 relative md:col-span-2">
                        <label className="label text-xs">Search Exercise (wger)</label>
                        <input type="text" className="input text-sm" placeholder="e.g., bench press..." value={exerciseSearch} onChange={e => handleExerciseSearch(e.target.value)} />
                        {exerciseSearching && <p className="text-xs text-secondary mt-1">Searching...</p>}
                        {exerciseResults.length > 0 && (
                          <div className="absolute z-20 top-full left-0 right-0 bg-[#1c2128] border border-[rgba(48,54,61,0.8)] rounded-lg mt-1 max-h-40 overflow-y-auto shadow-xl">
                            {exerciseResults.map((ex, i) => (
                              <button key={i} type="button" className="w-full text-left px-3 py-2 text-sm hover:bg-[rgba(59,130,246,0.1)] transition-colors border-b border-[rgba(48,54,61,0.5)] last:border-b-0" onClick={() => selectExercise(ex.value)}>
                                {ex.value}
                              </button>
                            ))}
                          </div>
                        )}
                      </div>
                      <div className="form-group mb-0">
                        <label className="label text-xs">Exercise Name</label>
                        <input type="text" className="input text-sm" required value={exerciseForm.exerciseName} onChange={e => setExerciseForm({...exerciseForm, exerciseName: e.target.value})} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label text-xs">Sets</label>
                        <input type="number" className="input text-sm" min={1} required value={exerciseForm.sets} onChange={e => setExerciseForm({...exerciseForm, sets: parseInt(e.target.value) || 0})} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label text-xs">Reps</label>
                        <input type="number" className="input text-sm" min={1} required value={exerciseForm.reps} onChange={e => setExerciseForm({...exerciseForm, reps: parseInt(e.target.value) || 0})} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label text-xs">Weight (kg)</label>
                        <input type="number" className="input text-sm" min={0} step={0.5} required value={exerciseForm.weightKg} onChange={e => setExerciseForm({...exerciseForm, weightKg: parseFloat(e.target.value) || 0})} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label text-xs">RPE (optional)</label>
                        <input type="number" className="input text-sm" min={1} max={10} step={0.5} placeholder="6\u201310" value={exerciseForm.rpe} onChange={e => setExerciseForm({...exerciseForm, rpe: e.target.value})} />
                      </div>
                      <div className="md:col-span-6 flex justify-end gap-2">
                        <button type="button" className="btn btn-ghost text-sm" onClick={() => setAddingExerciseForSession(null)}>Cancel</button>
                        <button type="submit" className="btn btn-primary text-sm">Add</button>
                      </div>
                    </form>
                  )}
                </div>
              </div>
            ))
          )}
        </div>

        <div className="flex flex-col gap-4">
          <h3 className="font-semibold flex items-center gap-2"><Trophy size={18} className="text-orange-400"/> Personal Records</h3>
          <div className="card-glass p-4">
             {loading ? <div className="h-32 skeleton"></div> : prs.length === 0 ? (
                <p className="text-secondary text-sm">Log some workouts to generate PRs.</p>
             ) : (
                <div className="flex flex-col gap-3">
                   {prs.map((pr, i) => (
                      <div key={i} className="flex flex-col pb-3 border-b border-[rgba(255,255,255,0.05)] last:border-0 last:pb-0">
                         <span className="font-medium text-primary">{pr.exerciseName}</span>
                         <div className="flex justify-between items-center mt-1">
                            <span className="text-orange-400 font-bold">{pr.weightKg} kg <span className="text-secondary text-xs font-normal">x {pr.reps}</span></span>
                            <span className="text-xs text-secondary bg-secondary px-2 py-1 rounded">Est. 1RM: {pr.estimatedOneRepMax.toFixed(0)}</span>
                         </div>
                      </div>
                   ))}
                </div>
             )}
          </div>
        </div>
      </div>
    </div>
  );
}
