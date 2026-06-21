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
                        </tr>
                      </thead>
                      <tbody>
                        {session.exercises.map(ex => (
                          <tr key={ex.id} className="border-b border-[rgba(255,255,255,0.05)] last:border-0">
                            <td className="py-2">{ex.exerciseName}</td>
                            <td className="py-2">{ex.sets} x {ex.reps}</td>
                            <td className="py-2">{ex.weightKg} kg</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p className="text-sm text-secondary italic">No exercises logged in this session yet.</p>
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
