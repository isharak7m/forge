import { useState, useEffect } from 'react';
import { nutritionApi } from '../api/nutrition';
import { DailyNutritionSummary, FoodLog } from '../types';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Trash2, Utensils, PieChart as PieChartIcon, ChevronLeft, ChevronRight, BarChart3 } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid, ReferenceLine } from 'recharts';
import { CardSkeleton } from '../components/ui/Skeleton';
import { NUTRITION_RDA, MICRO_FIELDS, getMicroLabel } from '../data/rda';

const UNITS = ['GRAM', 'KILOGRAM', 'MILLILITER', 'LITER', 'CUP', 'TABLESPOON', 'TEASPOON', 'OUNCE', 'PIECE', 'SLICE', 'SCOOP', 'SERVING', 'DROP', 'PACKET', 'CAN', 'BOTTLE', 'BOWL', 'PLATE'];
const RESULTS_PER_PAGE = 8;

const MICRO_INIT: Record<string, string> = {};
MICRO_FIELDS.forEach(k => { MICRO_INIT[k] = ''; });

export default function NutritionPage() {
  const [date, setDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [summary, setSummary] = useState<DailyNutritionSummary | null>(null);
  const [loading, setLoading] = useState(true);

  const [showForm, setShowForm] = useState(false);
  const [showMicros, setShowMicros] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [searching, setSearching] = useState(false);
  const [searchPage, setSearchPage] = useState(0);
  const [servingG, setServingG] = useState(100);
  const [basePer100g, setBasePer100g] = useState<Record<string, number>>({});
  const [formData, setFormData] = useState<Record<string, string>>({
    foodName: '',
    mealCategory: 'BREAKFAST',
    unit: 'GRAM',
    calories: '',
    proteinG: '',
    carbsG: '',
    fatG: '',
    fiberG: '',
    ...MICRO_INIT,
  });

  const applyServingScale = (base: Record<string, number>, serving: number) => {
    const ratio = serving / 100;
    const update: Record<string, string> = {};
    const fields = ['calories', 'proteinG', 'carbsG', 'fatG', 'fiberG', ...MICRO_FIELDS];
    for (const f of fields) {
      const val = (base[f] || 0) * ratio;
      update[f] = val > 0 && val < 0.01 ? '' : String(Math.round(val * 100) / 100);
    }
    setFormData(prev => ({ ...prev, ...update }));
  };

  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    setSearchPage(0);
    if (query.length < 3) { setSearchResults([]); return; }
    setSearching(true);
    try {
      const res = await nutritionApi.searchFoods(query);
      if (res.success) setSearchResults(res.data || []);
    } catch { setSearchResults([]); }
    finally { setSearching(false); }
  };

  const selectFood = (product: any) => {
    const base: Record<string, number> = { calories: product.caloriesPer100g, proteinG: product.proteinPer100g, carbsG: product.carbsPer100g, fatG: product.fatPer100g, fiberG: product.fiberPer100g };
    for (const f of MICRO_FIELDS) {
      base[f] = product[`${f}Per100g`] || 0;
    }
    setBasePer100g(base);
    setServingG(100);
    applyServingScale(base, 100);
    setFormData(prev => ({ ...prev, foodName: product.name }));
    setSearchQuery('');
    setSearchResults([]);
  };

  useEffect(() => { document.title = 'Nutrition - Forge'; }, []);
  useEffect(() => { loadData(); }, [date]);

  const loadData = async () => {
    try { setLoading(true); const res = await nutritionApi.getDailyAnalytics(date); if (res.success) setSummary(res.data); } catch { toast.error('Failed to load nutrition data'); } finally { setLoading(false); }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload: Record<string, any> = { ...formData, date };
    const numericFields = ['calories', 'proteinG', 'carbsG', 'fatG', 'fiberG', ...MICRO_FIELDS];
    for (const f of numericFields) {
      payload[f] = parseFloat(formData[f]) || 0;
    }
    const optimistic: FoodLog = { id: -Date.now(), date, mealCategory: payload.mealCategory, foodName: payload.foodName, servingSize: 1, unit: payload.unit, calories: payload.calories, proteinG: payload.proteinG, carbsG: payload.carbsG, fatG: payload.fatG, fiberG: payload.fiberG, loggedAt: new Date().toISOString() };
    setSummary(prev => prev ? { ...prev, meals: [...prev.meals, optimistic], totalCalories: prev.totalCalories + optimistic.calories, totalProtein: prev.totalProtein + optimistic.proteinG, totalCarbs: prev.totalCarbs + optimistic.carbsG, totalFat: prev.totalFat + optimistic.fatG } : prev);
    setShowForm(false);
    const emptyForm: Record<string, string> = { foodName: '', mealCategory: 'BREAKFAST', unit: 'GRAM', calories: '', proteinG: '', carbsG: '', fatG: '', fiberG: '', ...MICRO_INIT };
    setFormData(emptyForm);
    try { const res = await nutritionApi.logFood(payload); if (res.success) toast.success('Food logged!'); loadData(); } catch { toast.error('Failed to log food'); loadData(); }
  };

  const handleDeleteLog = async (id: number) => {
    if (!window.confirm('Delete this food log entry?')) return;
    setSummary(prev => prev ? { ...prev, meals: prev.meals.filter(m => m.id !== id) } : prev);
    try { await nutritionApi.deleteLog(id); toast.success('Food log deleted'); loadData(); } catch { toast.error('Failed to delete food log'); loadData(); }
  };

  const renderMealGroup = (category: string) => {
    if (!summary) return null;
    const meals = summary.meals.filter(m => m.mealCategory === category);
    if (meals.length === 0) return null;
    const cals = meals.reduce((sum, m) => sum + m.calories, 0);
    return (
      <div className="mb-5">
        <h4 className="flex justify-between text-sm font-medium mb-2.5 pb-2" style={{ borderBottom: '1px solid var(--border)', color: 'var(--text-secondary)' }}>
          {category} <span style={{ color: 'var(--primary)' }}>{cals.toFixed(0)} kcal</span>
        </h4>
        <div className="flex flex-col gap-1.5">
          {meals.map((m, i) => (
            <div key={m.id} className="flex justify-between items-center p-2.5 rounded-lg" style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)' }}>
              <div>
                <p className="text-sm font-medium" style={{ color: 'var(--text)' }}>{m.foodName}</p>
                <p className="text-xs" style={{ color: 'var(--text-muted)' }}>{m.proteinG}g P &middot; {m.carbsG}g C &middot; {m.fatG}g F</p>
              </div>
              <div className="flex items-center gap-2.5">
                <span className="text-sm font-medium" style={{ color: 'var(--primary-light)' }}>{m.calories}</span>
                <button onClick={() => handleDeleteLog(m.id)} className="btn-danger" style={{ padding: '3px 6px', borderRadius: '5px', fontSize: '11px' }}><Trash2 size={12} /></button>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const macroData = summary ? [
    { name: 'Protein', value: summary.totalProtein * 4, color: 'var(--accent)' },
    { name: 'Carbs', value: summary.totalCarbs * 4, color: 'var(--warm)' },
    { name: 'Fat', value: summary.totalFat * 9, color: 'var(--secondary)' },
  ] : [];

  return (
    <div className="flex flex-col gap-5">
      {/* Header */}
      <div className="flex justify-between items-center">
        <input type="date" className="input w-44" value={date} onChange={e => setDate(e.target.value)} />
        <motion.button whileHover={{ scale: 1.01 }} whileTap={{ scale: 0.99 }} className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          <Plus size={16} /> Add Food
        </motion.button>
      </div>

      {/* Food Log Form */}
      <AnimatePresence>
        {showForm && (
          <motion.div initial={{ opacity: 0, y: -8, scale: 0.99 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, y: -8, scale: 0.99 }} className="card">
            <h3 className="text-sm font-medium mb-4" style={{ color: 'var(--text)' }}>Log Food</h3>
            <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div style={{ position: 'relative' }} onBlur={() => setTimeout(() => setSearchResults([]), 200)}>
                <label className="label">Search Food</label>
                <input type="text" className="input" placeholder="e.g. chicken breast" value={searchQuery} onChange={e => handleSearch(e.target.value)} />
                {searching && <p className="text-xs mt-1" style={{ color: 'var(--text-muted)' }}>Searching...</p>}
                {searchResults.length > 0 && (
                  <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 50, background: 'var(--bg-elevated)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', marginTop: '4px', maxHeight: '280px', overflowY: 'auto', boxShadow: 'var(--shadow-lg)' }}>
                    <div style={{ padding: '6px 10px', fontSize: '10px', color: 'var(--text-muted)', borderBottom: '1px solid var(--border)' }}>
                      {searchResults.length} result{searchResults.length !== 1 ? 's' : ''} found
                    </div>
                    {searchResults.slice(searchPage * RESULTS_PER_PAGE, (searchPage + 1) * RESULTS_PER_PAGE).map((p, i) => (
                      <button key={i} type="button" className="w-full text-left px-3 py-2 text-sm" style={{ borderBottom: '1px solid var(--border)' }}
                        onMouseDown={() => selectFood(p)}
                        onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'hsla(var(--hue-primary), 55%, 50%, 0.06)'; }}
                        onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}>
                        <span className="font-medium" style={{ color: 'var(--text)' }}>{p.name}</span>
                        <span className="ml-2 text-xs" style={{ color: 'var(--text-muted)' }}>{p.caloriesPer100g} kcal/100g</span>
                      </button>
                    ))}
                    {searchResults.length > RESULTS_PER_PAGE && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 10px', borderTop: '1px solid var(--border)' }}>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          disabled={searchPage === 0}
                          style={{ padding: '2px 8px', fontSize: '11px' }}
                          onClick={() => setSearchPage(p => Math.max(0, p - 1))}
                        >
                          <ChevronLeft size={12} /> Prev
                        </button>
                        <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                          {searchPage + 1} / {Math.ceil(searchResults.length / RESULTS_PER_PAGE)}
                        </span>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          disabled={(searchPage + 1) * RESULTS_PER_PAGE >= searchResults.length}
                          style={{ padding: '2px 8px', fontSize: '11px' }}
                          onClick={() => setSearchPage(p => p + 1)}
                        >
                          Next <ChevronRight size={12} />
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
              <div>
                <label className="label">Serving (g)</label>
                <input type="number" className="input" min={1} value={servingG} onChange={e => { const v = parseInt(e.target.value) || 100; setServingG(v); if (Object.keys(basePer100g).length) applyServingScale(basePer100g, v); }} />
              </div>
              <div>
                <label className="label">Food Name</label>
                <input type="text" className="input" required value={formData.foodName} onChange={e => setFormData({...formData, foodName: e.target.value})} />
              </div>
              <div>
                <label className="label">Meal</label>
                <select className="input" value={formData.mealCategory} onChange={e => setFormData({...formData, mealCategory: e.target.value})}>
                  <option value="BREAKFAST">Breakfast</option>
                  <option value="PRE_WORKOUT">Pre-Workout</option>
                  <option value="POST_WORKOUT">Post-Workout</option>
                  <option value="LUNCH">Lunch</option>
                  <option value="DINNER">Dinner</option>
                  <option value="SNACK">Snack</option>
                </select>
              </div>
              <div>
                <label className="label">Unit</label>
                <select className="input" value={formData.unit} onChange={e => setFormData({...formData, unit: e.target.value})}>
                  {UNITS.map(u => <option key={u} value={u}>{u.charAt(0) + u.slice(1).toLowerCase()}</option>)}
                </select>
              </div>
              <div>
                <label className="label">Calories</label>
                <input type="number" className="input" required value={formData.calories} onChange={e => setFormData({...formData, calories: e.target.value})} />
              </div>
              <div>
                <label className="label">Protein (g)</label>
                <input type="number" className="input" value={formData.proteinG} onChange={e => setFormData({...formData, proteinG: e.target.value})} />
              </div>
              <div>
                <label className="label">Carbs (g)</label>
                <input type="number" className="input" value={formData.carbsG} onChange={e => setFormData({...formData, carbsG: e.target.value})} />
              </div>
              <div>
                <label className="label">Fat (g)</label>
                <input type="number" className="input" value={formData.fatG} onChange={e => setFormData({...formData, fatG: e.target.value})} />
              </div>
              <div className="md:col-span-2">
                <button type="button" className="btn btn-ghost text-xs gap-1" onClick={() => setShowMicros(!showMicros)}>
                  {showMicros ? '-' : '+'} Micronutrients
                </button>
              </div>
              {showMicros && MICRO_FIELDS.map(key => {
                const info = NUTRITION_RDA[key];
                return (
                  <div key={key}>
                    <label className="label">{info.label} ({info.unit})</label>
                    <input type="number" className="input" step="any" value={formData[key] ?? ''}
                      onChange={e => setFormData({...formData, [key]: e.target.value})} />
                  </div>
                );
              })}
              <div className="md:col-span-2 flex justify-end gap-2 mt-2">
                <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>Cancel</button>
                <motion.button type="submit" whileHover={{ scale: 1.01 }} whileTap={{ scale: 0.99 }} className="btn btn-primary">Save Log</motion.button>
              </div>
            </form>
          </motion.div>
        )}
      </AnimatePresence>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2"><CardSkeleton height={300} /></div>
          <CardSkeleton height={300} />
        </div>
      ) : summary ? (
        <>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2 card">
            <h3 className="text-sm font-medium mb-4 flex items-center gap-2" style={{ color: 'var(--text)' }}><Utensils size={16} /> Meals</h3>
            {summary.meals.length === 0 ? (
              <p className="text-center py-6 text-sm" style={{ color: 'var(--text-muted)' }}>No meals logged on this date.</p>
            ) : (
              <>{renderMealGroup('BREAKFAST')}{renderMealGroup('PRE_WORKOUT')}{renderMealGroup('POST_WORKOUT')}{renderMealGroup('LUNCH')}{renderMealGroup('DINNER')}{renderMealGroup('SNACK')}</>
            )}
          </div>

          <div className="card">
            <h3 className="text-sm font-medium mb-3 flex items-center gap-2" style={{ color: 'var(--text)' }}><PieChartIcon size={16} /> Macros</h3>
            <div className="h-44 mb-3">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={macroData} cx="50%" cy="50%" innerRadius={55} outerRadius={75} paddingAngle={4} dataKey="value">
                    {macroData.map((entry, index) => <Cell key={`cell-${index}`} fill={entry.color} />)}
                  </Pie>
                  <Tooltip contentStyle={{ background: 'var(--bg-elevated)', border: '1px solid var(--border)', borderRadius: '8px' }} itemStyle={{ color: 'var(--text)' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            {(() => {
              const proteinCals = summary.totalProtein * 4;
              const carbCals = summary.totalCarbs * 4;
              const fatCals = summary.totalFat * 9;
              const total = proteinCals + carbCals + fatCals;
              const pPct = total > 0 ? ((proteinCals / total) * 100).toFixed(0) : '0';
              const cPct = total > 0 ? ((carbCals / total) * 100).toFixed(0) : '0';
              const fPct = total > 0 ? ((fatCals / total) * 100).toFixed(0) : '0';
              return (
                <div className="flex flex-col gap-2 text-sm">
                  <div className="flex justify-between">
                    <span className="flex items-center gap-2"><div className="w-2.5 h-2.5 rounded-full" style={{ background: 'var(--accent)' }} /> Protein</span>
                    <span className="font-medium">{summary.totalProtein.toFixed(0)}g <span style={{ color: 'var(--text-muted)', fontSize: '11px' }}>({pPct}%)</span></span>
                  </div>
                  <div className="flex justify-between">
                    <span className="flex items-center gap-2"><div className="w-2.5 h-2.5 rounded-full" style={{ background: 'var(--warm)' }} /> Carbs</span>
                    <span className="font-medium">{summary.totalCarbs.toFixed(0)}g <span style={{ color: 'var(--text-muted)', fontSize: '11px' }}>({cPct}%)</span></span>
                  </div>
                  <div className="flex justify-between">
                    <span className="flex items-center gap-2"><div className="w-2.5 h-2.5 rounded-full" style={{ background: 'var(--secondary)' }} /> Fat</span>
                    <span className="font-medium">{summary.totalFat.toFixed(0)}g <span style={{ color: 'var(--text-muted)', fontSize: '11px' }}>({fPct}%)</span></span>
                  </div>
                </div>
              );
            })()}
          </div>
        </div>
        {summary?.micronutrients && (
          <div className="card">
            <h3 className="text-sm font-medium mb-4 flex items-center gap-2" style={{ color: 'var(--text)' }}>
              <BarChart3 size={16} /> Micronutrients <span className="text-xs font-normal" style={{ color: 'var(--text-muted)' }}>— % of daily target</span>
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-2">
              {MICRO_FIELDS.filter(k => (summary.micronutrients![k] || 0) > 0 || NUTRITION_RDA[k]?.rda > 0).map(key => {
                const info = NUTRITION_RDA[key];
                if (!info) return null;
                const achieved = summary.micronutrients![key] || 0;
                const pct = Math.min(200, (achieved / info.rda) * 100);
                const color = pct >= 80 ? 'var(--accent)' : pct >= 50 ? 'var(--warm)' : '#ef4444';
                return (
                  <div key={key} className="flex flex-col gap-1 p-2.5 rounded-lg" style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)' }}>
                    <div className="flex justify-between items-center">
                      <span style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 500 }}>{info.label}</span>
                      <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                        {achieved.toFixed(1)} / {info.rda} {info.unit}
                      </span>
                    </div>
                    <div style={{ height: '5px', background: 'rgba(255,255,255,0.04)', borderRadius: '99px', overflow: 'hidden' }}>
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${pct}%` }}
                        transition={{ duration: 0.8, ease: 'easeOut' }}
                        style={{ height: '100%', background: color, borderRadius: '99px' }}
                      />
                    </div>
                    <span style={{ fontSize: '10px', fontWeight: 600, color }}>{pct.toFixed(0)}%</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}
        </>
      ) : null}
    </div>
  );
}
